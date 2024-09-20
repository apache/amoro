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

package org.apache.amoro.hive.utils;

import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.CatalogTestBase;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.hive.catalog.MixedHiveCatalog;
import org.apache.amoro.hive.table.UnkeyedHiveTable;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestUpgradeHiveTableUtil extends CatalogTestBase {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private Table hiveTable;
  private TableIdentifier identifier;
  private final String db = "testUpgradeHiveDb";
  private final String table = "testUpgradeHiveTable";
  private final boolean isPartitioned;
  private final FileFormat fileFormat;
  private final String[] partitionNames = {"name", HiveTableTestHelper.COLUMN_NAME_OP_DAY};
  private final String[] partitionValues = {"Bob", "2020-01-01"};

  public TestUpgradeHiveTableUtil(
      CatalogTestHelper catalogTestHelper, boolean isPartitioned, FileFormat fileFormat)
      throws IOException {
    super(catalogTestHelper);
    folder.create();
    this.isPartitioned = isPartitioned;
    this.fileFormat = fileFormat;
  }

  @Before
  public void setUp() throws TException, IOException {
    Database database = new Database();
    database.setName(db);
    TEST_HMS.getHiveClient().createDatabase(database);
    PartitionSpec spec =
        isPartitioned
            ? PartitionSpec.builderFor(HiveTableTestHelper.HIVE_TABLE_SCHEMA)
                .identity("name")
                .identity(HiveTableTestHelper.COLUMN_NAME_OP_DAY)
                .build()
            : PartitionSpec.unpartitioned();
    hiveTable = createHiveTable(db, table, HiveTableTestHelper.HIVE_TABLE_SCHEMA, spec);
    if (isPartitioned) {
      createPartition();
    }
    identifier = TableIdentifier.of(getMixedFormatCatalog().name(), db, table);
  }

  @After
  public void dropTable() throws TException {
    getMixedFormatCatalog().dropTable(identifier, true);
    TEST_HMS.getHiveClient().dropDatabase(db);
  }

  @Parameterized.Parameters(name = "{0}, {1}, {2}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        true,
        FileFormat.PARQUET
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        false,
        FileFormat.PARQUET
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        true,
        FileFormat.ORC
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        false,
        FileFormat.ORC
      }
    };
  }

  @Test
  public void upgradeHiveTable() throws Exception {
    UpgradeHiveTableUtil.upgradeHiveTable(
        (MixedHiveCatalog) getMixedFormatCatalog(), identifier, new ArrayList<>(), new HashMap<>());
    MixedTable table = getMixedFormatCatalog().loadTable(identifier);
    UnkeyedHiveTable baseTable =
        table.isKeyedTable()
            ? (UnkeyedHiveTable) table.asKeyedTable().baseTable()
            : (UnkeyedHiveTable) table.asUnkeyedTable();
    if (table.spec().isPartitioned()) {
      List<Partition> partitions =
          HivePartitionUtil.getHiveAllPartitions(
              ((MixedHiveCatalog) getMixedFormatCatalog()).getHMSClient(), table.id());
      for (Partition partition : partitions) {
        StructLike partitionData =
            HivePartitionUtil.buildPartitionData(partition.getValues(), table.spec());
        Map<String, String> partitionProperties = baseTable.partitionProperty().get(partitionData);
        Assert.assertTrue(
            partitionProperties.containsKey(
                HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION));
        Assert.assertTrue(
            partitionProperties.containsKey(
                HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME));
        Assert.assertFalse(
            HiveMetaSynchronizer.partitionHasModified(baseTable, partition, partitionData));
      }
    } else {
      Map<String, String> partitionProperties =
          baseTable.partitionProperty().get(TablePropertyUtil.EMPTY_STRUCT);
      Assert.assertTrue(
          partitionProperties.containsKey(
              HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION));
      Assert.assertTrue(
          partitionProperties.containsKey(
              HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME));
      Assert.assertFalse(HiveMetaSynchronizer.tableHasModified(baseTable, hiveTable));
    }
  }

  private Table createHiveTable(String db, String table, Schema schema, PartitionSpec spec)
      throws TException, IOException {
    Table hiveTable = newHiveTable(db, table, schema, spec);
    hiveTable.setSd(
        HiveTableUtil.storageDescriptor(
            schema, spec, folder.newFolder(db).getAbsolutePath(), fileFormat));
    TEST_HMS.getHiveClient().createTable(hiveTable);
    return TEST_HMS.getHiveClient().getTable(db, table);
  }

  private void createPartition() throws TException {
    List<String> partitions = Lists.newArrayList();
    for (int i = 0; i < partitionNames.length; i++) {
      partitions.add(String.join("=", partitionNames[i], partitionValues[i]));
    }
    Partition newPartition =
        HivePartitionUtil.newPartition(
            hiveTable,
            partitions,
            hiveTable.getSd().getLocation() + "/" + String.join("/", partitions),
            new ArrayList<>(),
            (int) (System.currentTimeMillis() / 1000));
    TEST_HMS.getHiveClient().add_partition(newPartition);
  }

  private org.apache.hadoop.hive.metastore.api.Table newHiveTable(
      String db, String table, Schema schema, PartitionSpec partitionSpec) {
    final long currentTimeMillis = System.currentTimeMillis();
    org.apache.hadoop.hive.metastore.api.Table newTable =
        new org.apache.hadoop.hive.metastore.api.Table(
            table,
            db,
            System.getProperty("user.name"),
            (int) currentTimeMillis / 1000,
            (int) currentTimeMillis / 1000,
            Integer.MAX_VALUE,
            null,
            HiveSchemaUtil.hivePartitionFields(schema, partitionSpec),
            new HashMap<>(),
            null,
            null,
            TableType.EXTERNAL_TABLE.toString());

    newTable.getParameters().put("EXTERNAL", "TRUE");
    return newTable;
  }
}
