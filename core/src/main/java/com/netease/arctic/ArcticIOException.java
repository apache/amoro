package com.netease.arctic;

/** Exception thrown for table IO-related failures. */
public class ArcticIOException extends RuntimeException {

  private Throwable ioException;

  public ArcticIOException(String msg, Throwable t) {
    super(msg, t);
    this.ioException = t;
  }

  public ArcticIOException(Throwable t) {
    super(t.getMessage(), t);
    this.ioException = t;
  }

  public ArcticIOException(String message) {
    super(message);
  }

  public Throwable getInternalError() {
    return ioException;
  }
}
