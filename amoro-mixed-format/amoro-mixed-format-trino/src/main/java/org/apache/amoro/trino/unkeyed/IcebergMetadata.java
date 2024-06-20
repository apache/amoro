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

package org.apache.amoro.trino.unkeyed;

import static io.trino.plugin.base.util.Procedures.checkProcedureArgument;
import static io.trino.plugin.hive.HiveApplyProjectionUtil.extractSupportedProjectedColumns;
import static io.trino.plugin.hive.HiveApplyProjectionUtil.replaceWithNewVariables;
import static io.trino.plugin.hive.util.HiveUtil.isStructuralType;
import static io.trino.plugin.iceberg.ExpressionConverter.toIcebergExpression;
import static io.trino.plugin.iceberg.IcebergAnalyzeProperties.getColumnNames;
import static io.trino.plugin.iceberg.IcebergColumnHandle.TRINO_MERGE_FILE_RECORD_COUNT;
import static io.trino.plugin.iceberg.IcebergColumnHandle.TRINO_MERGE_PARTITION_DATA;
import static io.trino.plugin.iceberg.IcebergColumnHandle.TRINO_MERGE_PARTITION_SPEC_ID;
import static io.trino.plugin.iceberg.IcebergColumnHandle.TRINO_MERGE_ROW_ID;
import static io.trino.plugin.iceberg.IcebergColumnHandle.TRINO_ROW_ID_NAME;
import static io.trino.plugin.iceberg.IcebergColumnHandle.fileModifiedTimeColumnHandle;
import static io.trino.plugin.iceberg.IcebergColumnHandle.fileModifiedTimeColumnMetadata;
import static io.trino.plugin.iceberg.IcebergColumnHandle.pathColumnHandle;
import static io.trino.plugin.iceberg.IcebergColumnHandle.pathColumnMetadata;
import static io.trino.plugin.iceberg.IcebergErrorCode.ICEBERG_COMMIT_ERROR;
import static io.trino.plugin.iceberg.IcebergErrorCode.ICEBERG_FILESYSTEM_ERROR;
import static io.trino.plugin.iceberg.IcebergErrorCode.ICEBERG_INVALID_METADATA;
import static io.trino.plugin.iceberg.IcebergMetadataColumn.FILE_MODIFIED_TIME;
import static io.trino.plugin.iceberg.IcebergMetadataColumn.FILE_PATH;
import static io.trino.plugin.iceberg.IcebergMetadataColumn.isMetadataColumnId;
import static io.trino.plugin.iceberg.IcebergSessionProperties.getExpireSnapshotMinRetention;
import static io.trino.plugin.iceberg.IcebergSessionProperties.getRemoveOrphanFilesMinRetention;
import static io.trino.plugin.iceberg.IcebergSessionProperties.isExtendedStatisticsEnabled;
import static io.trino.plugin.iceberg.IcebergSessionProperties.isProjectionPushdownEnabled;
import static io.trino.plugin.iceberg.IcebergSessionProperties.isStatisticsEnabled;
import static io.trino.plugin.iceberg.IcebergTableProperties.FILE_FORMAT_PROPERTY;
import static io.trino.plugin.iceberg.IcebergTableProperties.FORMAT_VERSION_PROPERTY;
import static io.trino.plugin.iceberg.IcebergTableProperties.PARTITIONING_PROPERTY;
import static io.trino.plugin.iceberg.IcebergTableProperties.getPartitioning;
import static io.trino.plugin.iceberg.IcebergUtil.commit;
import static io.trino.plugin.iceberg.IcebergUtil.deserializePartitionValue;
import static io.trino.plugin.iceberg.IcebergUtil.fileName;
import static io.trino.plugin.iceberg.IcebergUtil.getColumnHandle;
import static io.trino.plugin.iceberg.IcebergUtil.getColumns;
import static io.trino.plugin.iceberg.IcebergUtil.getFileFormat;
import static io.trino.plugin.iceberg.IcebergUtil.getPartitionKeys;
import static io.trino.plugin.iceberg.IcebergUtil.getTableComment;
import static io.trino.plugin.iceberg.IcebergUtil.newCreateTableTransaction;
import static io.trino.plugin.iceberg.IcebergUtil.schemaFromMetadata;
import static io.trino.plugin.iceberg.PartitionFields.parsePartitionFields;
import static io.trino.plugin.iceberg.PartitionFields.toPartitionFields;
import static io.trino.plugin.iceberg.TableStatisticsReader.TRINO_STATS_COLUMN_ID_PATTERN;
import static io.trino.plugin.iceberg.TableStatisticsReader.TRINO_STATS_PREFIX;
import static io.trino.plugin.iceberg.TableType.DATA;
import static io.trino.plugin.iceberg.TypeConverter.toIcebergType;
import static io.trino.plugin.iceberg.TypeConverter.toTrinoType;
import static io.trino.plugin.iceberg.catalog.hms.TrinoHiveCatalog.DEPENDS_ON_TABLES;
import static io.trino.plugin.iceberg.procedure.IcebergTableProcedureId.DROP_EXTENDED_STATS;
import static io.trino.plugin.iceberg.procedure.IcebergTableProcedureId.EXPIRE_SNAPSHOTS;
import static io.trino.plugin.iceberg.procedure.IcebergTableProcedureId.OPTIMIZE;
import static io.trino.plugin.iceberg.procedure.IcebergTableProcedureId.REMOVE_ORPHAN_FILES;
import static io.trino.spi.StandardErrorCode.INVALID_ANALYZE_PROPERTY;
import static io.trino.spi.StandardErrorCode.NOT_SUPPORTED;
import static io.trino.spi.connector.MaterializedViewFreshness.Freshness.FRESH;
import static io.trino.spi.connector.MaterializedViewFreshness.Freshness.STALE;
import static io.trino.spi.connector.MaterializedViewFreshness.Freshness.UNKNOWN;
import static io.trino.spi.connector.RetryMode.NO_RETRIES;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.apache.amoro.shade.guava32.com.google.common.base.Preconditions.checkArgument;
import static org.apache.amoro.shade.guava32.com.google.common.base.Preconditions.checkState;
import static org.apache.amoro.shade.guava32.com.google.common.base.Verify.verify;
import static org.apache.amoro.shade.guava32.com.google.common.base.Verify.verifyNotNull;
import static org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.amoro.shade.guava32.com.google.common.collect.Maps.transformValues;
import static org.apache.iceberg.FileContent.POSITION_DELETES;
import static org.apache.iceberg.ReachableFileUtil.metadataFileLocations;
import static org.apache.iceberg.ReachableFileUtil.versionHintLocation;
import static org.apache.iceberg.SnapshotSummary.DELETED_RECORDS_PROP;
import static org.apache.iceberg.SnapshotSummary.REMOVED_EQ_DELETES_PROP;
import static org.apache.iceberg.SnapshotSummary.REMOVED_POS_DELETES_PROP;
import static org.apache.iceberg.TableProperties.DELETE_ISOLATION_LEVEL;
import static org.apache.iceberg.TableProperties.DELETE_ISOLATION_LEVEL_DEFAULT;
import static org.apache.iceberg.TableProperties.FORMAT_VERSION;
import static org.apache.iceberg.TableProperties.WRITE_LOCATION_PROVIDER_IMPL;
import static org.apache.iceberg.types.TypeUtil.indexParents;

import io.airlift.json.JsonCodec;
import io.airlift.log.Logger;
import io.airlift.slice.Slice;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import io.trino.filesystem.FileEntry;
import io.trino.filesystem.FileIterator;
import io.trino.filesystem.TrinoFileSystem;
import io.trino.filesystem.TrinoFileSystemFactory;
import io.trino.plugin.base.classloader.ClassLoaderSafeSystemTable;
import io.trino.plugin.hive.HiveApplyProjectionUtil;
import io.trino.plugin.hive.HiveApplyProjectionUtil.ProjectedColumnRepresentation;
import io.trino.plugin.hive.HiveWrittenPartitions;
import io.trino.plugin.iceberg.ColumnIdentity;
import io.trino.plugin.iceberg.CommitTaskData;
import io.trino.plugin.iceberg.FilesTable;
import io.trino.plugin.iceberg.HistoryTable;
import io.trino.plugin.iceberg.IcebergColumnHandle;
import io.trino.plugin.iceberg.IcebergConfig;
import io.trino.plugin.iceberg.IcebergFileFormat;
import io.trino.plugin.iceberg.IcebergInputInfo;
import io.trino.plugin.iceberg.IcebergMergeTableHandle;
import io.trino.plugin.iceberg.IcebergPartitioningHandle;
import io.trino.plugin.iceberg.IcebergSessionProperties;
import io.trino.plugin.iceberg.IcebergTableHandle;
import io.trino.plugin.iceberg.IcebergTableName;
import io.trino.plugin.iceberg.IcebergUpdateHandle;
import io.trino.plugin.iceberg.IcebergUtil;
import io.trino.plugin.iceberg.IcebergWritableTableHandle;
import io.trino.plugin.iceberg.ManifestsTable;
import io.trino.plugin.iceberg.PartitionData;
import io.trino.plugin.iceberg.PartitionTable;
import io.trino.plugin.iceberg.PropertiesTable;
import io.trino.plugin.iceberg.SnapshotsTable;
import io.trino.plugin.iceberg.TableStatisticsReader;
import io.trino.plugin.iceberg.TableStatisticsWriter;
import io.trino.plugin.iceberg.UnknownTableTypeException;
import io.trino.plugin.iceberg.aggregation.DataSketchStateSerializer;
import io.trino.plugin.iceberg.aggregation.IcebergThetaSketchForStats;
import io.trino.plugin.iceberg.catalog.TrinoCatalog;
import io.trino.plugin.iceberg.procedure.IcebergDropExtendedStatsHandle;
import io.trino.plugin.iceberg.procedure.IcebergExpireSnapshotsHandle;
import io.trino.plugin.iceberg.procedure.IcebergOptimizeHandle;
import io.trino.plugin.iceberg.procedure.IcebergRemoveOrphanFilesHandle;
import io.trino.plugin.iceberg.procedure.IcebergTableExecuteHandle;
import io.trino.plugin.iceberg.procedure.IcebergTableProcedureId;
import io.trino.plugin.iceberg.util.DataFileWithDeleteFiles;
import io.trino.spi.TrinoException;
import io.trino.spi.block.Block;
import io.trino.spi.connector.Assignment;
import io.trino.spi.connector.BeginTableExecuteResult;
import io.trino.spi.connector.CatalogSchemaName;
import io.trino.spi.connector.CatalogSchemaTableName;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ColumnMetadata;
import io.trino.spi.connector.ConnectorAnalyzeMetadata;
import io.trino.spi.connector.ConnectorInsertTableHandle;
import io.trino.spi.connector.ConnectorMaterializedViewDefinition;
import io.trino.spi.connector.ConnectorMergeTableHandle;
import io.trino.spi.connector.ConnectorMetadata;
import io.trino.spi.connector.ConnectorOutputMetadata;
import io.trino.spi.connector.ConnectorOutputTableHandle;
import io.trino.spi.connector.ConnectorPartitioningHandle;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorTableExecuteHandle;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTableLayout;
import io.trino.spi.connector.ConnectorTableMetadata;
import io.trino.spi.connector.ConnectorTableProperties;
import io.trino.spi.connector.ConnectorViewDefinition;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.ConstraintApplicationResult;
import io.trino.spi.connector.DiscretePredicates;
import io.trino.spi.connector.MaterializedViewFreshness;
import io.trino.spi.connector.MaterializedViewNotFoundException;
import io.trino.spi.connector.ProjectionApplicationResult;
import io.trino.spi.connector.RetryMode;
import io.trino.spi.connector.SchemaNotFoundException;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.connector.SchemaTablePrefix;
import io.trino.spi.connector.SystemTable;
import io.trino.spi.connector.TableColumnsMetadata;
import io.trino.spi.connector.TableNotFoundException;
import io.trino.spi.expression.ConnectorExpression;
import io.trino.spi.expression.FunctionName;
import io.trino.spi.expression.Variable;
import io.trino.spi.predicate.Domain;
import io.trino.spi.predicate.NullableValue;
import io.trino.spi.predicate.TupleDomain;
import io.trino.spi.security.TrinoPrincipal;
import io.trino.spi.statistics.ColumnStatisticMetadata;
import io.trino.spi.statistics.ComputedStatistics;
import io.trino.spi.statistics.TableStatistics;
import io.trino.spi.statistics.TableStatisticsMetadata;
import io.trino.spi.type.TypeManager;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Splitter;
import org.apache.amoro.shade.guava32.com.google.common.base.Suppliers;
import org.apache.amoro.shade.guava32.com.google.common.base.VerifyException;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.datasketches.theta.CompactSketch;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileMetadata;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.IsolationLevel;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ManifestFiles;
import org.apache.iceberg.ManifestReader;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.PartitionSpecParser;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SchemaParser;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateStatistics;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.types.Types.IntegerType;
import org.apache.iceberg.types.Types.LongType;
import org.apache.iceberg.types.Types.NestedField;
import org.apache.iceberg.types.Types.StringType;
import org.apache.iceberg.types.Types.StructType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Iceberg original metadata has some problems for mixed-format table, such as iceberg version,
 * table type. So copy from IcebergMetadata and made some change
 */
