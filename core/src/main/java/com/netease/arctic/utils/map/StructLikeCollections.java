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

package com.netease.arctic.utils.map;

import com.netease.arctic.utils.StructLikeSet;
import org.apache.iceberg.types.Types;

public class StructLikeCollections {

  public static final StructLikeCollections DEFAULT = new StructLikeCollections(false, 0L);

  private Long maxInMemorySizeInBytes;

  public StructLikeCollections(boolean enableSpillableMap, Long maxInMemorySizeInBytes) {
    if (enableSpillableMap) {
      this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
    }
  }

  public StructLikeBaseMap createStructLikeMap(Types.StructType type) {
    if (maxInMemorySizeInBytes == null) {
      return StructLikeMemoryMap.create(type);
    } else {
      return StructLikeSpillableMap.create(type, maxInMemorySizeInBytes);
    }
  }

  public StructLikeSet createStructLikeSet(Types.StructType type) {
    if (maxInMemorySizeInBytes == null) {
      return StructLikeSet.createMemorySet(type);
    } else {
      return StructLikeSet.createSpillableSet(type, maxInMemorySizeInBytes);
    }
  }
}
