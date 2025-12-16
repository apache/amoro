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

package org.apache.amoro.hive;

import org.apache.amoro.SingletonResourceUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestHMS {
  private static final Logger LOG = LoggerFactory.getLogger(TestHMS.class);
  private static HMSMockServer SINGLETON;
  private static Path SINGLETON_TEMP_DIR;

  private final HMSMockServer mockHms;
  private Path tempDir;

  static {
    try {
      if (SingletonResourceUtil.isUseSingletonResource()) {
        SINGLETON_TEMP_DIR = Files.createTempDirectory("amoro-test-hms-");
        SINGLETON = new HMSMockServer(SINGLETON_TEMP_DIR.resolve("hms").toFile());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public TestHMS() {
    this(null);
  }

  public TestHMS(Path tempDir) {
    this.tempDir = tempDir;
    if (SingletonResourceUtil.isUseSingletonResource()) {
      mockHms = SINGLETON;
    } else {
      try {
        if (tempDir == null) {
          this.tempDir = Files.createTempDirectory("amoro-test-hms-");
        }
        mockHms = new HMSMockServer(this.tempDir.resolve("hms").toFile());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  public HiveConf getHiveConf() {
    return mockHms.hiveConf();
  }

  public HiveMetaStoreClient getHiveClient() {
    return mockHms.getClient();
  }

  public int getMetastorePort() {
    return mockHms.getMetastorePort();
  }

  public String getWareHouseLocation() {
    return mockHms.getWareHouseLocation();
  }

  public void before() throws Exception {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      if (!mockHms.isStarted()) {
        mockHms.start();
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      SINGLETON.stop();
                      try {
                        deleteDirectory(SINGLETON_TEMP_DIR.toFile());
                      } catch (IOException e) {
                        // ignore
                      }
                      LOG.info("Stop singleton mock HMS after testing.");
                    }));
        LOG.info("Start singleton mock HMS before testing.");
      }
    } else {
      mockHms.start();
      LOG.info("Start mock HMS before testing.");
    }
  }

  public void after() {
    if (!SingletonResourceUtil.isUseSingletonResource()) {
      mockHms.stop();
      if (tempDir != null) {
        try {
          deleteDirectory(tempDir.toFile());
        } catch (IOException e) {
          // ignore
        }
      }
      LOG.info("Stop mock HMS after testing.");
    }
  }

  private void deleteDirectory(java.io.File directory) throws IOException {
    if (directory.exists()) {
      java.io.File[] files = directory.listFiles();
      if (files != null) {
        for (java.io.File file : files) {
          if (file.isDirectory()) {
            deleteDirectory(file);
          } else {
            file.delete();
          }
        }
      }
      directory.delete();
    }
  }
}
