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

package com.netease.arctic.spark.test.sql;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.spark.test.helper.TestSource;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Assertions;

import java.util.List;
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
    public String sourceDatabaseAndTable;
    public TableIdentifier sourceTableIdentifier;

    protected TableTestContext(
        String sparkCatalog, TableIdentifier tableId
    ) {
      this.sparkCatalogName = sparkCatalog;
      this.arcticCatalogName = arcticCatalogName(sparkCatalog);
      this.arcticCatalog = CatalogLoader.load(catalogUrl(arcticCatalogName));
      this.tableIdentifier = TableIdentifier.of(arcticCatalogName, tableId.getDatabase(), tableId.getTableName());
      this.databaseAndTable = tableId.getDatabase() + "." + tableId.getTableName();

      this.sourceTableIdentifier = TableIdentifier.of(
          arcticCatalogName, tableId.getDatabase(), "source_tbl");
      this.sourceDatabaseAndTable = this.sourceTableIdentifier.getDatabase() + "."
          + this.sourceTableIdentifier.getTableName();
    }

    private void sourceHiveTable(Table hiveTable) {
      this.sourceDatabaseAndTable = hiveTable.getDbName() + "." + hiveTable.getTableName();
      this.sourceTableIdentifier = TableIdentifier.of(arcticCatalogName, hiveTable.getDbName(),
          hiveTable.getTableName());
    }

    public ArcticTable loadTable() {
      return arcticCatalog.loadTable(this.tableIdentifier);
    }

    public boolean tableExists() {
      return arcticCatalog.tableExists(this.tableIdentifier);
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
    private final String database = "spark_table_test_db";

    private final boolean autoCreateDB = true;
    private final List<Consumer<TableTestContext>> beforeHocks = Lists.newArrayList();
    private final List<Consumer<TableTestContext>> afterHocks = Lists.newArrayList();
    private Table sourceHiveTable;

    public TableTestBuilder inSparkCatalog(String catalog) {
      this.catalog = catalog;
      this.beforeHocks.add(context -> {
        sql("USE " + catalog);
        if (autoCreateDB) {
          try {
            context.arcticCatalog.createDatabase(database);
          } catch (AlreadyExistsException e) {//pass
          }
        }
      });

      this.afterHocks.add(context ->
          context.arcticCatalog.dropTable(context.tableIdentifier, true)
      );
      return this;
    }

    public TableTestBuilder registerSourceView(TestSource source) {
      Dataset<Row> ds = source.toSparkDataset(spark);
      final String sourceView = "source_view";

      this.beforeHocks.add(context -> {
        ds.createOrReplaceTempView(sourceView);
        context.sourceTableIdentifier = null;
        context.sourceDatabaseAndTable = sourceView;
      });

      this.afterHocks.add(context -> {
        spark.catalog().dropTempView(sourceView);
        context.sourceDatabaseAndTable = "";
      });

      return this;
    }

    public TableTestBuilder cleanSourceTable() {
      this.afterHocks.add(context -> {
        if (context.sourceTableIdentifier != null) {
          context.arcticCatalog.dropTable(context.sourceTableIdentifier, true);
        }
      });
      return this;
    }

    public TableTestBuilder withHiveTable(Table table) {
      this.beforeHocks.add(context -> {
        Assertions.assertTrue(
            context.isHiveCatalog(),
            "withHiveTable only work when current is a hive catalog");
        env.createHiveDatabaseIfNotExist(table.getDbName());
        try {
          env.HMS.getHiveClient().createTable(table);
        } catch (TException e) {
          throw new RuntimeException(e);
        }
      });

      this.afterHocks.add(context -> {
        try {
          env.HMS.getHiveClient().dropTable(table.getDbName(), table.getTableName());
        } catch (NoSuchObjectException e) {
          // pass
        } catch (TException e) {
          throw new RuntimeException(e);
        }
      });
      this.sourceHiveTable = table;
      return this;
    }

    public void execute(Consumer<TableTestContext> test) {
      String table = "test_tbl";
      TableTestContext context = new TableTestContext(
          this.catalog,
          TableIdentifier.of(this.catalog, database, table)
      );
      if (sourceHiveTable != null) {
        context.sourceHiveTable(this.sourceHiveTable);
      }

      beforeHocks.forEach(context::before);
      afterHocks.forEach(context::after);
      context.test(test);
    }
  }
}
