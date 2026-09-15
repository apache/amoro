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
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestHMS extends ExternalResource {
  private static final Logger LOG = LoggerFactory.getLogger(TestHMS.class);
  private static HMSMockServer SINGLETON;
  private static TemporaryFolder SINGLETON_FOLDER;

  private final HMSMockServer mockHms;
  private TemporaryFolder hmsFolder;

  static {
    try {
      if (SingletonResourceUtil.isUseSingletonResource()) {
        SINGLETON_FOLDER = new TemporaryFolder();
        SINGLETON_FOLDER.create();
        SINGLETON = new HMSMockServer(SINGLETON_FOLDER.newFile());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public TestHMS() {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      mockHms = SINGLETON;
    } else {
      try {
        hmsFolder = new TemporaryFolder();
        hmsFolder.create();
        mockHms = new HMSMockServer(hmsFolder.newFile());
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

  public void createView(String database, String viewName) throws TException {
    createView(database, viewName, Collections.emptyMap());
  }

  public void createView(String database, String viewName, Map<String, String> parameters)
      throws TException {
    StorageDescriptor storageDescriptor = new StorageDescriptor();
    storageDescriptor.setCols(
        Collections.singletonList(new FieldSchema("id", "int", "view column")));
    storageDescriptor.setSerdeInfo(new SerDeInfo());
    Table view =
        new Table(
            viewName,
            database,
            System.getProperty("user.name"),
            (int) (System.currentTimeMillis() / 1000),
            0,
            0,
            storageDescriptor,
            Collections.emptyList(),
            new HashMap<>(parameters),
            "select 1 as id",
            "select 1 as id",
            TableType.VIRTUAL_VIEW.name());
    getHiveClient().createTable(view);
  }

  public int getMetastorePort() {
    return mockHms.getMetastorePort();
  }

  public String getWareHouseLocation() {
    return mockHms.getWareHouseLocation();
  }

  @Override
  public void before() throws Exception {
    if (SingletonResourceUtil.isUseSingletonResource()) {
      if (!mockHms.isStarted()) {
        mockHms.start();
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      SINGLETON.stop();
                      SINGLETON_FOLDER.delete();
                      LOG.info("Stop singleton mock HMS after testing.");
                    }));
        LOG.info("Start singleton mock HMS before testing.");
      }
    } else {
      mockHms.start();
      LOG.info("Start mock HMS before testing.");
    }
  }

  @Override
  public void after() {
    if (!SingletonResourceUtil.isUseSingletonResource()) {
      mockHms.stop();
      hmsFolder.delete();
      LOG.info("Stop mock HMS after testing.");
    }
  }
}
