/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.utils.map;

import com.netease.arctic.iceberg.optimize.StructLikeCopy;
import com.netease.arctic.iceberg.optimize.StructLikeWrapper;
import com.netease.arctic.iceberg.optimize.StructLikeWrapperFactory;
import com.netease.arctic.utils.SerializationUtils;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;

import java.io.IOException;

public class StructLikeWrapperSerializer implements Serializer<StructLikeWrapper> {

  protected final StructLikeWrapperFactory structLikeWrapperFactory;

  public StructLikeWrapperSerializer(StructLikeWrapperFactory structLikeWrapperFactory) {
    this.structLikeWrapperFactory = structLikeWrapperFactory;
  }

  public StructLikeWrapperSerializer(Types.StructType type) {
    this.structLikeWrapperFactory = new StructLikeWrapperFactory(type);
  }

  @Override
  public byte[] serialize(StructLikeWrapper structLikeWrapper) {
    StructLike copy = StructLikeCopy.copy(structLikeWrapper.get());
    try {
      return SerializationUtils.serialize(copy);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public StructLikeWrapper deserialize(byte[] bytes) {
    StructLikeCopy structLike = SerializationUtils.deserialize(bytes);
    return structLikeWrapperFactory.create().set(structLike);
  }
}
