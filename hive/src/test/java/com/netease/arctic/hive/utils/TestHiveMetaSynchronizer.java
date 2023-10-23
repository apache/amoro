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

package com.netease.arctic.hive.utils;

import static com.netease.arctic.utils.TablePropertyUtil.EMPTY_STRUCT;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.io.HiveDataTestHelpers;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TableFileUtil;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestHiveMetaSynchronizer extends TableTestBase {
  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestHiveMetaSynchronizer(boolean ifKeyed, boolean ifPartitioned) {
    super(
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(ifKeyed, ifPartitioned));
  }

  @Parameterized.Parameters(name = "ifKeyed = {0}, ifPartitioned = {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {true, true},
      {true, false},
      {false, true},
      {false, false}
    };
  }

  @Override
  protected SupportHive getArcticTable() {
    return (SupportHive) super.getArcticTable();
  }

  @Test
  public void testUnPartitionTableSyncInIceberg() throws Exception {
    Assume.assumeFalse(isPartitionedTable());
    UnkeyedTable baseTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Assert.assertEquals(0, partitionProperty.size());
    String newLocation = createEmptyLocationForHive(getArcticTable());
    baseTable
        .updatePartitionProperties(null)
        .set(EMPTY_STRUCT, HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION, newLocation)
        .commit();
    String hiveLocation =
        (getArcticTable())
            .getHMSClient()
            .run(
                client -> {
                  Table hiveTable =
                      client.getTable(
                          getArcticTable().id().getDatabase(),
                          getArcticTable().id().getTableName());
                  return hiveTable.getSd().getLocation();
                });
    Assert.assertNotEquals(newLocation, hiveLocation);

    HiveMetaSynchronizer.syncArcticDataToHive(getArcticTable());
    hiveLocation =
        (getArcticTable())
            .getHMSClient()
            .run(
                client -> {
                  Table hiveTable =
                      client.getTable(
                          getArcticTable().id().getDatabase(),
                          getArcticTable().id().getTableName());
                  return hiveTable.getSd().getLocation();
                });
    Assert.assertEquals(newLocation, hiveLocation);
  }

  @Test
  public void testUnPartitionTableSyncNotInIceberg() throws Exception {
    Assume.assumeFalse(isPartitionedTable());
    UnkeyedTable baseTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Assert.assertEquals(0, partitionProperty.size());

    String oldHiveLocation =
        getArcticTable()
            .getHMSClient()
            .run(
                client -> {
                  Table hiveTable =
                      client.getTable(
                          getArcticTable().id().getDatabase(),
                          getArcticTable().id().getTableName());
                  return hiveTable.getSd().getLocation();
                });

    HiveMetaSynchronizer.syncArcticDataToHive(getArcticTable());
    String newHiveLocation =
        getArcticTable()
            .getHMSClient()
            .run(
                client -> {
                  Table hiveTable =
                      client.getTable(
                          getArcticTable().id().getDatabase(),
                          getArcticTable().id().getTableName());
                  return hiveTable.getSd().getLocation();
                });
    Assert.assertEquals(oldHiveLocation, newHiveLocation);
  }

  @Test
  public void testSyncOnlyInIceberg() throws Exception {
    Assume.assumeTrue(isPartitionedTable());
    UnkeyedTable baseTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Assert.assertEquals(0, partitionProperty.size());
    List<DataFile> dataFiles = writeAndCommitBaseAndHive(getArcticTable(), 1, true);
    String partitionLocation = TableFileUtil.getFileDir(dataFiles.get(0).path().toString());
    baseTable
        .updatePartitionProperties(null)
        .set(
            dataFiles.get(0).partition(),
            HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION,
            partitionLocation)
        .commit();

    List<String> partitionValues =
        HivePartitionUtil.partitionValuesAsList(
            dataFiles.get(0).partition(), getArcticTable().spec().partitionType());
    Assert.assertThrows(
        NoSuchObjectException.class,
        () ->
            getArcticTable()
                .getHMSClient()
                .run(
                    client ->
                        client.getPartition(
                            getArcticTable().id().getDatabase(),
                            getArcticTable().id().getTableName(),
                            partitionValues)));

    HiveMetaSynchronizer.syncArcticDataToHive(getArcticTable());
    Partition hivePartition =
        getArcticTable()
            .getHMSClient()
            .run(
                client ->
                    client.getPartition(
                        getArcticTable().id().getDatabase(),
                        getArcticTable().id().getTableName(),
                        partitionValues));
    Assert.assertEquals(partitionLocation, hivePartition.getSd().getLocation());
  }

  @Test
  public void testSyncOnlyInHiveCreateByArctic() throws Exception {
    Assume.assumeTrue(isPartitionedTable());
    UnkeyedTable baseTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Assert.assertEquals(0, partitionProperty.size());

    List<DataFile> dataFiles = writeAndCommitBaseAndHive(getArcticTable(), 1, true);
    String partitionLocation = TableFileUtil.getFileDir(dataFiles.get(0).path().toString());
    List<String> partitionValues =
        HivePartitionUtil.partitionValuesAsList(
            dataFiles.get(0).partition(), getArcticTable().spec().partitionType());
    getArcticTable()
        .getHMSClient()
        .run(
            client -> {
              Table hiveTable =
                  client.getTable(
                      getArcticTable().id().getDatabase(), getArcticTable().id().getTableName());
              StorageDescriptor tableSd = hiveTable.getSd();
              PrincipalPrivilegeSet privilegeSet = hiveTable.getPrivileges();
              int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
              Partition p = new Partition();
              p.setValues(partitionValues);
              p.setDbName(hiveTable.getDbName());
              p.setTableName(hiveTable.getTableName());
              p.setCreateTime(lastAccessTime);
              p.setLastAccessTime(lastAccessTime);
              StorageDescriptor sd = tableSd.deepCopy();
              sd.setLocation(partitionLocation);
              p.setSd(sd);

              int files = dataFiles.size();
              long totalSize =
                  dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
              p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
              p.putToParameters("totalSize", totalSize + "");
              p.putToParameters("numFiles", files + "");
              p.putToParameters(HiveTableProperties.ARCTIC_TABLE_FLAG, "true");
              if (privilegeSet != null) {
                p.setPrivileges(privilegeSet.deepCopy());
              }

              return client.addPartition(p);
            });

    Partition hivePartition =
        getArcticTable()
            .getHMSClient()
            .run(
                client ->
                    client.getPartition(
                        getArcticTable().id().getDatabase(),
                        getArcticTable().id().getTableName(),
                        partitionValues));
    Assert.assertEquals(partitionLocation, hivePartition.getSd().getLocation());

    HiveMetaSynchronizer.syncArcticDataToHive(getArcticTable());

    Assert.assertThrows(
        NoSuchObjectException.class,
        () ->
            getArcticTable()
                .getHMSClient()
                .run(
                    client ->
                        client.getPartition(
                            getArcticTable().id().getDatabase(),
                            getArcticTable().id().getTableName(),
                            partitionValues)));
  }

  @Test
  public void testSyncOnlyInHiveCreateNotByArctic() throws Exception {
    Assume.assumeTrue(isPartitionedTable());
    UnkeyedTable baseTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Assert.assertEquals(0, partitionProperty.size());

    List<DataFile> dataFiles = writeAndCommitBaseAndHive(getArcticTable(), 1, true);
    String partitionLocation = TableFileUtil.getFileDir(dataFiles.get(0).path().toString());
    List<String> partitionValues =
        HivePartitionUtil.partitionValuesAsList(
            dataFiles.get(0).partition(), getArcticTable().spec().partitionType());
    getArcticTable()
        .getHMSClient()
        .run(
            client -> {
              Table hiveTable =
                  client.getTable(
                      getArcticTable().id().getDatabase(), getArcticTable().id().getTableName());
              StorageDescriptor tableSd = hiveTable.getSd();
              PrincipalPrivilegeSet privilegeSet = hiveTable.getPrivileges();
              int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
              Partition p = new Partition();
              p.setValues(partitionValues);
              p.setDbName(hiveTable.getDbName());
              p.setTableName(hiveTable.getTableName());
              p.setCreateTime(lastAccessTime);
              p.setLastAccessTime(lastAccessTime);
              StorageDescriptor sd = tableSd.deepCopy();
              sd.setLocation(partitionLocation);
              p.setSd(sd);

              int files = dataFiles.size();
              long totalSize =
                  dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
              p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
              p.putToParameters("totalSize", totalSize + "");
              p.putToParameters("numFiles", files + "");
              if (privilegeSet != null) {
                p.setPrivileges(privilegeSet.deepCopy());
              }

              return client.addPartition(p);
            });

    Partition hivePartition =
        getArcticTable()
            .getHMSClient()
            .run(
                client ->
                    client.getPartition(
                        getArcticTable().id().getDatabase(),
                        getArcticTable().id().getTableName(),
                        partitionValues));
    Assert.assertEquals(partitionLocation, hivePartition.getSd().getLocation());

    HiveMetaSynchronizer.syncArcticDataToHive(getArcticTable());

    hivePartition =
        getArcticTable()
            .getHMSClient()
            .run(
                client ->
                    client.getPartition(
                        getArcticTable().id().getDatabase(),
                        getArcticTable().id().getTableName(),
                        partitionValues));
    Assert.assertEquals(partitionLocation, hivePartition.getSd().getLocation());
  }

  @Test
  public void testSyncInBoth() throws Exception {
    Assume.assumeTrue(isPartitionedTable());
    UnkeyedTable baseTable =
        isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Assert.assertEquals(0, partitionProperty.size());

    List<DataFile> dataFiles = writeAndCommitBaseAndHive(getArcticTable(), 1, true);
    String partitionLocation = TableFileUtil.getFileDir(dataFiles.get(0).path().toString());
    List<String> partitionValues =
        HivePartitionUtil.partitionValuesAsList(
            dataFiles.get(0).partition(), getArcticTable().spec().partitionType());
    getArcticTable()
        .getHMSClient()
        .run(
            client -> {
              Table hiveTable =
                  client.getTable(
                      getArcticTable().id().getDatabase(), getArcticTable().id().getTableName());
              StorageDescriptor tableSd = hiveTable.getSd();
              PrincipalPrivilegeSet privilegeSet = hiveTable.getPrivileges();
              int lastAccessTime = (int) (System.currentTimeMillis() / 1000);
              Partition p = new Partition();
              p.setValues(partitionValues);
              p.setDbName(hiveTable.getDbName());
              p.setTableName(hiveTable.getTableName());
              p.setCreateTime(lastAccessTime);
              p.setLastAccessTime(lastAccessTime);
              StorageDescriptor sd = tableSd.deepCopy();
              sd.setLocation(partitionLocation);
              p.setSd(sd);

              int files = dataFiles.size();
              long totalSize =
                  dataFiles.stream().map(ContentFile::fileSizeInBytes).reduce(0L, Long::sum);
              p.putToParameters("transient_lastDdlTime", lastAccessTime + "");
              p.putToParameters("totalSize", totalSize + "");
              p.putToParameters("numFiles", files + "");
              if (privilegeSet != null) {
                p.setPrivileges(privilegeSet.deepCopy());
              }

              return client.addPartition(p);
            });

    Partition hivePartition =
        getArcticTable()
            .getHMSClient()
            .run(
                client ->
                    client.getPartition(
                        getArcticTable().id().getDatabase(),
                        getArcticTable().id().getTableName(),
                        partitionValues));
    Assert.assertEquals(partitionLocation, hivePartition.getSd().getLocation());

    List<DataFile> newDataFiles = writeAndCommitBaseAndHive(getArcticTable(), 2, true);
    String newPartitionLocation = TableFileUtil.getFileDir(newDataFiles.get(0).path().toString());
    baseTable
        .updatePartitionProperties(null)
        .set(
            newDataFiles.get(0).partition(),
            HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION,
            newPartitionLocation)
        .commit();
    Assert.assertNotEquals(newPartitionLocation, hivePartition.getSd().getLocation());

    HiveMetaSynchronizer.syncArcticDataToHive(getArcticTable());

    hivePartition =
        getArcticTable()
            .getHMSClient()
            .run(
                client ->
                    client.getPartition(
                        getArcticTable().id().getDatabase(),
                        getArcticTable().id().getTableName(),
                        partitionValues));
    Assert.assertEquals(newPartitionLocation, hivePartition.getSd().getLocation());
  }

  private String createEmptyLocationForHive(ArcticTable arcticTable) {
    // create a new empty location for hive
    String newLocation =
        ((SupportHive) arcticTable).hiveLocation() + "/ts_" + System.currentTimeMillis();
    OutputFile file = arcticTable.io().newOutputFile(newLocation + "/.keep");
    try {
      file.createOrOverwrite().close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return newLocation;
  }

  private List<DataFile> writeAndCommitBaseAndHive(
      ArcticTable table, long txId, boolean writeHive) {
    String hiveSubDir = HiveTableUtil.newHiveSubdirectory(txId);
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writeBaseStore(
            table, txId, createRecords(1, 100), false, writeHive, hiveSubDir);
    UnkeyedTable baseTable =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    AppendFiles baseAppend = baseTable.newAppend();
    dataFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
    return dataFiles;
  }

  public List<Record> createRecords(int start, int length) {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = start; i < start + length; i++) {
      builder.add(
          tableTestHelper()
              .generateTestRecord(
                  i, "name" + i, 0L, LocalDateTime.of(2022, 1, i % 2 + 1, 12, 0, 0).toString()));
    }
    return builder.build();
  }
}
