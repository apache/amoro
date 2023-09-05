package com.netease.arctic;

public class DatabaseNotEmptyException extends RuntimeException {

  public DatabaseNotEmptyException() {
  }

  public DatabaseNotEmptyException(String message) {
    super(message);
  }

  public DatabaseNotEmptyException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatabaseNotEmptyException(Throwable cause) {
    super(cause);
  }

  public DatabaseNotEmptyException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
