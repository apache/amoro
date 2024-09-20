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

package org.apache.amoro.mixed;

import org.apache.amoro.TableFormat;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.BasicKeyedTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MixedTables {
  private static final Logger LOG = LoggerFactory.getLogger(MixedTables.class);

  protected TableMetaStore tableMetaStore;
  protected Catalog icebergCatalog;

  protected Map<String, String> catalogProperties;

  public MixedTables(
      TableMetaStore tableMetaStore, Map<String, String> catalogProperties, Catalog catalog) {
    this.tableMetaStore = tableMetaStore;
    this.icebergCatalog = catalog;
    this.catalogProperties = catalogProperties;
  }

  /**
   * check the given iceberg table is a base store for mixed-iceberg.
   *
   * @param table iceberg table
   * @return base store of mixed-iceberg
   */
  public boolean isBaseStore(Table table) {
    return TablePropertyUtil.isBaseStore(table.properties(), TableFormat.MIXED_ICEBERG);
  }

  /**
   * parse change store identifier from base store
   *
   * @param base base store
   * @return change store table identifier.
   */
  public TableIdentifier parseChangeIdentifier(Table base) {
    return TablePropertyUtil.parseChangeIdentifier(base.properties());
  }

  /**
   * create change store table identifier
   *
   * @param baseIdentifier base store table identifier.
   * @return change store table identifier.
   */
  protected TableIdentifier generateChangeStoreIdentifier(TableIdentifier baseIdentifier) {
    String separator =
        catalogProperties.getOrDefault(
            CatalogMetaProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR,
            CatalogMetaProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR_DEFAULT);
    return TableIdentifier.of(
        baseIdentifier.namespace(), baseIdentifier.name() + separator + "change" + separator);
  }

  /**
   * load a mixed-format table.
   *
   * @param base - base store
   * @param tableIdentifier mixed-format table identifier.
   * @return mixed format table instance.
   */
  public MixedTable loadTable(Table base, org.apache.amoro.table.TableIdentifier tableIdentifier) {
    AuthenticatedFileIO io =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(this.tableMetaStore, base.io());
    PrimaryKeySpec keySpec =
        TablePropertyUtil.parsePrimaryKeySpec(base.schema(), base.properties());
    if (!keySpec.primaryKeyExisted()) {
      return new BasicUnkeyedTable(
          tableIdentifier, useMixedTableOperation(base, io), io, catalogProperties);
    }
    Table changeIcebergTable = loadChangeStore(base);
    BaseTable baseStore =
        new BasicKeyedTable.BaseInternalTable(
            tableIdentifier, useMixedTableOperation(base, io), io, catalogProperties);
    ChangeTable changeStore =
        new BasicKeyedTable.ChangeInternalTable(
            tableIdentifier, useMixedTableOperation(changeIcebergTable, io), io, catalogProperties);
    return new BasicKeyedTable(keySpec, baseStore, changeStore);
  }

  /**
   * create a mixed iceberg table
   *
   * @param identifier mixed catalog table identifier.
   * @param schema table schema
   * @param partitionSpec partition spec
   * @param keySpec key spec
   * @param properties table properties
   * @return mixed format table.
   */
  public MixedTable createTable(
      org.apache.amoro.table.TableIdentifier identifier,
      Schema schema,
      PartitionSpec partitionSpec,
      PrimaryKeySpec keySpec,
      Map<String, String> properties) {
    TableIdentifier baseIdentifier =
        TableIdentifier.of(identifier.getDatabase(), identifier.getTableName());
    TableIdentifier changeIdentifier = generateChangeStoreIdentifier(baseIdentifier);

    Table base = createBaseStore(baseIdentifier, schema, partitionSpec, keySpec, properties);
    AuthenticatedFileIO io =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(this.tableMetaStore, base.io());
    if (!keySpec.primaryKeyExisted()) {
      return new BasicUnkeyedTable(
          identifier, useMixedTableOperation(base, io), io, catalogProperties);
    }

    Table change =
        createChangeStore(
            baseIdentifier, changeIdentifier, schema, partitionSpec, keySpec, properties);
    BaseTable baseStore =
        new BasicKeyedTable.BaseInternalTable(
            identifier, useMixedTableOperation(base, io), io, catalogProperties);
    ChangeTable changeStore =
        new BasicKeyedTable.ChangeInternalTable(
            identifier, useMixedTableOperation(change, io), io, catalogProperties);
    return new BasicKeyedTable(keySpec, baseStore, changeStore);
  }

  /**
   * create base store for mixed-format
   *
   * @param baseIdentifier base store identifier
   * @param schema table schema
   * @param partitionSpec partition schema
   * @param keySpec key spec
   * @param properties table properties
   * @return base store iceberg table.
   */
  protected Table createBaseStore(
      TableIdentifier baseIdentifier,
      Schema schema,
      PartitionSpec partitionSpec,
      PrimaryKeySpec keySpec,
      Map<String, String> properties) {
    TableIdentifier changeIdentifier = generateChangeStoreIdentifier(baseIdentifier);
    if (keySpec.primaryKeyExisted() && tableStoreExists(changeIdentifier)) {
      throw new AlreadyExistsException("change store already exists");
    }

    Map<String, String> baseProperties = Maps.newHashMap(properties);
    baseProperties.putAll(
        TablePropertyUtil.baseStoreProperties(
            keySpec, changeIdentifier, TableFormat.MIXED_ICEBERG));
    Catalog.TableBuilder baseBuilder =
        icebergCatalog
            .buildTable(baseIdentifier, schema)
            .withPartitionSpec(partitionSpec)
            .withProperties(baseProperties);
    return baseBuilder.create();
  }

  /**
   * create change store for mixed-format
   *
   * @param baseIdentifier base store identifier
   * @param changeIdentifier change store identifier
   * @param schema table schema
   * @param partitionSpec partition spec
   * @param keySpec key spec
   * @param properties table properties
   * @return change table store.
   */
  protected Table createChangeStore(
      TableIdentifier baseIdentifier,
      TableIdentifier changeIdentifier,
      Schema schema,
      PartitionSpec partitionSpec,
      PrimaryKeySpec keySpec,
      Map<String, String> properties) {
    Map<String, String> changeProperties = Maps.newHashMap(properties);
    changeProperties.putAll(
        TablePropertyUtil.changeStoreProperties(keySpec, TableFormat.MIXED_ICEBERG));
    Catalog.TableBuilder changeBuilder =
        icebergCatalog
            .buildTable(changeIdentifier, schema)
            .withProperties(changeProperties)
            .withPartitionSpec(partitionSpec);
    Table change;
    try {
      change = tableMetaStore.doAs(changeBuilder::create);
      return change;
    } catch (RuntimeException e) {
      LOG.warn("Create base store failed for reason: {}", e.getMessage());
      tableMetaStore.doAs(() -> icebergCatalog.dropTable(baseIdentifier, true));
      throw e;
    }
  }

  /**
   * drop a mixed-format table
   *
   * @param table - mixed table
   * @param purge - purge data when drop table
   * @return - true if table was deleted successfully.
   */
  public boolean dropTable(MixedTable table, boolean purge) {
    UnkeyedTable base =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    boolean deleted =
        dropBaseStore(TableIdentifier.of(base.id().getDatabase(), base.id().getTableName()), purge);
    boolean changeDeleted = false;
    if (table.isKeyedTable()) {
      try {
        changeDeleted = dropChangeStore(parseChangeIdentifier(base.asUnkeyedTable()), purge);
        return deleted && changeDeleted;
      } catch (Exception e) {
        // pass
      }
    }
    return deleted;
  }

  protected boolean dropBaseStore(TableIdentifier tableStoreIdentifier, boolean purge) {
    return tableMetaStore.doAs(() -> icebergCatalog.dropTable(tableStoreIdentifier, purge));
  }

  protected boolean dropChangeStore(TableIdentifier changStoreIdentifier, boolean purge) {
    return tableMetaStore.doAs(() -> icebergCatalog.dropTable(changStoreIdentifier, purge));
  }

  private Table loadChangeStore(Table base) {
    TableIdentifier changeIdentifier = parseChangeIdentifier(base);
    return tableMetaStore.doAs(() -> icebergCatalog.loadTable(changeIdentifier));
  }

  private boolean tableStoreExists(TableIdentifier identifier) {
    return tableMetaStore.doAs(() -> icebergCatalog.tableExists(identifier));
  }

  private Table useMixedTableOperation(Table table, AuthenticatedFileIO io) {
    return MixedFormatCatalogUtil.useMixedTableOperations(
        table, table.location(), io, tableMetaStore.getConfiguration());
  }
}
