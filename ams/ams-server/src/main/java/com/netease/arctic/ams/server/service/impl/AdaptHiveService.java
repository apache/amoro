package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.server.model.UpgradeHiveMeta;
import com.netease.arctic.ams.server.model.UpgradeRunningInfo;
import com.netease.arctic.ams.server.model.UpgradeStatus;
import com.netease.arctic.ams.server.utils.AmsUtils;
import com.netease.arctic.ams.server.utils.FilesStatisticsBuilder;
import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.hive.table.HiveMetaStore;
import com.netease.arctic.hive.table.HiveTable;
import com.netease.arctic.hive.utils.HiveFileInfo;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.hive.utils.HiveUtils;
import com.netease.arctic.hive.utils.KeyData;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

;

public class AdaptHiveService {

  private static final Logger LOG = LoggerFactory.getLogger(AdaptHiveService.class);

  private static final int CORE_POOL_SIZE = 5;
  private static final long QUEUE_CAPACITY = 5;
  private static ConcurrentHashMap<TableIdentifier, UpgradeRunningInfo> runningInfoCache = new ConcurrentHashMap<>();
  private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE * 2,
      QUEUE_CAPACITY, TimeUnit.SECONDS, new LinkedBlockingDeque<>(5));
  private static final String DEFAULT_TXID = "txid=0";
  private static final int NODE_MASK_ID = -1;
  private static final int NODE_INDEX_ID = -2;
  private static final int BASE_FILE_ID = 0;
  private static final int FILE_TYPE_ID = -5;


  public void upgradeHiveTable(ArcticHiveCatalog ac, TableIdentifier tableIdentifier, UpgradeHiveMeta upgradeHiveMeta) {
    executor.submit(() -> {
      runningInfoCache.put(tableIdentifier, new UpgradeRunningInfo());
      boolean upgradeHive = false;
      try {
        HiveMetaStore hiveMetaStore = HiveMetaStore.getHiveMetaStore(ac);
        HiveTable hiveTable = hiveMetaStore.getHiveTable(tableIdentifier);
        List<String> pkList = upgradeHiveMeta.getPkList().stream()
            .map(UpgradeHiveMeta.PrimaryKeyField::getFieldName).collect(Collectors.toList());
        hiveTable.setPrimaryKeys(pkList);
        Schema schema = HiveSchemaUtil.hiveTableSchemaToIceberg(hiveTable, pkList);
        upgradeHiveMeta.getProperties().put(TableProperties.UPGRADE_ENABLE, "true");
        ArcticTable arcticTable = ac.newTableBuilder(tableIdentifier, schema)
            .withProperties(upgradeHiveMeta.getProperties())
            .withPartitionSpec(hiveTable.getPartitionKeys())
            .withPrimaryKeySpec(hiveTable.getPrimaryKeys())
            .create();
        hiveDataMigration(arcticTable, ac, tableIdentifier);
        runningInfoCache.get(tableIdentifier).setStatus(UpgradeStatus.SUCCESS.getName());
      } catch (Throwable t) {
        LOG.error("Failed to upgrade hive table to arctic");
        runningInfoCache.get(tableIdentifier).setErrorMessage(AmsUtils.getStackTrace(t));
        runningInfoCache.get(tableIdentifier).setStatus(UpgradeStatus.FAILED.getName());
      }
    });
  }

  public UpgradeRunningInfo getUpgradeRunningInfo(TableIdentifier tableIdentifier) {
    return runningInfoCache.get(tableIdentifier);
  }

  public void hiveDataMigration(ArcticTable arcticTable, ArcticHiveCatalog ac, TableIdentifier tableIdentifier)
      throws Exception {
    String hiveDataLocation = arcticTable.location() + "/hive_data";
    arcticTable.io().mkdirs(hiveDataLocation);
    HiveMetaStore hiveMetaStore = HiveMetaStore.getHiveMetaStore(ac);
    HiveTable hiveTable = hiveMetaStore.getHiveTable(tableIdentifier);
    if (hiveTable.getHivePartitionKeys().isEmpty()) {
      for (FileStatus fileStatus : arcticTable.io().list(arcticTable.location())) {
        if (!fileStatus.isDirectory()) {
          String newPath = null;
          if (hiveTable.getHivePartitionKeys().isEmpty()) {
            newPath = hiveDataLocation + "/" + System.currentTimeMillis() + UUID.randomUUID();
          }
          arcticTable.io().rename(fileStatus.getPath().toString(), newPath);
        }
      }
    } else {
      List<String> partitions = HiveUtils.getHivePartitions(hiveMetaStore, tableIdentifier);
      List<String> partitionLocations = HiveUtils.getHivePartitionLocations(hiveMetaStore, tableIdentifier);
      for (int i = 0; i < partitionLocations.size(); i++) {
        String partition = partitions.get(i);
        String oldLocation = partitionLocations.get(i);
        String newLoaction = hiveDataLocation + "/" + partition + "/" + DEFAULT_TXID;
        for (FileStatus fileStatus : arcticTable.io().list(oldLocation)) {
          if (!fileStatus.isDirectory()) {
            arcticTable.io().rename(fileStatus.getPath().toString(), newLoaction);
          }
        }
        HiveUtils.alterPartition(hiveMetaStore, tableIdentifier, partition, newLoaction);
      }
    }
    appendAllHiveFilesIntoBaseTable(ac, arcticTable, hiveMetaStore, hiveTable, true);
    try {
      HiveUtils.alterTableLocation(HiveMetaStore.getHiveMetaStore(ac), arcticTable.id(), hiveDataLocation);
      LOG.info("table{" + arcticTable.name() + "} alter hive table location " + hiveDataLocation + " success");
    } catch (IOException e) {
      LOG.warn("table{" + arcticTable.name() + "} alter hive table location failed", e);
      throw new RuntimeException(e);
    }
  }

  private void appendAllHiveFilesIntoBaseTable(ArcticHiveCatalog ac, ArcticTable arcticTable,
                                               HiveMetaStore hiveMetaStore, HiveTable hiveTable, boolean isKeyedTable) {
    Map<Partition, List<HiveFileInfo>> hiveTableFiles = HiveUtils.getHiveTableFiles(
        arcticTable.id(),
        HiveMetaStore.getHiveMetaStore(ac),
        arcticTable.io());
    if (hiveTableFiles.isEmpty()) {
      LOG.info("{} init, with no hive files", arcticTable.id());
      return;
    }
    AppendFiles baseAppend = null;
    if (isKeyedTable) {
      baseAppend = arcticTable.asKeyedTable().baseTable().newAppend();
    } else {
      baseAppend = arcticTable.asUnkeyedTable().newAppend();
    }

    FilesStatisticsBuilder fb = new FilesStatisticsBuilder();
    org.apache.iceberg.PartitionSpec partitionSpec = hiveTable.getPartitionKeys();
    AppendFiles finalBaseAppend = baseAppend;
    hiveTableFiles.forEach((partition, files) -> files.stream()
        .filter(file -> file.getLength() > 0)
        .forEach(file -> {
          DataFile dataFile =
              buildInternalTableFile(arcticTable, partition,
                  file.getPath(), file.getLength(), partitionSpec, hiveMetaStore);
          finalBaseAppend.appendFile(dataFile);
          fb.addFile(file.getLength());
        }));
    LOG.info("{} init, add hive files into arctic base table {}",
        arcticTable.id(), fb.build());
    baseAppend.commit();
    LOG.info("{} init, base commit success", arcticTable.id());
  }

  private DataFile buildInternalTableFile(ArcticTable arcticTable,
                                          Partition partition,
                                          String filePath,
                                          long length,
                                          org.apache.iceberg.PartitionSpec partitionSpec,
                                          HiveMetaStore hiveMetaStore) {
    Metrics metrics = new Metrics(1L,
        Maps.newHashMap(),
        Maps.newHashMap(),
        Maps.newHashMap(),
        Maps.newHashMap(),
        Maps.newHashMap(),
        Maps.newHashMap());
    metrics.lowerBounds().put(NODE_MASK_ID, wrapLong(0L));
    metrics.lowerBounds().put(NODE_INDEX_ID, wrapLong(0L));
    metrics.lowerBounds().put(FILE_TYPE_ID, wrapInt(BASE_FILE_ID));
    KeyData partitionData;
    if (partition == null) {
      partitionData = KeyData.get(HiveUtils.hiveTablePartitionSpec(hiveMetaStore, arcticTable.id()));
    } else {
      partitionData = HiveUtils
          .getPartition(HiveUtils.hiveTablePartitionSpec(hiveMetaStore, arcticTable.id()), partition.getValues());
    }
    org.apache.iceberg.DataFile icebergDataFile =
        org.apache.iceberg.DataFiles.builder(partitionSpec)
            .withPath(filePath)
            .withPartition(partitionData)
            .withFileSizeInBytes(length)
            .withMetrics(metrics)
            .withFormat(FileFormat.PARQUET)
            .build();
    return icebergDataFile;
  }

  private ByteBuffer wrapLong(long value) {
    return (ByteBuffer) ByteBuffer.allocate(Long.BYTES).putLong(value).rewind();
  }

  private ByteBuffer wrapInt(int value) {
    return (ByteBuffer) ByteBuffer.allocate(Integer.BYTES).putInt(value).rewind();
  }
}