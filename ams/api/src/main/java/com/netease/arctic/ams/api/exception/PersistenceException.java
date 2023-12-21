package com.netease.arctic.ams.api.exception;

public class PersistenceException extends ArcticRuntimeException {
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public PersistenceException(Throwable cause) {
    super(cause);
  }

  public PersistenceException(String message) {
    super(message);
  }
}
