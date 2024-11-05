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

package org.apache.amoro.utils.map;

import static org.apache.amoro.shade.guava32.com.google.common.base.Preconditions.checkNotNull;

import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeWrapper;

public class StructLikeWrapperSerializer
    implements SerializationUtil.SimpleSerializer<StructLikeWrapper> {

  protected final StructLikeWrapper structLikeWrapper;

  public StructLikeWrapperSerializer(StructLikeWrapper structLikeWrapper) {
    this.structLikeWrapper = structLikeWrapper;
  }

  public StructLikeWrapperSerializer(Types.StructType type) {
    this.structLikeWrapper = StructLikeWrapper.forType(type);
  }

  @Override
  public byte[] serialize(StructLikeWrapper structLikeWrapper) {
    checkNotNull(structLikeWrapper);
    StructLike copy = StructLikeCopy.copy(structLikeWrapper.get());
    return SerializationUtil.kryoSerialize(copy);
  }

  @Override
  public StructLikeWrapper deserialize(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    StructLikeCopy structLike = SerializationUtil.kryoDeserialize(bytes);
    return structLikeWrapper.copyFor(structLike);
  }

  public static class StructLikeCopy implements StructLike {

    public static StructLike copy(StructLike struct) {
      return struct != null ? new StructLikeCopy(struct) : null;
    }

    private final Object[] values;

    private StructLikeCopy(StructLike toCopy) {
      this.values = new Object[toCopy.size()];

      for (int i = 0; i < values.length; i += 1) {
        Object value = toCopy.get(i, Object.class);

        if (value instanceof StructLike) {
          values[i] = copy((StructLike) value);
        } else {
          values[i] = value;
        }
      }
    }

    @Override
    public int size() {
      return values.length;
    }

    @Override
    public <T> T get(int pos, Class<T> javaClass) {
      return javaClass.cast(values[pos]);
    }

    @Override
    public <T> void set(int pos, T value) {
      throw new UnsupportedOperationException("Struct copy cannot be modified");
    }
  }
}
