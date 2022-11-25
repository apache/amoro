package com.netease.arctic;

import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.utils.ManifestEntryFields;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.DataTask;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileMetadata;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.InclusiveMetricsEvaluator;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class ManifestReader {
  private final Table table;
  private final Long snapshotId;
  private final Expression dataFilter;
  private final boolean aliveEntry;
  private final boolean allFileContent;
  private final Set<FileContent> validFileContent;

  private Schema entriesTableSchema;
  private InclusiveMetricsEvaluator lazyMetricsEvaluator = null;
  private Map<String, Integer> lazyIndexOfDataFileType;
  private Map<String, Integer> lazyIndexOfEntryType;

  public static Builder builder(Table table) {
    return new Builder(table);
  }

  public static class Builder {
    private final Table table;
    private Long snapshotId;
    private Expression dataFilter;
    private boolean aliveEntry = true;
    private final Set<FileContent> fileContents = Sets.newHashSet();

    public Builder(Table table) {
      this.table = table;
    }

    public Builder withDataFilter(Expression dataFilter) {
      this.dataFilter = dataFilter;
      return this;
    }

    public Builder withAliveEntry(boolean aliveEntry) {
      this.aliveEntry = aliveEntry;
      return this;
    }

    public Builder includeFileContent(FileContent fileContent) {
      this.fileContents.add(fileContent);
      return this;
    }
    
    public Builder useSnapshot(long snapshotId) {
      this.snapshotId = snapshotId;
      return this;
    }

    public ManifestReader build() {
      return new ManifestReader(table, snapshotId, dataFilter, aliveEntry, fileContents.size() == 3, fileContents);
    }
  }


  public ManifestReader(Table table, Long snapshotId, Expression dataFilter, boolean aliveEntry, boolean allFileContent,
                        Set<FileContent> validFileContent) {
    this.table = table;
    this.dataFilter = dataFilter;
    this.aliveEntry = aliveEntry;
    this.allFileContent = allFileContent;
    this.validFileContent = validFileContent;
    this.snapshotId = snapshotId;
  }

  public CloseableIterable<ManifestEntry> entries() {
    Configuration hadoopConf = new Configuration();
    if (table.io() instanceof ArcticHadoopFileIO) {
      ArcticHadoopFileIO io = (ArcticHadoopFileIO) table.io();
      hadoopConf = io.conf();
    }
    HadoopTables tables = new HadoopTables(hadoopConf);
    Table entriesTable = tables.load(table.location() + "#" + MetadataTableType.ENTRIES);
    this.entriesTableSchema = entriesTable.schema();
    TableScan tableScan = entriesTable.newScan();
    if (snapshotId != null) {
      tableScan = tableScan.useSnapshot(snapshotId);
    }
    CloseableIterable<FileScanTask> manifestFileScanTasks = tableScan.planFiles();

    CloseableIterable<StructLike> entries = CloseableIterable.concat(entriesOfManifest(manifestFileScanTasks));

    CloseableIterable<ManifestEntry> allEntries =
        CloseableIterable.transform(entries, (entry -> {
          ManifestEntry.Status status =
              ManifestEntry.Status.of(entry.get(entryFieldIndex(ManifestEntryFields.STATUS.name()), Integer.class));
          long sequence = entry.get(entryFieldIndex(ManifestEntryFields.SEQUENCE_NUMBER.name()), Long.class);
          Long snapshotId = entry.get(entryFieldIndex(ManifestEntryFields.SNAPSHOT_ID.name()), Long.class);
          StructLike fileRecord =
              entry.get(entryFieldIndex(ManifestEntryFields.DATA_FILE_FIELD_NAME), StructLike.class);
          FileContent fileContent =
              getFileContent(fileRecord.get(dataFileFieldIndex(DataFile.CONTENT.name()), Integer.class));
          if (shouldKeep(status, fileContent)) {
            ContentFile<?> contentFile = buildContentFile(fileContent, fileRecord);
            if (metricsEvaluator().eval(contentFile)) {
              return new ManifestEntry(fileContent.id(), status, snapshotId, sequence, contentFile);
            }
          }
          return null;
        }));
    return CloseableIterable.filter(allEntries, Objects::nonNull);
  }
  
  private FileContent getFileContent(int contentId) {
    for (FileContent content : FileContent.values()) {
      if (content.id() == contentId) {
        return content;
      }
    }
    throw new IllegalArgumentException("not support content id " + contentId);
  }

  private boolean shouldKeep(ManifestEntry.Status status, FileContent fileContent) {
    if (aliveEntry && status == ManifestEntry.Status.DELETED) {
      return false;
    }
    if (allFileContent) {
      return true;
    }
    return validFileContent != null && validFileContent.contains(fileContent);
  }

  private Iterable<CloseableIterable<StructLike>> entriesOfManifest(CloseableIterable<FileScanTask> fileScanTasks) {
    return Iterables.transform(fileScanTasks, task -> {
      assert task != null;
      return ((DataTask) task).rows();
    });
  }

  private ContentFile<?> buildContentFile(FileContent fileContent, StructLike fileRecord) {
    ContentFile<?> file;
    if (fileContent == FileContent.DATA) {
      file = buildDataFile(fileRecord);
    } else {
      file = buildDeleteFile(fileRecord, fileContent);
    }
    return file;
  }


  private DataFile buildDataFile(StructLike fileRecord) {
    String filePath = fileRecord.get(dataFileFieldIndex(DataFile.FILE_PATH.name()), String.class);
    Long fileSize = fileRecord.get(dataFileFieldIndex(DataFile.FILE_SIZE.name()), Long.class);
    Long recordCount = fileRecord.get(dataFileFieldIndex(DataFile.RECORD_COUNT.name()), Long.class);
    DataFiles.Builder builder = DataFiles.builder(table.spec())
        .withPath(filePath)
        .withFileSizeInBytes(fileSize)
        .withRecordCount(recordCount)
        .withMetrics(buildMetrics(fileRecord));
    if (table.spec().isPartitioned()) {
      StructLike partition = fileRecord.get(dataFileFieldIndex(DataFile.PARTITION_NAME), StructLike.class);
      builder.withPartition(partition);
    }
    return builder.build();
  }

  private DeleteFile buildDeleteFile(StructLike fileRecord, FileContent fileContent) {
    String filePath = fileRecord.get(dataFileFieldIndex(DataFile.FILE_PATH.name()), String.class);
    Long fileSize = fileRecord.get(dataFileFieldIndex(DataFile.FILE_SIZE.name()), Long.class);
    Long recordCount = fileRecord.get(dataFileFieldIndex(DataFile.RECORD_COUNT.name()), Long.class);
    FileMetadata.Builder builder = FileMetadata.deleteFileBuilder(table.spec())
        .withPath(filePath)
        .withFileSizeInBytes(fileSize)
        .withRecordCount(recordCount)
        .withMetrics(buildMetrics(fileRecord));
    if (table.spec().isPartitioned()) {
      StructLike partition = fileRecord.get(dataFileFieldIndex(DataFile.PARTITION_NAME), StructLike.class);
      builder.withPartition(partition);
    }
    if (fileContent == FileContent.EQUALITY_DELETES) {
      builder.ofEqualityDeletes();
    } else {
      builder.ofPositionDeletes();
    }
    return builder.build();
  }

  @SuppressWarnings("unchecked")
  private Metrics buildMetrics(StructLike dataFile) {
    return new Metrics(dataFile.get(dataFileFieldIndex(DataFile.RECORD_COUNT.name()), Long.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.COLUMN_SIZES.name()), Map.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.VALUE_COUNTS.name()), Map.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.NULL_VALUE_COUNTS.name()), Map.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.NAN_VALUE_COUNTS.name()), Map.class),
        (Map<Integer, ByteBuffer>) dataFile.get(dataFileFieldIndex(DataFile.LOWER_BOUNDS.name()), Map.class),
        (Map<Integer, ByteBuffer>) dataFile.get(dataFileFieldIndex(DataFile.UPPER_BOUNDS.name()), Map.class));
  }

  private int entryFieldIndex(String fieldName) {
    if (lazyIndexOfEntryType == null) {
      List<Types.NestedField> fields = entriesTableSchema.columns();
      Map<String, Integer> map = Maps.newHashMap();
      for (int i = 0; i < fields.size(); i++) {
        map.put(fields.get(i).name(), i);
      }
      lazyIndexOfEntryType = map;
    }
    return lazyIndexOfEntryType.get(fieldName);
  }

  private int dataFileFieldIndex(String fieldName) {
    if (lazyIndexOfDataFileType == null) {
      List<Types.NestedField> fields =
          entriesTableSchema.findType(ManifestEntryFields.DATA_FILE_FIELD_NAME).asStructType().fields();
      Map<String, Integer> map = Maps.newHashMap();
      for (int i = 0; i < fields.size(); i++) {
        map.put(fields.get(i).name(), i);
      }
      lazyIndexOfDataFileType = map;
    }
    return lazyIndexOfDataFileType.get(fieldName);
  }

  private InclusiveMetricsEvaluator metricsEvaluator() {
    if (lazyMetricsEvaluator == null) {
      if (dataFilter != null) {
        this.lazyMetricsEvaluator =
            new InclusiveMetricsEvaluator(table.spec().schema(), dataFilter, true);
      } else {
        this.lazyMetricsEvaluator =
            new InclusiveMetricsEvaluator(table.spec().schema(), Expressions.alwaysTrue(), true);
      }
    }
    return lazyMetricsEvaluator;
  }

}
