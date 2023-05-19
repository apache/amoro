package com.netease.arctic.server.persistence;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;
import com.netease.arctic.server.utils.ThriftServiceProxy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatedPersistentProxy<T extends PersistentBase> implements InvocationHandler {

  private static final Map<Class<? extends PersistentBase>, List<State>> stateMetaCache = Maps.newHashMap();
  private final T persistentObj;
  private Lock stateLock = new ReentrantLock();
  private List<State> states = Lists.newArrayList();

  public StatedPersistentProxy(T persistentObj) {
    this.persistentObj = persistentObj;
    initStateFields();
  }

  @SuppressWarnings("unchecked")
  public static <T extends PersistentBase> T get(Class<T> serviceClazz, T persistentObj) {
    return (T) Proxy.newProxyInstance(ThriftServiceProxy.class.getClassLoader(),
        new Class<?>[]{serviceClazz}, new StatedPersistentProxy<>(persistentObj));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaredAnnotationsByType(StateConsistency.class).length > 0) {
      return invokeConsisitency(method, args);
    } else {
      return method.invoke(persistentObj, args);
    }
  }

  public Object invokeConsisitency(Method method, Object[] args) throws Throwable {
    stateLock.lock();
    try {
      retainStates();
      return method.invoke(persistentObj, args);
    } catch (Throwable throwable) {
      restoreStates();
      throw throwable;
    } finally {
      stateLock.unlock();
    }
  }

  private void initStateFields() {
    List<State> statesCache = stateMetaCache.get(persistentObj.getClass());
    if (!stateMetaCache.containsKey(persistentObj.getClass())) {
      synchronized (stateMetaCache) {
        if (!stateMetaCache.containsKey(persistentObj.getClass())) {
          statesCache = Lists.newArrayList();
          Class<?> clazz = persistentObj.getClass();
          while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
              if (field.getDeclaredAnnotationsByType(StateField.class).length > 0) {
                states.add(new State(field));
              }
            }
            clazz = clazz.getSuperclass();
          }
          stateMetaCache.put(persistentObj.getClass(), statesCache);
        }
      }
    }
    states = new ArrayList<>(statesCache.size());
    statesCache.forEach(state -> states.add(new State(state.field)));
  }

  private void retainStates() {
    states.forEach(State::retain);
  }

  private void restoreStates() {
    states.forEach(State::restore);
  }

  private static class State {
    private Object value;
    private Field field;

    State(Field field) {
      this.field = field;
    }

    void retain() {
      try {
        field.setAccessible(true);
        value = field.get(this);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }

    void restore() {
      try {
        field.set(this, value);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface StateField {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface StateConsistency {
  }
}

