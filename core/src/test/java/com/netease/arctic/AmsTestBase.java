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

package com.netease.arctic;

import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AmsTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(AmsTestBase.class);

  protected static MockArcticMetastoreServer AMS;
  private static final AtomicLong REFERENCE_COUNT = new AtomicLong(0);

  @BeforeClass
  public static void setupAMS() {
    if (AMS == null) {
      AMS = new MockArcticMetastoreServer();
    }
    if (REFERENCE_COUNT.incrementAndGet() == 1) {
      if (AMS.isStarted()) {
        LOG.warn("Mock AMS had been started before testing");
      } else {
        AMS.start();
        LOG.info("Start mock AMS before testing");
      }
    }
  }

  @AfterClass
  public static void closeAMS() {
    if (REFERENCE_COUNT.decrementAndGet() == 0) {
      if (AMS.isStarted()) {
        AMS.stopAndCleanUp();
        AMS = null;
        LOG.info("Stop mock AMS after testing");
      } else {
        LOG.warn("Mock AMS had been stopped before testing completed");
      }
    }
  }

  protected static String getServerUrl() {
    return AMS.getServerUrl();
  }

  protected static MockArcticMetastoreServer.AmsHandler getAmsHandler() {
    return AMS.handler();
  }
}
