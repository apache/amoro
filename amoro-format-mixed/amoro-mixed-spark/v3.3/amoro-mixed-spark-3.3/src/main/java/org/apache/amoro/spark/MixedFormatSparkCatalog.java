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

package org.apache.amoro.spark;

import static org.apache.amoro.spark.mixed.SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES;
import static org.apache.amoro.spark.mixed.SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT;
import static org.apache.iceberg.spark.SparkSQLProperties.HANDLE_TIMESTAMP_WITHOUT_TIMEZONE;

import org.apache.amoro.hive.utils.CatalogUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.spark.mixed.MixedSparkCatalogBase;
import org.apache.amoro.spark.mixed.MixedTableStoreType;
import org.apache.amoro.spark.table.MixedSparkTable;
import org.apache.amoro.spark.table.SparkChangeTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.catalyst.analysis.NonEmptyNamespaceException;
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.catalog.TableChange;
import org.apache.spark.sql.connector.catalog.TableChange.ColumnChange;
import org.apache.spark.sql.connector.catalog.TableChange.RemoveProperty;
import org.apache.spark.sql.connector.catalog.TableChange.SetProperty;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.types.StructType;
import scala.Option;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MixedFormatSparkCatalog extends MixedSparkCatalogBase {

  @Override
  public Table loadTable(Identifier ident) throws NoSuchTableException {
    checkAndRefreshCatalogMeta();
    TableIdentifier identifier;
    MixedTable table;
    try {
      if (isInnerTableIdentifier(ident)) {
        MixedTableStoreType type = MixedTableStoreType.from(ident.name());
        identifier = buildInnerTableIdentifier(ident);
        table = catalog.loadTable(identifier);
        return loadInnerTable(table, type);
      } else {
        identifier = buildIdentifier(ident);
        table = catalog.loadTable(identifier);
      }
    } catch (org.apache.iceberg.exceptions.NoSuchTableException e) {
      throw new NoSuchTableException(ident);
    }
    return MixedSparkTable.ofMixedTable(table, catalog, name());
  }

  private Table loadInnerTable(MixedTable table, MixedTableStoreType type) {
    if (type != null) {
      switch (type) {
        case CHANGE:
          return new SparkChangeTable(
              (BasicUnkeyedTable) table.asKeyedTable().changeTable(), false);
        default:
          throw new IllegalArgumentException("Unknown inner table type: " + type);
      }
    } else {
      throw new IllegalArgumentException("Table does not exist: " + table);
    }
  }

  @Override
  public Table createTable(
      Identifier ident, StructType schema, Transform[] transforms, Map<String, String> properties)
      throws TableAlreadyExistsException {
    checkAndRefreshCatalogMeta();
    properties = Maps.newHashMap(properties);
    Schema finalSchema = checkAndConvertSchema(schema, properties);
    TableIdentifier identifier = buildIdentifier(ident);
    TableBuilder builder = catalog.newTableBuilder(identifier, finalSchema);
    PartitionSpec spec = Spark3Util.toPartitionSpec(finalSchema, transforms);
    if (properties.containsKey(TableCatalog.PROP_LOCATION)
        && isIdentifierLocation(properties.get(TableCatalog.PROP_LOCATION), ident)) {
      properties.remove(TableCatalog.PROP_LOCATION);
    }
    try {
      if (properties.containsKey("primary.keys")) {
        PrimaryKeySpec primaryKeySpec =
            PrimaryKeySpec.fromDescription(finalSchema, properties.get("primary.keys"));
        properties.remove("primary.keys");
        builder
            .withPartitionSpec(spec)
            .withProperties(properties)
            .withPrimaryKeySpec(primaryKeySpec);
      } else {
        builder.withPartitionSpec(spec).withProperties(properties);
      }
      MixedTable table = builder.create();
      return MixedSparkTable.ofMixedTable(table, catalog, name());
    } catch (AlreadyExistsException e) {
      throw new TableAlreadyExistsException("Table " + ident + " already exists", Option.apply(e));
    }
  }

  private Schema checkAndConvertSchema(StructType schema, Map<String, String> properties) {
    Schema convertSchema;
    boolean useTimestampWithoutZoneInNewTables;
    SparkSession sparkSession = SparkSession.active();
    if (CatalogUtil.isMixedHiveCatalog(catalog)) {
      useTimestampWithoutZoneInNewTables = true;
    } else {
      useTimestampWithoutZoneInNewTables =
          Boolean.parseBoolean(
              sparkSession
                  .conf()
                  .get(
                      USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES,
                      USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT));
    }
    if (useTimestampWithoutZoneInNewTables) {
      sparkSession.conf().set(HANDLE_TIMESTAMP_WITHOUT_TIMEZONE, true);
      convertSchema = SparkSchemaUtil.convert(schema, true);
    } else {
      convertSchema = SparkSchemaUtil.convert(schema, false);
    }

    // schema add primary keys
    if (properties.containsKey("primary.keys")) {
      PrimaryKeySpec primaryKeySpec =
          PrimaryKeySpec.fromDescription(convertSchema, properties.get("primary.keys"));
      List<String> primaryKeys = primaryKeySpec.fieldNames();
      Set<String> pkSet = new HashSet<>(primaryKeys);
      Set<Integer> identifierFieldIds = new HashSet<>();
      List<Types.NestedField> columnsWithPk = new ArrayList<>();
      convertSchema
          .columns()
          .forEach(
              nestedField -> {
                if (pkSet.contains(nestedField.name())) {
                  columnsWithPk.add(nestedField.asRequired());
                  identifierFieldIds.add(nestedField.fieldId());
                } else {
                  columnsWithPk.add(nestedField);
                }
              });
      return new Schema(columnsWithPk, identifierFieldIds);
    }
    return convertSchema;
  }

  @Override
  public Table alterTable(Identifier ident, TableChange... changes) throws NoSuchTableException {
    TableIdentifier identifier = buildIdentifier(ident);
    MixedTable table;
    try {
      table = catalog.loadTable(identifier);
    } catch (org.apache.iceberg.exceptions.NoSuchTableException e) {
      throw new NoSuchTableException(ident);
    }
    if (table.isUnkeyedTable()) {
      alterUnKeyedTable(table.asUnkeyedTable(), changes);
      return MixedSparkTable.ofMixedTable(table, catalog, name());
    } else if (table.isKeyedTable()) {
      alterKeyedTable(table.asKeyedTable(), changes);
      return MixedSparkTable.ofMixedTable(table, catalog, name());
    }
    throw new UnsupportedOperationException("Unsupported alter table");
  }

  private void alterKeyedTable(KeyedTable table, TableChange... changes) {
    List<TableChange> schemaChanges = new ArrayList<>();
    List<TableChange> propertyChanges = new ArrayList<>();
    for (TableChange change : changes) {
      if (change instanceof ColumnChange) {
        schemaChanges.add(change);
      } else if (change instanceof SetProperty) {
        propertyChanges.add(change);
      } else if (change instanceof RemoveProperty) {
        propertyChanges.add(change);
      } else {
        throw new UnsupportedOperationException("Cannot apply unknown table change: " + change);
      }
    }
    commitKeyedChanges(table, schemaChanges, propertyChanges);
  }

  private void commitKeyedChanges(
      KeyedTable table, List<TableChange> schemaChanges, List<TableChange> propertyChanges) {
    if (!schemaChanges.isEmpty()) {
      Spark3Util.applySchemaChanges(table.updateSchema(), schemaChanges).commit();
    }

    if (!propertyChanges.isEmpty()) {
      Spark3Util.applyPropertyChanges(table.updateProperties(), propertyChanges).commit();
    }
  }

  private void alterUnKeyedTable(UnkeyedTable table, TableChange... changes) {
    SetProperty setLocation = null;
    SetProperty setSnapshotId = null;
    SetProperty pickSnapshotId = null;
    List<TableChange> propertyChanges = new ArrayList<>();
    List<TableChange> schemaChanges = new ArrayList<>();

    for (TableChange change : changes) {
      if (change instanceof SetProperty) {
        TableChange.SetProperty set = (TableChange.SetProperty) change;
        if (TableCatalog.PROP_LOCATION.equalsIgnoreCase(set.property())) {
          setLocation = set;
        } else if ("current-snapshot-id".equalsIgnoreCase(set.property())) {
          setSnapshotId = set;
        } else if ("cherry-pick-snapshot-id".equalsIgnoreCase(set.property())) {
          pickSnapshotId = set;
        } else if ("sort-order".equalsIgnoreCase(set.property())) {
          throw new UnsupportedOperationException(
              "Cannot specify the 'sort-order' because it's a reserved table "
                  + "property. Please use the command 'ALTER TABLE ... WRITE ORDERED BY' to specify write sort-orders.");
        } else {
          propertyChanges.add(set);
        }
      } else if (change instanceof RemoveProperty) {
        propertyChanges.add(change);
      } else if (change instanceof ColumnChange) {
        schemaChanges.add(change);
      } else {
        throw new UnsupportedOperationException("Cannot apply unknown table change: " + change);
      }
    }

    commitUnKeyedChanges(
        table, setLocation, setSnapshotId, pickSnapshotId, propertyChanges, schemaChanges);
  }

  protected void commitUnKeyedChanges(
      UnkeyedTable table,
      SetProperty setLocation,
      SetProperty setSnapshotId,
      SetProperty pickSnapshotId,
      List<TableChange> propertyChanges,
      List<TableChange> schemaChanges) {
    // don't allow setting the snapshot and picking a commit at the same time because order is
    // ambiguous and choosing
    // one order leads to different results
    Preconditions.checkArgument(
        setSnapshotId == null || pickSnapshotId == null,
        "Cannot set the current the current snapshot ID and cherry-pick snapshot changes");

    if (setSnapshotId != null) {
      long newSnapshotId = Long.parseLong(setSnapshotId.value());
      table.manageSnapshots().setCurrentSnapshot(newSnapshotId).commit();
    }

    // if updating the table snapshot, perform that update first in case it fails
    if (pickSnapshotId != null) {
      long newSnapshotId = Long.parseLong(pickSnapshotId.value());
      table.manageSnapshots().cherrypick(newSnapshotId).commit();
    }

    Transaction transaction = table.newTransaction();

    if (setLocation != null) {
      transaction.updateLocation().setLocation(setLocation.value()).commit();
    }

    if (!propertyChanges.isEmpty()) {
      Spark3Util.applyPropertyChanges(transaction.updateProperties(), propertyChanges).commit();
    }

    if (!schemaChanges.isEmpty()) {
      Spark3Util.applySchemaChanges(transaction.updateSchema(), schemaChanges).commit();
    }

    transaction.commitTransaction();
  }

  @Override
  public boolean dropNamespace(String[] namespace, boolean cascade)
      throws NoSuchNamespaceException, NonEmptyNamespaceException {
    String database = namespace[0];
    catalog.dropDatabase(database);
    return true;
  }
}
