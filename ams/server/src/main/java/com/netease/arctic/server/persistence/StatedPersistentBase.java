package com.netease.arctic.server.persistence;

import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class StatedPersistentBase extends PersistentBase {

  private static final Map<Class<? extends PersistentBase>, Field[]> metaCache =
      Maps.newConcurrentMap();
  private static final Object NULL_VALUE = new Object();
  private final Lock stateLock = new ReentrantLock();
  private final Field[] consistentFields;

  protected StatedPersistentBase() {
    consistentFields = getOrCreateConsistentFields();
  }

  protected final void invokeConsisitency(Runnable runnable) {
    stateLock.lock();
    Map<Field, Object> states = retainStates();
    try {
      doAsTransaction(runnable);
    } catch (Throwable throwable) {
      restoreStates(states);
      throw throwable;
    } finally {
      stateLock.unlock();
    }
  }

  protected final <T> T invokeConsisitency(Supplier<T> supplier) {
    stateLock.lock();
    Map<Field, Object> states = retainStates();
    try {
      return supplier.get();
    } catch (Throwable throwable) {
      restoreStates(states);
      throw throwable;
    } finally {
      stateLock.unlock();
    }
  }

  protected final void invokeInStateLock(Runnable runnable) {
    stateLock.lock();
    try {
      runnable.run();
    } finally {
      stateLock.unlock();
    }
  }

  Map<Field, Object> retainStates() {
    return Arrays.stream(consistentFields)
        .collect(Collectors.toMap(field -> field, this::getValue));
  }

  void restoreStates(Map<Field, Object> statesMap) {
    statesMap.forEach(this::setValue);
  }

  private Field[] getOrCreateConsistentFields() {
    return metaCache.computeIfAbsent(
        getClass(),
        clz -> {
          List<Field> fields = new ArrayList<>();
          while (clz != PersistentBase.class) {
            Arrays.stream(clz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(StateField.class))
                .forEach(fields::add);
            clz = clz.getSuperclass().asSubclass(PersistentBase.class);
          }
          return fields.toArray(new Field[0]);
        });
  }

  private Object getValue(Field field) {
    try {
      field.setAccessible(true);
      return Optional.ofNullable(field.get(StatedPersistentBase.this)).orElse(NULL_VALUE);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  void setValue(Field field, Object value) {
    try {
      field.set(StatedPersistentBase.this, NULL_VALUE.equals(value) ? null : value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface StateField {}
}
