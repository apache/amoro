package com.netease.arctic.ams.api.exception;

public class UndefinedException extends ArcticRuntimeException {
  public UndefinedException(Throwable throwable) {
    super(throwable);
  }

  public UndefinedException(String message) {
    super(message);
  }
}
