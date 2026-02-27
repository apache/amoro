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

package org.apache.amoro.trino.keyed;

import static io.trino.plugin.iceberg.ExpressionConverter.toIcebergExpression;

import io.trino.plugin.iceberg.IcebergTableHandle;
import io.trino.plugin.iceberg.PartitionData;
import io.trino.spi.classloader.ThreadContextClassLoader;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplitManager;
import io.trino.spi.connector.ConnectorSplitSource;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.DynamicFilter;
import io.trino.spi.connector.FixedSplitSource;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.connector.TableNotFoundException;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.scan.KeyedTableScan;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.trino.MixedFormatSessionProperties;
import org.apache.amoro.trino.MixedFormatTransactionManager;
import org.apache.amoro.trino.util.MetricUtil;
import org.apache.amoro.trino.util.ObjectSerializerUtil;
import org.apache.iceberg.PartitionSpecParser;
import org.apache.iceberg.io.CloseableIterable;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** ConnectorSplitManager for Keyed Table */
public class KeyedConnectorSplitManager implements ConnectorSplitManager {
  private final MixedFormatTransactionManager mixedFormatTransactionManager;

  @Inject
  public KeyedConnectorSplitManager(MixedFormatTransactionManager mixedFormatTransactionManager) {
    this.mixedFormatTransactionManager = mixedFormatTransactionManager;
  }

  @Override
  public ConnectorSplitSource getSplits(
      ConnectorTransactionHandle transaction,
      ConnectorSession session,
      ConnectorTableHandle handle,
      DynamicFilter dynamicFilter,
      Constraint constraint) {
    KeyedTableHandle keyedTableHandle = (KeyedTableHandle) handle;
    IcebergTableHandle icebergTableHandle = keyedTableHandle.getIcebergTableHandle();
    KeyedTable keyedTable =
        (mixedFormatTransactionManager.get(transaction))
            .getMixedTable(
                new SchemaTableName(
                    icebergTableHandle.getSchemaName(), icebergTableHandle.getTableName()))
            .asKeyedTable();
    if (keyedTable == null) {
      throw new TableNotFoundException(
          new SchemaTableName(
              icebergTableHandle.getSchemaName(), icebergTableHandle.getTableName()));
    }

    KeyedTableScan tableScan =
        keyedTable
            .newScan()
            .filter(
                toIcebergExpression(
                    icebergTableHandle
                        .getEnforcedPredicate()
                        .intersect(icebergTableHandle.getUnenforcedPredicate())));

    if (MixedFormatSessionProperties.enableSplitTaskByDeleteRatio(session)) {
      tableScan.enableSplitTaskByDeleteRatio(
          MixedFormatSessionProperties.splitTaskByDeleteRatio(session));
    }

    ClassLoader pluginClassloader = keyedTable.getClass().getClassLoader();

    try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(pluginClassloader)) {
      // Optimization
      CloseableIterable<CombinedScanTask> combinedScanTasks =
          MetricUtil.duration(tableScan::planTasks, "plan tasks");

      List<KeyedTableScanTask> fileScanTaskList = new ArrayList<>();
      for (CombinedScanTask combinedScanTask : combinedScanTasks) {
        fileScanTaskList.addAll(combinedScanTask.tasks());
      }

      List<KeyedConnectorSplit> keyedConnectorSplits =
          fileScanTaskList.stream()
              .map(
                  s -> {
                    MixedFileScanTask mixedFileScanTask = s.dataTasks().get(0);
                    KeyedConnectorSplit keyedConnectorSplit =
                        new KeyedConnectorSplit(
                            ObjectSerializerUtil.write(s),
                            PartitionSpecParser.toJson(mixedFileScanTask.spec()),
                            PartitionData.toJson(mixedFileScanTask.file().partition()));
                    return keyedConnectorSplit;
                  })
              .collect(Collectors.toList());

      return new FixedSplitSource(keyedConnectorSplits);
    }
  }
}
