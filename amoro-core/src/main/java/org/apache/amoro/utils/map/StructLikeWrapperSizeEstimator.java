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

import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeWrapper;
import org.apache.lucene.util.RamUsageEstimator;

import java.util.Arrays;
import java.util.Objects;

/** Size Estimator for StructLikeWrapper record payload. */
public class StructLikeWrapperSizeEstimator implements SizeEstimator<StructLikeWrapper> {
  @Override
  public long sizeEstimate(StructLikeWrapper structLikeWrapper) {
    if (structLikeWrapper == null) {
      return 0;
    }
    StructLike structLike = structLikeWrapper.get();
    return sizeOf(structLikeObjects(structLike));
  }

  private long sizeOf(Object[] objects) {
    if (objects == null) {
      return 0;
    }

    return Arrays.stream(objects)
        .filter(Objects::nonNull)
        .mapToLong(
            object -> {
              if (object.getClass().isArray()) {
                return sizeOf((Object[]) object);
              } else {
                return RamUsageEstimator.sizeOfObject(object, 0);
              }
            })
        .sum();
  }

  private Object[] structLikeObjects(StructLike structLike) {
    if (structLike == null) {
      return null;
    }

    Object[] values = new Object[structLike.size()];
    for (int i = 0; i < values.length; i += 1) {
      Object value = structLike.get(i, Object.class);

      if (value instanceof StructLike) {
        values[i] = structLikeObjects((StructLike) value);
      } else {
        values[i] = value;
      }
    }

    return values;
  }
}
