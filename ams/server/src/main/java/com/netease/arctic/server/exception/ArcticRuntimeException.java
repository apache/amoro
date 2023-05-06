package com.netease.arctic.server.exception;

import com.netease.arctic.ams.api.ArcticException;
import com.netease.arctic.ams.api.ErrorCodes;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.server.table.ServerTableIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArcticRuntimeException extends RuntimeException {

  private static final Map<Class<? extends ArcticRuntimeException>, Integer> CODE_MAP = new HashMap<>();

  static {
    CODE_MAP.put(PersistenceException.class, ErrorCodes.PERSISTENCE_ERROR_CODE);
    CODE_MAP.put(ObjectNotExistsException.class, ErrorCodes.OBJECT_NOT_EXISTS_ERROR_CODE);
    CODE_MAP.put(AlreadyExistsException.class, ErrorCodes.ALREADY_EXISTS_ERROR_CODE);
    CODE_MAP.put(IllegalMetadataException.class, ErrorCodes.ILLEGAL_METADATA_ERROR_CODE);

    CODE_MAP.put(TaskNotFoundException.class, ErrorCodes.TASK_NOT_FOUND_ERROR_CODE);
    CODE_MAP.put(DuplicateRuntimeException.class, ErrorCodes.DUPLICATED_TASK_ERROR_CODE);
    CODE_MAP.put(OptimizingClosedException.class, ErrorCodes.OPTIMIZING_CLOSED_ERROR_CODE);
    CODE_MAP.put(IllegalTaskStateException.class, ErrorCodes.ILLEGAL_TASK_STATE_ERROR_CODE);
    CODE_MAP.put(PluginAuthException.class, ErrorCodes.PLUGIN_AUTH_ERROR_CODE);
    CODE_MAP.put(PluginRetryAuthException.class, ErrorCodes.PLUGIN_RETRY_AUTH_ERROR_CODE);
    
    CODE_MAP.put(OperationConflictException.class, ErrorCodes.OPERATION_CONFLICT_ERROR_CODE);
  }

  private static final int UNDEFINED = -1;

  private final int errorCode;
  private final String errorName;

  protected ArcticRuntimeException() {
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  protected ArcticRuntimeException(String message) {
    super(message);
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  protected ArcticRuntimeException(Throwable throwable) {
    super(throwable);
    this.errorCode = Optional.ofNullable(CODE_MAP.get(getClass())).orElse(UNDEFINED);
    this.errorName = getClass().getSimpleName();
  }

  protected ArcticRuntimeException(String message, Throwable throwable) {
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

  private ArcticException transform() {
    return new ArcticException(errorCode, errorName, getMessage());
  }

  protected static String getObjectName(TableIdentifier tableIdentifier) {
    return new StringBuilder().append("Table ")
            .append(tableIdentifier.getCatalog())
            .append('.')
            .append(tableIdentifier.getDatabase())
            .append('.')
            .append(tableIdentifier.getTableName())
            .toString();
  }

  protected static String getObjectName(ServerTableIdentifier tableIdentifier) {
    return new StringBuilder().append("Table ")
            .append(tableIdentifier.getCatalog())
            .append('.')
            .append(tableIdentifier.getDatabase())
            .append('.')
            .append(tableIdentifier.getTableName())
            .toString();
  }

  public static ArcticException transformThrift(Throwable throwable) {
    return buildArcticException(throwable).transform();
  }

  private static ArcticRuntimeException buildArcticException(Throwable throwable) {
    if (throwable instanceof ArcticRuntimeException) {
      return (ArcticRuntimeException) throwable;
    } else {
      return new UndefinedException(throwable);
    }
  }
}
