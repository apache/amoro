package com.netease.arctic.server.persistence;

import com.netease.arctic.ams.api.StateField;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class StatedPersistentBase extends PersistentBase {

  private static final Map<Class<? extends StatedPersistentBase>, StateOwnerMeta> metaCache =
      Maps.newConcurrentMap();
  private final ThreadLocal<StateOwner> STATE_OWNER = ThreadLocal.withInitial(StateOwner::new);

  private static final Object NULL_VALUE = new Object();
  private final StateOwnerMeta stateOwnerMeta;
  protected final Lock stateLock = new ReentrantLock();

  protected StatedPersistentBase() {
    stateOwnerMeta = getOrCreateStateOwnerMeta();
  }

  private void retainStates() {
    STATE_OWNER.get().retain();
  }

  private void restoreStates() {
    if (STATE_OWNER.get().tryRestore()) {
      STATE_OWNER.remove();
    }
  }

  private void releaseStates() {
    if (STATE_OWNER.get().tryRelease()) {
      STATE_OWNER.remove();
    }
  }

  protected final void invokeConsistency(Runnable runnable) {
    stateLock.lock();
    retainStates();
    try {
      doAsTransaction(runnable);
    } catch (Throwable throwable) {
      restoreStates();
      throw throwable;
    } finally {
      releaseStates();
      stateLock.unlock();
    }
  }

  private StateOwnerMeta getOrCreateStateOwnerMeta() {
    Class<? extends StatedPersistentBase> thisClass = getClass();
    return metaCache.computeIfAbsent(
        thisClass,
        clz -> {
          List<Field> fields = new ArrayList<>();
          while (clz != StatedPersistentBase.class) {
            Arrays.stream(clz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(StateField.class))
                .forEach(fields::add);
            clz = clz.getSuperclass().asSubclass(StatedPersistentBase.class);
          }
          return new StateOwnerMeta(
              thisClass.getEnclosingClass() != null ? thisClass.getEnclosingClass() : thisClass,
              fields.toArray(new Field[0]));
        });
  }

  private static class StateOwnerMeta {
    private final Class<?> stateOwnerClass;
    private final Field[] consistentFields;

    private StateOwnerMeta(Class<?> stateOwnerClass, Field[] consistentFields) {
      this.stateOwnerClass = stateOwnerClass;
      this.consistentFields = consistentFields;
    }

    private Class<?> getOwnerClass() {
      return stateOwnerClass;
    }

    public Field[] getConsistentFields() {
      return consistentFields;
    }
  }

  /** this is a thread-local object */
  private class StateOwner {
    private Map<Field, Object> retainStates;
    private int count;

    private StateOwner() {
      this.count = 0;
    }

    private void retain() {
      if (count++ == 0) {
        retainStates =
            Arrays.stream(stateOwnerMeta.getConsistentFields())
                .collect(Collectors.toMap(field -> field, this::getValue));
      }
    }

    private boolean tryRelease() {
      Preconditions.checkState(count > 0);
      return --count == 0;
    }

    private boolean tryRestore() {
      Preconditions.checkState(count > 0);
      if (--count == 0) {
        retainStates.forEach(this::setValue);
        return true;
      }
      return false;
    }

    private Object getValue(Field field) {
      try {
        field.setAccessible(true);
        return Optional.ofNullable(field.get(stateOwnerMeta.getOwnerClass())).orElse(NULL_VALUE);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }

    void setValue(Field field, Object value) {
      try {
        field.set(stateOwnerMeta.getOwnerClass(), NULL_VALUE.equals(value) ? null : value);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