public class IcebergMetadata implements ConnectorMetadata {
  private static final Logger log = Logger.get(io.trino.plugin.iceberg.IcebergMetadata.class);
  private static final Pattern PATH_PATTERN = Pattern.compile("(.*)/[^/]+");
  private static final int OPTIMIZE_MAX_SUPPORTED_TABLE_VERSION = 2;
  private static final int CLEANING_UP_PROCEDURES_MAX_SUPPORTED_TABLE_VERSION = 2;
  private static final String RETENTION_THRESHOLD = "retention_threshold";
  private static final String UNKNOWN_SNAPSHOT_TOKEN = "UNKNOWN";
  public static final Set<String> UPDATABLE_TABLE_PROPERTIES =
      ImmutableSet.of(FILE_FORMAT_PROPERTY, FORMAT_VERSION_PROPERTY, PARTITIONING_PROPERTY);

  public static final String ORC_BLOOM_FILTER_COLUMNS_KEY = "orc.bloom.filter.columns";
  public static final String ORC_BLOOM_FILTER_FPP_KEY = "orc.bloom.filter.fpp";

  public static final String NUMBER_OF_DISTINCT_VALUES_NAME = "NUMBER_OF_DISTINCT_VALUES";
  private static final FunctionName NUMBER_OF_DISTINCT_VALUES_FUNCTION =
      new FunctionName(IcebergThetaSketchForStats.NAME);

  private static final Integer DELETE_BATCH_SIZE = 1000;

  private final TypeManager typeManager;
  private final JsonCodec<CommitTaskData> commitTaskCodec;
  private final TrinoCatalog catalog;
  private final TrinoFileSystemFactory fileSystemFactory;
  private final TableStatisticsWriter tableStatisticsWriter;

  private final Map<IcebergTableHandle, TableStatistics> tableStatisticsCache =
      new ConcurrentHashMap<>();

  private final Map<SchemaTableName, Table> tableCache = new ConcurrentHashMap<>();

  private Transaction transaction;

  public IcebergMetadata(
      TypeManager typeManager,
      JsonCodec<CommitTaskData> commitTaskCodec,
      TrinoCatalog catalog,
      TrinoFileSystemFactory fileSystemFactory,
      TableStatisticsWriter tableStatisticsWriter) {
    this.typeManager = requireNonNull(typeManager, "typeManager is null");
    this.commitTaskCodec = requireNonNull(commitTaskCodec, "commitTaskCodec is null");
    this.catalog = requireNonNull(catalog, "catalog is null");
    this.fileSystemFactory = requireNonNull(fileSystemFactory, "fileSystemFactory is null");
    this.tableStatisticsWriter =
        requireNonNull(tableStatisticsWriter, "tableStatisticsWriter is null");
  }

  @Override
  public boolean schemaExists(ConnectorSession session, String schemaName) {
    return catalog.namespaceExists(session, schemaName);
  }

  @Override
  public List<String> listSchemaNames(ConnectorSession session) {
    return catalog.listNamespaces(session);
  }

  @Override
  public Map<String, Object> getSchemaProperties(
      ConnectorSession session, CatalogSchemaName schemaName) {
    return catalog.loadNamespaceMetadata(session, schemaName.getSchemaName());
  }

  @Override
  public Optional<TrinoPrincipal> getSchemaOwner(
      ConnectorSession session, CatalogSchemaName schemaName) {
    return catalog.getNamespacePrincipal(session, schemaName.getSchemaName());
  }

