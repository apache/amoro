package com.netease.arctic.server.persistence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StatedPersistentBase extends PersistentBase {

  private Lock stateLock = new ReentrantLock();
  private Map<Field, Object> fieldValues = new HashMap<>();

  public void modifyStateSafely(Runnable runnable) {
    stateLock.lock();
    try {
      retainStates();
      runnable.run();
    } catch (Throwable throwable) {
      restoreStates();
      throw new RuntimeException(throwable);
    } finally {
      stateLock.unlock();
    }
  }

  private void retainStates() {
    try {
      fieldValues.clear();
      Class<?> clazz = getClass();
      while (clazz != null) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
          field.setAccessible(true);
          fieldValues.put(field, field.get(this));
        }
        clazz = clazz.getSuperclass();
      }
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private void restoreStates() {
    try {
      for (Map.Entry<Field, Object> entry : fieldValues.entrySet()) {
        entry.getKey().set(this, entry.getValue());
      }
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
