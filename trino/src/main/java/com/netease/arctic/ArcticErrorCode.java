/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.netease.arctic;

import io.trino.spi.ErrorCode;
import io.trino.spi.ErrorCodeSupplier;
import io.trino.spi.ErrorType;

import static io.trino.spi.ErrorType.EXTERNAL;
import static io.trino.spi.ErrorType.INTERNAL_ERROR;
import static io.trino.spi.ErrorType.USER_ERROR;

public enum ArcticErrorCode
    implements ErrorCodeSupplier {
  ARCTIC_UNKNOWN_TABLE_TYPE(0, EXTERNAL),
  ARCTIC_INVALID_METADATA(1, EXTERNAL),
  ARCTIC_TOO_MANY_OPEN_PARTITIONS(2, USER_ERROR),
  ARCTIC_INVALID_PARTITION_VALUE(3, EXTERNAL),
  ARCTIC_BAD_DATA(4, EXTERNAL),
  ARCTIC_MISSING_DATA(5, EXTERNAL),
  ARCTIC_CANNOT_OPEN_SPLIT(6, EXTERNAL),
  ARCTIC_WRITER_OPEN_ERROR(7, EXTERNAL),
  ARCTIC_FILESYSTEM_ERROR(8, EXTERNAL),
  ARCTIC_CURSOR_ERROR(9, EXTERNAL),
  ARCTIC_WRITE_VALIDATION_FAILED(10, INTERNAL_ERROR),
  ARCTIC_INVALID_SNAPSHOT_ID(11, USER_ERROR);

  private final ErrorCode errorCode;

  ArcticErrorCode(int code, ErrorType type) {
    errorCode = new ErrorCode(code + 0x0504_0000, name(), type);
  }

  @Override
  public ErrorCode toErrorCode() {
    return errorCode;
  }
}
