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

package org.apache.amoro.formats.iceberg.maintainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.maintainer.TableMaintainerContext;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.IcebergThreadPools;
import org.apache.iceberg.ExpireSnapshots;
import org.apache.iceberg.Table;
import org.junit.jupiter.api.Test;

class TestIcebergTableMaintainer {

  @Test
  void testExpireSnapshotsUsesMaintenancePool() {
    IcebergThreadPools.initMaintenanceThreadPool(1);

    Table table = mock(Table.class);
    ExpireSnapshots expireSnapshots = mock(ExpireSnapshots.class);
    when(table.name()).thenReturn("test_table");
    when(table.io()).thenReturn(mock(AuthenticatedFileIO.class));
    when(table.expireSnapshots()).thenReturn(expireSnapshots);
    when(expireSnapshots.retainLast(1)).thenReturn(expireSnapshots);
    when(expireSnapshots.expireOlderThan(100L)).thenReturn(expireSnapshots);
    when(expireSnapshots.deleteWith(any())).thenReturn(expireSnapshots);
    when(expireSnapshots.planWith(any())).thenReturn(expireSnapshots);
    when(expireSnapshots.cleanExpiredFiles(true)).thenReturn(expireSnapshots);

    IcebergTableMaintainer tableMaintainer =
        new IcebergTableMaintainer(
            table,
            TableIdentifier.of("test_catalog", "test_database", "test_table"),
            mock(TableMaintainerContext.class));

    tableMaintainer.expireSnapshots(100L, 1);

    verify(expireSnapshots).planWith(IcebergThreadPools.getMaintenanceExecutor());
    verify(expireSnapshots).commit();
  }
}
