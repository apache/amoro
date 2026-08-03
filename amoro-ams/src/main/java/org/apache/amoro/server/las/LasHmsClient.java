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

package org.apache.amoro.server.las;

import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.util.List;
import java.util.function.Function;

/** Small HMS3 SDK facade that keeps catalog selection explicit without hiding HMS data types. */
public final class LasHmsClient {

  private final HMSClientPool discoveryPool;
  private final Function<String, HMSClientPool> catalogPoolFactory;

  public LasHmsClient(LasIntegrationContext context) {
    this(context.newHmsClientPool(), context::newHmsClientPool);
  }

  LasHmsClient(HMSClientPool discoveryPool, Function<String, HMSClientPool> catalogPoolFactory) {
    this.discoveryPool = discoveryPool;
    this.catalogPoolFactory = catalogPoolFactory;
  }

  public List<String> listCatalogs() throws TException, InterruptedException {
    return discoveryPool.run(HMSClient::getCatalogs);
  }

  public List<String> listDatabases(String catalogName) throws TException, InterruptedException {
    return catalogPoolFactory.apply(catalogName).run(HMSClient::getAllDatabases);
  }

  public List<String> listTables(String catalogName, String databaseName)
      throws TException, InterruptedException {
    return catalogPoolFactory.apply(catalogName).run(client -> client.getAllTables(databaseName));
  }

  public Table loadTable(String catalogName, String databaseName, String tableName)
      throws TException, InterruptedException {
    return catalogPoolFactory
        .apply(catalogName)
        .run(client -> client.getTable(databaseName, tableName));
  }
}
