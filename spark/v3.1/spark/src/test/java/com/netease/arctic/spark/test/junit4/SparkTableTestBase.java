/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.test.junit4;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.junit.After;
import org.junit.Before;

public class SparkTableTestBase extends SparkTestBase {

  public String database() {
    return "spark_test_db";
  }

  public String table() {
    return "test_table";
  }

  @Before
  public void setUpDatabase() {
    sql("USE " + catalog());
    sql("CREATE DATABASE IF NOT EXISTS " + database());
  }

  @After
  public void cleanUpTable() {
    sql("USE " + catalog());
    sql("DROP TABLE IF EXISTS " + database() + "." + table());
  }

  protected ArcticTable loadTable() {

    ArcticCatalog arcticCatalog = CatalogLoader.load(url);
    return arcticCatalog.loadTable(TableIdentifier.of(arcticCatalogName, database(), table()));
  }
}
