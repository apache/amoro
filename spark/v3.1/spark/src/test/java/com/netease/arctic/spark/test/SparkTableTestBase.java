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
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.thrift.TException;

import java.util.function.Consumer;

public class SparkTableTestBase extends SparkTestBase {
   
  public TableTestBuilder test() {
    return new TableTestBuilder();
  }

  protected class TableTestContext extends TestContext<TableTestContext> {

    public final String sparkCatalogName;
    public final String arcticCatalogName;

    public final ArcticCatalog arcticCatalog;
    public final TableIdentifier tableIdentifier;
    public final String databaseAndTable;

    protected TableTestContext(String sparkCatalog, TableIdentifier tableId) {
      this.sparkCatalogName = sparkCatalog;
      this.arcticCatalogName = arcticCatalogName(sparkCatalog);
      this.arcticCatalog = CatalogLoader.load(catalogUrl(arcticCatalogName));
      this.tableIdentifier = TableIdentifier.of(arcticCatalogName, tableId.getDatabase(), tableId.getTableName());
      this.databaseAndTable = tableId.getDatabase() + "." + tableId.getTableName();
    }

    public ArcticTable loadTable() {
      return arcticCatalog.loadTable(this.tableIdentifier);
    }

    public Table loadHiveTable() {
      try {
        return env.HMS.getHiveClient().getTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
      } catch (TException e) {
        throw new RuntimeException(e);
      }
    }

    public boolean isHiveCatalog() {
      return SparkTableTestBase.this.isHiveCatalog(sparkCatalogName);
    }

    @Override
    TableTestContext testEnv() {
      return this;
    }
  }

  protected class TableTestBuilder {
    private String catalog;
    private String database = "spark_table_test_db";
    private String table = "test_tbl";

    private boolean autoCreateDB = true;

    public TableTestBuilder inSparkCatalog(String catalog) {
      this.catalog = catalog;
      return this;
    }

    public void execute(Consumer<TableTestContext> test) {
      TableTestContext context = new TableTestContext(
          this.catalog,
          TableIdentifier.of(this.catalog, database, table));

      context.before(() -> {
        sql("USE " + catalog);
        if (autoCreateDB) {
          try {
            context.arcticCatalog.createDatabase(database);
          } catch (AlreadyExistsException e) {//pass
          }
        }
      }).after(() ->
          context.arcticCatalog.dropTable(context.tableIdentifier, true)
      );

      context.test(test);
    }
  }
}
