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

package org.apache.amoro.server.resource;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.util.Objects;

public class OptimizerThread {
  private final int threadId;
  private final OptimizerInstance optimizer;

  protected OptimizerThread(int threadId, OptimizerInstance optimizer) {
    this.threadId = threadId;
    this.optimizer = optimizer;
  }

  public String getToken() {
    return optimizer.getToken();
  }

  public int getThreadId() {
    return threadId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptimizerThread that = (OptimizerThread) o;
    return threadId == that.threadId && Objects.equals(optimizer, that.optimizer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(threadId, optimizer);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("threadId", threadId)
        .add("optimizer", optimizer)
        .toString();
  }
}
