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

package com.netease.arctic.table.blocker;

import java.util.List;
import java.util.Map;

/**
 * Base {@link Blocker} implementation.
 * This Blocker has expiration time, after which it will be invalid.
 */
public class BaseBlocker implements Blocker {
  private final String blockerId;
  private final List<BlockableOperation> operations;
  private final long createTime;
  private final long expirationTime;
  private final Map<String, String> properties;

  public BaseBlocker(String blockerId, List<BlockableOperation> operations, long createTime, long expirationTime,
                     Map<String, String> properties) {
    this.blockerId = blockerId;
    this.operations = operations;
    this.createTime = createTime;
    this.expirationTime = expirationTime;
    this.properties = properties;
  }

  @Override
  public String blockerId() {
    return blockerId;
  }

  @Override
  public Map<String, String> properties() {
    return properties;
  }

  @Override
  public List<BlockableOperation> operations() {
    return operations;
  }

  public long getCreateTime() {
    return createTime;
  }

  public long getExpirationTime() {
    return expirationTime;
  }

  @Override
  public String toString() {
    return "BaseBlocker{" +
        "blockerId='" + blockerId + '\'' +
        ", operations=" + operations +
        ", createTime=" + createTime +
        ", expirationTime=" + expirationTime +
        ", properties=" + properties +
        '}';
  }
}
