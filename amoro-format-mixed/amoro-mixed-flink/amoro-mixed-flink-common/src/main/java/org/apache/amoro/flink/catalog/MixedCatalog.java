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

package org.apache.amoro.flink.catalog;

import static org.apache.amoro.flink.FlinkSchemaUtil.generateExtraOptionsFrom;
import static org.apache.amoro.flink.FlinkSchemaUtil.getPhysicalSchema;
import static org.apache.amoro.flink.FlinkSchemaUtil.toSchema;
import static org.apache.flink.table.factories.FactoryUtil.CONNECTOR;
import static org.apache.flink.util.Preconditions.checkNotNull;

import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.table.MixedDynamicTableFactory;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableColumn.ComputedColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.CatalogDatabase;
import org.apache.flink.table.catalog.CatalogFunction;
import org.apache.flink.table.catalog.CatalogPartition;
import org.apache.flink.table.catalog.CatalogPartitionSpec;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.CatalogTableImpl;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.table.catalog.exceptions.DatabaseAlreadyExistException;
import org.apache.flink.table.catalog.exceptions.DatabaseNotExistException;
import org.apache.flink.table.catalog.exceptions.FunctionNotExistException;
import org.apache.flink.table.catalog.exceptions.PartitionSpecInvalidException;
import org.apache.flink.table.catalog.exceptions.TableAlreadyExistException;
import org.apache.flink.table.catalog.exceptions.TableNotExistException;
import org.apache.flink.table.catalog.exceptions.TableNotPartitionedException;
import org.apache.flink.table.catalog.stats.CatalogColumnStatistics;
import org.apache.flink.table.catalog.stats.CatalogTableStatistics;
import org.apache.flink.table.expressions.Expression;
import org.apache.flink.table.factories.Factory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.flink.FlinkFilters;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.util.FlinkAlterTableUtil;
import org.apache.iceberg.io.CloseableIterable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Catalogs for mixed table format(include mixed-iceberg and mixed-hive). */
public class MixedCatalog extends AbstractCatalog {
  public static final String DEFAULT_DB = "default";

  /**
   * To distinguish 'CREATE TABLE LIKE' by checking stack {@link
   * org.apache.flink.table.planner.operations.SqlCreateTableConverter#lookupLikeSourceTable}
   */
  public static final String SQL_LIKE_METHOD = "lookupLikeSourceTable";

  public static final String LOCATION = "location";

  public static final String CHERRY_PICK_SNAPSHOT_ID = "cherry-pick-snapshot-id";

  public static final String CURRENT_SNAPSHOT_ID = "current-snapshot-id";

  private final InternalCatalogBuilder catalogBuilder;

  private MixedFormatCatalog internalCatalog;

  public MixedCatalog(String name, String defaultDatabase, InternalCatalogBuilder catalogBuilder) {
    super(name, defaultDatabase);
    this.catalogBuilder = catalogBuilder;
  }

  public MixedCatalog(MixedCatalog copy) {
    this(copy.getName(), copy.getDefaultDatabase(), copy.catalogBuilder);
  }

  @Override
  public void open() throws CatalogException {
    internalCatalog = catalogBuilder.build();
  }

  @Override
  public void close() throws CatalogException {}

  @Override
  public List<String> listDatabases() throws CatalogException {
    return internalCatalog.listDatabases();
  }

  @Override
  public CatalogDatabase getDatabase(String databaseName) throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean databaseExists(String databaseName) throws CatalogException {
    return listDatabases().stream().anyMatch(db -> db.equalsIgnoreCase(databaseName));
  }

  @Override
  public void createDatabase(String name, CatalogDatabase database, boolean ignoreIfExists)
      throws CatalogException, DatabaseAlreadyExistException {
    try {
      internalCatalog.createDatabase(name);
    } catch (AlreadyExistsException e) {
      if (!ignoreIfExists) {
        throw new DatabaseAlreadyExistException(getName(), name, e);
      }
    }
  }

  @Override
  public void dropDatabase(String name, boolean ignoreIfNotExists, boolean cascade)
      throws CatalogException, DatabaseNotExistException {
    try {
      internalCatalog.dropDatabase(name);
    } catch (NoSuchDatabaseException e) {
      if (!ignoreIfNotExists) {
        throw new DatabaseNotExistException(getName(), name);
      }
    }
  }

