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

package com.netease.arctic.server.resource;

import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.resource.Resource;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

import java.util.UUID;

public class OptimizerInstance extends Resource {

  private String token;
  private long startTime;
  private long touchTime;

  public OptimizerInstance() {}

  public OptimizerInstance(OptimizerRegisterInfo registerInfo, String containerName) {
    super(registerInfo, containerName);
    this.token = UUID.randomUUID().toString();
    this.touchTime = System.currentTimeMillis();
    this.startTime = registerInfo.getStartTime();
  }

  public OptimizerInstance touch() {
    touchTime = System.currentTimeMillis();
    return this;
  }

  public String getToken() {
    return token;
  }

  public long getTouchTime() {
    return touchTime;
  }

  public long getStartTime() {
    return startTime;
  }

  public OptimizerThread getThread(int threadId) {
    return new OptimizerThread(threadId, this);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("token", token)
        .add("startTime", startTime)
        .add("touchTime", touchTime)
        .toString();
  }
}
