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

package com.netease.arctic.server.table.executor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.op.SnapshotSummary;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.table.DataExpirationConfig;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ManifestEntryFields;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.ContentScanTask;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.Literal;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DataExpiringExecutor extends BaseTableExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DataExpiringExecutor.class);

  private final Duration interval;

  public static final String EXPIRE_TIMESTAMP_MS = "TIMESTAMP_MS";
  public static final String EXPIRE_TIMESTAMP_S = "TIMESTAMP_S";

  protected DataExpiringExecutor(TableManager tableManager, int poolSize, Duration interval) {
    super(tableManager, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval.toMillis();
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().getExpiringDataConfig().isEnabled();
  }

  @Override
  public void handleConfigChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    scheduleIfNecessary(tableRuntime, getStartDelay());
  }

  private boolean validateExpirationField(ArcticTable table, String expirationField) {
    Types.NestedField field = table.schema().findField(expirationField);

    if (StringUtils.isBlank(expirationField) || null == field) {
      LOG.warn(
          String.format(
              "Field(%s) used to determine data expiration is illegal for table(%s)",
              expirationField, table.name()));
      return false;
    }
    Type.TypeID typeID = field.type().typeId();
    if (!DataExpirationConfig.FIELD_TYPES.contains(typeID)) {
      LOG.warn(
          String.format(
              "Table(%s) field(%s) type(%s) is not supported for data expiration, please use the "
                  + "following types: %s",
              table.name(),
              expirationField,
              typeID.name(),
              StringUtils.join(DataExpirationConfig.FIELD_TYPES, ", ")));
      return false;
    }

    return true;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      ArcticTable arcticTable = (ArcticTable) loadTable(tableRuntime).originalTable();
      DataExpirationConfig expirationConfig =
          tableRuntime.getTableConfiguration().getExpiringDataConfig();
      if (!expirationConfig.isEnabled()
          || expirationConfig.getRetentionTime() <= 0
          || !validateExpirationField(arcticTable, expirationConfig.getExpirationField())) {
        return;
      }

      purgeTableFrom(
          arcticTable,
          expirationConfig,
          Instant.now()
              .atZone(
                  getDefaultZoneId(
                      arcticTable.schema().findField(expirationConfig.getExpirationField())))
              .toInstant());
    } catch (Throwable t) {
      LOG.error("Unexpected purge error for table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  /**
   * Purge data older than the specified UTC timestamp
   *
   * @param table Arctic table
   * @param expirationConfig expiration configs
   * @param instant timestamp/timestampz/long field type uses UTC, others will use the local time
   *     zone
   */
  protected static void purgeTableFrom(
      ArcticTable table, DataExpirationConfig expirationConfig, Instant instant) {
    long expireTimestamp = instant.minusMillis(expirationConfig.getRetentionTime()).toEpochMilli();
    LOG.info(
        "Expiring Data older than {} in table {} ",
        Instant.ofEpochMilli(expireTimestamp)
            .atZone(
                getDefaultZoneId(table.schema().findField(expirationConfig.getExpirationField())))
            .toLocalDateTime(),
        table.name());

    purgeTableData(table, expirationConfig, expireTimestamp);
  }

  protected static ZoneId getDefaultZoneId(Types.NestedField expireField) {
    Type type = expireField.type();
    if (type.typeId() == Type.TypeID.STRING) {
      return ZoneId.systemDefault();
    }
    return ZoneOffset.UTC;
  }

  private static CloseableIterable<IcebergFileEntry> fileScan(
      UnkeyedTable table, Expression partitionFilter, Snapshot snapshot) {
    TableScan tableScan = table.newScan().filter(partitionFilter).includeColumnStats();

    CloseableIterable<FileScanTask> tasks;
    long snapshotId =
        null == snapshot ? ArcticServiceConstants.INVALID_SNAPSHOT_ID : snapshot.snapshotId();
    if (snapshotId == ArcticServiceConstants.INVALID_SNAPSHOT_ID) {
      tasks = tableScan.planFiles();
    } else {
      tasks = tableScan.useSnapshot(snapshotId).planFiles();
    }
    CloseableIterable<DataFile> dataFiles =
        CloseableIterable.transform(tasks, ContentScanTask::file);
    Set<DeleteFile> deleteFiles =
        StreamSupport.stream(tasks.spliterator(), false)
            .flatMap(e -> e.deletes().stream())
            .collect(Collectors.toSet());

    return CloseableIterable.transform(
        CloseableIterable.withNoopClose(Iterables.concat(dataFiles, deleteFiles)),
        contentFile ->
            new IcebergFileEntry(
                snapshotId,
                contentFile.dataSequenceNumber(),
                ManifestEntryFields.Status.EXISTING,
                contentFile));
  }

  private static void purgeTableData(
      ArcticTable table, DataExpirationConfig expirationConfig, long expireTimestamp) {
    Expression dataFilter = getDataExpression(table, expirationConfig, expireTimestamp);
    Map<StructLike, DataFileFreshness> partitionFreshness = Maps.newHashMap();

    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      ChangeTable changeTable = keyedTable.changeTable();
      BaseTable baseTable = keyedTable.baseTable();
      Snapshot changeSnapshot = changeTable.currentSnapshot();
      Snapshot baseSnapshot = baseTable.currentSnapshot();

      CloseableIterable<IcebergFileEntry> changeEntries =
          fileScan(changeTable, dataFilter, changeSnapshot);
      CloseableIterable<IcebergFileEntry> baseEntries =
          fileScan(baseTable, dataFilter, baseSnapshot);
      ExpireFiles changeExpiredFiles = new ExpireFiles();
      ExpireFiles baseExpiredFiles = new ExpireFiles();
      CloseableIterable<FileEntry> changed =
          CloseableIterable.transform(changeEntries, e -> new FileEntry(e, true));
      CloseableIterable<FileEntry> based =
          CloseableIterable.transform(baseEntries, e -> new FileEntry(e, false));

      try (CloseableIterable<FileEntry> entries =
          CloseableIterable.withNoopClose(Iterables.concat(changed, based))) {
        CloseableIterable<FileEntry> mayExpiredFiles =
            CloseableIterable.withNoopClose(
                Lists.newArrayList(
                    CloseableIterable.filter(
                        entries,
                        e ->
                            mayExpired(
                                table, e, expirationConfig, partitionFreshness, expireTimestamp))));
        CloseableIterable.filter(
                mayExpiredFiles, e -> willNotRetain(e, expirationConfig, partitionFreshness))
            .forEach(
                e -> {
                  if (e.isChange) {
                    changeExpiredFiles.addFile(e);
                  } else {
                    baseExpiredFiles.addFile(e);
                  }
                });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      expireFiles(changeTable, changeSnapshot.snapshotId(), changeExpiredFiles, expireTimestamp);
      expireFiles(baseTable, baseSnapshot.snapshotId(), baseExpiredFiles, expireTimestamp);
    } else {
      UnkeyedTable unkeyedTable = table.asUnkeyedTable();
      Snapshot snapshot = unkeyedTable.currentSnapshot();
      ExpireFiles expiredFiles = new ExpireFiles();
      try (CloseableIterable<IcebergFileEntry> entries =
          fileScan(unkeyedTable, dataFilter, snapshot)) {
        CloseableIterable<IcebergFileEntry> mayExpiredFiles =
            CloseableIterable.withNoopClose(
                Lists.newArrayList(
                    CloseableIterable.filter(
                        entries,
                        e ->
                            mayExpired(
                                table, e, expirationConfig, partitionFreshness, expireTimestamp))));
        CloseableIterable.filter(
                mayExpiredFiles, e -> willNotRetain(e, expirationConfig, partitionFreshness))
            .forEach(expiredFiles::addFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      expireFiles(unkeyedTable, snapshot.snapshotId(), expiredFiles, expireTimestamp);
    }
  }

  /**
   * Create a filter expression for expired files for the `FILE` level. For the `PARTITION` level,
   * we need to collect the oldest files to determine if the partition is obsolete, so we will not
   * filter for expired files at the scanning stage
   *
   * @param expirationConfig expiration configuration
   * @param expireTimestamp expired timestamp
   * @return filter expression
   */
  private static Expression getDataExpression(
      ArcticTable table, DataExpirationConfig expirationConfig, long expireTimestamp) {
    if (expirationConfig.getExpirationLevel().equals(DataExpirationConfig.ExpireLevel.PARTITION)) {
      return Expressions.alwaysTrue();
    }

    Types.NestedField field = table.schema().findField(expirationConfig.getExpirationField());
    Type.TypeID typeID = field.type().typeId();
    switch (typeID) {
      case TIMESTAMP:
        return Expressions.lessThanOrEqual(field.name(), expireTimestamp * 1000);
      case LONG:
        if (expirationConfig.getNumberDateFormat().equals(EXPIRE_TIMESTAMP_MS)) {
          return Expressions.lessThanOrEqual(field.name(), expireTimestamp);
        } else if (expirationConfig.getNumberDateFormat().equals(EXPIRE_TIMESTAMP_S)) {
          return Expressions.lessThanOrEqual(field.name(), expireTimestamp / 1000);
        } else {
          return Expressions.alwaysTrue();
        }
      case STRING:
        String expireDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(expireTimestamp), getDefaultZoneId(field))
                .format(
                    DateTimeFormatter.ofPattern(
                        expirationConfig.getDateTimePattern(), Locale.getDefault()));
        return Expressions.lessThanOrEqual(field.name(), expireDateTime);
      default:
        return Expressions.alwaysTrue();
    }
  }

  private static void expireFiles(
      UnkeyedTable table, long snapshotId, ExpireFiles expiredFiles, long expireTimestamp) {
    List<DataFile> dataFiles = expiredFiles.dataFiles;
    List<DeleteFile> deleteFiles = expiredFiles.deleteFiles;
    if (dataFiles.isEmpty() && deleteFiles.isEmpty()) {
      return;
    }
    // expire data files
    DeleteFiles delete = table.newDelete();
    dataFiles.forEach(delete::deleteFile);
    delete.set(SnapshotSummary.SNAPSHOT_PRODUCER, "DATA_EXPIRATION");
    delete.commit();
    // expire delete files
    if (!deleteFiles.isEmpty()) {
      RewriteFiles rewriteFiles = table.newRewrite().validateFromSnapshot(snapshotId);
      deleteFiles.forEach(rewriteFiles::deleteFile);
      rewriteFiles.set(SnapshotSummary.SNAPSHOT_PRODUCER, "DATA_EXPIRATION");
      rewriteFiles.commit();
    }

    // TODO: persistent table expiration record. Contains some meta information such as table_id,
    // snapshotId,
    //  file_infos(file_content, path, recordCount, fileSizeInBytes, equalityFieldIds,
    // partitionPath,
    //  sequenceNumber) and expireTimestamp...

    LOG.info(
        "Expired {} files older than {}, {} data files[{}] and {} delete files[{}]",
        table.name(),
        expireTimestamp,
        dataFiles.size(),
        dataFiles.stream().map(ContentFile::path).collect(Collectors.joining(",")),
        deleteFiles.size(),
        deleteFiles.stream().map(ContentFile::path).collect(Collectors.joining(",")));
  }

  private static class ExpireFiles {
    List<DataFile> dataFiles;
    List<DeleteFile> deleteFiles;

    ExpireFiles() {
      this.dataFiles = new LinkedList<>();
      this.deleteFiles = new LinkedList<>();
    }

    void addFile(IcebergFileEntry entry) {
      ContentFile<?> file = entry.getFile();
      switch (file.content()) {
        case DATA:
          dataFiles.add((DataFile) file.copyWithoutStats());
          break;
        case EQUALITY_DELETES:
        case POSITION_DELETES:
          deleteFiles.add((DeleteFile) file.copyWithoutStats());
          break;
        default:
          throw new IllegalArgumentException(file.content().name() + "cannot be expired");
      }
    }
  }

  private static class DataFileFreshness {
    long latestExpiredSeq;
    long latestUpdateMillis;
    long expiredDataFileCount;
    long totalDataFileCount;

    DataFileFreshness(long sequenceNumber, long latestUpdateMillis) {
      this.latestExpiredSeq = sequenceNumber;
      this.latestUpdateMillis = latestUpdateMillis;
    }

    DataFileFreshness updateLatestMillis(long ts) {
      this.latestUpdateMillis = ts;
      return this;
    }

    DataFileFreshness updateExpiredSeq(Long seq) {
      this.latestExpiredSeq = seq;
      return this;
    }

    DataFileFreshness incTotalCount() {
      totalDataFileCount++;
      return this;
    }

    DataFileFreshness incExpiredCount() {
      expiredDataFileCount++;
      return this;
    }
  }

  private static boolean mayExpired(
      ArcticTable table,
      IcebergFileEntry fileEntry,
      DataExpirationConfig expirationConfig,
      Map<StructLike, DataFileFreshness> partitionFreshness,
      Long expireTimestamp) {
    ContentFile<?> contentFile = fileEntry.getFile();
    StructLike partition = contentFile.partition();

    boolean expired = true;
    Types.NestedField field = table.schema().findField(expirationConfig.getExpirationField());
    if (contentFile.content().equals(FileContent.DATA)) {
      Literal<Long> literal =
          getExpireTimestampLiteral(
              contentFile,
              field,
              DateTimeFormatter.ofPattern(
                  expirationConfig.getDateTimePattern(), Locale.getDefault()),
              expirationConfig.getNumberDateFormat());
      if (partitionFreshness.containsKey(partition)) {
        DataFileFreshness freshness = partitionFreshness.get(partition).incTotalCount();
        if (freshness.latestUpdateMillis <= literal.value()) {
          partitionFreshness.put(partition, freshness.updateLatestMillis(literal.value()));
        }
      } else {
        partitionFreshness.putIfAbsent(
            partition,
            new DataFileFreshness(fileEntry.getSequenceNumber(), literal.value()).incTotalCount());
      }
      expired = literal.comparator().compare(expireTimestamp, literal.value()) >= 0;
      if (expired) {
        partitionFreshness.computeIfPresent(
            partition,
            (k, v) -> v.updateExpiredSeq(fileEntry.getSequenceNumber()).incExpiredCount());
      }
    }

    return expired;
  }

  private static boolean willNotRetain(
      IcebergFileEntry fileEntry,
      DataExpirationConfig expirationConfig,
      Map<StructLike, DataFileFreshness> partitionFreshness) {
    ContentFile<?> contentFile = fileEntry.getFile();

    switch (expirationConfig.getExpirationLevel()) {
      case PARTITION:
        // if only partial expired files in a partition, all the files in that partition should be
        // preserved
        return partitionFreshness.containsKey(contentFile.partition())
            && partitionFreshness.get(contentFile.partition()).expiredDataFileCount
                == partitionFreshness.get(contentFile.partition()).totalDataFileCount;
      case FILE:
        if (!contentFile.content().equals(FileContent.DATA)) {
          long seqUpperBound =
              partitionFreshness.getOrDefault(
                      contentFile.partition(),
                      new DataFileFreshness(Long.MIN_VALUE, Long.MAX_VALUE))
                  .latestExpiredSeq;
          // only expire delete files with sequence-number less or equal to expired data file
          // there may be some dangling delete files, they will be cleaned by
          // OrphanFileCleaningExecutor
          return fileEntry.getSequenceNumber() <= seqUpperBound;
        } else {
          return true;
        }
      default:
        return false;
    }
  }

  private static Literal<Long> getExpireTimestampLiteral(
      ContentFile<?> contentFile,
      Types.NestedField field,
      DateTimeFormatter formatter,
      String numberDateFormatter) {
    Type type = field.type();
    Object upperBound =
        Conversions.fromByteBuffer(type, contentFile.upperBounds().get(field.fieldId()));
    Literal<Long> literal = Literal.of(Long.MAX_VALUE);
    if (null == upperBound) {
      return literal;
    } else if (upperBound instanceof Long) {
      switch (type.typeId()) {
        case TIMESTAMP:
          // nanosecond -> millisecond
          literal = Literal.of((Long) upperBound / 1000);
          break;
        default:
          if (numberDateFormatter.equals(EXPIRE_TIMESTAMP_MS)) {
            literal = Literal.of((Long) upperBound);
          } else if (numberDateFormatter.equals(EXPIRE_TIMESTAMP_S)) {
            // second -> millisecond
            literal = Literal.of((Long) upperBound * 1000);
          }
      }
    } else if (type.typeId().equals(Type.TypeID.STRING)) {
      literal =
          Literal.of(
              LocalDate.parse(upperBound.toString(), formatter)
                  .atStartOfDay()
                  .atZone(getDefaultZoneId(field))
                  .toInstant()
                  .toEpochMilli());
    }
    return literal;
  }

  protected static class FileEntry extends IcebergFileEntry {

    private final boolean isChange;

    FileEntry(IcebergFileEntry fileEntry, boolean isChange) {
      super(
          fileEntry.getSnapshotId(),
          fileEntry.getSequenceNumber(),
          fileEntry.getStatus(),
          fileEntry.getFile());
      this.isChange = isChange;
    }
  }
}
