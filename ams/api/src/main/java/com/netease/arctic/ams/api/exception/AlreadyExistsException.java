package com.netease.arctic.ams.api.exception;

import com.netease.arctic.ams.api.TableIdentifier;

public class AlreadyExistsException extends ArcticRuntimeException {
  public AlreadyExistsException(String object) {
    super(object + " already exists.");
  }

  public AlreadyExistsException(TableIdentifier tableIdentifier) {
    super(getObjectName(tableIdentifier) + " already exists.");
  }
}
