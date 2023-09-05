package com.netease.arctic.server.table.executor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.table.ExpiringDataConfig;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.ConfigurationUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.trace.SnapshotSummary;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Iterables;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataExpiringExecutor extends BaseTableExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DataExpiringExecutor.class);

  private final Duration interval;
  private static final Set<Type.TypeID> FIELD_TYPES = Sets.newHashSet(
      Type.TypeID.TIMESTAMP,
      Type.TypeID.STRING,
      Type.TypeID.LONG
  );

  public enum ExpireLevel {
    PARTITION,
    FILE;

    public static ExpireLevel fromString(String level) {
      Preconditions.checkArgument(null != level, "Invalid level type: null");
      try {
        return ExpireLevel.valueOf(level.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid level type: %s", level), e);
      }
    }
  }

  public static final String  EXPIRE_TIMESTAMP_MS = "TIMESTAMP_MS";
  public static final String  EXPIRE_TIMESTAMP_S = "TIMESTAMP_S";

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

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      ArcticTable arcticTable = loadTable(tableRuntime);
      if (!CompatiblePropertyUtil.propertyAsBoolean(
          arcticTable.properties(),
          TableProperties.ENABLE_DATA_EXPIRATION,
          TableProperties.ENABLE_DATA_EXPIRATION_DEFAULT)) {
        return;
      }

      DataExpirationConfig expirationConfig =
          new DataExpirationConfig(arcticTable, tableRuntime.getTableConfiguration().getExpiringDataConfig());
      if (expirationConfig.retentionTime == 0) {
        return;
      }
      purgeTableFrom(arcticTable, expirationConfig,
          Instant.now().atZone(getDefaultZoneId(expirationConfig.expirationField)).toInstant());
    } catch (Throwable t) {
      LOG.error("Unexpected purge error for table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  /**
   * Purge data older than the specified UTC timestamp
   * @param table Arctic table
   * @param expirationConfig expiration configs
   * @param instant timestamp/timestampz/long field type uses UTC, others will use the local time zone
   */
  protected static void purgeTableFrom(ArcticTable table, DataExpirationConfig expirationConfig, Instant instant) {
    long expireTimestamp = Math.max(instant.toEpochMilli() - expirationConfig.retentionTime, 0L);
    LOG.info("Expiring Data older than {} in table {} ", Instant.ofEpochMilli(expireTimestamp)
            .atZone(getDefaultZoneId(expirationConfig.expirationField))
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

  private static TableEntriesScan fileEntriesScan(UnkeyedTable table, Expression partitionFilter, Snapshot snapshot) {
    TableEntriesScan.Builder builder = TableEntriesScan.builder(table)
        .includeFileContent(FileContent.DATA, FileContent.EQUALITY_DELETES, FileContent.POSITION_DELETES)
        .withDataFilter(partitionFilter)
        .includeColumnStats();

    if (null == snapshot || snapshot.snapshotId() == ArcticServiceConstants.INVALID_SNAPSHOT_ID) {
      return builder.build();
    } else {
      return builder.useSnapshot(snapshot.snapshotId()).build();
    }
  }

  private static void purgeTableData(ArcticTable table, DataExpirationConfig expirationConfig, long expireTimestamp) {
    Expression partitionFilter = getPartitionExpression(expirationConfig, expireTimestamp);
    Map<StructLike, DataFileFreshness> partitionFreshness = Maps.newHashMap();

    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      ChangeTable changeTable = keyedTable.changeTable();
      BaseTable baseTable = keyedTable.baseTable();
      Snapshot changeSnapshot = changeTable.currentSnapshot();
      Snapshot baseSnapshot = baseTable.currentSnapshot();

      TableEntriesScan changeScan = fileEntriesScan(changeTable, partitionFilter, changeSnapshot);
      TableEntriesScan baseScan = fileEntriesScan(baseTable, partitionFilter, baseSnapshot);
      ExpireFiles changeExpiredFiles = new ExpireFiles();
      ExpireFiles baseExpiredFiles = new ExpireFiles();
      CloseableIterable<FileEntry> changed = CloseableIterable.transform(changeScan.entries(),
          e -> new FileEntry(e, true));
      CloseableIterable<FileEntry> based = CloseableIterable.transform(baseScan.entries(),
          e -> new FileEntry(e, false));

      try (CloseableIterable<FileEntry> entries = CloseableIterable.withNoopClose(Iterables.concat(changed, based))) {
        CloseableIterable<FileEntry> mayExpiredFiles = CloseableIterable.withNoopClose(
            Lists.newArrayList(CloseableIterable.filter(entries,
                e -> mayExpired(e, expirationConfig, partitionFreshness, expireTimestamp, TableTypeUtil.isHive(table)))));
        CloseableIterable.filter(mayExpiredFiles, e -> willNotRetain(e, expirationConfig, partitionFreshness))
            .forEach(e -> {
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
      try (CloseableIterable<IcebergFileEntry> entries
          = fileEntriesScan(unkeyedTable, partitionFilter, snapshot).entries()) {
        CloseableIterable<IcebergFileEntry> mayExpiredFiles = CloseableIterable.withNoopClose(
            Lists.newArrayList(CloseableIterable.filter(entries,
                e -> mayExpired(e, expirationConfig, partitionFreshness, expireTimestamp, TableTypeUtil.isHive(table)))));
        CloseableIterable.filter(
                mayExpiredFiles,
                e -> willNotRetain(e, expirationConfig, partitionFreshness))
            .forEach(expiredFiles::addFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      expireFiles(unkeyedTable, snapshot.snapshotId(), expiredFiles, expireTimestamp);
    }
  }

  private static Expression getPartitionExpression(DataExpirationConfig expirationConfig, long expireTimestamp) {
    if (expirationConfig.expirationLevel.equals(ExpireLevel.PARTITION)) {
      return Expressions.alwaysTrue();
    }

    Type.TypeID typeID = expirationConfig.expirationField.type().typeId();
    if (typeID == Type.TypeID.TIMESTAMP) {
      return Expressions.lessThan(expirationConfig.expirationField.name(), expireTimestamp * 1000);
    }
    return Expressions.alwaysTrue();
  }

  private static void expireFiles(UnkeyedTable table, long snapshotId, ExpireFiles expiredFiles, long expireTimestamp) {
    List<DataFile> dataFiles = expiredFiles.dataFiles;
    List<DeleteFile> deleteFiles = expiredFiles.deleteFiles;
    if (dataFiles.isEmpty() && deleteFiles.isEmpty()) {
      return;
    }
    // expire data files
    DeleteFiles delete = table.newDelete();
    dataFiles.forEach(delete::deleteFile);
    delete.set(SnapshotSummary.SNAPSHOT_PRODUCER, "Amoro data expiration");
    delete.commit();
    // expire delete files
    if (!deleteFiles.isEmpty()) {
      RewriteFiles rewriteFiles = table.newRewrite().validateFromSnapshot(snapshotId);
      deleteFiles.forEach(rewriteFiles::deleteFile);
      rewriteFiles.set(SnapshotSummary.SNAPSHOT_PRODUCER, "Amoro data expiration");
      rewriteFiles.commit();
    }

    // TODO: persistent table expiration record. Contains some meta information such as table_id, snapshotId,
    //  file_infos(file_content, path, recordCount, fileSizeInBytes, equalityFieldIds, partitionPath,
    //  sequenceNumber) and expireTimestamp...

    LOG.info("Expired {} files older than {}, {} data files[{}] and {} delete files[{}]",
        table.name(),
        expireTimestamp,
        dataFiles.size(), dataFiles.stream().map(ContentFile::path).collect(Collectors.joining(",")),
        deleteFiles.size(), deleteFiles.stream().map(ContentFile::path).collect(Collectors.joining(",")));
  }

  static class ExpireFiles {
    List<DataFile>  dataFiles;
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

  static class DataFileFreshness {
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
      IcebergFileEntry fileEntry,
      DataExpirationConfig expirationConfig,
      Map<StructLike, DataFileFreshness> partitionFreshness,
      Long expireTimestamp,
      boolean isHiveTable) {
    ContentFile<?> contentFile = fileEntry.getFile();
    StructLike partition = contentFile.partition();

    boolean expired = true;
    if (contentFile.content().equals(FileContent.DATA)) {
      Literal<Long> literal = getExpireTimestampLiteral(
          contentFile, expirationConfig.expirationField.type(),
          expirationConfig.expirationField, expirationConfig.dateFormatter,
          expirationConfig.numberDateFormat, isHiveTable);
      if (partitionFreshness.containsKey(partition)) {
        DataFileFreshness freshness = partitionFreshness.get(partition).incTotalCount();
        if (freshness.latestUpdateMillis <= literal.value()) {
          partitionFreshness.put(partition, freshness.updateLatestMillis(literal.value()));
        }
      } else {
        partitionFreshness.putIfAbsent(partition,
            new DataFileFreshness(fileEntry.getSequenceNumber(), literal.value()).incTotalCount());
      }
      expired = literal.comparator().compare(expireTimestamp, literal.value()) >= 0;
      if (expired) {
        partitionFreshness.computeIfPresent(partition,
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

    switch (expirationConfig.expirationLevel) {
      case PARTITION:
        // if only partial expired files in a partition, all the files in that partition should be preserved
        return partitionFreshness.containsKey(contentFile.partition()) &&
            partitionFreshness.get(contentFile.partition()).expiredDataFileCount ==
            partitionFreshness.get(contentFile.partition()).totalDataFileCount;
      case FILE:
        if (!contentFile.content().equals(FileContent.DATA)) {
          long seqUpperBound = partitionFreshness.getOrDefault(contentFile.partition(),
              new DataFileFreshness(Long.MIN_VALUE, Long.MAX_VALUE)).latestExpiredSeq;
          // only expire delete files with sequence-number less or equal to expired data file
          // there may be some dangling delete files, they will be cleaned by OrphanFileCleaningExecutor
          return fileEntry.getSequenceNumber() <= seqUpperBound;
        } else {
          return true;
        }
      default: return false;
    }
  }

  private static Literal<Long> getExpireTimestampLiteral(
      ContentFile<?> contentFile,
      Type type,
      Types.NestedField field,
      DateTimeFormatter formatter,
      String numberDateFormatter,
      boolean isHiveTable) {
    Object upperBound = Conversions.fromByteBuffer(type, contentFile.upperBounds().get(field.fieldId()));
    Literal<Long> literal = Literal.of(Long.MAX_VALUE);
    if (null == upperBound) {
      return literal;
    } else if (upperBound instanceof Long) {
      switch (type.typeId()) {
        case TIMESTAMP:
          // nanosecond -> millisecond
          long millis = (Long) upperBound / 1000;
          literal = Literal.of(millis);
          if (isHiveTable && !((Types.TimestampType) type).shouldAdjustToUTC()) {
            literal =
                Literal.of(Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .toInstant(ZoneOffset.UTC).toEpochMilli());
          }
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
      literal = Literal.of(
          LocalDate.parse(upperBound.toString(), formatter)
              .atStartOfDay()
              .atZone(getDefaultZoneId(field)).toInstant().toEpochMilli());
    }
    return literal;
  }

  protected static class DataExpirationConfig {
    Types.NestedField expirationField;
    ExpireLevel expirationLevel;
    long retentionTime;
    DateTimeFormatter dateFormatter;
    String numberDateFormat;

    DataExpirationConfig(ArcticTable table, ExpiringDataConfig config) {
      String field = config.getField();
      expirationField = table.schema().findField(field);
      Preconditions.checkArgument(StringUtils.isNoneBlank(field) && null != expirationField,
          String.format("Field(%s) used to determine data expiration is illegal for table(%s)", field, table.name()));
      Type.TypeID typeID = expirationField.type().typeId();
      Preconditions.checkArgument(FIELD_TYPES.contains(typeID),
          String.format("The type(%s) of filed(%s) is incompatible for table(%s)", typeID.name(), field, table.name()));

      expirationLevel = ExpireLevel.fromString(config.getLevel());
      if (StringUtils.isNotBlank(config.getRetentionTime())) {
        retentionTime = ConfigurationUtil.TimeUtils.parseDuration(config.getRetentionTime()).toMillis();
      }
      dateFormatter = DateTimeFormatter.ofPattern(config.getDateStringFormat(), Locale.getDefault());
      numberDateFormat = config.getDateNumberFormat();
    }
  }

  protected static class FileEntry extends IcebergFileEntry {

    private final boolean isChange;

    FileEntry(IcebergFileEntry fileEntry, boolean isChange) {
      super(fileEntry.getSnapshotId(), fileEntry.getSequenceNumber(), fileEntry.getStatus(), fileEntry.getFile());
      this.isChange = isChange;
    }
  }
}
