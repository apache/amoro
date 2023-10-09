package com.netease.arctic.server.exception;

public class LoadingPluginException extends ArcticRuntimeException {

  public LoadingPluginException(String message) {
    super(message);
  }

  public LoadingPluginException(String message, Throwable cause) {
    super(message, cause);
  }
}