  @Override
  public void alterDatabase(String name, CatalogDatabase newDatabase, boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> listTables(String databaseName) throws CatalogException {
    return internalCatalog.listTables(databaseName).stream()
        .map(TableIdentifier::getTableName)
        .collect(Collectors.toList());
  }

  @Override
  public List<String> listViews(String databaseName) throws CatalogException {
    return Collections.emptyList();
  }

  @Override
  public CatalogBaseTable getTable(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    TableIdentifier tableIdentifier = getTableIdentifier(tablePath);
    if (!internalCatalog.tableExists(tableIdentifier)) {
      throw new TableNotExistException(this.getName(), tablePath);
    }
    MixedTable table = internalCatalog.loadTable(tableIdentifier);
    Schema mixedTableSchema = table.schema();

    Map<String, String> mixedTableProperties = Maps.newHashMap(table.properties());
    fillTableProperties(mixedTableProperties);
    fillTableMetaPropertiesIfLookupLike(mixedTableProperties, tableIdentifier);

    List<String> partitionKeys = toPartitionKeys(table.spec(), table.schema());
    return CatalogTable.of(
        toSchema(mixedTableSchema, MixedFormatUtils.getPrimaryKeys(table), mixedTableProperties)
            .toSchema(),
        null,
        partitionKeys,
        mixedTableProperties);
  }

  /**
   * For now, 'CREATE TABLE LIKE' would be treated as the case which users want to add watermark in
   * temporal join, as an alternative of lookup join, and use mixed-format table as build table,
   * i.e. right table. So the properties those required in temporal join will be put automatically.
   *
   * <p>If you don't want the properties, 'EXCLUDING ALL' is what you need. More details @see <a
   * href="https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/sql/create/#like">LIKE</a>
   */
  private void fillTableMetaPropertiesIfLookupLike(
      Map<String, String> properties, TableIdentifier tableIdentifier) {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    boolean isLookupLike = false;
    for (StackTraceElement stackTraceElement : stackTraceElements) {
      if (Objects.equal(SQL_LIKE_METHOD, stackTraceElement.getMethodName())) {
        isLookupLike = true;
        break;
      }
    }

    if (!isLookupLike) {
      return;
    }

    properties.put(CONNECTOR.key(), MixedDynamicTableFactory.IDENTIFIER);
    properties.put(MixedFormatValidator.MIXED_FORMAT_CATALOG.key(), tableIdentifier.getCatalog());
    properties.put(MixedFormatValidator.MIXED_FORMAT_TABLE.key(), tableIdentifier.getTableName());
    properties.put(MixedFormatValidator.MIXED_FORMAT_DATABASE.key(), tableIdentifier.getDatabase());
    properties.put(CatalogFactoryOptions.METASTORE_URL.key(), catalogBuilder.getMetastoreUrl());
  }

  private static List<String> toPartitionKeys(PartitionSpec spec, Schema icebergSchema) {
    List<String> partitionKeys = Lists.newArrayList();
    for (PartitionField field : spec.fields()) {
      if (field.transform().isIdentity()) {
        partitionKeys.add(icebergSchema.findColumnName(field.sourceId()));
      } else {
        // Not created by Flink SQL.
        // For compatibility with iceberg tables, return empty.
        // TODO modify this after Flink support partition transform.
        return Collections.emptyList();
      }
    }
    return partitionKeys;
  }

  private void fillTableProperties(Map<String, String> tableProperties) {
    boolean enableStream =
        CompatiblePropertyUtil.propertyAsBoolean(
            tableProperties,
            TableProperties.ENABLE_LOG_STORE,
            TableProperties.ENABLE_LOG_STORE_DEFAULT);
    if (enableStream) {
      tableProperties.putIfAbsent(
          FactoryUtil.FORMAT.key(),
          tableProperties.getOrDefault(
              TableProperties.LOG_STORE_DATA_FORMAT,
              TableProperties.LOG_STORE_DATA_FORMAT_DEFAULT));
    }
  }

  private TableIdentifier getTableIdentifier(ObjectPath tablePath) {
    return TableIdentifier.of(
        internalCatalog.name(), tablePath.getDatabaseName(), tablePath.getObjectName());
  }

  @Override
  public boolean tableExists(ObjectPath tablePath) throws CatalogException {
    return internalCatalog.tableExists(getTableIdentifier(tablePath));
  }

  @Override
  public void dropTable(ObjectPath tablePath, boolean ignoreIfNotExists) throws CatalogException {
    internalCatalog.dropTable(getTableIdentifier(tablePath), true);
  }

  @Override
  public void renameTable(ObjectPath tablePath, String newTableName, boolean ignoreIfNotExists)
      throws CatalogException {
    internalCatalog.renameTable(getTableIdentifier(tablePath), newTableName);
  }

  @Override
  public void createTable(ObjectPath tablePath, CatalogBaseTable table, boolean ignoreIfExists)
      throws CatalogException, TableAlreadyExistException {
    validateFlinkTable(table);
    validateColumnOrder(table);
    createAmoroTable(tablePath, table, ignoreIfExists);
  }

  private void createAmoroTable(
      ObjectPath tablePath, CatalogBaseTable table, boolean ignoreIfExists)
      throws CatalogException, TableAlreadyExistException {
    TableSchema tableSchema = table.getSchema();
    // get PhysicalColumn for TableSchema
    TableSchema physicalSchema = getPhysicalSchema(tableSchema);
    Schema icebergSchema = FlinkSchemaUtil.convert(physicalSchema);
    TableBuilder tableBuilder =
        internalCatalog.newTableBuilder(getTableIdentifier(tablePath), icebergSchema);

    tableSchema
        .getPrimaryKey()
        .ifPresent(
            k -> {
              PrimaryKeySpec.Builder builder = PrimaryKeySpec.builderFor(icebergSchema);
              k.getColumns().forEach(builder::addColumn);
              tableBuilder.withPrimaryKeySpec(builder.build());
            });

    PartitionSpec spec = toPartitionSpec(((CatalogTable) table).getPartitionKeys(), icebergSchema);
    tableBuilder.withPartitionSpec(spec);

    Map<String, String> properties = table.getOptions();
    // update computed columns and watermark to properties
    Map<String, String> extraOptions = generateExtraOptionsFrom(tableSchema);
    properties.putAll(extraOptions);

    tableBuilder.withProperties(properties);

    try {
      tableBuilder.create();
    } catch (AlreadyExistsException e) {
      if (!ignoreIfExists) {
        throw new TableAlreadyExistException(getName(), tablePath, e);
      }
    }
  }

  private static PartitionSpec toPartitionSpec(List<String> partitionKeys, Schema icebergSchema) {
    PartitionSpec.Builder builder = PartitionSpec.builderFor(icebergSchema);
    partitionKeys.forEach(builder::identity);
    return builder.build();
  }

  private static void validateFlinkTable(CatalogBaseTable table) {
    Preconditions.checkArgument(
        table instanceof CatalogTable, "The Table should be a CatalogTable.");
  }

  @Override
  public void alterTable(ObjectPath tablePath, CatalogBaseTable newTable, boolean ignoreIfNotExists)
      throws CatalogException, TableNotExistException {
    validateFlinkTable(newTable);

    TableIdentifier tableIdentifier = getTableIdentifier(tablePath);
    MixedTable mixedTable;
    try {
      mixedTable = internalCatalog.loadTable(tableIdentifier);
    } catch (NoSuchTableException e) {
      if (!ignoreIfNotExists) {
        throw new TableNotExistException(internalCatalog.name(), tablePath, e);
      } else {
        return;
      }
    }

    // Currently, Flink SQL only support altering table properties.
    validateTableSchemaAndPartition(
        toCatalogTable(mixedTable, tableIdentifier), (CatalogTable) newTable);

    if (mixedTable.isUnkeyedTable()) {
      alterUnKeyedTable(mixedTable.asUnkeyedTable(), newTable);
    } else if (mixedTable.isKeyedTable()) {
      alterKeyedTable(mixedTable.asKeyedTable(), newTable);
    } else {
      throw new UnsupportedOperationException("Unsupported alter table");
    }
  }

  @Override
  public List<CatalogPartitionSpec> listPartitions(ObjectPath tablePath)
      throws CatalogException, TableNotPartitionedException {
    return listPartitionsByFilter(tablePath, Collections.emptyList());
  }

  @Override
  public List<CatalogPartitionSpec> listPartitions(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws CatalogException, TableNotPartitionedException, PartitionSpecInvalidException {
    checkNotNull(tablePath, "Table path cannot be null");
    checkNotNull(partitionSpec, "CatalogPartitionSpec cannot be null");
    TableIdentifier tableIdentifier = getTableIdentifier(tablePath);
    checkValidPartitionSpec(
        partitionSpec, internalCatalog.loadTable(tableIdentifier).spec(), tablePath);
    List<CatalogPartitionSpec> catalogPartitionSpecs = listPartitions(tablePath);
    return catalogPartitionSpecs.stream()
        .filter(spec -> spec.equals(partitionSpec))
        .collect(Collectors.toList());
  }

  @Override
  public List<CatalogPartitionSpec> listPartitionsByFilter(
      ObjectPath tablePath, List<Expression> filters)
      throws CatalogException, TableNotPartitionedException {
    TableIdentifier tableIdentifier = getTableIdentifier(tablePath);
    MixedTable mixedTable = internalCatalog.loadTable(tableIdentifier);

    org.apache.iceberg.expressions.Expression filter;
    List<org.apache.iceberg.expressions.Expression> expressions =
        filters.stream()
            .map(FlinkFilters::convert)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    filter =
        expressions.isEmpty()
            ? Expressions.alwaysTrue()
            : expressions.stream().reduce(Expressions::and).orElse(Expressions.alwaysTrue());

    if (mixedTable.spec().isUnpartitioned()) {
      throw new TableNotPartitionedException(internalCatalog.name(), tablePath);
    }
    Set<CatalogPartitionSpec> set = Sets.newHashSet();
    if (mixedTable.isKeyedTable()) {
      KeyedTable table = mixedTable.asKeyedTable();
      try (CloseableIterable<CombinedScanTask> combinedScanTasks =
          table.newScan().filter(filter).planTasks()) {
        for (CombinedScanTask combinedScanTask : combinedScanTasks) {
          combinedScanTask.tasks().stream()
              .flatMap(
                  (Function<KeyedTableScanTask, Stream<MixedFileScanTask>>)
                      keyedTableScanTask ->
                          Stream.of(
                                  keyedTableScanTask.dataTasks(),
                                  keyedTableScanTask.mixedEquityDeletes())
                              .flatMap(List::stream))
              .forEach(
                  mixedFileScanTask -> {
                    Map<String, String> map = Maps.newHashMap();
                    StructLike structLike = mixedFileScanTask.partition();
                    PartitionSpec spec = table.spec();
                    for (int i = 0; i < structLike.size(); i++) {
                      map.put(
                          spec.fields().get(i).name(),
                          String.valueOf(structLike.get(i, Object.class)));
                    }
                    set.add(new CatalogPartitionSpec(map));
                  });
        }
      } catch (IOException e) {
        throw new CatalogException(
            String.format("Failed to list partitions of table %s", tablePath), e);
      }
    } else {
      UnkeyedTable table = mixedTable.asUnkeyedTable();
      try (CloseableIterable<FileScanTask> tasks = table.newScan().filter(filter).planFiles()) {
        for (DataFile dataFile : CloseableIterable.transform(tasks, FileScanTask::file)) {
          Map<String, String> map = Maps.newHashMap();
          StructLike structLike = dataFile.partition();
          PartitionSpec spec = table.specs().get(dataFile.specId());
          for (int i = 0; i < structLike.size(); i++) {
            map.put(spec.fields().get(i).name(), String.valueOf(structLike.get(i, Object.class)));
          }
          set.add(new CatalogPartitionSpec(map));
        }
      } catch (IOException e) {
        throw new CatalogException(
            String.format("Failed to list partitions of table %s", tablePath), e);
      }
    }
    return Lists.newArrayList(set);
  }

  @Override
  public CatalogPartition getPartition(ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean partitionExists(ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createPartition(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogPartition partition,
      boolean ignoreIfExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void dropPartition(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec, boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void alterPartition(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogPartition newPartition,
      boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> listFunctions(String dbName) throws CatalogException {
    return Collections.emptyList();
  }

  @Override
  public CatalogFunction getFunction(ObjectPath functionPath)
      throws FunctionNotExistException, CatalogException {
    throw new FunctionNotExistException(getName(), functionPath);
  }

  @Override
  public boolean functionExists(ObjectPath functionPath) throws CatalogException {
    return false;
  }

  @Override
  public void createFunction(
      ObjectPath functionPath, CatalogFunction function, boolean ignoreIfExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void alterFunction(
      ObjectPath functionPath, CatalogFunction newFunction, boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void dropFunction(ObjectPath functionPath, boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public CatalogTableStatistics getTableStatistics(ObjectPath tablePath) throws CatalogException {
    return CatalogTableStatistics.UNKNOWN;
  }

  @Override
  public CatalogColumnStatistics getTableColumnStatistics(ObjectPath tablePath)
      throws CatalogException {
    return CatalogColumnStatistics.UNKNOWN;
  }

  @Override
  public CatalogTableStatistics getPartitionStatistics(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec) throws CatalogException {
    return CatalogTableStatistics.UNKNOWN;
  }

  @Override
  public CatalogColumnStatistics getPartitionColumnStatistics(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec) throws CatalogException {
    return CatalogColumnStatistics.UNKNOWN;
  }

  @Override
  public void alterTableStatistics(
      ObjectPath tablePath, CatalogTableStatistics tableStatistics, boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void alterTableColumnStatistics(
      ObjectPath tablePath, CatalogColumnStatistics columnStatistics, boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void alterPartitionStatistics(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogTableStatistics partitionStatistics,
      boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void alterPartitionColumnStatistics(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogColumnStatistics columnStatistics,
      boolean ignoreIfNotExists)
      throws CatalogException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Factory> getFactory() {
    return Optional.of(new MixedDynamicTableFactory(this));
  }

  public InternalCatalogBuilder catalogBuilder() {
    return catalogBuilder;
  }

  public String amsCatalogName() {
    return internalCatalog.name();
  }

  /**
   * Check whether a list of partition values are valid based on the given list of partition keys.
   *
   * @param partitionSpec a partition spec.
   * @param mixedTablePartitionSpec mixedTablePartitionSpec
   * @param tablePath tablePath
   * @throws PartitionSpecInvalidException thrown if any key in partitionSpec doesn't exist in
   *     partitionKeys.
   */
  private void checkValidPartitionSpec(
      CatalogPartitionSpec partitionSpec,
      PartitionSpec mixedTablePartitionSpec,
      ObjectPath tablePath)
      throws PartitionSpecInvalidException {
    List<String> partitionKeys =
        mixedTablePartitionSpec.fields().stream()
            .map(PartitionField::name)
            .collect(Collectors.toList());
    for (String key : partitionSpec.getPartitionSpec().keySet()) {
      if (!partitionKeys.contains(key)) {
        throw new PartitionSpecInvalidException(getName(), partitionKeys, tablePath, partitionSpec);
      }
    }
  }

  private void validateColumnOrder(CatalogBaseTable table) {
    TableSchema schema = table.getSchema();
    List<TableColumn> tableColumns = schema.getTableColumns();

    boolean foundComputeColumn = false;
    for (TableColumn tableColumn : tableColumns) {
      if (tableColumn instanceof ComputedColumn) {
        foundComputeColumn = true;
      } else if (foundComputeColumn) {
        throw new IllegalStateException(
            "compute column must be listed after all physical columns. ");
      }
    }
  }

  /**
   * copy from
   * https://github.com/apache/iceberg/blob/main/flink/v1.16/flink/src/main/java/org/apache/iceberg/flink/FlinkCatalog.java#L425C23-L425C54
   *
   * @param ct1 CatalogTable before
   * @param ct2 CatalogTable after
   */
  private static void validateTableSchemaAndPartition(CatalogTable ct1, CatalogTable ct2) {
    TableSchema ts1 = ct1.getSchema();
    TableSchema ts2 = ct2.getSchema();
    boolean equalsPrimary = false;

    if (ts1.getPrimaryKey().isPresent() && ts2.getPrimaryKey().isPresent()) {
      equalsPrimary =
          Objects.equal(ts1.getPrimaryKey().get().getType(), ts2.getPrimaryKey().get().getType())
              && Objects.equal(
                  ts1.getPrimaryKey().get().getColumns(), ts2.getPrimaryKey().get().getColumns());
    } else if (!ts1.getPrimaryKey().isPresent() && !ts2.getPrimaryKey().isPresent()) {
      equalsPrimary = true;
    }

    if (!(Objects.equal(ts1.getTableColumns(), ts2.getTableColumns())
        && Objects.equal(ts1.getWatermarkSpecs(), ts2.getWatermarkSpecs())
        && equalsPrimary)) {
      throw new UnsupportedOperationException("Altering schema is not supported yet.");
    }

    if (!ct1.getPartitionKeys().equals(ct2.getPartitionKeys())) {
      throw new UnsupportedOperationException("Altering partition keys is not supported yet.");
    }
  }

  private void alterUnKeyedTable(UnkeyedTable table, CatalogBaseTable newTable) {
    Map<String, String> oldProperties = table.properties();
    Map<String, String> setProperties = Maps.newHashMap();

    String setLocation = null;
    String setSnapshotId = null;
    String pickSnapshotId = null;

    for (Map.Entry<String, String> entry : newTable.getOptions().entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if (Objects.equal(value, oldProperties.get(key))) {
        continue;
      }

      if (LOCATION.equalsIgnoreCase(key)) {
        setLocation = value;
      } else if (CURRENT_SNAPSHOT_ID.equalsIgnoreCase(key)) {
        setSnapshotId = value;
      } else if (CHERRY_PICK_SNAPSHOT_ID.equalsIgnoreCase(key)) {
        pickSnapshotId = value;
      } else {
        setProperties.put(key, value);
      }
    }

    oldProperties
        .keySet()
        .forEach(
            k -> {
              if (!newTable.getOptions().containsKey(k)) {
                setProperties.put(k, null);
              }
            });

    FlinkAlterTableUtil.commitChanges(
        table, setLocation, setSnapshotId, pickSnapshotId, setProperties);
  }

  private CatalogTable toCatalogTable(MixedTable table, TableIdentifier tableIdentifier) {
    Schema mixedTableSchema = table.schema();

    Map<String, String> mixedTableProperties = Maps.newHashMap(table.properties());
    fillTableProperties(mixedTableProperties);
    fillTableMetaPropertiesIfLookupLike(mixedTableProperties, tableIdentifier);

    List<String> partitionKeys = toPartitionKeys(table.spec(), table.schema());
    return new CatalogTableImpl(
        toSchema(mixedTableSchema, MixedFormatUtils.getPrimaryKeys(table), mixedTableProperties),
        partitionKeys,
        mixedTableProperties,
        null);
  }

  private void alterKeyedTable(KeyedTable table, CatalogBaseTable newTable) {
    Map<String, String> oldProperties = table.properties();
    Map<String, String> setProperties = Maps.newHashMap();
    for (Map.Entry<String, String> entry : newTable.getOptions().entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (!Objects.equal(value, oldProperties.get(key))) {
        setProperties.put(key, value);
      }
    }
    oldProperties
        .keySet()
        .forEach(
            k -> {
              if (!newTable.getOptions().containsKey(k)) {
                setProperties.put(k, null);
              }
            });
    commitKeyedChanges(table, setProperties);
  }

  private void commitKeyedChanges(KeyedTable table, Map<String, String> setProperties) {
    if (!setProperties.isEmpty()) {
      updateTransactionKey(table.updateProperties(), setProperties);
    }
  }

  private void updateTransactionKey(
      UpdateProperties updateProperties, Map<String, String> setProperties) {
    setProperties.forEach(
        (k, v) -> {
          if (v == null) {
            updateProperties.remove(k);
          } else {
            updateProperties.set(k, v);
          }
        });
    updateProperties.commit();
  }
}
