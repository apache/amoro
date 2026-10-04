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

package org.apache.amoro.formats.iceberg;

import org.apache.amoro.TableFormat;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.exceptions.CleanableFailure;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;

/** Compatibility boundary for automatic changes made by Amoro's bundled Iceberg writer. */
public final class IcebergMaintenanceCompatibility {
  private IcebergMaintenanceCompatibility() {}

  /**
   * Check already loaded metadata without an extra refresh. Iceberg 1.8 can parse V3 but cannot
   * safely write metadata produced by newer V3 writers. Automatic writers must use forUpdate.
   */
  public static void checkSupported(Table table) {
    if (isMixedTable(table)) {
      return;
    }
    if (!(table instanceof HasTableOperations)) {
      throw new UnsupportedTableException(
          "Cannot determine Iceberg format version for table " + table.name());
    }
    checkMetadata(((HasTableOperations) table).operations().current(), table.name());
  }

  /**
   * Return a facade used only by automatic writers. Catalog reads and user table settings remain
   * unchanged. Every storage commit, including transaction retries, checks both base and result.
   */
  public static Table forUpdate(Table table) {
    if (isMixedTable(table)) {
      return table;
    }
    table.refresh();
    checkSupported(table);
    TableOperations operations = ((HasTableOperations) table).operations();
    if (operations instanceof GuardedOperations) {
      return table;
    }
    return new BaseTable(new GuardedOperations(operations, table.name()), table.name());
  }

  private static boolean isMixedTable(Table table) {
    return table instanceof MixedTable && ((MixedTable) table).format() != TableFormat.ICEBERG;
  }

  private static void checkMetadata(TableMetadata metadata, String tableName) {
    if (metadata == null) {
      throw new UnsupportedTableException(
          "Cannot determine Iceberg format version for table " + tableName);
    }
    if (metadata.formatVersion() > 2) {
      throw new UnsupportedTableException(
          "Unsupported Iceberg format version: "
              + metadata.formatVersion()
              + " for table "
              + tableName
              + "; Amoro automatic maintenance supports V1 and V2");
    }
  }

  /** A definite rejection, safe for Iceberg to clean up its uncommitted metadata files. */
  public static class UnsupportedTableException extends IllegalArgumentException
      implements CleanableFailure {
    private UnsupportedTableException(String message) {
      super(message);
    }
  }

  private static class GuardedOperations implements TableOperations {
    private final TableOperations delegate;
    private final String tableName;

    private GuardedOperations(TableOperations delegate, String tableName) {
      this.delegate = delegate;
      this.tableName = tableName;
    }

    @Override
    public TableMetadata current() {
      return delegate.current();
    }

    @Override
    public TableMetadata refresh() {
      return delegate.refresh();
    }

    @Override
    public void commit(TableMetadata base, TableMetadata metadata) {
      checkMetadata(base, tableName);
      checkMetadata(metadata, tableName);
      // Preserve the catalog's atomic conflict detection. A stale V2 commit fails there;
      // if Iceberg retries against V3, the checks above reject it before serialization.
      delegate.commit(base, metadata);
    }

    @Override
    public FileIO io() {
      return delegate.io();
    }

    @Override
    public EncryptionManager encryption() {
      return delegate.encryption();
    }

    @Override
    public String metadataFileLocation(String fileName) {
      return delegate.metadataFileLocation(fileName);
    }

    @Override
    public LocationProvider locationProvider() {
      return delegate.locationProvider();
    }

    @Override
    public TableOperations temp(TableMetadata metadata) {
      return new GuardedOperations(delegate.temp(metadata), tableName);
    }

    @Override
    public long newSnapshotId() {
      return delegate.newSnapshotId();
    }

    @Override
    public boolean requireStrictCleanup() {
      return delegate.requireStrictCleanup();
    }
  }
}
