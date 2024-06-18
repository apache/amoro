/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.exception;

import org.apache.amoro.ErrorCodes;
import org.apache.amoro.api.AmoroException;
import org.apache.amoro.api.InvalidObjectException;
import org.apache.amoro.api.MetaException;
import org.apache.amoro.api.NoSuchObjectException;
import org.apache.amoro.api.OperationConflictException;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AmoroRuntimeException extends RuntimeException {

  private static final Map<Class<? extends AmoroRuntimeException>, Integer> CODE_MAP =
      new HashMap<>();

  static {
    CODE_MAP.put(PersistenceException.class, ErrorCodes.PERSISTENCE_ERROR_CODE);
    CODE_MAP.put(ObjectNotExistsException.class, ErrorCodes.OBJECT_NOT_EXISTS_ERROR_CODE);
    CODE_MAP.put(AlreadyExistsException.class, ErrorCodes.ALREADY_EXISTS_ERROR_CODE);
    CODE_MAP.put(IllegalMetadataException.class, ErrorCodes.ILLEGAL_METADATA_ERROR_CODE);
    CODE_MAP.put(ForbiddenException.class, ErrorCodes.FORBIDDEN_ERROR_CODE);

    CODE_MAP.put(TaskNotFoundException.class, ErrorCodes.TASK_NOT_FOUND_ERROR_CODE);
    CODE_MAP.put(TaskRuntimeException.class, ErrorCodes.TASK_RUNTIME_ERROR_CODE);
    CODE_MAP.put(OptimizingClosedException.class, ErrorCodes.OPTIMIZING_CLOSED_ERROR_CODE);
    CODE_MAP.put(IllegalTaskStateException.class, ErrorCodes.ILLEGAL_TASK_STATE_ERROR_CODE);
    CODE_MAP.put(PluginAuthException.class, ErrorCodes.PLUGIN_AUTH_ERROR_CODE);
    CODE_MAP.put(PluginRetryAuthException.class, ErrorCodes.PLUGIN_RETRY_AUTH_ERROR_CODE);

    CODE_MAP.put(BlockerConflictException.class, ErrorCodes.BLOCKER_CONFLICT_ERROR_CODE);
  }

  private static final int UNDEFINED = -1;

  private final int errorCode;
  private final String errorName;

  protected AmoroRuntimeException() {
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  protected AmoroRuntimeException(String message) {
    super(message);
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  protected AmoroRuntimeException(Throwable throwable) {
    super(throwable);
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  protected AmoroRuntimeException(String message, Throwable throwable) {
    super(message, throwable);
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getErrorName() {
    return errorName;
  }

  private AmoroException transform() {
    return new AmoroException(errorCode, errorName, getMessage());
  }

  protected static String getObjectName(TableIdentifier tableIdentifier) {
    return "Table "
        + tableIdentifier.getCatalog()
        + '.'
        + tableIdentifier.getDatabase()
        + '.'
        + tableIdentifier.getTableName();
  }

  protected static String getObjectName(ServerTableIdentifier tableIdentifier) {
    return "Table "
        + tableIdentifier.getCatalog()
        + '.'
        + tableIdentifier.getDatabase()
        + '.'
        + tableIdentifier.getTableName();
  }

  public static AmoroException normalize(Throwable throwable) {
    return wrap(throwable).transform();
  }

  public static TException normalizeCompatibly(Throwable throwable) {
    if (throwable.getClass().equals(ObjectNotExistsException.class)) {
      return new NoSuchObjectException(throwable.getMessage());
    } else if (throwable.getClass().equals(AlreadyExistsException.class)) {
      return new org.apache.amoro.api.AlreadyExistsException(throwable.getMessage());
    } else if (throwable.getClass().equals(IllegalMetadataException.class)
        || throwable.getClass().equals(PersistenceException.class)) {
      return new MetaException(throwable.getMessage());
    } else if (throwable.getClass().equals(IllegalArgumentException.class)) {
      return new InvalidObjectException(throwable.getMessage());
    } else if (throwable.getClass().equals(BlockerConflictException.class)) {
      return new OperationConflictException(throwable.getMessage());
    }
    return new TException(throwable.getMessage());
  }

  public static AmoroRuntimeException wrap(
      Throwable throwable, Function<Throwable, AmoroRuntimeException> exceptionTransform) {
    if (throwable instanceof AmoroRuntimeException) {
      return (AmoroRuntimeException) throwable;
    } else {
      return exceptionTransform.apply(throwable);
    }
  }

  private static AmoroRuntimeException wrap(Throwable throwable) {
    return wrap(throwable, UndefinedException::new);
  }
}
