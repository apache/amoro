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

import static org.apache.amoro.Constants.THRIFT_TABLE_SERVICE_NAME;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.TABLE_FORMAT;

import org.apache.amoro.AlreadyExistsException;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.TableFormat;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.client.AmsThriftUrl;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.catalog.factories.FlinkUnifiedCatalogFactory;
import org.apache.amoro.flink.catalog.factories.iceberg.IcebergFlinkCatalogFactory;
import org.apache.amoro.flink.catalog.factories.mixed.MixedCatalogFactory;
import org.apache.amoro.flink.catalog.factories.paimon.PaimonFlinkCatalogFactory;
import org.apache.amoro.flink.table.AmoroDynamicTableFactory;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.CatalogDatabase;
import org.apache.flink.table.catalog.CatalogFunction;
import org.apache.flink.table.catalog.CatalogPartition;
import org.apache.flink.table.catalog.CatalogPartitionSpec;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.table.catalog.exceptions.DatabaseAlreadyExistException;
import org.apache.flink.table.catalog.exceptions.DatabaseNotExistException;
import org.apache.flink.table.catalog.exceptions.FunctionNotExistException;
import org.apache.flink.table.catalog.exceptions.PartitionAlreadyExistsException;
import org.apache.flink.table.catalog.exceptions.PartitionNotExistException;
import org.apache.flink.table.catalog.exceptions.PartitionSpecInvalidException;
import org.apache.flink.table.catalog.exceptions.TableAlreadyExistException;
import org.apache.flink.table.catalog.exceptions.TableNotExistException;
import org.apache.flink.table.catalog.exceptions.TableNotPartitionedException;
import org.apache.flink.table.catalog.exceptions.TablePartitionedException;
import org.apache.flink.table.catalog.stats.CatalogColumnStatistics;
import org.apache.flink.table.catalog.stats.CatalogTableStatistics;
import org.apache.flink.table.expressions.Expression;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.table.factories.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** This is a Flink catalog wrap a unified catalog. */
public class FlinkUnifiedCatalog extends AbstractCatalog {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkUnifiedCatalog.class);

  private final UnifiedCatalog unifiedCatalog;
  private final String amsUri;
  private final String amoroCatalogName;
  /**
   * Available Flink catalogs for Unified Catalog.
   *
   * <p>May include: Iceberg, Mixed and Paimon Catalogs, etc.
   */
  private Map<TableFormat, AbstractCatalog> availableCatalogs;

  private final CatalogFactory.Context context;
  private final org.apache.hadoop.conf.Configuration hadoopConf;

  public FlinkUnifiedCatalog(
      String amsUri,
      String defaultDatabase,
      UnifiedCatalog unifiedCatalog,
      CatalogFactory.Context context,
      org.apache.hadoop.conf.Configuration hadoopConf) {
    super(context.getName(), defaultDatabase);
    this.amsUri = amsUri;
    this.amoroCatalogName = AmsThriftUrl.parse(amsUri, THRIFT_TABLE_SERVICE_NAME).catalogName();
    this.unifiedCatalog = unifiedCatalog;
    this.context = context;
    this.hadoopConf = hadoopConf;
  }

  @Override
  public void open() throws CatalogException {
    availableCatalogs = Maps.newHashMap();
  }

  @Override
  public void close() throws CatalogException {
    if (availableCatalogs != null) {
      availableCatalogs.forEach((tableFormat, catalog) -> catalog.close());
    }
  }

  @Override
  public List<String> listDatabases() {
    return unifiedCatalog.listDatabases();
  }

  @Override
  public CatalogDatabase getDatabase(String databaseName) {
    throw new UnsupportedOperationException("Unsupported operation: get database.");
  }

  @Override
  public boolean databaseExists(String databaseName) {
    return unifiedCatalog.databaseExists(databaseName);
  }

  @Override
  public void createDatabase(String name, CatalogDatabase database, boolean ignoreIfExists)
      throws DatabaseAlreadyExistException {
    try {
      unifiedCatalog.createDatabase(name);
    } catch (AlreadyExistsException e) {
      if (!ignoreIfExists) {
        throw new DatabaseAlreadyExistException(getName(), name);
      }
    }
  }

  @Override
  public void dropDatabase(String name, boolean ignoreIfNotExists, boolean cascade)
      throws DatabaseNotExistException {
    try {
      unifiedCatalog.dropDatabase(name);
    } catch (NoSuchDatabaseException e) {
      if (!ignoreIfNotExists) {
        throw new DatabaseNotExistException(getName(), name);
      }
    }
  }

  @Override
  public void alterDatabase(String name, CatalogDatabase newDatabase, boolean ignoreIfNotExists) {
    throw new UnsupportedOperationException("Unsupported operation: alter database.");
  }

  @Override
  public List<String> listTables(String databaseName) {
    return unifiedCatalog.listTables(databaseName).stream()
        .map(table -> table.getIdentifier().getTableName())
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public List<String> listViews(String databaseName) {
    return Collections.emptyList();
  }

  @Override
  public CatalogBaseTable getTable(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    AmoroTable<?> amoroTable;
    try {
      amoroTable = unifiedCatalog.loadTable(tablePath.getDatabaseName(), tablePath.getObjectName());
    } catch (NoSuchTableException e) {
      throw new TableNotExistException(getName(), tablePath, e);
    }
    AbstractCatalog catalog = originalCatalog(amoroTable);
    CatalogTable catalogTable = (CatalogTable) catalog.getTable(tablePath);
    final Map<String, String> flinkProperties = Maps.newHashMap(catalogTable.getOptions());
    flinkProperties.put(TABLE_FORMAT.key(), amoroTable.format().toString());

    return CatalogTable.of(
        catalogTable.getUnresolvedSchema(),
        catalogTable.getComment(),
        catalogTable.getPartitionKeys(),
        flinkProperties);
  }

  @Override
  public boolean tableExists(ObjectPath tablePath) {
    try {
      return unifiedCatalog.tableExists(tablePath.getDatabaseName(), tablePath.getObjectName());
    } catch (NoSuchDatabaseException | NoSuchTableException e) {
      return false;
    }
  }

  @Override
  public void dropTable(ObjectPath tablePath, boolean ignoreIfNotExists)
      throws TableNotExistException {
    try {
      unifiedCatalog.dropTable(tablePath.getDatabaseName(), tablePath.getObjectName(), true);
    } catch (NoSuchTableException e) {
      if (!ignoreIfNotExists) {
        throw new TableNotExistException(getName(), tablePath);
      }
    }
  }

  @Override
  public void renameTable(ObjectPath tablePath, String newTableName, boolean ignoreIfNotExists)
      throws TableNotExistException, TableAlreadyExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.renameTable(tablePath, newTableName, ignoreIfNotExists);
  }

  @Override
  public void createTable(ObjectPath tablePath, CatalogBaseTable table, boolean ignoreIfExists)
      throws TableAlreadyExistException, DatabaseNotExistException, CatalogException {
    Configuration configuration = new Configuration();
    table.getOptions().forEach(configuration::setString);
    TableFormat format = configuration.get(TABLE_FORMAT);
    TableIdentifier tableIdentifier =
        TableIdentifier.of(
            unifiedCatalog.name(), tablePath.getDatabaseName(), tablePath.getObjectName());
    String errorMessage =
        String.format(
            "Can't decide table format of table %s, Please specify 'table.format' "
                + "in table properties",
            tableIdentifier);

    Preconditions.checkNotNull(format, errorMessage);
    try {
      unifiedCatalog.loadTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
      if (!ignoreIfExists) {
        throw new TableAlreadyExistException(getName(), tablePath);
      }
      return;
    } catch (NoSuchTableException e) {
      // do nothing
    }

    final TableFormat catalogFormat = format;
    AbstractCatalog catalog =
        getOriginalCatalog(format)
            .orElseGet(() -> createOriginalCatalog(tableIdentifier, catalogFormat));
    catalog.createTable(tablePath, table, ignoreIfExists);
  }

  @Override
  public void alterTable(ObjectPath tablePath, CatalogBaseTable newTable, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.alterTable(tablePath, newTable, ignoreIfNotExists);
  }

  @Override
  public List<CatalogPartitionSpec> listPartitions(ObjectPath tablePath)
      throws TableNotExistException, TableNotPartitionedException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.listPartitions(tablePath);
  }

  @Override
  public List<CatalogPartitionSpec> listPartitions(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws TableNotExistException, TableNotPartitionedException, PartitionSpecInvalidException,
          CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.listPartitions(tablePath, partitionSpec);
  }

  @Override
  public List<CatalogPartitionSpec> listPartitionsByFilter(
      ObjectPath tablePath, List<Expression> filters)
      throws TableNotExistException, TableNotPartitionedException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.listPartitionsByFilter(tablePath, filters);
  }

  @Override
  public CatalogPartition getPartition(ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.getPartition(tablePath, partitionSpec);
  }

  @Override
  public boolean partitionExists(ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.partitionExists(tablePath, partitionSpec);
  }

  @Override
  public void createPartition(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogPartition partition,
      boolean ignoreIfExists)
      throws TableNotExistException, TableNotPartitionedException, PartitionSpecInvalidException,
          PartitionAlreadyExistsException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.createPartition(tablePath, partitionSpec, partition, ignoreIfExists);
  }

  @Override
  public void dropPartition(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec, boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.dropPartition(tablePath, partitionSpec, ignoreIfNotExists);
  }

  @Override
  public void alterPartition(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogPartition newPartition,
      boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.alterPartition(tablePath, partitionSpec, newPartition, ignoreIfNotExists);
  }

  @Override
  public Optional<Factory> getFactory() {
    return Optional.of(new AmoroDynamicTableFactory(availableCatalogs));
  }

  @Override
  public List<String> listFunctions(String dbName) {
    return Collections.emptyList();
  }

  @Override
  public CatalogFunction getFunction(ObjectPath functionPath) throws FunctionNotExistException {
    throw new FunctionNotExistException(getName(), functionPath);
  }

  @Override
  public boolean functionExists(ObjectPath functionPath) {
    return false;
  }

  @Override
  public void createFunction(
      ObjectPath functionPath, CatalogFunction function, boolean ignoreIfExists) {
    throw new UnsupportedOperationException("Unsupported operation: create function.");
  }

  @Override
  public void alterFunction(
      ObjectPath functionPath, CatalogFunction newFunction, boolean ignoreIfNotExists) {
    throw new UnsupportedOperationException("Unsupported operation: alter function.");
  }

  @Override
  public void dropFunction(ObjectPath functionPath, boolean ignoreIfNotExists) {
    throw new UnsupportedOperationException("Unsupported operation: drop function.");
  }

  @Override
  public CatalogTableStatistics getTableStatistics(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.getTableStatistics(tablePath);
  }

  @Override
  public CatalogColumnStatistics getTableColumnStatistics(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.getTableColumnStatistics(tablePath);
  }

  @Override
  public CatalogTableStatistics getPartitionStatistics(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.getPartitionStatistics(tablePath, partitionSpec);
  }

  @Override
  public CatalogColumnStatistics getPartitionColumnStatistics(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    return catalog.getPartitionColumnStatistics(tablePath, partitionSpec);
  }

  @Override
  public void alterTableStatistics(
      ObjectPath tablePath, CatalogTableStatistics tableStatistics, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.alterTableStatistics(tablePath, tableStatistics, ignoreIfNotExists);
  }

  @Override
  public void alterTableColumnStatistics(
      ObjectPath tablePath, CatalogColumnStatistics columnStatistics, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException, TablePartitionedException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.alterTableColumnStatistics(tablePath, columnStatistics, ignoreIfNotExists);
  }

  @Override
  public void alterPartitionStatistics(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogTableStatistics partitionStatistics,
      boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.alterPartitionStatistics(
        tablePath, partitionSpec, partitionStatistics, ignoreIfNotExists);
  }

  @Override
  public void alterPartitionColumnStatistics(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogColumnStatistics columnStatistics,
      boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog = originalCatalog(tablePath);
    catalog.alterPartitionColumnStatistics(
        tablePath, partitionSpec, columnStatistics, ignoreIfNotExists);
  }

  /**
   * Get the original flink catalog for the given table, if the flink catalog is not exists in the
   * cache, would create a new original flink catalog for this table format.
   *
   * @param amoroTable amoroTable
   * @return original Flink catalog
   */
  private AbstractCatalog originalCatalog(AmoroTable<?> amoroTable) {
    TableFormat format = amoroTable.format();
    TableIdentifier tableIdentifier = amoroTable.id();
    return getOriginalCatalog(format)
        .orElseGet(() -> createOriginalCatalog(tableIdentifier, format));
  }

  private AbstractCatalog originalCatalog(ObjectPath tablePath) {
    AmoroTable<?> amoroTable = loadAmoroTable(tablePath);
    return originalCatalog(amoroTable);
  }

  private Optional<AbstractCatalog> getOriginalCatalog(TableFormat format) {
    return Optional.ofNullable(availableCatalogs.get(format));
  }

  private AmoroTable<?> loadAmoroTable(ObjectPath tablePath) {
    return unifiedCatalog.loadTable(tablePath.getDatabaseName(), tablePath.getObjectName());
  }

  private AbstractCatalog createOriginalCatalog(
      TableIdentifier tableIdentifier, TableFormat tableFormat) {
    CatalogFactory catalogFactory;

    switch (tableFormat) {
      case MIXED_ICEBERG:
      case MIXED_HIVE:
        catalogFactory = new MixedCatalogFactory();
        break;
      case ICEBERG:
        catalogFactory = new IcebergFlinkCatalogFactory(hadoopConf);
        break;
      case PAIMON:
        catalogFactory =
            new PaimonFlinkCatalogFactory(
                unifiedCatalog.properties(), unifiedCatalog.metastoreType());
        break;
      default:
        throw new UnsupportedOperationException(
            String.format(
                "Unsupported table format: [%s] in the unified catalog, table identifier is [%s], the supported table formats are [%s].",
                tableFormat, tableIdentifier, FlinkUnifiedCatalogFactory.SUPPORTED_FORMATS));
    }

    AbstractCatalog originalCatalog;
    try {
      context.getOptions().put(CatalogFactoryOptions.FLINK_TABLE_FORMATS.key(), tableFormat.name());
      originalCatalog = (AbstractCatalog) catalogFactory.createCatalog(context);
    } catch (CatalogException e) {
      if (e.getMessage().contains("must implement createCatalog(Context)")) {
        originalCatalog =
            (AbstractCatalog) catalogFactory.createCatalog(context.getName(), context.getOptions());
      } else {
        throw e;
      }
    }
    originalCatalog.open();
    availableCatalogs.put(tableFormat, originalCatalog);
    return originalCatalog;
  }

  @Override
  public String toString() {
    return "FlinkUnifiedCatalog{"
        + "name='"
        + getName()
        + '\''
        + ", defaultDatabase='"
        + getDefaultDatabase()
        + '\''
        + ", amsUri='"
        + amsUri
        + '\''
        + ", amoroCatalogName='"
        + amoroCatalogName
        + '\''
        + ", availableCatalogs size="
        + availableCatalogs.size()
        + "}";
  }
}
