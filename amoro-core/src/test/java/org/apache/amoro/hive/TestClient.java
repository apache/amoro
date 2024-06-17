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

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedCatalogUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClient {

  @Test
  public void test() throws TException, InterruptedException {
    HiveConf conf = new HiveConf();
    conf.set("hive.metastore.uris", "thrift://127.0.0.1:9083");
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogProperties.URI, conf.get(HiveConf.ConfVars.METASTOREURIS.varname));
    CatalogMeta meta =
        CatalogTestHelpers.buildHiveCatalogMeta("hive", properties, conf, TableFormat.HUDI);
    TableMetaStore metaStore = MixedCatalogUtil.buildMetaStore(meta);
    CachedHiveClientPool client = new CachedHiveClientPool(metaStore, new HashMap<>());

    //    try {
    //      Database db = new Database();
    //      db.setName("amoro");
    //      client.run( c -> {
    //        c.createDatabase(db);
    //        return null;
    //      });
    //    }catch (Exception e) {
    //      e.printStackTrace();
    //    }
    List<String> list = client.run(c -> c.getAllDatabases());
    System.out.println(list);
    Database databases = client.run(c -> c.getDatabase("amoro"));
    System.out.println(databases);
  }
}
