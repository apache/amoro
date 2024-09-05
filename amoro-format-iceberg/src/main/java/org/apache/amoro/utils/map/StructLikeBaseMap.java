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
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeWrapper;

import java.io.IOException;

public abstract class StructLikeBaseMap<T> implements SimpleMap<StructLike, T> {

  protected final StructLikeWrapper structLikeWrapper;
  protected final ThreadLocal<StructLikeWrapper> wrappers;

  protected StructLikeBaseMap(Types.StructType type) {
    this.structLikeWrapper = StructLikeWrapper.forType(type);
    this.wrappers = ThreadLocal.withInitial(() -> structLikeWrapper.copyFor(null));
  }

  @Override
  public T get(StructLike key) {
    StructLikeWrapper wrapper = wrappers.get();
    T value = getInternalMap().get(wrapper.set((key)));
    wrapper.set(null); // don't hold a reference to the key.
    return value;
  }

  @Override
  public void put(StructLike key, T value) {
    getInternalMap().put(structLikeWrapper.copyFor(key), value);
  }

  @Override
  public void delete(StructLike key) {
    StructLikeWrapper wrapper = wrappers.get();
    getInternalMap().delete(wrapper.set(key));
    wrapper.set(null); // don't hold a reference to the key.
  }

  @Override
  public void close() throws IOException {
    getInternalMap().close();
  }

  protected abstract SimpleMap<StructLikeWrapper, T> getInternalMap();
}
