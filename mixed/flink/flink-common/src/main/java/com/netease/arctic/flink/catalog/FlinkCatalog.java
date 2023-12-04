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

package com.netease.arctic.flink.catalog;

import static com.netease.arctic.ams.api.Constants.THRIFT_TABLE_SERVICE_NAME;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.TABLE_FORMAT;

import com.netease.arctic.AlreadyExistsException;
import com.netease.arctic.AmoroTable;
import com.netease.arctic.NoSuchDatabaseException;
import com.netease.arctic.NoSuchTableException;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.UnifiedCatalogLoader;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.client.ArcticThriftUrl;
import com.netease.arctic.flink.table.AmoroDynamicTableFactory;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.CatalogDatabase;
import org.apache.flink.table.catalog.CatalogFunction;
import org.apache.flink.table.catalog.CatalogPartition;
import org.apache.flink.table.catalog.CatalogPartitionSpec;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.table.catalog.exceptions.DatabaseAlreadyExistException;
import org.apache.flink.table.catalog.exceptions.DatabaseNotEmptyException;
import org.apache.flink.table.catalog.exceptions.DatabaseNotExistException;
import org.apache.flink.table.catalog.exceptions.FunctionAlreadyExistException;
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
import org.apache.flink.table.factories.Factory;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** This is a Flink catalog wrap a unified catalog. */
public class FlinkCatalog extends AbstractCatalog {
  private UnifiedCatalog unifiedCatalog;
  private final String amsUri;
  private final String amoroCatalogName;
  /**
   * Available Flink catalogs for Unified Catalog.
   *
   * <p>May include: Iceberg, Mixed and Paimon Catalogs, etc.
   */
  private final Map<TableFormat, AbstractCatalog> availableCatalogs;

  public FlinkCatalog(
      String amsUri,
      String name,
      String defaultDatabase,
      Map<TableFormat, AbstractCatalog> availableCatalogs) {
    super(name, defaultDatabase);
    this.amsUri = amsUri;
    this.amoroCatalogName = ArcticThriftUrl.parse(amsUri, THRIFT_TABLE_SERVICE_NAME).catalogName();
    this.availableCatalogs = availableCatalogs;
  }

  @Override
  public void open() throws CatalogException {
    unifiedCatalog =
        UnifiedCatalogLoader.loadUnifiedCatalog(amsUri, amoroCatalogName, Maps.newHashMap());
    availableCatalogs.forEach((tableFormat, catalog) -> catalog.open());
  }

  @Override
  public void close() throws CatalogException {
    if (availableCatalogs != null) {
      availableCatalogs.forEach((tableFormat, catalog) -> catalog.close());
    }
  }

  @Override
  public List<String> listDatabases() throws CatalogException {
    return unifiedCatalog.listDatabases();
  }

  @Override
  public CatalogDatabase getDatabase(String databaseName)
      throws DatabaseNotExistException, CatalogException {
    throw new UnsupportedOperationException("Unsupported operation: get database.");
  }

  @Override
  public boolean databaseExists(String databaseName) throws CatalogException {
    return listDatabases().stream().anyMatch(db -> db.equalsIgnoreCase(databaseName));
  }

