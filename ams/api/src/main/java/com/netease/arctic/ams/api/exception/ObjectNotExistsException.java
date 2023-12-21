package com.netease.arctic.ams.api.exception;

import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.TableIdentifier;

public class ObjectNotExistsException extends ArcticRuntimeException {
  public ObjectNotExistsException(String object) {
    super(object + " not exists");
  }

  public ObjectNotExistsException(TableIdentifier tableIdentifier) {
    super(getObjectName(tableIdentifier) + " not exists");
  }

  public ObjectNotExistsException(ServerTableIdentifier tableIdentifier) {
    super(getObjectName(tableIdentifier) + " not exists");
  }
}
