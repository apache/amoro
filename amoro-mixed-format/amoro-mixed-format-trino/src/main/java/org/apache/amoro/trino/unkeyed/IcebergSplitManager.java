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

import static io.trino.plugin.iceberg.IcebergSessionProperties.getDynamicFilteringWaitTimeout;
import static io.trino.plugin.iceberg.IcebergSessionProperties.getMinimumAssignedSplitWeight;
import static java.util.Objects.requireNonNull;

import org.apache.amoro.trino.MixedFormatTransactionManager;
import org.apache.amoro.trino.TableNameResolve;
import io.airlift.units.Duration;
import io.trino.filesystem.TrinoFileSystemFactory;
import io.trino.plugin.base.classloader.ClassLoaderSafeConnectorSplitSource;
import io.trino.plugin.iceberg.IcebergTableHandle;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplitManager;
import io.trino.spi.connector.ConnectorSplitSource;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.DynamicFilter;
import io.trino.spi.connector.FixedSplitSource;
import io.trino.spi.type.TypeManager;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableScan;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;

import javax.inject.Inject;

/**
 * Iceberg original IcebergSplitManager has some problems for mixed-format table, such as iceberg version, table
 * type.
 */
public class IcebergSplitManager implements ConnectorSplitManager {

  private final MixedFormatTransactionManager transactionManager;
  private final TypeManager typeManager;
  private final TrinoFileSystemFactory fileSystemFactory;

  @Inject
  public IcebergSplitManager(
      MixedFormatTransactionManager transactionManager,
      TypeManager typeManager,
      TrinoFileSystemFactory fileSystemFactory) {
    this.transactionManager = requireNonNull(transactionManager, "transactionManager is null");
    this.typeManager = requireNonNull(typeManager, "typeManager is null");
    this.fileSystemFactory = requireNonNull(fileSystemFactory, "fileSystemFactory is null");
  }

  @Override
  public ConnectorSplitSource getSplits(
      ConnectorTransactionHandle transaction,
      ConnectorSession session,
      ConnectorTableHandle handle,
      DynamicFilter dynamicFilter,
      Constraint constraint) {
    IcebergTableHandle table = (IcebergTableHandle) handle;

    if (table.getSnapshotId().isEmpty()) {
      if (table.isRecordScannedFiles()) {
        return new FixedSplitSource(ImmutableList.of(), ImmutableList.of());
      }
      return new FixedSplitSource(ImmutableList.of());
    }

    Table icebergTable =
        transactionManager
            .get(transaction)
            .getMixedTable(table.getSchemaTableName())
            .asUnkeyedTable();
    Duration dynamicFilteringWaitTimeout = getDynamicFilteringWaitTimeout(session);

    TableScan tableScan = icebergTable.newScan().useSnapshot(table.getSnapshotId().get());

    TableNameResolve resolve = new TableNameResolve(table.getTableName());
    IcebergSplitSource splitSource =
        new IcebergSplitSource(
            fileSystemFactory,
            session,
            table,
            tableScan,
            table.getMaxScannedFileSize(),
            dynamicFilter,
            dynamicFilteringWaitTimeout,
            constraint,
            typeManager,
            table.isRecordScannedFiles(),
            getMinimumAssignedSplitWeight(session),
            resolve.withSuffix() && !resolve.isBase());

    return new ClassLoaderSafeConnectorSplitSource(
        splitSource, Thread.currentThread().getContextClassLoader());
  }
}
