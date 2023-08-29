package com.netease.arctic.server.table.executor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.server.table.ExpiringDataConfig;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.trace.SnapshotSummary;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.commons.lang3.StringUtils;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataExpiringExecutor extends BaseTableExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DataExpiringExecutor.class);

  private final long interval;
  private static final Set<Type.TypeID> FIELD_TYPES = Sets.newHashSet(
      Type.TypeID.TIMESTAMP,
      Type.TypeID.STRING,
      Type.TypeID.LONG,
      Type.TypeID.INTEGER
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

  protected DataExpiringExecutor(TableManager tableManager, int poolSize, long interval) {
    super(tableManager, poolSize);
    this.interval = interval;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return interval * 1000;
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
      purgeTable(arcticTable, expirationConfig);
    } catch (Throwable t) {
      LOG.error("Unexpected purge error for table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  private void purgeTable(ArcticTable table, DataExpirationConfig expirationConfig) {
    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      purgeTableData(keyedTable.changeTable(), expirationConfig);
      purgeTableData(keyedTable.baseTable(), expirationConfig);
    } else {
      purgeTableData(table.asUnkeyedTable(), expirationConfig);
    }
  }

  private void purgeTableData(ArcticTable table, DataExpirationConfig expirationConfig) {
    long startTimestamp = System.currentTimeMillis();
    long expireTimestamp = Math.max(startTimestamp - expirationConfig.retentionTime, 0L);
    Expression partitionFilter = getPartitionExpression(expirationConfig, expireTimestamp);

    UnkeyedTable unkeyedTable = table.asUnkeyedTable();
    Snapshot snapshot = unkeyedTable.currentSnapshot();
    if (null == snapshot) {
      return;
    }
    TableEntriesScan scan = TableEntriesScan.builder(unkeyedTable)
        .useSnapshot(snapshot.snapshotId())
        .includeFileContent(FileContent.DATA, FileContent.EQUALITY_DELETES, FileContent.POSITION_DELETES)
        .withDataFilter(partitionFilter)
        .includeColumnStats()
        .build();

    ExpireFiles expiredFiles = new ExpireFiles();
    Map<StructLike, DataFileFreshness> partitionFreshness = Maps.newHashMap();
    try (CloseableIterable<IcebergFileEntry> entries = scan.entries()) {
      CloseableIterable.filter(
          CloseableIterable.filter(entries, e -> mayExpired(e, expirationConfig, partitionFreshness, expireTimestamp)),
              e -> willNotRetain(e, expirationConfig, partitionFreshness))
          .forEach(expiredFiles::addFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    expireFiles(unkeyedTable, snapshot.snapshotId(), expiredFiles, expireTimestamp);
  }

  private Expression getPartitionExpression(DataExpirationConfig expirationConfig, long expireTimestamp) {
    Type.TypeID typeID = expirationConfig.expirationField.type().typeId();
    switch (typeID) {
      case TIMESTAMP:
        return Expressions.lessThan(expirationConfig.expirationField.name(), expireTimestamp * 1000);
      case STRING:
        return Expressions.lessThan(
            expirationConfig.expirationField.name(),
            expirationConfig.dateFormatter.print(expireTimestamp));
      case LONG:
      case INTEGER:
        return getNumberDateTimeExpression(expirationConfig.expirationField.name(),
            expireTimestamp,
            expirationConfig.numberDateFormat);
      default:
        throw new IllegalArgumentException(String.format("Field type(%s) doest not support data expiration", typeID));
    }
  }

  private Expression getNumberDateTimeExpression(String fieldName, long expireTimestamp, String numberDateFormat) {
    if (numberDateFormat.equals(EXPIRE_TIMESTAMP_S)) {
      return Expressions.lessThan(fieldName, expireTimestamp / 1000);
    } else {
      return Expressions.lessThan(fieldName, expireTimestamp);
    }
  }


  private void expireFiles(UnkeyedTable table, long snapshotId, ExpireFiles expiredFiles, long expireTimestamp) {
    List<DataFile> dataFiles = expiredFiles.dataFiles;
    List<DeleteFile> deleteFiles = expiredFiles.deleteFiles;
    if (dataFiles.isEmpty() && deleteFiles.isEmpty()) {
      return;
    }
    // expire data files
    DeleteFiles delete = table.newDelete();
    dataFiles.forEach(delete::deleteFile);
    delete.set(SnapshotSummary.SNAPSHOT_PRODUCER, this.getThreadName());
    delete.commit();
    // expire delete files
    if (!deleteFiles.isEmpty()) {
      RewriteFiles rewriteFiles = table.newRewrite().validateFromSnapshot(snapshotId);
      deleteFiles.forEach(rewriteFiles::deleteFile);
      rewriteFiles.set(SnapshotSummary.SNAPSHOT_PRODUCER, this.getThreadName());
      rewriteFiles.commit();
    }
    LOG.info("Expired files older than {}, {} data files[{}] and {} delete files[{}]",
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
    long latestUpdateMill;
    long expiredDataFileCount;
    long totalDataFileCount;

    DataFileFreshness(long sequenceNumber, long latestUpdateMill) {
      this.latestExpiredSeq = sequenceNumber;
      this.latestUpdateMill = latestUpdateMill;
    }

    DataFileFreshness updateLatestMill(long ts) {
      this.latestUpdateMill = ts;
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

  private boolean mayExpired(
      IcebergFileEntry fileEntry,
      DataExpirationConfig expirationConfig,
      Map<StructLike, DataFileFreshness> partitionFreshness,
      Long expireTimestamp) {
    ContentFile<?> contentFile = fileEntry.getFile();
    StructLike partition = contentFile.partition();

    boolean expired = true;
    if (contentFile.content().equals(FileContent.DATA)) {
      Literal<Long> literal = getExpireTimestampLiteral(
          contentFile, expirationConfig.expirationField.type(),
          expirationConfig.expirationField, expirationConfig.dateFormatter, expirationConfig.numberDateFormat);
      if (partitionFreshness.containsKey(partition)) {
        DataFileFreshness freshness = partitionFreshness.get(partition);
        if (freshness.latestUpdateMill <= literal.value()) {
          partitionFreshness.put(partition, freshness.updateLatestMill(literal.value()).incTotalCount());
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

  private boolean willNotRetain(
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

  private Literal<Long> getExpireTimestampLiteral(
      ContentFile<?> contentFile,
      Type type,
      Types.NestedField field,
      DateTimeFormatter formatter,
      String numberDateFormatter) {
    Object upperBound = Conversions.fromByteBuffer(type, contentFile.upperBounds().get(field.fieldId()));
    Literal<Long> literal = Literal.of(Long.MAX_VALUE);
    if (null == upperBound) {
      return literal;
    } else if (upperBound instanceof Long) {
      switch (type.typeId()) {
        case TIMESTAMP:
          literal = Literal.of((Long) upperBound / 1000);
          break;
        default:
          literal = Literal.of((Long) upperBound);
      }
    } else if (upperBound instanceof String && type.typeId().equals(Type.TypeID.STRING)) {
      literal = Literal.of(formatter.parseMillis(upperBound.toString()));
    } else if (upperBound instanceof Integer &&
        type.typeId().equals(Type.TypeID.INTEGER) && numberDateFormatter.equals(EXPIRE_TIMESTAMP_S)) {
      literal = Literal.of((Integer) upperBound * 1000L);
    }
    return literal;
  }

  private static class DataExpirationConfig {
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
      retentionTime = config.getRetentionTime() * 1000;
      dateFormatter = DateTimeFormat.forPattern(config.getDateStringFormat());
      numberDateFormat = config.getDateNumberFormat();
      Preconditions.checkArgument(typeID.equals(Type.TypeID.TIMESTAMP) ||
          typeID.equals(Type.TypeID.STRING) ||
          (typeID.equals(Type.TypeID.LONG) && numberDateFormat.equals(EXPIRE_TIMESTAMP_MS)) ||
          (typeID.equals(Type.TypeID.INTEGER) && numberDateFormat.equals(EXPIRE_TIMESTAMP_S)),
          String.format("The type(%s) of filed(%s) cannot convert to %s which in the table(%s)", typeID.name(), field,
              numberDateFormat, table.name()));
    }
  }
}