  @Override
  public IcebergTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName) {
    IcebergTableName name = IcebergTableName.from(tableName.getTableName());
    if (name.getTableType() != DATA) {
      // Pretend the table does not exist to produce better error message in case of table redirects
      // to Hive
      return null;
    }

    Table table;
    try {
      table =
          loadTable(session, new SchemaTableName(tableName.getSchemaName(), name.getTableName()));
    } catch (TableNotFoundException e) {
      return null;
    }
    Optional<Long> snapshotId = getCurrentSnapshotId(table);

    Map<String, String> tableProperties = table.properties();
    String nameMappingJson = tableProperties.get(TableProperties.DEFAULT_NAME_MAPPING);
    if (TableTypeUtil.isHive((MixedTable) table)) {
      return new AdaptHiveIcebergTableHandle(
          tableName.getSchemaName(),
          name.getTableName(),
          name.getTableType(),
          snapshotId,
          SchemaParser.toJson(table.schema()),
          Optional.of(table.spec()).map(PartitionSpecParser::toJson),
          ((HasTableOperations) table).operations().current().formatVersion(),
          TupleDomain.all(),
          TupleDomain.all(),
          ImmutableSet.of(),
          Optional.ofNullable(nameMappingJson),
          table.location(),
          table.properties(),
          NO_RETRIES,
          ImmutableList.of(),
          false,
          Optional.empty());
    }
    return new IcebergTableHandle(
        tableName.getSchemaName(),
        name.getTableName(),
        name.getTableType(),
        snapshotId,
        SchemaParser.toJson(table.schema()),
        Optional.of(table.spec()).map(PartitionSpecParser::toJson),
        ((HasTableOperations) table).operations().current().formatVersion(),
        TupleDomain.all(),
        TupleDomain.all(),
        ImmutableSet.of(),
        Optional.ofNullable(nameMappingJson),
        table.location(),
        table.properties(),
        NO_RETRIES,
        ImmutableList.of(),
        false,
        Optional.empty());
  }

  @Override
  public Optional<SystemTable> getSystemTable(ConnectorSession session, SchemaTableName tableName) {
    return getRawSystemTable(session, tableName)
        .map(
            systemTable ->
                new ClassLoaderSafeSystemTable(systemTable, getClass().getClassLoader()));
  }

  private Optional<SystemTable> getRawSystemTable(
      ConnectorSession session, SchemaTableName tableName) {
    IcebergTableName name = IcebergTableName.from(tableName.getTableName());
    if (name.getTableType() == DATA) {
      return Optional.empty();
    }

    // load the base table for the system table
    Table table;
    try {
      table =
          loadTable(session, new SchemaTableName(tableName.getSchemaName(), name.getTableName()));
    } catch (TableNotFoundException e) {
      return Optional.empty();
    } catch (UnknownTableTypeException e) {
      // avoid dealing with non Iceberg tables
      return Optional.empty();
    }

    SchemaTableName systemTableName =
        new SchemaTableName(tableName.getSchemaName(), name.getTableNameWithType());
    switch (name.getTableType()) {
      case DATA:
        // Handled above.
        break;
      case HISTORY:
        return Optional.of(new HistoryTable(systemTableName, table));
      case SNAPSHOTS:
        return Optional.of(new SnapshotsTable(systemTableName, typeManager, table));
      case PARTITIONS:
        return Optional.of(
            new PartitionTable(systemTableName, typeManager, table, getCurrentSnapshotId(table)));
      case MANIFESTS:
        return Optional.of(new ManifestsTable(systemTableName, table, getCurrentSnapshotId(table)));
      case FILES:
        return Optional.of(
            new FilesTable(systemTableName, typeManager, table, getCurrentSnapshotId(table)));
      case PROPERTIES:
        return Optional.of(new PropertiesTable(systemTableName, table));
    }
    return Optional.empty();
  }

  @Override
  public ConnectorTableProperties getTableProperties(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;

    if (table.getSnapshotId().isEmpty()) {
      // A table with missing snapshot id produces no splits, so we optimize here by returning
      // TupleDomain.none() as the predicate
      return new ConnectorTableProperties(
          TupleDomain.none(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          ImmutableList.of());
    }

    Table icebergTable = loadTable(session, table.getSchemaTableName());

    // Extract identity partition fields that are present in all partition specs, for creating the
    // discrete predicates.
    Set<Integer> partitionSourceIds = identityPartitionColumnsInAllSpecs(icebergTable);

    TupleDomain<IcebergColumnHandle> enforcedPredicate = table.getEnforcedPredicate();

    DiscretePredicates discretePredicates = null;
    if (!partitionSourceIds.isEmpty()) {
      // Extract identity partition columns
      Map<Integer, IcebergColumnHandle> columns =
          getColumns(icebergTable.schema(), typeManager).stream()
              .filter(column -> partitionSourceIds.contains(column.getId()))
              .collect(toImmutableMap(IcebergColumnHandle::getId, identity()));

      Supplier<List<FileScanTask>> lazyFiles =
          Suppliers.memoize(
              () -> {
                TableScan tableScan =
                    icebergTable
                        .newScan()
                        .useSnapshot(table.getSnapshotId().get())
                        .filter(toIcebergExpression(enforcedPredicate));

                try (CloseableIterable<FileScanTask> iterator = tableScan.planFiles()) {
                  return ImmutableList.copyOf(iterator);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });

      Iterable<FileScanTask> files = () -> lazyFiles.get().iterator();

      Iterable<TupleDomain<ColumnHandle>> discreteTupleDomain =
          Iterables.transform(
              files,
              fileScan -> {
                // Extract partition values in the data file
                Map<Integer, Optional<String>> partitionColumnValueStrings =
                    getPartitionKeys(fileScan);
                Map<ColumnHandle, NullableValue> partitionValues =
                    partitionSourceIds.stream()
                        .filter(partitionColumnValueStrings::containsKey)
                        .collect(
                            toImmutableMap(
                                columns::get,
                                columnId -> {
                                  IcebergColumnHandle column = columns.get(columnId);
                                  Object prestoValue =
                                      deserializePartitionValue(
                                          column.getType(),
                                          partitionColumnValueStrings.get(columnId).orElse(null),
                                          column.getName());

                                  return NullableValue.of(column.getType(), prestoValue);
                                }));

                return TupleDomain.fromFixedValues(partitionValues);
              });

      discretePredicates =
          new DiscretePredicates(
              columns.values().stream().map(ColumnHandle.class::cast).collect(toImmutableList()),
              discreteTupleDomain);
    }

    return new ConnectorTableProperties(
        // Using the predicate here directly avoids eagerly loading all partition values. Logically,
        // this
        // still keeps predicate and discretePredicates evaluation the same on every row of the
        // table. This
        // can be further optimized by intersecting with partition values at the cost of iterating
        // over all tableScan.planFiles() and caching partition values in table handle.
        enforcedPredicate.transformKeys(ColumnHandle.class::cast),
        // TODO: implement table partitioning
        Optional.empty(),
        Optional.empty(),
        Optional.ofNullable(discretePredicates),
        ImmutableList.of());
  }

  @Override
  public ConnectorTableMetadata getTableMetadata(
      ConnectorSession session, ConnectorTableHandle table) {
    IcebergTableHandle tableHandle = (IcebergTableHandle) table;
    Table icebergTable = loadTable(session, tableHandle.getSchemaTableName());
    List<ColumnMetadata> columns =
        getColumnMetadatas(SchemaParser.fromJson(tableHandle.getTableSchemaJson()));
    ImmutableMap.Builder<String, Object> properties = ImmutableMap.builder();
    properties.put(FILE_FORMAT_PROPERTY, getFileFormat(icebergTable));
    if (!icebergTable.spec().fields().isEmpty()) {
      properties.put(PARTITIONING_PROPERTY, toPartitionFields(icebergTable.spec()));
    }

    //        if (!icebergTable.location().isEmpty()) {
    //            properties.put(LOCATION_PROPERTY, icebergTable.location());
    //        }

    int formatVersion = ((HasTableOperations) icebergTable).operations().current().formatVersion();
    properties.put(FORMAT_VERSION_PROPERTY, formatVersion);

    return new ConnectorTableMetadata(
        tableHandle.getSchemaTableName(),
        columns,
        properties.buildOrThrow(),
        getTableComment(icebergTable));
  }

  @Override
  public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> schemaName) {
    return catalog.listTables(session, schemaName);
  }

  @Override
  public Map<String, ColumnHandle> getColumnHandles(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    ImmutableMap.Builder<String, ColumnHandle> columnHandles = ImmutableMap.builder();
    for (IcebergColumnHandle columnHandle :
        getColumns(SchemaParser.fromJson(table.getTableSchemaJson()), typeManager)) {
      columnHandles.put(columnHandle.getName(), columnHandle);
    }
    columnHandles.put(FILE_PATH.getColumnName(), pathColumnHandle());
    columnHandles.put(FILE_MODIFIED_TIME.getColumnName(), fileModifiedTimeColumnHandle());
    return columnHandles.buildOrThrow();
  }

  @Override
  public ColumnMetadata getColumnMetadata(
      ConnectorSession session, ConnectorTableHandle tableHandle, ColumnHandle columnHandle) {
    IcebergColumnHandle column = (IcebergColumnHandle) columnHandle;
    return ColumnMetadata.builder()
        .setName(column.getName())
        .setType(column.getType())
        .setComment(column.getComment())
        .build();
  }

  @Override
  public Map<SchemaTableName, List<ColumnMetadata>> listTableColumns(
      ConnectorSession session, SchemaTablePrefix prefix) {
    throw new UnsupportedOperationException(
        "The deprecated listTableColumns is not supported because streamTableColumns is implemented instead");
  }

  @Override
  public Iterator<TableColumnsMetadata> streamTableColumns(
      ConnectorSession session, SchemaTablePrefix prefix) {
    requireNonNull(prefix, "prefix is null");
    List<SchemaTableName> schemaTableNames;
    if (prefix.getTable().isEmpty()) {
      schemaTableNames = catalog.listTables(session, prefix.getSchema());
    } else {
      schemaTableNames = ImmutableList.of(prefix.toSchemaTableName());
    }
    return schemaTableNames.stream()
        .flatMap(
            tableName -> {
              try {
                if (redirectTable(session, tableName).isPresent()) {
                  return Stream.of(TableColumnsMetadata.forRedirectedTable(tableName));
                }

                Table icebergTable = loadTable(session, tableName);
                List<ColumnMetadata> columns = getColumnMetadatas(icebergTable.schema());
                return Stream.of(TableColumnsMetadata.forTable(tableName, columns));
              } catch (TableNotFoundException e) {
                // Table disappeared during listing operation
                return Stream.empty();
              } catch (UnknownTableTypeException e) {
                // Skip unsupported table type in case that the table redirects are not enabled
                return Stream.empty();
              } catch (RuntimeException e) {
                // Table can be being removed and this may cause all sorts of exceptions.
                // Log, because we're catching broadly.
                log.warn(
                    e,
                    "Failed to access metadata of table %s during streaming table columns for %s",
                    tableName,
                    prefix);
                return Stream.empty();
              }
            })
        .iterator();
  }

  @Override
  public void createSchema(
      ConnectorSession session,
      String schemaName,
      Map<String, Object> properties,
      TrinoPrincipal owner) {
    catalog.createNamespace(session, schemaName, properties, owner);
  }

  @Override
  public void dropSchema(ConnectorSession session, String schemaName) {
    catalog.dropNamespace(session, schemaName);
  }

  @Override
  public void renameSchema(ConnectorSession session, String source, String target) {
    catalog.renameNamespace(session, source, target);
  }

  @Override
  public void setSchemaAuthorization(
      ConnectorSession session, String schemaName, TrinoPrincipal principal) {
    catalog.setNamespacePrincipal(session, schemaName, principal);
  }

  @Override
  public void createTable(
      ConnectorSession session, ConnectorTableMetadata tableMetadata, boolean ignoreExisting) {
    Optional<ConnectorTableLayout> layout = getNewTableLayout(session, tableMetadata);
    finishCreateTable(
        session,
        beginCreateTable(session, tableMetadata, layout, RetryMode.NO_RETRIES),
        ImmutableList.of(),
        ImmutableList.of());
  }

  @Override
  public void setTableComment(
      ConnectorSession session, ConnectorTableHandle tableHandle, Optional<String> comment) {
    catalog.updateTableComment(
        session, ((IcebergTableHandle) tableHandle).getSchemaTableName(), comment);
  }

  @Override
  public Optional<ConnectorTableLayout> getNewTableLayout(
      ConnectorSession session, ConnectorTableMetadata tableMetadata) {
    Schema schema = schemaFromMetadata(tableMetadata.getColumns());
    PartitionSpec partitionSpec =
        parsePartitionFields(schema, getPartitioning(tableMetadata.getProperties()));
    return getWriteLayout(schema, partitionSpec, false);
  }

  @Override
  public ConnectorOutputTableHandle beginCreateTable(
      ConnectorSession session,
      ConnectorTableMetadata tableMetadata,
      Optional<ConnectorTableLayout> layout,
      RetryMode retryMode) {
    verify(transaction == null, "transaction already set");
    String schemaName = tableMetadata.getTable().getSchemaName();
    if (!schemaExists(session, schemaName)) {
      throw new SchemaNotFoundException(schemaName);
    }
    transaction = newCreateTableTransaction(catalog, tableMetadata, session);
    String location = transaction.table().location();
    TrinoFileSystem fileSystem = fileSystemFactory.create(session);
    try {
      if (fileSystem.listFiles(location).hasNext()) {
        throw new TrinoException(
            ICEBERG_FILESYSTEM_ERROR,
            format(
                "Cannot create a table on a non-empty location:"
                    + " %s, set 'iceberg.unique-table-location=true' in your Iceberg catalog properties "
                    + "to use unique table locations for every table.",
                location));
      }
      return newWritableTableHandle(tableMetadata.getTable(), transaction.table(), retryMode);
    } catch (IOException e) {
      throw new TrinoException(
          ICEBERG_FILESYSTEM_ERROR, "Failed checking new table's location: " + location, e);
    }
  }

  @Override
  public Optional<ConnectorOutputMetadata> finishCreateTable(
      ConnectorSession session,
      ConnectorOutputTableHandle tableHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics) {
    if (fragments.isEmpty()) {
      // Commit the transaction if the table is being created without data
      AppendFiles appendFiles = transaction.newFastAppend();
      commit(appendFiles, session);
      transaction.commitTransaction();
      transaction = null;
      return Optional.empty();
    }

    return finishInsert(
        session, (IcebergWritableTableHandle) tableHandle, fragments, computedStatistics);
  }

  @Override
  public ConnectorMergeTableHandle beginMerge(
      ConnectorSession session, ConnectorTableHandle tableHandle, RetryMode retryMode) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    verifyTableVersionForUpdate(table);

    Table icebergTable = loadTable(session, table.getSchemaTableName());
    validateNotModifyingOldSnapshot(table, icebergTable);
    validateNotPartitionedByNestedField(icebergTable.schema(), icebergTable.spec());

    beginTransaction(icebergTable);

    IcebergTableHandle newTableHandle = table.withRetryMode(retryMode);
    IcebergWritableTableHandle insertHandle =
        newWritableTableHandle(table.getSchemaTableName(), icebergTable, retryMode);

    return new IcebergMergeTableHandle(newTableHandle, insertHandle);
  }

  @Override
  public void finishMerge(
      ConnectorSession session,
      ConnectorMergeTableHandle tableHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics) {
    IcebergTableHandle handle = ((IcebergMergeTableHandle) tableHandle).getTableHandle();
    finishWrite(session, handle, fragments, true);
  }

  private static void verifyTableVersionForUpdate(IcebergTableHandle table) {
    if (table.getFormatVersion() < 2) {
      throw new TrinoException(
          NOT_SUPPORTED, "Iceberg table updates require at least format version 2");
    }
  }

  private void finishWrite(
      ConnectorSession session,
      IcebergTableHandle table,
      Collection<Slice> fragments,
      boolean runUpdateValidations) {
    Table icebergTable = transaction.table();

    List<CommitTaskData> commitTasks =
        fragments.stream()
            .map(slice -> commitTaskCodec.fromJson(slice.getBytes()))
            .collect(toImmutableList());

    if (commitTasks.isEmpty()) {
      // Avoid recording "empty" write operation
      transaction = null;
      return;
    }

    Schema schema = SchemaParser.fromJson(table.getTableSchemaJson());

    Map<String, List<CommitTaskData>> deletesByFilePath =
        commitTasks.stream()
            .filter(task -> task.getContent() == POSITION_DELETES)
            .collect(groupingBy(task -> task.getReferencedDataFile().orElseThrow()));
    Map<String, List<CommitTaskData>> fullyDeletedFiles =
        deletesByFilePath.entrySet().stream()
            .filter(entry -> fileIsFullyDeleted(entry.getValue()))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

    if (!deletesByFilePath.keySet().equals(fullyDeletedFiles.keySet())
        || commitTasks.stream().anyMatch(task -> task.getContent() == FileContent.DATA)) {
      RowDelta rowDelta = transaction.newRowDelta();
      table
          .getSnapshotId()
          .map(icebergTable::snapshot)
          .ifPresent(s -> rowDelta.validateFromSnapshot(s.snapshotId()));
      TupleDomain<IcebergColumnHandle> dataColumnPredicate =
          table
              .getEnforcedPredicate()
              .filter((column, domain) -> !isMetadataColumnId(column.getId()));
      if (!dataColumnPredicate.isAll()) {
        rowDelta.conflictDetectionFilter(toIcebergExpression(dataColumnPredicate));
      }
      IsolationLevel isolationLevel =
          IsolationLevel.fromName(
              icebergTable
                  .properties()
                  .getOrDefault(DELETE_ISOLATION_LEVEL, DELETE_ISOLATION_LEVEL_DEFAULT));
      if (isolationLevel == IsolationLevel.SERIALIZABLE) {
        rowDelta.validateNoConflictingDataFiles();
      }

      if (runUpdateValidations) {
        // Ensure a row that is updated by this commit was not deleted by a separate commit
        rowDelta.validateDeletedFiles();
        rowDelta.validateNoConflictingDeleteFiles();
      }

      ImmutableSet.Builder<String> writtenFiles = ImmutableSet.builder();
      ImmutableSet.Builder<String> referencedDataFiles = ImmutableSet.builder();
      for (CommitTaskData task : commitTasks) {
        PartitionSpec partitionSpec =
            PartitionSpecParser.fromJson(schema, task.getPartitionSpecJson());
        Type[] partitionColumnTypes =
            partitionSpec.fields().stream()
                .map(field -> field.transform().getResultType(schema.findType(field.sourceId())))
                .toArray(Type[]::new);
        switch (task.getContent()) {
          case POSITION_DELETES:
            if (fullyDeletedFiles.containsKey(task.getReferencedDataFile().orElseThrow())) {
              continue;
            }

            FileMetadata.Builder deleteBuilder =
                FileMetadata.deleteFileBuilder(partitionSpec)
                    .withPath(task.getPath())
                    .withFormat(task.getFileFormat().toIceberg())
                    .ofPositionDeletes()
                    .withFileSizeInBytes(task.getFileSizeInBytes())
                    .withMetrics(task.getMetrics().metrics());

            if (!partitionSpec.fields().isEmpty()) {
              String partitionDataJson =
                  task.getPartitionDataJson()
                      .orElseThrow(
                          () -> new VerifyException("No partition data for partitioned table"));
              deleteBuilder.withPartition(
                  PartitionData.fromJson(partitionDataJson, partitionColumnTypes));
            }

            rowDelta.addDeletes(deleteBuilder.build());
            writtenFiles.add(task.getPath());
            task.getReferencedDataFile().ifPresent(referencedDataFiles::add);
            break;
          case DATA:
            DataFiles.Builder builder =
                DataFiles.builder(partitionSpec)
                    .withPath(task.getPath())
                    .withFormat(task.getFileFormat().toIceberg())
                    .withFileSizeInBytes(task.getFileSizeInBytes())
                    .withMetrics(task.getMetrics().metrics());

            if (!icebergTable.spec().fields().isEmpty()) {
              String partitionDataJson =
                  task.getPartitionDataJson()
                      .orElseThrow(
                          () -> new VerifyException("No partition data for partitioned table"));
              builder.withPartition(
                  PartitionData.fromJson(partitionDataJson, partitionColumnTypes));
            }
            rowDelta.addRows(builder.build());
            writtenFiles.add(task.getPath());
            break;
          default:
            throw new UnsupportedOperationException(
                "Unsupported task content: " + task.getContent());
        }
      }

      // try to leave as little garbage as possible behind
      if (table.getRetryMode() != NO_RETRIES) {
        cleanExtraOutputFiles(session, writtenFiles.build());
      }

      rowDelta.validateDataFilesExist(referencedDataFiles.build());
      try {
        commit(rowDelta, session);
      } catch (ValidationException e) {
        throw new TrinoException(
            ICEBERG_COMMIT_ERROR,
            "Failed to commit Iceberg update to table: " + table.getSchemaTableName(),
            e);
      }
    }

    if (!fullyDeletedFiles.isEmpty()) {
      try {
        TrinoFileSystem fileSystem = fileSystemFactory.create(session);
        fileSystem.deleteFiles(
            fullyDeletedFiles.values().stream()
                .flatMap(Collection::stream)
                .map(CommitTaskData::getPath)
                .collect(toImmutableSet()));
      } catch (IOException e) {
        log.warn(e, "Failed to clean up uncommitted position delete files");
      }
    }

    try {
      if (!fullyDeletedFiles.isEmpty()) {
        DeleteFiles deleteFiles = transaction.newDelete();
        fullyDeletedFiles.keySet().forEach(deleteFiles::deleteFile);
        commit(deleteFiles, session);
      }
      transaction.commitTransaction();
    } catch (ValidationException e) {
      throw new TrinoException(
          ICEBERG_COMMIT_ERROR,
          "Failed to commit Iceberg update to table: " + table.getSchemaTableName(),
          e);
    }
    transaction = null;
  }

  private static boolean fileIsFullyDeleted(List<CommitTaskData> positionDeletes) {
    checkArgument(!positionDeletes.isEmpty(), "Cannot call fileIsFullyDeletes with an empty list");
    String referencedDataFile = positionDeletes.get(0).getReferencedDataFile().orElseThrow();
    long fileRecordCount = positionDeletes.get(0).getFileRecordCount().orElseThrow();
    checkArgument(
        positionDeletes.stream()
            .allMatch(
                positionDelete ->
                    positionDelete.getReferencedDataFile().orElseThrow().equals(referencedDataFile)
                        && positionDelete.getFileRecordCount().orElseThrow() == fileRecordCount),
        "All position deletes must be for the same file and have the same fileRecordCount");
    long deletedRowCount =
        positionDeletes.stream()
            .map(CommitTaskData::getDeletedRowCount)
            .mapToLong(Optional::orElseThrow)
            .sum();
    checkState(
        deletedRowCount <= fileRecordCount, "Found more deleted rows than exist in the file");
    return fileRecordCount == deletedRowCount;
  }

  @Override
  public Optional<ConnectorTableLayout> getInsertLayout(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    Schema schema = SchemaParser.fromJson(table.getTableSchemaJson());
    PartitionSpec partitionSpec =
        PartitionSpecParser.fromJson(
            schema,
            table
                .getPartitionSpecJson()
                .orElseThrow(
                    () -> new VerifyException("Partition spec missing in the table handle")));
    return getWriteLayout(schema, partitionSpec, false);
  }

  private Optional<ConnectorTableLayout> getWriteLayout(
      Schema tableSchema, PartitionSpec partitionSpec, boolean forceRepartitioning) {
    if (partitionSpec.isUnpartitioned()) {
      return Optional.empty();
    }

    validateNotPartitionedByNestedField(tableSchema, partitionSpec);
    Map<Integer, IcebergColumnHandle> columnById =
        getColumns(tableSchema, typeManager).stream()
            .collect(toImmutableMap(IcebergColumnHandle::getId, identity()));

    List<IcebergColumnHandle> partitioningColumns =
        partitionSpec.fields().stream()
            .sorted(Comparator.comparing(PartitionField::sourceId))
            .map(
                field ->
                    requireNonNull(
                        columnById.get(field.sourceId()),
                        () -> "Cannot find source column for partitioning field " + field))
            .distinct()
            .collect(toImmutableList());
    List<String> partitioningColumnNames =
        partitioningColumns.stream().map(IcebergColumnHandle::getName).collect(toImmutableList());

    if (!forceRepartitioning
        && partitionSpec.fields().stream().allMatch(field -> field.transform().isIdentity())) {
      // Do not set partitioningHandle, to let engine determine whether to repartition data or not,
      // on stat-based basis.
      return Optional.of(new ConnectorTableLayout(partitioningColumnNames));
    }
    IcebergPartitioningHandle partitioningHandle =
        new IcebergPartitioningHandle(toPartitionFields(partitionSpec), partitioningColumns);
    return Optional.of(new ConnectorTableLayout(partitioningHandle, partitioningColumnNames));
  }

  @Override
  public ConnectorInsertTableHandle beginInsert(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      List<ColumnHandle> columns,
      RetryMode retryMode) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    Table icebergTable = loadTable(session, table.getSchemaTableName());

    validateNotModifyingOldSnapshot(table, icebergTable);
    validateNotPartitionedByNestedField(icebergTable.schema(), icebergTable.spec());

    beginTransaction(icebergTable);

    return newWritableTableHandle(table.getSchemaTableName(), icebergTable, retryMode);
  }

  private IcebergWritableTableHandle newWritableTableHandle(
      SchemaTableName name, Table table, RetryMode retryMode) {
    return new IcebergWritableTableHandle(
        name,
        SchemaParser.toJson(table.schema()),
        transformValues(table.specs(), PartitionSpecParser::toJson),
        table.spec().specId(),
        getColumns(table.schema(), typeManager),
        table.location(),
        getFileFormat(table),
        table.properties(),
        retryMode);
  }

  @Override
  public Optional<ConnectorOutputMetadata> finishInsert(
      ConnectorSession session,
      ConnectorInsertTableHandle insertHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics) {
    List<CommitTaskData> commitTasks =
        fragments.stream()
            .map(slice -> commitTaskCodec.fromJson(slice.getBytes()))
            .collect(toImmutableList());

    if (commitTasks.isEmpty()) {
      transaction = null;
      return Optional.empty();
    }

    IcebergWritableTableHandle table = (IcebergWritableTableHandle) insertHandle;
    Table icebergTable = transaction.table();
    Type[] partitionColumnTypes =
        icebergTable.spec().fields().stream()
            .map(
                field ->
                    field
                        .transform()
                        .getResultType(icebergTable.schema().findType(field.sourceId())))
            .toArray(Type[]::new);

    AppendFiles appendFiles = transaction.newAppend();
    ImmutableSet.Builder<String> writtenFiles = ImmutableSet.builder();
    for (CommitTaskData task : commitTasks) {
      DataFiles.Builder builder =
          DataFiles.builder(icebergTable.spec())
              .withPath(task.getPath())
              .withFileSizeInBytes(task.getFileSizeInBytes())
              .withFormat(table.getFileFormat().toIceberg())
              .withMetrics(task.getMetrics().metrics());

      if (!icebergTable.spec().fields().isEmpty()) {
        String partitionDataJson =
            task.getPartitionDataJson()
                .orElseThrow(() -> new VerifyException("No partition data for partitioned table"));
        builder.withPartition(PartitionData.fromJson(partitionDataJson, partitionColumnTypes));
      }

      appendFiles.appendFile(builder.build());
      writtenFiles.add(task.getPath());
    }

    // try to leave as little garbage as possible behind
    if (table.getRetryMode() != NO_RETRIES) {
      cleanExtraOutputFiles(session, writtenFiles.build());
    }

    commit(appendFiles, session);
    transaction.commitTransaction();
    transaction = null;

    return Optional.of(
        new HiveWrittenPartitions(
            commitTasks.stream().map(CommitTaskData::getPath).collect(toImmutableList())));
  }

  private void cleanExtraOutputFiles(ConnectorSession session, Set<String> writtenFiles) {
    TrinoFileSystem fileSystem = fileSystemFactory.create(session);
    Set<String> locations = getOutputFilesLocations(writtenFiles);
    Set<String> fileNames = getOutputFilesFileNames(writtenFiles);
    for (String location : locations) {
      cleanExtraOutputFiles(fileSystem, session.getQueryId(), location, fileNames);
    }
  }

  private static void cleanExtraOutputFiles(
      TrinoFileSystem fileSystem, String queryId, String location, Set<String> fileNamesToKeep) {
    checkArgument(!queryId.contains("-"), "query ID should not contain hyphens: %s", queryId);

    Deque<String> filesToDelete = new ArrayDeque<>();
    try {
      log.debug("Deleting failed attempt files from %s for query %s", location, queryId);

      FileIterator iterator = fileSystem.listFiles(location);
      while (iterator.hasNext()) {
        FileEntry entry = iterator.next();
        String name = fileName(entry.path());
        if (name.startsWith(queryId + "-") && !fileNamesToKeep.contains(name)) {
          filesToDelete.add(name);
        }
      }

      if (filesToDelete.isEmpty()) {
        return;
      }

      log.info(
          "Found %s files to delete and %s to retain in location %s for query %s",
          filesToDelete.size(), fileNamesToKeep.size(), location, queryId);
      ImmutableList.Builder<String> deletedFilesBuilder = ImmutableList.builder();
      Iterator<String> filesToDeleteIterator = filesToDelete.iterator();
      List<String> deleteBatch = new ArrayList<>();
      while (filesToDeleteIterator.hasNext()) {
        String fileName = filesToDeleteIterator.next();
        deletedFilesBuilder.add(fileName);
        filesToDeleteIterator.remove();

        deleteBatch.add(location + "/" + fileName);
        if (deleteBatch.size() >= DELETE_BATCH_SIZE) {
          log.debug("Deleting failed attempt files %s for query %s", deleteBatch, queryId);
          fileSystem.deleteFiles(deleteBatch);
          deleteBatch.clear();
        }
      }

      if (!deleteBatch.isEmpty()) {
        log.debug("Deleting failed attempt files %s for query %s", deleteBatch, queryId);
        fileSystem.deleteFiles(deleteBatch);
      }

      List<String> deletedFiles = deletedFilesBuilder.build();
      if (!deletedFiles.isEmpty()) {
        log.info(
            "Deleted failed attempt files %s from %s for query %s",
            deletedFiles, location, queryId);
      }
    } catch (IOException e) {
      throw new TrinoException(
          ICEBERG_FILESYSTEM_ERROR,
          format("Could not clean up extraneous output files; remaining files: %s", filesToDelete),
          e);
    }
  }

  private static Set<String> getOutputFilesLocations(Set<String> writtenFiles) {
    return writtenFiles.stream().map(IcebergMetadata::getLocation).collect(toImmutableSet());
  }

  private static Set<String> getOutputFilesFileNames(Set<String> writtenFiles) {
    return writtenFiles.stream().map(IcebergUtil::fileName).collect(toImmutableSet());
  }

  private static String getLocation(String path) {
    Matcher matcher = PATH_PATTERN.matcher(path);
    verify(matcher.matches(), "path %s does not match pattern", path);
    return matcher.group(1);
  }

  @Override
  public Optional<ConnectorTableExecuteHandle> getTableHandleForExecute(
      ConnectorSession session,
      ConnectorTableHandle connectorTableHandle,
      String procedureName,
      Map<String, Object> executeProperties,
      RetryMode retryMode) {
    IcebergTableHandle tableHandle = (IcebergTableHandle) connectorTableHandle;
    checkArgument(
        tableHandle.getTableType() == DATA,
        "Cannot execute table procedure %s on non-DATA table: %s",
        procedureName,
        tableHandle.getTableType());
    Table icebergTable = loadTable(session, tableHandle.getSchemaTableName());
    if (tableHandle.getSnapshotId().isPresent()
        && (tableHandle.getSnapshotId().get() != icebergTable.currentSnapshot().snapshotId())) {
      throw new TrinoException(
          NOT_SUPPORTED,
          "Cannot execute table procedure %s on old snapshot %s"
              .formatted(procedureName, tableHandle.getSnapshotId().get()));
    }

    IcebergTableProcedureId procedureId;
    try {
      procedureId = IcebergTableProcedureId.valueOf(procedureName);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unknown procedure '" + procedureName + "'");
    }

    return switch (procedureId) {
      case OPTIMIZE -> getTableHandleForOptimize(tableHandle, executeProperties, retryMode);
      case DROP_EXTENDED_STATS -> getTableHandleForDropExtendedStats(session, tableHandle);
      case EXPIRE_SNAPSHOTS -> getTableHandleForExpireSnapshots(
          session, tableHandle, executeProperties);
      case REMOVE_ORPHAN_FILES -> getTableHandleForRemoveOrphanFiles(
          session, tableHandle, executeProperties);
    };
  }

  private Optional<ConnectorTableExecuteHandle> getTableHandleForOptimize(
      IcebergTableHandle tableHandle, Map<String, Object> executeProperties, RetryMode retryMode) {
    DataSize maxScannedFileSize = (DataSize) executeProperties.get("file_size_threshold");

    return Optional.of(
        new IcebergTableExecuteHandle(
            tableHandle.getSchemaTableName(),
            OPTIMIZE,
            new IcebergOptimizeHandle(
                tableHandle.getSnapshotId(),
                tableHandle.getTableSchemaJson(),
                tableHandle
                    .getPartitionSpecJson()
                    .orElseThrow(
                        () -> new VerifyException("Partition spec missing in the table handle")),
                getColumns(SchemaParser.fromJson(tableHandle.getTableSchemaJson()), typeManager),
                getFileFormat(tableHandle.getStorageProperties()),
                tableHandle.getStorageProperties(),
                maxScannedFileSize,
                retryMode != NO_RETRIES),
            tableHandle.getTableLocation()));
  }

  private Optional<ConnectorTableExecuteHandle> getTableHandleForDropExtendedStats(
      ConnectorSession session, IcebergTableHandle tableHandle) {
    Table icebergTable = loadTable(session, tableHandle.getSchemaTableName());

    return Optional.of(
        new IcebergTableExecuteHandle(
            tableHandle.getSchemaTableName(),
            DROP_EXTENDED_STATS,
            new IcebergDropExtendedStatsHandle(),
            icebergTable.location()));
  }

  private Optional<ConnectorTableExecuteHandle> getTableHandleForExpireSnapshots(
      ConnectorSession session,
      IcebergTableHandle tableHandle,
      Map<String, Object> executeProperties) {
    Duration retentionThreshold = (Duration) executeProperties.get(RETENTION_THRESHOLD);
    Table icebergTable = loadTable(session, tableHandle.getSchemaTableName());

    return Optional.of(
        new IcebergTableExecuteHandle(
            tableHandle.getSchemaTableName(),
            EXPIRE_SNAPSHOTS,
            new IcebergExpireSnapshotsHandle(retentionThreshold),
            icebergTable.location()));
  }

  private Optional<ConnectorTableExecuteHandle> getTableHandleForRemoveOrphanFiles(
      ConnectorSession session,
      IcebergTableHandle tableHandle,
      Map<String, Object> executeProperties) {
    Duration retentionThreshold = (Duration) executeProperties.get(RETENTION_THRESHOLD);
    Table icebergTable = loadTable(session, tableHandle.getSchemaTableName());

    return Optional.of(
        new IcebergTableExecuteHandle(
            tableHandle.getSchemaTableName(),
            REMOVE_ORPHAN_FILES,
            new IcebergRemoveOrphanFilesHandle(retentionThreshold),
            icebergTable.location()));
  }

  @Override
  public Optional<ConnectorTableLayout> getLayoutForTableExecute(
      ConnectorSession session, ConnectorTableExecuteHandle tableExecuteHandle) {
    IcebergTableExecuteHandle executeHandle = (IcebergTableExecuteHandle) tableExecuteHandle;
    switch (executeHandle.getProcedureId()) {
      case OPTIMIZE:
        return getLayoutForOptimize(session, executeHandle);
      case DROP_EXTENDED_STATS:
      case EXPIRE_SNAPSHOTS:
      case REMOVE_ORPHAN_FILES:
        // handled via executeTableExecute
    }
    throw new IllegalArgumentException(
        "Unknown procedure '" + executeHandle.getProcedureId() + "'");
  }

  private Optional<ConnectorTableLayout> getLayoutForOptimize(
      ConnectorSession session, IcebergTableExecuteHandle executeHandle) {
    Table icebergTable = loadTable(session, executeHandle.getSchemaTableName());
    // from performance perspective it is better to have lower number of bigger files than other way
    // around
    // thus we force repartitioning for optimize to achieve this
    return getWriteLayout(icebergTable.schema(), icebergTable.spec(), true);
  }

  @Override
  public BeginTableExecuteResult<ConnectorTableExecuteHandle, ConnectorTableHandle>
      beginTableExecute(
          ConnectorSession session,
          ConnectorTableExecuteHandle tableExecuteHandle,
          ConnectorTableHandle updatedSourceTableHandle) {
    IcebergTableExecuteHandle executeHandle = (IcebergTableExecuteHandle) tableExecuteHandle;
    IcebergTableHandle table = (IcebergTableHandle) updatedSourceTableHandle;
    switch (executeHandle.getProcedureId()) {
      case OPTIMIZE:
        return beginOptimize(session, executeHandle, table);
      case DROP_EXTENDED_STATS:
      case EXPIRE_SNAPSHOTS:
      case REMOVE_ORPHAN_FILES:
        // handled via executeTableExecute
    }
    throw new IllegalArgumentException(
        "Unknown procedure '" + executeHandle.getProcedureId() + "'");
  }

  private BeginTableExecuteResult<ConnectorTableExecuteHandle, ConnectorTableHandle> beginOptimize(
      ConnectorSession session, IcebergTableExecuteHandle executeHandle, IcebergTableHandle table) {
    IcebergOptimizeHandle optimizeHandle =
        (IcebergOptimizeHandle) executeHandle.getProcedureHandle();
    Table icebergTable = loadTable(session, table.getSchemaTableName());

    validateNotModifyingOldSnapshot(table, icebergTable);
    validateNotPartitionedByNestedField(icebergTable.schema(), icebergTable.spec());

    int tableFormatVersion =
        ((HasTableOperations) icebergTable).operations().current().formatVersion();
    if (tableFormatVersion > OPTIMIZE_MAX_SUPPORTED_TABLE_VERSION) {
      throw new TrinoException(
          NOT_SUPPORTED,
          format(
              "%s is not supported for Iceberg table format version > %d. Table %s format version is %s.",
              OPTIMIZE.name(),
              OPTIMIZE_MAX_SUPPORTED_TABLE_VERSION,
              table.getSchemaTableName(),
              tableFormatVersion));
    }

    beginTransaction(icebergTable);

    return new BeginTableExecuteResult<>(
        executeHandle, table.forOptimize(true, optimizeHandle.getMaxScannedFileSize()));
  }

  @Override
  public void finishTableExecute(
      ConnectorSession session,
      ConnectorTableExecuteHandle tableExecuteHandle,
      Collection<Slice> fragments,
      List<Object> splitSourceInfo) {
    IcebergTableExecuteHandle executeHandle = (IcebergTableExecuteHandle) tableExecuteHandle;
    switch (executeHandle.getProcedureId()) {
      case OPTIMIZE:
        finishOptimize(session, executeHandle, fragments, splitSourceInfo);
        return;
      case DROP_EXTENDED_STATS:
      case EXPIRE_SNAPSHOTS:
      case REMOVE_ORPHAN_FILES:
        // handled via executeTableExecute
    }
    throw new IllegalArgumentException(
        "Unknown procedure '" + executeHandle.getProcedureId() + "'");
  }

  private void finishOptimize(
      ConnectorSession session,
      IcebergTableExecuteHandle executeHandle,
      Collection<Slice> fragments,
      List<Object> splitSourceInfo) {
    IcebergOptimizeHandle optimizeHandle =
        (IcebergOptimizeHandle) executeHandle.getProcedureHandle();
    Table icebergTable = transaction.table();

    // files to be deleted
    ImmutableSet.Builder<DataFile> scannedDataFilesBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<DeleteFile> scannedDeleteFilesBuilder = ImmutableSet.builder();
    splitSourceInfo.stream()
        .map(DataFileWithDeleteFiles.class::cast)
        .forEach(
            dataFileWithDeleteFiles -> {
              scannedDataFilesBuilder.add(dataFileWithDeleteFiles.getDataFile());
              scannedDeleteFilesBuilder.addAll(dataFileWithDeleteFiles.getDeleteFiles());
            });

    Set<DataFile> scannedDataFiles = scannedDataFilesBuilder.build();
    Set<DeleteFile> fullyAppliedDeleteFiles = scannedDeleteFilesBuilder.build();

    List<CommitTaskData> commitTasks =
        fragments.stream()
            .map(slice -> commitTaskCodec.fromJson(slice.getBytes()))
            .collect(toImmutableList());

    Type[] partitionColumnTypes =
        icebergTable.spec().fields().stream()
            .map(
                field ->
                    field
                        .transform()
                        .getResultType(icebergTable.schema().findType(field.sourceId())))
            .toArray(Type[]::new);

    Set<DataFile> newFiles = new HashSet<>();
    for (CommitTaskData task : commitTasks) {
      DataFiles.Builder builder =
          DataFiles.builder(icebergTable.spec())
              .withPath(task.getPath())
              .withFileSizeInBytes(task.getFileSizeInBytes())
              .withFormat(optimizeHandle.getFileFormat().toIceberg())
              .withMetrics(task.getMetrics().metrics());

      if (!icebergTable.spec().fields().isEmpty()) {
        String partitionDataJson =
            task.getPartitionDataJson()
                .orElseThrow(() -> new VerifyException("No partition data for partitioned table"));
        builder.withPartition(PartitionData.fromJson(partitionDataJson, partitionColumnTypes));
      }

      newFiles.add(builder.build());
    }

    if (optimizeHandle.getSnapshotId().isEmpty()
        || scannedDataFiles.isEmpty() && fullyAppliedDeleteFiles.isEmpty() && newFiles.isEmpty()) {
      // Either the table is empty, or the table scan turned out to be empty, nothing to commit
      transaction = null;
      return;
    }

    // try to leave as little garbage as possible behind
    if (optimizeHandle.isRetriesEnabled()) {
      cleanExtraOutputFiles(
          session,
          newFiles.stream().map(dataFile -> dataFile.path().toString()).collect(toImmutableSet()));
    }

    RewriteFiles rewriteFiles = transaction.newRewrite();
    rewriteFiles.rewriteFiles(
        scannedDataFiles, fullyAppliedDeleteFiles, newFiles, ImmutableSet.of());
    // Table.snapshot method returns null if there is no matching snapshot
    Snapshot snapshot =
        requireNonNull(
            icebergTable.snapshot(optimizeHandle.getSnapshotId().get()), "snapshot is null");
    rewriteFiles.validateFromSnapshot(snapshot.snapshotId());
    commit(rewriteFiles, session);
    transaction.commitTransaction();
    transaction = null;
  }

  @Override
  public void executeTableExecute(
      ConnectorSession session, ConnectorTableExecuteHandle tableExecuteHandle) {
    IcebergTableExecuteHandle executeHandle = (IcebergTableExecuteHandle) tableExecuteHandle;
    switch (executeHandle.getProcedureId()) {
      case DROP_EXTENDED_STATS:
        executeDropExtendedStats(session, executeHandle);
        return;
      case EXPIRE_SNAPSHOTS:
        executeExpireSnapshots(session, executeHandle);
        return;
      case REMOVE_ORPHAN_FILES:
        executeRemoveOrphanFiles(session, executeHandle);
        return;
      default:
        throw new IllegalArgumentException(
            "Unknown procedure '" + executeHandle.getProcedureId() + "'");
    }
  }

  private void executeDropExtendedStats(
      ConnectorSession session, IcebergTableExecuteHandle executeHandle) {
    checkArgument(
        executeHandle.getProcedureHandle() instanceof IcebergDropExtendedStatsHandle,
        "Unexpected procedure handle %s",
        executeHandle.getProcedureHandle());

    Table icebergTable = loadTable(session, executeHandle.getSchemaTableName());
    beginTransaction(icebergTable);
    UpdateStatistics updateStatistics = transaction.updateStatistics();
    for (StatisticsFile statisticsFile : icebergTable.statisticsFiles()) {
      updateStatistics.removeStatistics(statisticsFile.snapshotId());
    }
    updateStatistics.commit();
    UpdateProperties updateProperties = transaction.updateProperties();
    for (String key : transaction.table().properties().keySet()) {
      if (key.startsWith(TRINO_STATS_PREFIX)) {
        updateProperties.remove(key);
      }
    }
    updateProperties.commit();
    transaction.commitTransaction();
    transaction = null;
  }

  private void executeExpireSnapshots(
      ConnectorSession session, IcebergTableExecuteHandle executeHandle) {
    IcebergExpireSnapshotsHandle expireSnapshotsHandle =
        (IcebergExpireSnapshotsHandle) executeHandle.getProcedureHandle();

    Table table = loadTable(session, executeHandle.getSchemaTableName());
    Duration retention =
        requireNonNull(expireSnapshotsHandle.getRetentionThreshold(), "retention is null");
    validateTableExecuteParameters(
        table,
        executeHandle.getSchemaTableName(),
        EXPIRE_SNAPSHOTS.name(),
        retention,
        getExpireSnapshotMinRetention(session),
        IcebergConfig.EXPIRE_SNAPSHOTS_MIN_RETENTION,
        IcebergSessionProperties.EXPIRE_SNAPSHOTS_MIN_RETENTION);

    long expireTimestampMillis = session.getStart().toEpochMilli() - retention.toMillis();
    TrinoFileSystem fileSystem = fileSystemFactory.create(session);
    List<String> pathsToDelete = new ArrayList<>();
    // deleteFunction is not accessed from multiple threads unless .executeDeleteWith() is used
    Consumer<String> deleteFunction =
        path -> {
          pathsToDelete.add(path);
          if (pathsToDelete.size() == DELETE_BATCH_SIZE) {
            try {
              fileSystem.deleteFiles(pathsToDelete);
              pathsToDelete.clear();
            } catch (IOException e) {
              throw new TrinoException(
                  ICEBERG_FILESYSTEM_ERROR, "Failed to delete files during snapshot expiration", e);
            }
          }
        };

    table
        .expireSnapshots()
        .expireOlderThan(expireTimestampMillis)
        .deleteWith(deleteFunction)
        .commit();
    try {
      fileSystem.deleteFiles(pathsToDelete);
    } catch (IOException e) {
      throw new TrinoException(
          ICEBERG_FILESYSTEM_ERROR, "Failed to delete files during snapshot expiration", e);
    }
  }

  private static void validateTableExecuteParameters(
      Table table,
      SchemaTableName schemaTableName,
      String procedureName,
      Duration retentionThreshold,
      Duration minRetention,
      String minRetentionParameterName,
      String sessionMinRetentionParameterName) {
    int tableFormatVersion = ((HasTableOperations) table).operations().current().formatVersion();
    if (tableFormatVersion > CLEANING_UP_PROCEDURES_MAX_SUPPORTED_TABLE_VERSION) {
      // It is not known if future version won't bring any new kind of metadata or data files
      // because of the way procedures are implemented it is safer to fail here than to potentially
      // remove
      // files that should stay there
      throw new TrinoException(
          NOT_SUPPORTED,
          format(
              "%s is not supported for Iceberg table format version > %d. "
                  + "Table %s format version is %s.",
              procedureName,
              CLEANING_UP_PROCEDURES_MAX_SUPPORTED_TABLE_VERSION,
              schemaTableName,
              tableFormatVersion));
    }
    Map<String, String> properties = table.properties();
    if (properties.containsKey(WRITE_LOCATION_PROVIDER_IMPL)) {
      throw new TrinoException(
          NOT_SUPPORTED,
          "Table "
              + schemaTableName
              + " specifies "
              + properties.get(WRITE_LOCATION_PROVIDER_IMPL)
              + " as a location provider. Writing to Iceberg tables with custom location provider is not supported.");
    }

    Duration retention = requireNonNull(retentionThreshold, "retention is null");
    checkProcedureArgument(
        retention.compareTo(minRetention) >= 0,
        "Retention specified (%s) is shorter than the minimum retention configured in the system (%s). "
            + "Minimum retention can be changed with %s configuration property or iceberg.%s session property",
        retention,
        minRetention,
        minRetentionParameterName,
        sessionMinRetentionParameterName);
  }

  public void executeRemoveOrphanFiles(
      ConnectorSession session, IcebergTableExecuteHandle executeHandle) {
    IcebergRemoveOrphanFilesHandle removeOrphanFilesHandle =
        (IcebergRemoveOrphanFilesHandle) executeHandle.getProcedureHandle();

    Table table = loadTable(session, executeHandle.getSchemaTableName());
    Duration retention =
        requireNonNull(removeOrphanFilesHandle.getRetentionThreshold(), "retention is null");
    validateTableExecuteParameters(
        table,
        executeHandle.getSchemaTableName(),
        REMOVE_ORPHAN_FILES.name(),
        retention,
        getRemoveOrphanFilesMinRetention(session),
        IcebergConfig.REMOVE_ORPHAN_FILES_MIN_RETENTION,
        IcebergSessionProperties.REMOVE_ORPHAN_FILES_MIN_RETENTION);

    if (table.currentSnapshot() == null) {
      log.debug("Skipping remove_orphan_files procedure for empty table %s", table);
      return;
    }

    long expireTimestampMillis = session.getStart().toEpochMilli() - retention.toMillis();
    removeOrphanFiles(table, session, executeHandle.getSchemaTableName(), expireTimestampMillis);
  }

  private void removeOrphanFiles(
      Table table,
      ConnectorSession session,
      SchemaTableName schemaTableName,
      long expireTimestamp) {
    Set<String> processedManifestFilePaths = new HashSet<>();
    // Similarly to issues like https://github.com/trinodb/trino/issues/13759,
    // equivalent paths may have different String representations due to things like double slashes.
    // Using file names may result in retaining files which could be removed.
    // However, in practice Iceberg metadata and data files have UUIDs in their names which makes
    // this unlikely.
    ImmutableSet.Builder<String> validMetadataFileNames = ImmutableSet.builder();
    ImmutableSet.Builder<String> validDataFileNames = ImmutableSet.builder();

    for (Snapshot snapshot : table.snapshots()) {
      if (snapshot.manifestListLocation() != null) {
        validMetadataFileNames.add(fileName(snapshot.manifestListLocation()));
      }

      for (ManifestFile manifest : snapshot.allManifests(table.io())) {
        if (!processedManifestFilePaths.add(manifest.path())) {
          // Already read this manifest
          continue;
        }

        validMetadataFileNames.add(fileName(manifest.path()));
        try (ManifestReader<? extends ContentFile<?>> manifestReader =
            readerForManifest(table, manifest)) {
          for (ContentFile<?> contentFile : manifestReader) {
            validDataFileNames.add(fileName(contentFile.path().toString()));
          }
        } catch (IOException e) {
          throw new TrinoException(
              ICEBERG_FILESYSTEM_ERROR,
              "Unable to list manifest file content from " + manifest.path(),
              e);
        }
      }
    }

    metadataFileLocations(table, false).stream()
        .map(IcebergUtil::fileName)
        .forEach(validMetadataFileNames::add);
    validMetadataFileNames.add(fileName(versionHintLocation(table)));

    scanAndDeleteInvalidFiles(
        table, session, schemaTableName, expireTimestamp, validDataFileNames.build(), "data");
    scanAndDeleteInvalidFiles(
        table,
        session,
        schemaTableName,
        expireTimestamp,
        validMetadataFileNames.build(),
        "metadata");
  }

  private static ManifestReader<? extends ContentFile<?>> readerForManifest(
      Table table, ManifestFile manifest) {
    return switch (manifest.content()) {
      case DATA -> ManifestFiles.read(manifest, table.io());
      case DELETES -> ManifestFiles.readDeleteManifest(manifest, table.io(), table.specs());
    };
  }

  private void scanAndDeleteInvalidFiles(
      Table table,
      ConnectorSession session,
      SchemaTableName schemaTableName,
      long expireTimestamp,
      Set<String> validFiles,
      String subfolder) {
    try {
      List<String> filesToDelete = new ArrayList<>();
      TrinoFileSystem fileSystem = fileSystemFactory.create(session);
      FileIterator allFiles = fileSystem.listFiles(table.location() + "/" + subfolder);
      while (allFiles.hasNext()) {
        FileEntry entry = allFiles.next();
        if (entry.lastModified() < expireTimestamp
            && !validFiles.contains(fileName(entry.path()))) {
          filesToDelete.add(entry.path());
          if (filesToDelete.size() >= DELETE_BATCH_SIZE) {
            log.debug(
                "Deleting files while removing orphan files for table %s [%s]",
                schemaTableName, filesToDelete);
            fileSystem.deleteFiles(filesToDelete);
            filesToDelete.clear();
          }
        } else {
          log.debug(
              "%s file retained while removing orphan files %s",
              entry.path(), schemaTableName.getTableName());
        }
      }
      if (!filesToDelete.isEmpty()) {
        log.debug(
            "Deleting files while removing orphan files for table %s %s",
            schemaTableName, filesToDelete);
        fileSystem.deleteFiles(filesToDelete);
      }
    } catch (IOException e) {
      throw new TrinoException(
          ICEBERG_FILESYSTEM_ERROR, "Failed accessing data for table: " + schemaTableName, e);
    }
  }

  @Override
  public Optional<Object> getInfo(ConnectorTableHandle tableHandle) {
    IcebergTableHandle icebergTableHandle = (IcebergTableHandle) tableHandle;
    Optional<Boolean> partitioned =
        icebergTableHandle
            .getPartitionSpecJson()
            .map(
                partitionSpecJson ->
                    PartitionSpecParser.fromJson(
                            SchemaParser.fromJson(icebergTableHandle.getTableSchemaJson()),
                            partitionSpecJson)
                        .isPartitioned());

    return Optional.of(
        new IcebergInputInfo(
            icebergTableHandle.getSnapshotId(),
            partitioned,
            getFileFormat(icebergTableHandle.getStorageProperties()).name()));
  }

  @Override
  public void dropTable(ConnectorSession session, ConnectorTableHandle tableHandle) {
    catalog.dropTable(session, ((IcebergTableHandle) tableHandle).getSchemaTableName());
  }

  @Override
  public void renameTable(
      ConnectorSession session, ConnectorTableHandle tableHandle, SchemaTableName newTable) {
    catalog.renameTable(session, ((IcebergTableHandle) tableHandle).getSchemaTableName(), newTable);
  }

  @Override
  public void setTableProperties(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      Map<String, Optional<Object>> properties) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    Table icebergTable = loadTable(session, table.getSchemaTableName());

    transaction = icebergTable.newTransaction();
    UpdateProperties updateProperties = transaction.updateProperties();

    for (Map.Entry<String, Optional<Object>> propertyEntry : properties.entrySet()) {
      String trinoPropertyName = propertyEntry.getKey();
      Optional<Object> propertyValue = propertyEntry.getValue();

      switch (trinoPropertyName) {
        case FILE_FORMAT_PROPERTY:
          updateProperties.defaultFormat(
              ((IcebergFileFormat) propertyValue.orElseThrow()).toIceberg());
          break;
        case FORMAT_VERSION_PROPERTY:
          // UpdateProperties#commit will trigger any necessary metadata updates required for the
          // new spec version
          updateProperty(
              updateProperties,
              FORMAT_VERSION,
              propertyValue,
              formatVersion -> Integer.toString((int) formatVersion));
          break;
        default:
          // TODO: Support updating partitioning https://github.com/trinodb/trino/issues/12174
          throw new TrinoException(
              NOT_SUPPORTED, "Updating the " + trinoPropertyName + " property is not supported");
      }
    }

    try {
      updateProperties.commit();
      transaction.commitTransaction();
    } catch (RuntimeException e) {
      throw new TrinoException(ICEBERG_COMMIT_ERROR, "Failed to commit new table properties", e);
    }
  }

  private static void updateProperty(
      UpdateProperties updateProperties,
      String icebergPropertyName,
      Optional<Object> value,
      Function<Object, String> toIcebergString) {
    if (value.isPresent()) {
      updateProperties.set(icebergPropertyName, toIcebergString.apply(value.get()));
    } else {
      updateProperties.remove(icebergPropertyName);
    }
  }

  @Override
  public void addColumn(
      ConnectorSession session, ConnectorTableHandle tableHandle, ColumnMetadata column) {
    Table icebergTable =
        loadTable(session, ((IcebergTableHandle) tableHandle).getSchemaTableName());
    icebergTable
        .updateSchema()
        .addColumn(column.getName(), toIcebergType(column.getType()), column.getComment())
        .commit();
  }

  @Override
  public void dropColumn(
      ConnectorSession session, ConnectorTableHandle tableHandle, ColumnHandle column) {
    IcebergColumnHandle handle = (IcebergColumnHandle) column;
    Table icebergTable =
        loadTable(session, ((IcebergTableHandle) tableHandle).getSchemaTableName());
    icebergTable.updateSchema().deleteColumn(handle.getName()).commit();
  }

  @Override
  public void renameColumn(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      ColumnHandle source,
      String target) {
    IcebergColumnHandle columnHandle = (IcebergColumnHandle) source;
    Table icebergTable =
        loadTable(session, ((IcebergTableHandle) tableHandle).getSchemaTableName());
    icebergTable.updateSchema().renameColumn(columnHandle.getName(), target).commit();
  }

  private List<ColumnMetadata> getColumnMetadatas(Schema schema) {
    ImmutableList.Builder<ColumnMetadata> columns = ImmutableList.builder();

    List<ColumnMetadata> schemaColumns =
        schema.columns().stream()
            .map(
                column ->
                    ColumnMetadata.builder()
                        .setName(column.name())
                        .setType(toTrinoType(column.type(), typeManager))
                        .setNullable(column.isOptional())
                        .setComment(Optional.ofNullable(column.doc()))
                        .build())
            .collect(toImmutableList());
    columns.addAll(schemaColumns);
    columns.add(pathColumnMetadata());
    columns.add(fileModifiedTimeColumnMetadata());
    return columns.build();
  }

  @Override
  public ConnectorAnalyzeMetadata getStatisticsCollectionMetadata(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      Map<String, Object> analyzeProperties) {
    if (!isExtendedStatisticsEnabled(session)) {
      throw new TrinoException(
          NOT_SUPPORTED,
          "Analyze is not enabled. You can enable analyze using %s config or %s catalog session property"
              .formatted(
                  IcebergConfig.EXTENDED_STATISTICS_CONFIG,
                  IcebergSessionProperties.EXTENDED_STATISTICS_ENABLED));
    }

    IcebergTableHandle handle = (IcebergTableHandle) tableHandle;
    checkArgument(
        handle.getTableType() == DATA, "Cannot analyze non-DATA table: %s", handle.getTableType());

    if (handle.getSnapshotId().isEmpty()) {
      // No snapshot, table is empty
      return new ConnectorAnalyzeMetadata(tableHandle, TableStatisticsMetadata.empty());
    }

    ConnectorTableMetadata tableMetadata = getTableMetadata(session, handle);
    Set<String> allScalarColumnNames =
        tableMetadata.getColumns().stream()
            .filter(column -> !column.isHidden())
            .filter(column -> column.getType().getTypeParameters().isEmpty()) // is scalar type
            .map(ColumnMetadata::getName)
            .collect(toImmutableSet());

    Set<String> analyzeColumnNames =
        getColumnNames(analyzeProperties)
            .map(
                columnNames -> {
                  // validate that proper column names are passed via `columns` analyze property
                  if (columnNames.isEmpty()) {
                    throw new TrinoException(
                        INVALID_ANALYZE_PROPERTY,
                        "Cannot specify empty list of columns for analysis");
                  }
                  if (!allScalarColumnNames.containsAll(columnNames)) {
                    throw new TrinoException(
                        INVALID_ANALYZE_PROPERTY,
                        format(
                            "Invalid columns specified for analysis: %s",
                            Sets.difference(columnNames, allScalarColumnNames)));
                  }
                  return columnNames;
                })
            .orElse(allScalarColumnNames);

    Set<ColumnStatisticMetadata> columnStatistics =
        tableMetadata.getColumns().stream()
            .filter(column -> analyzeColumnNames.contains(column.getName()))
            .map(
                column ->
                    new ColumnStatisticMetadata(
                        column.getName(),
                        NUMBER_OF_DISTINCT_VALUES_NAME,
                        NUMBER_OF_DISTINCT_VALUES_FUNCTION))
            .collect(toImmutableSet());

    return new ConnectorAnalyzeMetadata(
        tableHandle,
        new TableStatisticsMetadata(columnStatistics, ImmutableSet.of(), ImmutableList.of()));
  }

  @Override
  public ConnectorTableHandle beginStatisticsCollection(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    IcebergTableHandle handle = (IcebergTableHandle) tableHandle;
    Table icebergTable = loadTable(session, handle.getSchemaTableName());
    beginTransaction(icebergTable);
    return handle;
  }

  @Override
  public void finishStatisticsCollection(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      Collection<ComputedStatistics> computedStatistics) {
    IcebergTableHandle handle = (IcebergTableHandle) tableHandle;
    Table table = transaction.table();
    if (handle.getSnapshotId().isEmpty()) {
      // No snapshot, table is empty
      verify(
          computedStatistics.isEmpty(),
          "Unexpected computed statistics that cannot be attached to a snapshot because none exists: %s",
          computedStatistics);

      // TODO (https://github.com/trinodb/trino/issues/15397): remove support for Trino-specific
      // statistics properties
      // Drop all stats. Empty table needs none
      UpdateProperties updateProperties = transaction.updateProperties();
      table.properties().keySet().stream()
          .filter(key -> key.startsWith(TRINO_STATS_PREFIX))
          .forEach(updateProperties::remove);
      updateProperties.commit();

      transaction.commitTransaction();
      transaction = null;
      return;
    }
    long snapshotId = handle.getSnapshotId().orElseThrow();

    Map<String, Integer> columnNameToId =
        table.schema().columns().stream()
            .collect(
                toImmutableMap(
                    nestedField -> nestedField.name().toLowerCase(ENGLISH),
                    Types.NestedField::fieldId));
    Set<Integer> columnIds = ImmutableSet.copyOf(columnNameToId.values());

    // TODO (https://github.com/trinodb/trino/issues/15397): remove support for Trino-specific
    // statistics properties
    // Drop stats for obsolete columns
    UpdateProperties updateProperties = transaction.updateProperties();
    table.properties().keySet().stream()
        .filter(
            key -> {
              if (!key.startsWith(TRINO_STATS_PREFIX)) {
                return false;
              }
              Matcher matcher = TRINO_STATS_COLUMN_ID_PATTERN.matcher(key);
              if (!matcher.matches()) {
                return false;
              }
              return !columnIds.contains(Integer.parseInt(matcher.group("columnId")));
            })
        .forEach(updateProperties::remove);
    updateProperties.commit();

    ImmutableMap.Builder<Integer, CompactSketch> ndvSketches = ImmutableMap.builder();
    for (ComputedStatistics computedStatistic : computedStatistics) {
      verify(
          computedStatistic.getGroupingColumns().isEmpty()
              && computedStatistic.getGroupingValues().isEmpty(),
          "Unexpected grouping");
      verify(computedStatistic.getTableStatistics().isEmpty(), "Unexpected table statistics");
      for (Map.Entry<ColumnStatisticMetadata, Block> entry :
          computedStatistic.getColumnStatistics().entrySet()) {
        ColumnStatisticMetadata statisticMetadata = entry.getKey();
        if (statisticMetadata.getConnectorAggregationId().equals(NUMBER_OF_DISTINCT_VALUES_NAME)) {
          Integer columnId =
              verifyNotNull(
                  columnNameToId.get(statisticMetadata.getColumnName()),
                  "Column not found in table: [%s]",
                  statisticMetadata.getColumnName());
          CompactSketch sketch = DataSketchStateSerializer.deserialize(entry.getValue(), 0);
          ndvSketches.put(columnId, sketch);
        } else {
          throw new UnsupportedOperationException("Unsupported statistic: " + statisticMetadata);
        }
      }
    }

    StatisticsFile statisticsFile =
        tableStatisticsWriter.writeStatisticsFile(
            session, table, snapshotId, ndvSketches.buildOrThrow());
    transaction.updateStatistics().setStatistics(snapshotId, statisticsFile).commit();

    transaction.commitTransaction();
    transaction = null;
  }

  @Override
  public Optional<ConnectorTableHandle> applyDelete(
      ConnectorSession session, ConnectorTableHandle handle) {
    IcebergTableHandle table = (IcebergTableHandle) handle;
    TupleDomain<IcebergColumnHandle> medataColumnPredicate =
        table.getEnforcedPredicate().filter((column, domain) -> isMetadataColumnId(column.getId()));
    if (!medataColumnPredicate.isAll()) {
      return Optional.empty();
    }
    return Optional.of(handle);
  }

  @Override
  public Optional<ConnectorPartitioningHandle> getUpdateLayout(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    return Optional.of(IcebergUpdateHandle.INSTANCE);
  }

  private static void validateNotModifyingOldSnapshot(
      IcebergTableHandle table, Table icebergTable) {
    if (table.getSnapshotId().isPresent()
        && (table.getSnapshotId().get() != icebergTable.currentSnapshot().snapshotId())) {
      throw new TrinoException(NOT_SUPPORTED, "Modifying old snapshot is not supported in Iceberg");
    }
  }

  public static void validateNotPartitionedByNestedField(
      Schema schema, PartitionSpec partitionSpec) {
    Map<Integer, Integer> indexParents = indexParents(schema.asStruct());
    for (PartitionField field : partitionSpec.fields()) {
      if (indexParents.containsKey(field.sourceId())) {
        throw new TrinoException(
            NOT_SUPPORTED, "Partitioning by nested field is unsupported: " + field.name());
      }
    }
  }

  @Override
  public void createView(
      ConnectorSession session,
      SchemaTableName viewName,
      ConnectorViewDefinition definition,
      boolean replace) {
    catalog.createView(session, viewName, definition, replace);
  }

  @Override
  public void renameView(ConnectorSession session, SchemaTableName source, SchemaTableName target) {
    catalog.renameView(session, source, target);
  }

  @Override
  public void setViewAuthorization(
      ConnectorSession session, SchemaTableName viewName, TrinoPrincipal principal) {
    catalog.setViewPrincipal(session, viewName, principal);
  }

  @Override
  public void dropView(ConnectorSession session, SchemaTableName viewName) {
    catalog.dropView(session, viewName);
  }

  @Override
  public List<SchemaTableName> listViews(ConnectorSession session, Optional<String> schemaName) {
    return catalog.listViews(session, schemaName);
  }

  @Override
  public Map<SchemaTableName, ConnectorViewDefinition> getViews(
      ConnectorSession session, Optional<String> schemaName) {
    return catalog.getViews(session, schemaName);
  }

  @Override
  public Optional<ConnectorViewDefinition> getView(
      ConnectorSession session, SchemaTableName viewName) {
    return catalog.getView(session, viewName);
  }

  @Override
  public ColumnHandle getMergeRowIdColumnHandle(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    StructType type =
        StructType.of(
            ImmutableList.<NestedField>builder()
                .add(MetadataColumns.FILE_PATH)
                .add(MetadataColumns.ROW_POSITION)
                .add(
                    NestedField.required(
                        TRINO_MERGE_FILE_RECORD_COUNT, "file_record_count", LongType.get()))
                .add(
                    NestedField.required(
                        TRINO_MERGE_PARTITION_SPEC_ID, "partition_spec_id", IntegerType.get()))
                .add(
                    NestedField.required(
                        TRINO_MERGE_PARTITION_DATA, "partition_data", StringType.get()))
                .build());

    NestedField field = NestedField.required(TRINO_MERGE_ROW_ID, TRINO_ROW_ID_NAME, type);
    return getColumnHandle(field, typeManager);
  }

  @Override
  public OptionalLong executeDelete(ConnectorSession session, ConnectorTableHandle tableHandle) {
    IcebergTableHandle handle = (IcebergTableHandle) tableHandle;

    Table icebergTable = loadTable(session, handle.getSchemaTableName());

    DeleteFiles deleteFiles =
        icebergTable
            .newDelete()
            .deleteFromRowFilter(toIcebergExpression(handle.getEnforcedPredicate()));
    commit(deleteFiles, session);

    Map<String, String> summary = icebergTable.currentSnapshot().summary();
    String deletedRowsStr = summary.get(DELETED_RECORDS_PROP);
    if (deletedRowsStr == null) {
      // TODO Iceberg should guarantee this is always present
      // (https://github.com/apache/iceberg/issues/4647)
      return OptionalLong.empty();
    }
    long deletedRecords = Long.parseLong(deletedRowsStr);
    long removedPositionDeletes =
        Long.parseLong(summary.getOrDefault(REMOVED_POS_DELETES_PROP, "0"));
    long removedEqualityDeletes =
        Long.parseLong(summary.getOrDefault(REMOVED_EQ_DELETES_PROP, "0"));
    return OptionalLong.of(deletedRecords - removedPositionDeletes - removedEqualityDeletes);
  }

  public void rollback() {
    // TODO: cleanup open transaction
  }

  @Override
  public Optional<ConstraintApplicationResult<ConnectorTableHandle>> applyFilter(
      ConnectorSession session, ConnectorTableHandle handle, Constraint constraint) {
    IcebergTableHandle table = (IcebergTableHandle) handle;
    Table icebergTable = loadTable(session, table.getSchemaTableName());

    Set<Integer> partitionSourceIds = identityPartitionColumnsInAllSpecs(icebergTable);
    BiPredicate<IcebergColumnHandle, Domain> isIdentityPartition =
        (column, domain) -> partitionSourceIds.contains(column.getId());
    // Iceberg metadata columns can not be used in table scans
    BiPredicate<IcebergColumnHandle, Domain> isMetadataColumn =
        (column, domain) -> isMetadataColumnId(column.getId());

    TupleDomain<IcebergColumnHandle> newEnforcedConstraint =
        constraint
            .getSummary()
            .transformKeys(IcebergColumnHandle.class::cast)
            .filter(isIdentityPartition)
            .intersect(table.getEnforcedPredicate());

    TupleDomain<IcebergColumnHandle> remainingConstraint =
        constraint
            .getSummary()
            .transformKeys(IcebergColumnHandle.class::cast)
            .filter(isIdentityPartition.negate())
            .filter(isMetadataColumn.negate());

    TupleDomain<IcebergColumnHandle> newUnenforcedConstraint =
        remainingConstraint
            // TODO: Remove after completing https://github.com/trinodb/trino/issues/8759
            // Only applies to the unenforced constraint because structural types cannot be
            // partition keys
            .filter((columnHandle, predicate) -> !isStructuralType(columnHandle.getType()))
            .intersect(table.getUnenforcedPredicate());

    if (newEnforcedConstraint.equals(table.getEnforcedPredicate())
        && newUnenforcedConstraint.equals(table.getUnenforcedPredicate())) {
      return Optional.empty();
    }
    if (table instanceof AdaptHiveIcebergTableHandle) {
      return Optional.of(
          new ConstraintApplicationResult<>(
              new AdaptHiveIcebergTableHandle(
                  table.getSchemaName(),
                  table.getTableName(),
                  table.getTableType(),
                  table.getSnapshotId(),
                  table.getTableSchemaJson(),
                  table.getPartitionSpecJson(),
                  table.getFormatVersion(),
                  newUnenforcedConstraint,
                  newEnforcedConstraint,
                  table.getProjectedColumns(),
                  table.getNameMappingJson(),
                  table.getTableLocation(),
                  table.getStorageProperties(),
                  table.getRetryMode(),
                  table.getUpdatedColumns(),
                  table.isRecordScannedFiles(),
                  table.getMaxScannedFileSize()),
              remainingConstraint.transformKeys(ColumnHandle.class::cast),
              false));
    }

    return Optional.of(
        new ConstraintApplicationResult<>(
            new IcebergTableHandle(
                table.getSchemaName(),
                table.getTableName(),
                table.getTableType(),
                table.getSnapshotId(),
                table.getTableSchemaJson(),
                table.getPartitionSpecJson(),
                table.getFormatVersion(),
                newUnenforcedConstraint,
                newEnforcedConstraint,
                table.getProjectedColumns(),
                table.getNameMappingJson(),
                table.getTableLocation(),
                table.getStorageProperties(),
                table.getRetryMode(),
                table.getUpdatedColumns(),
                table.isRecordScannedFiles(),
                table.getMaxScannedFileSize()),
            remainingConstraint.transformKeys(ColumnHandle.class::cast),
            false));
  }

  private static Set<Integer> identityPartitionColumnsInAllSpecs(Table table) {
    // Extract identity partition column source ids common to ALL specs
    return table.spec().fields().stream()
        .filter(field -> field.transform().isIdentity())
        .filter(
            field ->
                table.specs().values().stream().allMatch(spec -> spec.fields().contains(field)))
        .map(PartitionField::sourceId)
        .collect(toImmutableSet());
  }

  @Override
  public Optional<ProjectionApplicationResult<ConnectorTableHandle>> applyProjection(
      ConnectorSession session,
      ConnectorTableHandle handle,
      List<ConnectorExpression> projections,
      Map<String, ColumnHandle> assignments) {
    if (!isProjectionPushdownEnabled(session)) {
      return Optional.empty();
    }

    // Create projected column representations for supported sub expressions. Simple column
    // references and chain of
    // dereferences on a variable are supported right now.
    Set<ConnectorExpression> projectedExpressions =
        projections.stream()
            .flatMap(expression -> extractSupportedProjectedColumns(expression).stream())
            .collect(toImmutableSet());

    Map<ConnectorExpression, ProjectedColumnRepresentation> columnProjections =
        projectedExpressions.stream()
            .collect(
                toImmutableMap(
                    identity(), HiveApplyProjectionUtil::createProjectedColumnRepresentation));

    IcebergTableHandle icebergTableHandle = (IcebergTableHandle) handle;

    // all references are simple variables
    if (columnProjections.values().stream().allMatch(ProjectedColumnRepresentation::isVariable)) {
      Set<IcebergColumnHandle> projectedColumns =
          assignments.values().stream()
              .map(IcebergColumnHandle.class::cast)
              .collect(toImmutableSet());
      if (icebergTableHandle.getProjectedColumns().equals(projectedColumns)) {
        return Optional.empty();
      }
      List<Assignment> assignmentsList =
          assignments.entrySet().stream()
              .map(
                  assignment ->
                      new Assignment(
                          assignment.getKey(),
                          assignment.getValue(),
                          ((IcebergColumnHandle) assignment.getValue()).getType()))
              .collect(toImmutableList());

      return Optional.of(
          new ProjectionApplicationResult<>(
              icebergTableHandle.withProjectedColumns(projectedColumns),
              projections,
              assignmentsList,
              false));
    }

    Map<String, Assignment> newAssignments = new HashMap<>();
    ImmutableMap.Builder<ConnectorExpression, Variable> newVariablesBuilder =
        ImmutableMap.builder();
    ImmutableSet.Builder<IcebergColumnHandle> projectedColumnsBuilder = ImmutableSet.builder();

    for (Map.Entry<ConnectorExpression, ProjectedColumnRepresentation> entry :
        columnProjections.entrySet()) {
      ConnectorExpression expression = entry.getKey();
      ProjectedColumnRepresentation projectedColumn = entry.getValue();

      IcebergColumnHandle baseColumnHandle =
          (IcebergColumnHandle) assignments.get(projectedColumn.getVariable().getName());
      IcebergColumnHandle projectedColumnHandle =
          createProjectedColumnHandle(
              baseColumnHandle, projectedColumn.getDereferenceIndices(), expression.getType());
      String projectedColumnName = projectedColumnHandle.getQualifiedName();

      Variable projectedColumnVariable = new Variable(projectedColumnName, expression.getType());
      Assignment newAssignment =
          new Assignment(projectedColumnName, projectedColumnHandle, expression.getType());
      newAssignments.putIfAbsent(projectedColumnName, newAssignment);

      newVariablesBuilder.put(expression, projectedColumnVariable);
      projectedColumnsBuilder.add(projectedColumnHandle);
    }

    // Modify projections to refer to new variables
    Map<ConnectorExpression, Variable> newVariables = newVariablesBuilder.buildOrThrow();
    List<ConnectorExpression> newProjections =
        projections.stream()
            .map(expression -> replaceWithNewVariables(expression, newVariables))
            .collect(toImmutableList());

    List<Assignment> outputAssignments = ImmutableList.copyOf(newAssignments.values());
    return Optional.of(
        new ProjectionApplicationResult<>(
            icebergTableHandle.withProjectedColumns(projectedColumnsBuilder.build()),
            newProjections,
            outputAssignments,
            false));
  }

  private static IcebergColumnHandle createProjectedColumnHandle(
      IcebergColumnHandle column,
      List<Integer> indices,
      io.trino.spi.type.Type projectedColumnType) {
    if (indices.isEmpty()) {
      return column;
    }
    ImmutableList.Builder<Integer> fullPath = ImmutableList.builder();
    fullPath.addAll(column.getPath());

    ColumnIdentity projectedColumnIdentity = column.getColumnIdentity();
    for (int index : indices) {
      // Position based lookup, not FieldId based
      projectedColumnIdentity = projectedColumnIdentity.getChildren().get(index);
      fullPath.add(projectedColumnIdentity.getId());
    }

    return new IcebergColumnHandle(
        column.getBaseColumnIdentity(),
        column.getBaseType(),
        fullPath.build(),
        projectedColumnType,
        Optional.empty());
  }

  @Override
  public TableStatistics getTableStatistics(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (!isStatisticsEnabled(session)) {
      return TableStatistics.empty();
    }

    IcebergTableHandle originalHandle = (IcebergTableHandle) tableHandle;
    // Certain table handle attributes are not applicable to select queries (which need stats).
    // If this changes, the caching logic may here may need to be revised.
    checkArgument(originalHandle.getUpdatedColumns().isEmpty(), "Unexpected updated columns");
    checkArgument(!originalHandle.isRecordScannedFiles(), "Unexpected scanned files recording set");
    checkArgument(
        originalHandle.getMaxScannedFileSize().isEmpty(), "Unexpected max scanned file size set");

    return tableStatisticsCache.computeIfAbsent(
        new IcebergTableHandle(
            originalHandle.getSchemaName(),
            originalHandle.getTableName(),
            originalHandle.getTableType(),
            originalHandle.getSnapshotId(),
            originalHandle.getTableSchemaJson(),
            originalHandle.getPartitionSpecJson(),
            originalHandle.getFormatVersion(),
            originalHandle.getUnenforcedPredicate(),
            originalHandle.getEnforcedPredicate(),
            ImmutableSet.of(), // projectedColumns don't affect stats
            originalHandle.getNameMappingJson(),
            originalHandle.getTableLocation(),
            originalHandle.getStorageProperties(),
            NO_RETRIES, // retry mode doesn't affect stats
            originalHandle.getUpdatedColumns(),
            originalHandle.isRecordScannedFiles(),
            originalHandle.getMaxScannedFileSize()),
        handle -> {
          Table icebergTable = loadTable(session, handle.getSchemaTableName());
          return TableStatisticsReader.getTableStatistics(
              typeManager, session, handle, icebergTable);
        });
  }

  @Override
  public void setTableAuthorization(
      ConnectorSession session, SchemaTableName tableName, TrinoPrincipal principal) {
    catalog.setTablePrincipal(session, tableName, principal);
  }

  private Optional<Long> getCurrentSnapshotId(Table table) {
    return Optional.ofNullable(table.currentSnapshot()).map(Snapshot::snapshotId);
  }

  Table getIcebergTable(ConnectorSession session, SchemaTableName schemaTableName) {
    return loadTable(session, schemaTableName);
  }

  @Override
  public void createMaterializedView(
      ConnectorSession session,
      SchemaTableName viewName,
      ConnectorMaterializedViewDefinition definition,
      boolean replace,
      boolean ignoreExisting) {
    catalog.createMaterializedView(session, viewName, definition, replace, ignoreExisting);
  }

  @Override
  public void dropMaterializedView(ConnectorSession session, SchemaTableName viewName) {
    catalog.dropMaterializedView(session, viewName);
  }

  @Override
  public boolean delegateMaterializedViewRefreshToConnector(
      ConnectorSession session, SchemaTableName viewName) {
    return false;
  }

  @Override
  public ConnectorInsertTableHandle beginRefreshMaterializedView(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      List<ConnectorTableHandle> sourceTableHandles,
      RetryMode retryMode) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    Table icebergTable = loadTable(session, table.getSchemaTableName());
    beginTransaction(icebergTable);

    return newWritableTableHandle(table.getSchemaTableName(), icebergTable, retryMode);
  }

  @Override
  public Optional<ConnectorOutputMetadata> finishRefreshMaterializedView(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      ConnectorInsertTableHandle insertHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics,
      List<ConnectorTableHandle> sourceTableHandles) {
    IcebergWritableTableHandle table = (IcebergWritableTableHandle) insertHandle;

    Table icebergTable = transaction.table();
    // delete before insert .. simulating overwrite
    transaction.newDelete().deleteFromRowFilter(Expressions.alwaysTrue()).commit();

    List<CommitTaskData> commitTasks =
        fragments.stream()
            .map(slice -> commitTaskCodec.fromJson(slice.getBytes()))
            .collect(toImmutableList());

    Type[] partitionColumnTypes =
        icebergTable.spec().fields().stream()
            .map(
                field ->
                    field
                        .transform()
                        .getResultType(icebergTable.schema().findType(field.sourceId())))
            .toArray(Type[]::new);

    AppendFiles appendFiles = transaction.newFastAppend();
    ImmutableSet.Builder<String> writtenFiles = ImmutableSet.builder();
    for (CommitTaskData task : commitTasks) {
      DataFiles.Builder builder =
          DataFiles.builder(icebergTable.spec())
              .withPath(task.getPath())
              .withFileSizeInBytes(task.getFileSizeInBytes())
              .withFormat(table.getFileFormat().toIceberg())
              .withMetrics(task.getMetrics().metrics());

      if (!icebergTable.spec().fields().isEmpty()) {
        String partitionDataJson =
            task.getPartitionDataJson()
                .orElseThrow(() -> new VerifyException("No partition data for partitioned table"));
        builder.withPartition(PartitionData.fromJson(partitionDataJson, partitionColumnTypes));
      }

      appendFiles.appendFile(builder.build());
      writtenFiles.add(task.getPath());
    }

    String dependencies =
        sourceTableHandles.stream()
            .map(
                handle -> {
                  if (!(handle instanceof IcebergTableHandle icebergHandle)) {
                    return UNKNOWN_SNAPSHOT_TOKEN;
                  }
                  return icebergHandle.getSchemaTableName()
                      + "="
                      + icebergHandle.getSnapshotId().map(Object.class::cast).orElse("");
                })
            .distinct()
            .collect(joining(","));

    // try to leave as little garbage as possible behind
    if (table.getRetryMode() != NO_RETRIES) {
      cleanExtraOutputFiles(session, writtenFiles.build());
    }

    // Update the 'dependsOnTables' property that tracks tables on which the materialized view
    // depends
    // and the corresponding snapshot ids of the tables
    appendFiles.set(DEPENDS_ON_TABLES, dependencies);
    commit(appendFiles, session);

    transaction.commitTransaction();
    transaction = null;
    return Optional.of(
        new HiveWrittenPartitions(
            commitTasks.stream().map(CommitTaskData::getPath).collect(toImmutableList())));
  }

  @Override
  public List<SchemaTableName> listMaterializedViews(
      ConnectorSession session, Optional<String> schemaName) {
    return catalog.listMaterializedViews(session, schemaName);
  }

  @Override
  public Map<SchemaTableName, ConnectorMaterializedViewDefinition> getMaterializedViews(
      ConnectorSession session, Optional<String> schemaName) {
    Map<SchemaTableName, ConnectorMaterializedViewDefinition> materializedViews = new HashMap<>();
    for (SchemaTableName name : listMaterializedViews(session, schemaName)) {
      try {
        getMaterializedView(session, name).ifPresent(view -> materializedViews.put(name, view));
      } catch (RuntimeException e) {
        // Materialized view can be being removed and this may cause all sorts of exceptions.
        // Log, because we're catching broadly.
        log.warn(e, "Failed to access metadata of materialized view %s during listing", name);
      }
    }
    return materializedViews;
  }

  @Override
  public Optional<ConnectorMaterializedViewDefinition> getMaterializedView(
      ConnectorSession session, SchemaTableName viewName) {
    return catalog.getMaterializedView(session, viewName);
  }

  @Override
  public void renameMaterializedView(
      ConnectorSession session, SchemaTableName source, SchemaTableName target) {
    // TODO (https://github.com/trinodb/trino/issues/9594) support rename across schemas
    if (!source.getSchemaName().equals(target.getSchemaName())) {
      throw new TrinoException(
          NOT_SUPPORTED, "Materialized View rename across schemas is not supported");
    }
    catalog.renameMaterializedView(session, source, target);
  }

  public Optional<TableToken> getTableToken(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    IcebergTableHandle table = (IcebergTableHandle) tableHandle;
    Table icebergTable = loadTable(session, table.getSchemaTableName());
    return Optional.ofNullable(icebergTable.currentSnapshot())
        .map(snapshot -> new TableToken(snapshot.snapshotId()));
  }

  public boolean isTableCurrent(
      ConnectorSession session, ConnectorTableHandle tableHandle, Optional<TableToken> tableToken) {
    Optional<TableToken> currentToken = getTableToken(session, tableHandle);

    if (tableToken.isEmpty() || currentToken.isEmpty()) {
      return false;
    }

    return tableToken.get().getSnapshotId() == currentToken.get().getSnapshotId();
  }

  @Override
  public MaterializedViewFreshness getMaterializedViewFreshness(
      ConnectorSession session, SchemaTableName materializedViewName) {
    Optional<ConnectorMaterializedViewDefinition> materializedViewDefinition =
        getMaterializedView(session, materializedViewName);
    if (materializedViewDefinition.isEmpty()) {
      // View not found, might have been concurrently deleted
      return new MaterializedViewFreshness(STALE);
    }

    SchemaTableName storageTableName =
        materializedViewDefinition
            .get()
            .getStorageTable()
            .map(CatalogSchemaTableName::getSchemaTableName)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Storage table missing in definition of materialized view "
                            + materializedViewName));

    Table icebergTable = loadTable(session, storageTableName);
    String dependsOnTables =
        icebergTable.currentSnapshot().summary().getOrDefault(DEPENDS_ON_TABLES, "");
    if (dependsOnTables.isEmpty()) {
      // Information missing. While it's "unknown" whether storage is stale,
      // we return "stale": under no normal circumstances dependsOnTables should be missing.
      return new MaterializedViewFreshness(STALE);
    }
    boolean hasUnknownTables = false;
    Iterable<String> tableToSnapshotIds = Splitter.on(',').split(dependsOnTables);
    for (String entry : tableToSnapshotIds) {
      if (entry.equals(UNKNOWN_SNAPSHOT_TOKEN)) {
        // This is a "federated" materialized view (spanning across connectors).
        // Trust user's choice and assume "fresh or fresh enough".
        hasUnknownTables = true;
        continue;
      }
      List<String> keyValue = Splitter.on("=").splitToList(entry);
      if (keyValue.size() != 2) {
        throw new TrinoException(
            ICEBERG_INVALID_METADATA,
            format("Invalid entry in '%s' property: %s'", DEPENDS_ON_TABLES, entry));
      }
      String tableName = keyValue.get(0);
      String value = keyValue.get(1);
      List<String> strings = Splitter.on(".").splitToList(tableName);
      if (strings.size() == 3) {
        strings = strings.subList(1, 3);
      } else if (strings.size() != 2) {
        throw new TrinoException(
            ICEBERG_INVALID_METADATA,
            format("Invalid table name in '%s' property: %s'", DEPENDS_ON_TABLES, strings));
      }
      String schema = strings.get(0);
      String name = strings.get(1);
      SchemaTableName schemaTableName = new SchemaTableName(schema, name);
      IcebergTableHandle tableHandle = getTableHandle(session, schemaTableName);

      if (tableHandle == null) {
        throw new MaterializedViewNotFoundException(materializedViewName);
      }
      Optional<TableToken> tableToken;
      if (value.isEmpty()) {
        tableToken = Optional.empty();
      } else {
        tableToken = Optional.of(new TableToken(Long.parseLong(value)));
      }
      if (!isTableCurrent(session, tableHandle, tableToken)) {
        return new MaterializedViewFreshness(STALE);
      }
    }
    return new MaterializedViewFreshness(hasUnknownTables ? UNKNOWN : FRESH);
  }

  @Override
  public boolean supportsReportingWrittenBytes(
      ConnectorSession session, ConnectorTableHandle connectorTableHandle) {
    return true;
  }

  @Override
  public boolean supportsReportingWrittenBytes(
      ConnectorSession session,
      SchemaTableName fullTableName,
      Map<String, Object> tableProperties) {
    return true;
  }

  @Override
  public void setColumnComment(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      ColumnHandle column,
      Optional<String> comment) {
    catalog.updateColumnComment(
        session,
        ((IcebergTableHandle) tableHandle).getSchemaTableName(),
        ((IcebergColumnHandle) column).getColumnIdentity(),
        comment);
  }

  @Override
  public Optional<CatalogSchemaTableName> redirectTable(
      ConnectorSession session, SchemaTableName tableName) {
    return catalog.redirectTable(session, tableName);
  }

  private void beginTransaction(Table icebergTable) {
    verify(transaction == null, "transaction already set");
    transaction = icebergTable.newTransaction();
  }

  private Table loadTable(ConnectorSession session, SchemaTableName tableName) {
    return tableCache.computeIfAbsent(tableName, ignore -> catalog.loadTable(session, tableName));
  }

  private static class TableToken {
    // Current Snapshot ID of the table
    private final long snapshotId;

    public TableToken(long snapshotId) {
      this.snapshotId = snapshotId;
    }

    public long getSnapshotId() {
      return snapshotId;
    }
  }
}
