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

package com.netease.arctic.spark.test;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.CollectionHelper;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.junit.Assert;

import java.util.List;

public class SparkTableTestBase extends SparkTestBase {

  protected String database = "spark_test_database";

  protected String table = "test_table";

  protected void testInCatalog(String catalog, Runnable test) {
    doTest(catalog, database, table, test);
  }

  protected void doTest(String catalog, String database, String table, Runnable runnable) {
    sql("USE " + catalog);
    sql("CREATE DATABASE IF NOT EXISTS " + database);

    try {
      runnable.run();
    } finally {
      sql("USE " + catalog);
      sql("DROP TABLE IF EXISTS " + database + "." + table);
    }
  }

  protected static void assertType(Type expect, Type actual) {
    Assert.assertEquals(
        "type should be same",
        expect.isPrimitiveType(), actual.isPrimitiveType());
    if (expect.isPrimitiveType()) {
      Assert.assertEquals(expect, actual);
    } else {
      List<Types.NestedField> expectFields = expect.asNestedType().fields();
      List<Types.NestedField> actualFields = actual.asNestedType().fields();
      Assert.assertEquals(expectFields.size(), actualFields.size());

      CollectionHelper.zip(expectFields, actualFields)
          .forEach(x -> {
            Assert.assertEquals(x.getLeft().name(), x.getRight().name());
            Assert.assertEquals(x.getLeft().isOptional(), x.getRight().isOptional());
            Assert.assertEquals(x.getLeft().doc(), x.getRight().doc());
            assertType(x.getLeft().type(), x.getRight().type());
          });
    }
  }

  protected ArcticTable loadTable(String sparkCatalog, String database, String table) {
    ArcticCatalog arcticCatalog = CatalogLoader.load(catalogUrl(sparkCatalog));
    return arcticCatalog.loadTable(TableIdentifier.of(arcticCatalog.name(), database, table));
  }

  protected Table loadHiveTable(String database, String table) {
    try {
      return env.HMS.getHiveClient().getTable(database, table);
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }
}
