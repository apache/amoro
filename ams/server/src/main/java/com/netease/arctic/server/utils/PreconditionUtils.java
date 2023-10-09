package com.netease.arctic.server.utils;

import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.ObjectNotExistsException;

public class PreconditionUtils {

  public static void checkExist(boolean exists, String objectName) {
    if (!exists) {
      throw new ObjectNotExistsException(objectName);
    }
  }

  public static void checkNotExist(boolean exists, String objectName) {
    if (exists) {
      throw new AlreadyExistsException(objectName);
    }
  }
}