  @Override
  public void createDatabase(String name, CatalogDatabase database, boolean ignoreIfExists)
      throws DatabaseAlreadyExistException, CatalogException {
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
      throws DatabaseNotExistException, DatabaseNotEmptyException, CatalogException {
    try {
      unifiedCatalog.dropDatabase(name);
    } catch (NoSuchDatabaseException e) {
      if (!ignoreIfNotExists) {
        throw new DatabaseNotExistException(getName(), name);
      }
    }
  }

  @Override
  public void alterDatabase(String name, CatalogDatabase newDatabase, boolean ignoreIfNotExists)
      throws DatabaseNotExistException, CatalogException {
    throw new UnsupportedOperationException("Unsupported operation: alter database.");
  }

  @Override
  public List<String> listTables(String databaseName)
      throws DatabaseNotExistException, CatalogException {
    return unifiedCatalog.listTables(databaseName).stream()
        .map(table -> table.getIdentifier().getTableName())
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public List<String> listViews(String databaseName)
      throws DatabaseNotExistException, CatalogException {
    return Collections.emptyList();
  }

  @Override
  public CatalogBaseTable getTable(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    AmoroTable<?> amoroTable =
        unifiedCatalog.loadTable(tablePath.getDatabaseName(), tablePath.getObjectName());
    AbstractCatalog catalog = availableCatalogs.get(amoroTable.format());
    if (catalog == null) {
      throw new UnsupportedOperationException(
          String.format(
              "Unsupported operation: get table [%s], %s: %s.",
              tablePath, TABLE_FORMAT.key(), amoroTable.format()));
    }
    CatalogBaseTable catalogBaseTable = catalog.getTable(tablePath);
    catalogBaseTable.getOptions().put(TABLE_FORMAT.key(), amoroTable.format().toString());
    return catalogBaseTable;
  }

  @Override
  public boolean tableExists(ObjectPath tablePath) throws CatalogException {
    try {
      unifiedCatalog.loadTable(tablePath.getDatabaseName(), tablePath.getObjectName());
      return true;
    } catch (NoSuchDatabaseException | NoSuchTableException e) {
      return false;
    }
  }

  @Override
  public void dropTable(ObjectPath tablePath, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException {
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
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: rename table."));
    catalog.renameTable(tablePath, newTableName, ignoreIfNotExists);
  }

  @Override
  public void createTable(ObjectPath tablePath, CatalogBaseTable table, boolean ignoreIfExists)
      throws TableAlreadyExistException, DatabaseNotExistException, CatalogException {
    Configuration configuration = new Configuration();
    table.getOptions().forEach(configuration::setString);
    TableFormat format = configuration.get(TABLE_FORMAT);
    AbstractCatalog catalog = availableCatalogs.get(format);
    if (catalog == null) {
      throw new UnsupportedOperationException(
          String.format(
              "Unsupported operation: create table, %s: %s.", TABLE_FORMAT.key(), format));
    }
    catalog.createTable(tablePath, table, ignoreIfExists);
  }

  @Override
  public void alterTable(ObjectPath tablePath, CatalogBaseTable newTable, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: alter table."));
    catalog.alterTable(tablePath, newTable, ignoreIfNotExists);
  }

  @Override
  public List<CatalogPartitionSpec> listPartitions(ObjectPath tablePath)
      throws TableNotExistException, TableNotPartitionedException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: list partitions."));
    return catalog.listPartitions(tablePath);
  }

  @Override
  public List<CatalogPartitionSpec> listPartitions(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws TableNotExistException, TableNotPartitionedException, PartitionSpecInvalidException,
          CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: list partitions."));
    return catalog.listPartitions(tablePath, partitionSpec);
  }

  @Override
  public List<CatalogPartitionSpec> listPartitionsByFilter(
      ObjectPath tablePath, List<Expression> filters)
      throws TableNotExistException, TableNotPartitionedException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: list partitions by filter."));
    return catalog.listPartitionsByFilter(tablePath, filters);
  }

  @Override
  public CatalogPartition getPartition(ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: get partition."));
    return catalog.getPartition(tablePath, partitionSpec);
  }

  @Override
  public boolean partitionExists(ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws CatalogException {
    return getOriginalCatalog(tablePath)
        .map(catalog -> catalog.partitionExists(tablePath, partitionSpec))
        .orElseThrow(
            () -> new UnsupportedOperationException("Unsupported operation: partition exists."));
  }

  @Override
  public void createPartition(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogPartition partition,
      boolean ignoreIfExists)
      throws TableNotExistException, TableNotPartitionedException, PartitionSpecInvalidException,
          PartitionAlreadyExistsException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException("Unsupported operation: create partition."));
    catalog.createPartition(tablePath, partitionSpec, partition, ignoreIfExists);
  }

