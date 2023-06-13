/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.exception;

import com.netease.arctic.ams.api.AlreadyExistsException;
import com.netease.arctic.ams.api.InvalidObjectException;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.NotSupportedException;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.ams.api.OperationErrorException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ArcticRuntimeException extends RuntimeException {
  private static final List<Class> EXPECTED_EXCEPTION_ARRAY = new ArrayList<>();

  static {
    EXPECTED_EXCEPTION_ARRAY.add(NoSuchObjectException.class);
    EXPECTED_EXCEPTION_ARRAY.add(AlreadyExistsException.class);
    EXPECTED_EXCEPTION_ARRAY.add(NotSupportedException.class);
    EXPECTED_EXCEPTION_ARRAY.add(MetaException.class);
    EXPECTED_EXCEPTION_ARRAY.add(InvalidObjectException.class);
    EXPECTED_EXCEPTION_ARRAY.add(OperationConflictException.class);
    EXPECTED_EXCEPTION_ARRAY.add(OperationErrorException.class);
  }

  public static TException normalizeCompatibly(Throwable throwable) {
    if (!EXPECTED_EXCEPTION_ARRAY.contains(throwable.getClass())) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      throwable.printStackTrace(pw);
      return new TApplicationException(sw.toString());
    } else {
      return (TException) throwable;
    }
  }
}
