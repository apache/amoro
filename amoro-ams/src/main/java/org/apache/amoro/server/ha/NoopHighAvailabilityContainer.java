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

package org.apache.amoro.server.ha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/** No-op HA container that never blocks and performs no leader election. */
public class NoopHighAvailabilityContainer implements HighAvailabilityContainer {
  private static final Logger LOG = LoggerFactory.getLogger(NoopHighAvailabilityContainer.class);
  private final CountDownLatch followerLatch = new CountDownLatch(1);

  @Override
  /** Returns immediately without blocking. */
  public void waitLeaderShip() {
    LOG.info("Noop HA: waiting leadership returns immediately");
  }

  @Override
  /** Block followership forever for noop. */
  public void waitFollowerShip() throws InterruptedException {
    followerLatch.await();
    LOG.warn("Noop HA: waiting followership should not return");
  }

  @Override
  /** No-op close operation. */
  public void close() {
    LOG.info("Noop HA: closed");
  }

  @Override
  public void registAndElect() throws Exception {}
}
