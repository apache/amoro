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

package com.netease.arctic.hive.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.avro.AvroSchemaUtil;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.hash.Hasher;
import org.apache.iceberg.relocated.com.google.common.hash.Hashing;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.time.Instant.EPOCH;
import static org.apache.iceberg.util.DateTimeUtil.EPOCH_DAY;

public class KeyData
    implements IndexedRecord, StructLike, SpecificData.SchemaConstructable, Serializable {

  private static final int DATA_CACHE_SIZE = 1000;
  private static final LoadingCache<Types.StructType, ThreadLocal<List<KeyData>>> dataCache =
      Caffeine.newBuilder().build(type -> ThreadLocal.withInitial(ArrayList::new));

  static Schema keyDataSchema(Types.StructType keyType) {
    return AvroSchemaUtil.convert(keyType, KeyData.class.getName());
  }

  private final Types.StructType keyType;
  private final int size;
  private final Object[] data;
  private transient Schema schema = null;

  /**
   * Used by Avro reflection to instantiate this class when reading manifest files.
   */
  public KeyData(Schema schema) {
    this.keyType = AvroSchemaUtil.convert(schema).asNestedType().asStructType();
    this.size = keyType.fields().size();
    this.data = new Object[size];
  }

  private KeyData(Types.StructType keyType) {
    for (Types.NestedField field : keyType.fields()) {
      Preconditions.checkArgument(field.type().isPrimitiveType(),
          "key data cannot contain nested types: %s", field.type());
    }

    this.keyType = keyType;
    this.size = keyType.fields().size();
    this.data = new Object[size];
  }

  /**
   * Copy constructor
   */
  private KeyData(KeyData toCopy) {
    this.keyType = toCopy.keyType;
    this.size = toCopy.size;
    this.data = copyData(toCopy.keyType, toCopy.data);
    this.schema = toCopy.schema;
  }

  public Types.StructType getKeyType() {
    return keyType;
  }

  public Object[] getData() {
    return data;
  }

  @Override
  public Schema getSchema() {
    if (schema == null) {
      this.schema = keyDataSchema(keyType);
    }
    return schema;
  }

  public Type getType(int pos) {
    return keyType.fields().get(pos).type();
  }

  public void clear() {
    Arrays.fill(data, null);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(int pos, Class<T> javaClass) {
    Object value = get(pos);
    if (value == null || javaClass.isInstance(value)) {
      return javaClass.cast(value);
    }

    throw new IllegalArgumentException(String.format(
        "Wrong class, %s, for object: %s",
        javaClass.getName(), String.valueOf(value)));
  }

  public static KeyData get(Types.StructType type) {
    ThreadLocal<List<KeyData>> localCache = dataCache.get(type);
    List<KeyData> cache = localCache.get();
    if (cache.size() > 0) {
      return cache.remove(0);
    } else {
      return new KeyData(type);
    }
  }

  @Override
  public Object get(int pos) {
    if (pos >= data.length) {
      return null;
    }

    if (data[pos] instanceof byte[]) {
      return ByteBuffer.wrap((byte[]) data[pos]);
    }

    if (data[pos] instanceof LocalDateTime) {
      return ChronoUnit.MICROS.between(EPOCH, ((LocalDateTime) data[pos]).atOffset(ZoneOffset.UTC));
    }

    if (data[pos] instanceof OffsetDateTime) {
      return Timestamp.valueOf(((OffsetDateTime) data[pos]).atZoneSameInstant(ZoneOffset.UTC)
          .toLocalDateTime()).getTime();
    }

    if (data[pos] instanceof LocalDate) {
      return (int) (ChronoUnit.DAYS.between(EPOCH_DAY, (LocalDate) data[pos]));
    }

    return data[pos];
  }

  @Override
  public <T> void set(int pos, T value) {
    if (value instanceof Utf8) {
      // Utf8 is not Serializable
      data[pos] = value.toString();
    } else if (value instanceof ByteBuffer) {
      // ByteBuffer is not Serializable
      ByteBuffer buffer = (ByteBuffer) value;
      byte[] bytes = new byte[buffer.remaining()];
      buffer.duplicate().get(bytes);
      data[pos] = bytes;
    } else {
      data[pos] = value;
    }
  }

  @Override
  public void put(int i, Object v) {
    set(i, v);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("KeyData{");
    for (int i = 0; i < data.length; i += 1) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(keyType.fields().get(i).name())
          .append("=")
          .append(data[i]);
    }
    sb.append("}");
    return sb.toString();
  }

  public ByteBuffer byteBuffer() {
    List<ByteBuffer> dataBuffers = Lists.newArrayListWithCapacity(data.length);
    for (int i = 0; i < data.length; i += 1) {
      switch (keyType.fields().get(i).type().typeId()) {
        case STRING:
        case BINARY:
        case FIXED:
        case DECIMAL:
          ByteBuffer dataBuffer = Conversions.toByteBuffer(keyType.fields().get(i).type(), get(i));
          ByteBuffer keyBuffer = ByteBuffer.allocate(dataBuffer.capacity() + 4);
          keyBuffer.putInt(dataBuffer.capacity());
          keyBuffer.put(dataBuffer.array());
          dataBuffers.add(keyBuffer);
          break;
        default:
          dataBuffers.add(Conversions.toByteBuffer(keyType.fields().get(i).type(), get(i)));
      }

    }
    int keyDataBufferLength = dataBuffers.stream().mapToInt(ByteBuffer::capacity).sum();
    final ByteBuffer keyDataBuffer = ByteBuffer.allocate(keyDataBufferLength);
    dataBuffers.forEach(byteBuffer -> keyDataBuffer.put(byteBuffer.array()));
    keyDataBuffer.rewind();
    return keyDataBuffer;
  }

  public static KeyData fromByteBuffer(ByteBuffer byteBuffer, Types.StructType type) {
    KeyData keyData = get(type);
    for (int i = 0; i < type.fields().size(); i++) {
      switch (type.fields().get(i).type().typeId()) {
        case STRING:
        case BINARY:
        case FIXED:
        case DECIMAL:
          int dataLength = byteBuffer.getInt();
          byte[] dataBytes = new byte[dataLength];
          ByteBuffer dataByteBuffer = byteBuffer.get(dataBytes, 0, dataLength);
          keyData.set(i, Conversions.fromByteBuffer(type.fields().get(i).type(), dataByteBuffer));
          break;
        default:
          keyData.set(i, Conversions.fromByteBuffer(type.fields().get(i).type(), byteBuffer));
      }
    }
    byteBuffer.rewind();
    return keyData;
  }

  public String toStringWithUnderscore() {
    List<String> dataList = new ArrayList<>();
    for (Object obj : data) {
      dataList.add(String.valueOf(obj));
    }
    return String.join("_", dataList);
  }

  public KeyData copy() {
    return new KeyData(this);
  }

  public static KeyData copyKeyData(Types.StructType type, StructLike data) {
    KeyData keyData = get(type);
    for (int i = 0; i < type.fields().size(); ++i) {
      keyData.set(i, data.get(i, Object.class));
    }
    return keyData;
  }

  public void setValues(StructLike struct) {
    Preconditions.checkArgument(keyType.fields().size() == struct.size(),
        "Struct value count not match");
    for (int i = 0; i < struct.size(); i++) {
      set(i, struct.get(i, Object.class));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof KeyData)) {
      return false;
    }

    KeyData that = (KeyData) o;
    return keyType.equals(that.keyType) && Arrays.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    Hasher hasher = Hashing.goodFastHash(32).newHasher();
    Stream.of(data).map(Objects::hashCode).forEach(hasher::putInt);
    keyType.fields().stream().map(Objects::hashCode).forEach(hasher::putInt);
    return hasher.hash().hashCode();
  }

  public static Object[] copyData(Types.StructType type, Object[] data) {
    List<Types.NestedField> fields = type.fields();
    Object[] copy = new Object[data.length];
    for (int i = 0; i < data.length; i += 1) {
      if (data[i] == null) {
        copy[i] = null;
      } else {
        Types.NestedField field = fields.get(i);
        switch (field.type().typeId()) {
          case STRUCT:
          case LIST:
          case MAP:
            throw new IllegalArgumentException("Unsupported type in key data: " + type);
          case BINARY:
          case FIXED:
            byte[] buffer = (byte[]) data[i];
            copy[i] = Arrays.copyOf(buffer, buffer.length);
            break;
          case STRING:
            copy[i] = data[i].toString();
            break;
          default:
            // no need to copy the object
            copy[i] = data[i];
        }
      }
    }

    return copy;
  }

  public void release() {
    ThreadLocal<List<KeyData>> localCache = dataCache.get(keyType);
    List<KeyData> cache = localCache.get();
    if (cache.size() < DATA_CACHE_SIZE) {
      for (int i = 0; i < size; i++) {
        set(i, null);
      }
      cache.add(this);
    }
  }

  public static void release(KeyData... releaseData) {
    for (KeyData keyData : releaseData) {
      if (keyData != null) {
        keyData.release();
      }
    }
  }
}