  @Override
  public void dropPartition(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec, boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: drop partition."));
    catalog.dropPartition(tablePath, partitionSpec, ignoreIfNotExists);
  }

  @Override
  public void alterPartition(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogPartition newPartition,
      boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported operation: alter partition."));
    catalog.alterPartition(tablePath, partitionSpec, newPartition, ignoreIfNotExists);
  }

  @Override
  public Optional<Factory> getFactory() {
    return Optional.of(new AmoroDynamicTableFactory(availableCatalogs));
  }

  @Override
  public List<String> listFunctions(String dbName)
      throws DatabaseNotExistException, CatalogException {
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
      throws FunctionAlreadyExistException, DatabaseNotExistException, CatalogException {
    throw new UnsupportedOperationException("Unsupported operation: create function.");
  }

  @Override
  public void alterFunction(
      ObjectPath functionPath, CatalogFunction newFunction, boolean ignoreIfNotExists)
      throws FunctionNotExistException, CatalogException {
    throw new UnsupportedOperationException("Unsupported operation: alter function.");
  }

  @Override
  public void dropFunction(ObjectPath functionPath, boolean ignoreIfNotExists)
      throws FunctionNotExistException, CatalogException {
    throw new UnsupportedOperationException("Unsupported operation: drop function.");
  }

  @Override
  public CatalogTableStatistics getTableStatistics(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: get table statistics."));
    return catalog.getTableStatistics(tablePath);
  }

  @Override
  public CatalogColumnStatistics getTableColumnStatistics(ObjectPath tablePath)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: get table column statistics."));
    return catalog.getTableColumnStatistics(tablePath);
  }

  @Override
  public CatalogTableStatistics getPartitionStatistics(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: get partition statistics."));
    return catalog.getPartitionStatistics(tablePath, partitionSpec);
  }

  @Override
  public CatalogColumnStatistics getPartitionColumnStatistics(
      ObjectPath tablePath, CatalogPartitionSpec partitionSpec)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: get partition column statistics."));
    return catalog.getPartitionColumnStatistics(tablePath, partitionSpec);
  }

  @Override
  public void alterTableStatistics(
      ObjectPath tablePath, CatalogTableStatistics tableStatistics, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: alter table statistics."));
    catalog.alterTableStatistics(tablePath, tableStatistics, ignoreIfNotExists);
  }

  @Override
  public void alterTableColumnStatistics(
      ObjectPath tablePath, CatalogColumnStatistics columnStatistics, boolean ignoreIfNotExists)
      throws TableNotExistException, CatalogException, TablePartitionedException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: alter table column statistics."));
    catalog.alterTableColumnStatistics(tablePath, columnStatistics, ignoreIfNotExists);
  }

  @Override
  public void alterPartitionStatistics(
      ObjectPath tablePath,
      CatalogPartitionSpec partitionSpec,
      CatalogTableStatistics partitionStatistics,
      boolean ignoreIfNotExists)
      throws PartitionNotExistException, CatalogException {
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: alter partition statistics."));
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
    AbstractCatalog catalog =
        getOriginalCatalog(tablePath)
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unsupported operation: alter partition column statistics."));
    catalog.alterPartitionColumnStatistics(
        tablePath, partitionSpec, columnStatistics, ignoreIfNotExists);
  }

  private Optional<AbstractCatalog> getOriginalCatalog(ObjectPath tablePath) {
    TableFormat format = getTableFormat(tablePath);
    return Optional.of(availableCatalogs.get(format));
  }

  private TableFormat getTableFormat(ObjectPath tablePath) throws CatalogException {
    AmoroTable<?> amoroTable =
        unifiedCatalog.loadTable(tablePath.getDatabaseName(), tablePath.getObjectName());
    return amoroTable.format();
  }

  @Override
  public String toString() {
    return "FlinkCatalog{"
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
