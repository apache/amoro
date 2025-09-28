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

package org.apache.iceberg.data;

import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.DateTimeUtil;
import org.apache.iceberg.util.UUIDUtil;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.function.Function;

/**
 * This class is a copy of {@link InternalRecordWrapper} that adds proper handling for UUID types in
 * the <a href="https://github.com/apache/iceberg">Iceberg</a> format.
 *
 * <p>It serves as a temporary solution for an issue discussed in <a
 * href="https://github.com/apache/amoro/pull/3797">this pull request</a>.
 *
 * <p>Once the related <a href="https://github.com/apache/iceberg/pull/14208">pull request</a> is
 * merged, the Iceberg dependency version should be updated accordingly, and this class will be
 * removed.
 */
public class IcebergRecordWrapper implements StructLike {
  private final Function<Object, Object>[] transforms;
  private StructLike wrapped = null;

  @SuppressWarnings("unchecked")
  public IcebergRecordWrapper(Types.StructType struct) {
    this(
        struct.fields().stream()
            .map(field -> converter(field.type()))
            .toArray(
                length -> (Function<Object, Object>[]) Array.newInstance(Function.class, length)));
  }

  private IcebergRecordWrapper(Function<Object, Object>[] transforms) {
    this.transforms = transforms;
  }

  private static Function<Object, Object> converter(Type type) {
    switch (type.typeId()) {
      case DATE:
        return date -> DateTimeUtil.daysFromDate((LocalDate) date);
      case TIME:
        return time -> DateTimeUtil.microsFromTime((LocalTime) time);
      case TIMESTAMP:
        if (((Types.TimestampType) type).shouldAdjustToUTC()) {
          return timestamp -> DateTimeUtil.microsFromTimestamptz((OffsetDateTime) timestamp);
        } else {
          return timestamp -> DateTimeUtil.microsFromTimestamp((LocalDateTime) timestamp);
        }
      case FIXED:
        return bytes -> ByteBuffer.wrap((byte[]) bytes);
      case UUID:
        return uuid -> {
          if (uuid instanceof byte[]) {
            return UUIDUtil.convert((byte[]) uuid);
          } else {
            return uuid;
          }
        };
      case STRUCT:
        InternalRecordWrapper wrapper = new InternalRecordWrapper(type.asStructType());
        return struct -> wrapper.wrap((StructLike) struct);
      default:
    }
    return null;
  }

  public StructLike get() {
    return wrapped;
  }

  public IcebergRecordWrapper copyFor(StructLike record) {
    return new IcebergRecordWrapper(transforms).wrap(record);
  }

  public IcebergRecordWrapper wrap(StructLike record) {
    this.wrapped = record;
    return this;
  }

  @Override
  public int size() {
    return wrapped.size();
  }

  @Override
  public <T> T get(int pos, Class<T> javaClass) {
    if (transforms[pos] != null) {
      Object value = wrapped.get(pos, Object.class);
      if (value == null) {
        // transforms function don't allow to handle null values, so just return null here.
        return null;
      } else {
        return javaClass.cast(transforms[pos].apply(value));
      }
    }
    return wrapped.get(pos, javaClass);
  }

  @Override
  public <T> void set(int pos, T value) {
    throw new UnsupportedOperationException("Cannot update IcebergRecordWrapper");
  }
}
