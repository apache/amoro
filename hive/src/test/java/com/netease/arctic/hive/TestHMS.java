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

package com.netease.arctic.hive;

import com.netease.arctic.SingletonResourceUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHMS extends ExternalResource {
  private static final Logger LOG = LoggerFactory.getLogger(TestHMS.class);
  private static HMSMockServer SINGLETON;

  private final HMSMockServer mockHms;

  static {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      SINGLETON = new HMSMockServer();
    }
  }

  public TestHMS() {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      mockHms = SINGLETON;
    } else {
      mockHms = new HMSMockServer();
    }
  }

  public HiveConf getHiveConf() {
    return mockHms.hiveConf();
  }

  public HiveMetaStoreClient getHiveClient() {
    return mockHms.getClient();
  }

  @Override
  protected void before() throws Throwable {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      if (!mockHms.isStarted()) {
        mockHms.start();
        LOG.info("Start singleton mock HMS before testing.");
      }
    } else {
      mockHms.start();
      LOG.info("Start mock HMS before testing.");
    }
  }

  @Override
  protected void after() {
    if (!SingletonResourceUtil.isUseSingletonResource()) {
      mockHms.stop();
      LOG.info("Stop mock HMS after testing.");
    }
  }
}
