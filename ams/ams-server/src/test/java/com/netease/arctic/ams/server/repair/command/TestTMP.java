/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.client.OptimizeManagerClient;
import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.ams.server.repair.RepairConfig;
import com.netease.arctic.catalog.CatalogManager;
import org.junit.Test;

public class TestTMP {

  @Test
  public void init() throws Exception {
    String url = "thrift://sloth-commerce-test1.jd.163.org:18112/";
    String catalog = "trino_online_env_hive";
    CallFactory callFactory = new DefaultCallFactory(
        new RepairConfig(url, catalog, 10, 1),
        new CatalogManager(url),
        new OptimizeManagerClient(url),
        new PooledAmsClient(url));

    // callFactory.generateTableCall("trino_online_env_hive.arctic_100w_impala_0310_2.stock",
    //     TableCall.TableOperation.SYNC_HIVE_DATA).call(new Context());

    callFactory.generateAnalyzeCall("trino_online_env_hive.arctic_100w_impala_0310_2.customer").call(new Context());

  }
}
