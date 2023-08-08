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

package com.netease.arctic.spark;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.table.SparkTableBuilders;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.catalyst.analysis.NonEmptyNamespaceException;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.catalog.TableChange;
import org.apache.spark.sql.connector.catalog.TableChange.ColumnChange;
import org.apache.spark.sql.connector.catalog.TableChange.RemoveProperty;
import org.apache.spark.sql.connector.catalog.TableChange.SetProperty;

import java.util.ArrayList;
import java.util.List;

import static com.netease.arctic.spark.utils.SparkCatalogUtil.buildIdentifier;

public class ArcticSparkCatalog extends SparkCatalogBase implements TableCatalog, SupportsNamespaces {

  public ArcticSparkCatalog() {
    super.registerTableBuilder(TableFormat.ICEBERG, SparkTableBuilders.icebergTableBuilderFactory());
    super.registerTableBuilder(TableFormat.MIXED_ICEBERG, SparkTableBuilders.mixedIcebergTableBuilderFactory());
    super.registerTableBuilder(TableFormat.MIXED_HIVE, SparkTableBuilders.mixedHiveTableBuilderFactory());
  }

  @Override
  public Table alterTable(Identifier ident, TableChange... changes) throws NoSuchTableException {
    TableIdentifier identifier = buildIdentifier(registeredCatalogName(), ident);
    ArcticTable table;
    try {
      table = catalog().loadTable(identifier);
    } catch (org.apache.iceberg.exceptions.NoSuchTableException e) {
      throw new NoSuchTableException(ident);
    }
    if (table.isUnkeyedTable()) {
      alterUnKeyedTable(table.asUnkeyedTable(), changes);
    } else if (table.isKeyedTable()) {
      alterKeyedTable(table.asKeyedTable(), changes);
    } else {
      throw new UnsupportedOperationException("Unsupported alter table");
    }
    return toSparkTable(table, null);
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
          throw new UnsupportedOperationException("Cannot specify the 'sort-order' because it's a reserved table " +
              "property. Please use the command 'ALTER TABLE ... WRITE ORDERED BY' to specify write sort-orders.");
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

    commitUnKeyedChanges(table, setLocation, setSnapshotId, pickSnapshotId, propertyChanges, schemaChanges);
  }

  protected void commitUnKeyedChanges(
      UnkeyedTable table, SetProperty setLocation, SetProperty setSnapshotId,
      SetProperty pickSnapshotId, List<TableChange> propertyChanges,
      List<TableChange> schemaChanges
  ) {
    // don't allow setting the snapshot and picking a commit at the same time because order is ambiguous and choosing
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
      transaction.updateLocation()
          .setLocation(setLocation.value())
          .commit();
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
    return super.dropNamespace(namespace);
  }
}
