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

package org.apache.amoro.trino;

import static io.trino.plugin.hive.util.HiveUtil.isHiveSystemSchema;
import static io.trino.spi.StandardErrorCode.NOT_SUPPORTED;

import io.airlift.slice.Slice;
import io.trino.spi.TrinoException;
import io.trino.spi.connector.BeginTableExecuteResult;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ColumnMetadata;
import io.trino.spi.connector.ConnectorAnalyzeMetadata;
import io.trino.spi.connector.ConnectorInsertTableHandle;
import io.trino.spi.connector.ConnectorMergeTableHandle;
import io.trino.spi.connector.ConnectorMetadata;
import io.trino.spi.connector.ConnectorOutputMetadata;
import io.trino.spi.connector.ConnectorOutputTableHandle;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorTableExecuteHandle;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTableLayout;
import io.trino.spi.connector.ConnectorTableMetadata;
import io.trino.spi.connector.ConnectorTableProperties;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.ConstraintApplicationResult;
import io.trino.spi.connector.ProjectionApplicationResult;
import io.trino.spi.connector.RetryMode;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.connector.SchemaTablePrefix;
import io.trino.spi.connector.SystemTable;
import io.trino.spi.connector.TableColumnsMetadata;
import io.trino.spi.expression.ConnectorExpression;
import io.trino.spi.statistics.ComputedStatistics;
import io.trino.spi.statistics.TableStatistics;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterators;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.trino.keyed.KeyedConnectorMetadata;
import org.apache.amoro.trino.keyed.KeyedTableHandle;
import org.apache.amoro.trino.unkeyed.IcebergMetadata;
import org.apache.iceberg.exceptions.NoSuchTableException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * {@link MixedFormatConnectorMetadata} is a Union {@link ConnectorMetadata} contain {@link
 * KeyedConnectorMetadata} and {@link IcebergMetadata}. This is final {@link ConnectorMetadata}
 * provided to Trino
 */
public class MixedFormatConnectorMetadata implements ConnectorMetadata {

  private final KeyedConnectorMetadata keyedConnectorMetadata;

  private final IcebergMetadata icebergMetadata;

  private final MixedFormatCatalog mixedFormatCatalog;

  private final Map<SchemaTableName, MixedTable> tableCache = new ConcurrentHashMap<>();

  public MixedFormatConnectorMetadata(
      KeyedConnectorMetadata keyedConnectorMetadata,
      IcebergMetadata icebergMetadata,
      MixedFormatCatalog mixedFormatCatalog) {
    this.keyedConnectorMetadata = keyedConnectorMetadata;
    this.icebergMetadata = icebergMetadata;
    this.mixedFormatCatalog = mixedFormatCatalog;
  }

  @Override
  public List<String> listSchemaNames(ConnectorSession session) {
    return mixedFormatCatalog.listDatabases().stream()
        .map(s -> s.toLowerCase(Locale.ROOT))
        .collect(Collectors.toList());
  }

  @Override
  public ConnectorTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName) {
    MixedTable mixedTable;
    try {
      mixedTable = getMixedTable(tableName);
    } catch (NoSuchTableException e) {
      return null;
    }
    if (mixedTable.isKeyedTable()) {
      return keyedConnectorMetadata.getTableHandle(session, tableName);
    } else {
      return icebergMetadata.getTableHandle(session, tableName);
    }
  }

  @Override
  public Optional<SystemTable> getSystemTable(ConnectorSession session, SchemaTableName tableName) {
    return icebergMetadata.getSystemTable(session, tableName);
  }

  public ConnectorTableProperties getTableProperties(
      ConnectorSession session, ConnectorTableHandle table) {
    if (table instanceof KeyedTableHandle) {
      return new ConnectorTableProperties();
    } else {
      return icebergMetadata.getTableProperties(session, table);
    }
  }

  @Override
  public ConnectorTableMetadata getTableMetadata(
      ConnectorSession session, ConnectorTableHandle table) {
    if (table instanceof KeyedTableHandle) {
      return keyedConnectorMetadata.getTableMetadata(session, table);
    } else {
      return icebergMetadata.getTableMetadata(session, table);
    }
  }

  @Override
  public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> schemaName) {
    return listNamespaces(session, schemaName).stream()
        .flatMap(s -> mixedFormatCatalog.listTables(s).stream())
        .map(s -> new SchemaTableName(s.getDatabase(), s.getTableName()))
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, ColumnHandle> getColumnHandles(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      return keyedConnectorMetadata.getColumnHandles(session, tableHandle);
    } else {
      return icebergMetadata.getColumnHandles(session, tableHandle);
    }
  }

  @Override
  public ColumnMetadata getColumnMetadata(
      ConnectorSession session, ConnectorTableHandle tableHandle, ColumnHandle columnHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      return keyedConnectorMetadata.getColumnMetadata(session, tableHandle, columnHandle);
    } else {
      return icebergMetadata.getColumnMetadata(session, tableHandle, columnHandle);
    }
  }

  @Override
  public Iterator<TableColumnsMetadata> streamTableColumns(
      ConnectorSession session, SchemaTablePrefix prefix) {
    if (prefix.getTable().isPresent()) {
      MixedTable mixedTable;
      try {
        mixedTable =
            getMixedTable(new SchemaTableName(prefix.getSchema().get(), prefix.getTable().get()));
      } catch (NoSuchTableException e) {
        List<TableColumnsMetadata> schemaTableNames = ImmutableList.of();
        return schemaTableNames.iterator();
      }
      if (mixedTable.isKeyedTable()) {
        return keyedConnectorMetadata.streamTableColumns(session, prefix);
      } else {
        return icebergMetadata.streamTableColumns(session, prefix);
      }
    } else {
      return Iterators.concat(
          keyedConnectorMetadata.streamTableColumns(session, prefix),
          icebergMetadata.streamTableColumns(session, prefix));
    }
  }

  @Override
  public void createTable(
      ConnectorSession session, ConnectorTableMetadata tableMetadata, boolean ignoreExisting) {
    icebergMetadata.createTable(session, tableMetadata, ignoreExisting);
  }

  @Override
  public Optional<ConnectorTableLayout> getNewTableLayout(
      ConnectorSession session, ConnectorTableMetadata tableMetadata) {
    return icebergMetadata.getNewTableLayout(session, tableMetadata);
  }

  @Override
  public ConnectorOutputTableHandle beginCreateTable(
      ConnectorSession session,
      ConnectorTableMetadata tableMetadata,
      Optional<ConnectorTableLayout> layout,
      RetryMode retryMode) {
    return icebergMetadata.beginCreateTable(session, tableMetadata, layout, retryMode);
  }

  @Override
  public Optional<ConnectorOutputMetadata> finishCreateTable(
      ConnectorSession session,
      ConnectorOutputTableHandle tableHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics) {
    return icebergMetadata.finishCreateTable(session, tableHandle, fragments, computedStatistics);
  }

  @Override
  public Optional<ConnectorTableLayout> getInsertLayout(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    return icebergMetadata.getInsertLayout(session, tableHandle);
  }

  @Override
  public ConnectorInsertTableHandle beginInsert(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      List<ColumnHandle> columns,
      RetryMode retryMode) {
    return icebergMetadata.beginInsert(session, tableHandle, columns, retryMode);
  }

  @Override
  public Optional<ConnectorOutputMetadata> finishInsert(
      ConnectorSession session,
      ConnectorInsertTableHandle insertHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics) {
    return icebergMetadata.finishInsert(session, insertHandle, fragments, computedStatistics);
  }

  @Override
  public Optional<ConnectorTableExecuteHandle> getTableHandleForExecute(
      ConnectorSession session,
      ConnectorTableHandle connectorTableHandle,
      String procedureName,
      Map<String, Object> executeProperties,
      RetryMode retryMode) {
    if (connectorTableHandle instanceof KeyedTableHandle) {
      return ConnectorMetadata.super.getTableHandleForExecute(
          session, connectorTableHandle, procedureName, executeProperties, retryMode);
    } else {
      return icebergMetadata.getTableHandleForExecute(
          session, connectorTableHandle, procedureName, executeProperties, retryMode);
    }
  }

  @Override
  public Optional<ConnectorTableLayout> getLayoutForTableExecute(
      ConnectorSession session, ConnectorTableExecuteHandle tableExecuteHandle) {
    return icebergMetadata.getLayoutForTableExecute(session, tableExecuteHandle);
  }

  @Override
  public BeginTableExecuteResult<ConnectorTableExecuteHandle, ConnectorTableHandle>
      beginTableExecute(
          ConnectorSession session,
          ConnectorTableExecuteHandle tableExecuteHandle,
          ConnectorTableHandle updatedSourceTableHandle) {
    return icebergMetadata.beginTableExecute(session, tableExecuteHandle, updatedSourceTableHandle);
  }

  @Override
  public void finishTableExecute(
      ConnectorSession session,
      ConnectorTableExecuteHandle tableExecuteHandle,
      Collection<Slice> fragments,
      List<Object> splitSourceInfo) {
    icebergMetadata.finishTableExecute(session, tableExecuteHandle, fragments, splitSourceInfo);
  }

  @Override
  public void executeTableExecute(
      ConnectorSession session, ConnectorTableExecuteHandle tableExecuteHandle) {
    icebergMetadata.executeTableExecute(session, tableExecuteHandle);
  }

  @Override
  public Optional<Object> getInfo(ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      return ConnectorMetadata.super.getInfo(tableHandle);
    } else {
      return icebergMetadata.getInfo(tableHandle);
    }
  }

  @Override
  public void dropTable(ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "key table UnSupport drop table");
    } else {
      icebergMetadata.dropTable(session, tableHandle);
    }
  }

  @Override
  public void setTableProperties(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      Map<String, Optional<Object>> properties) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "key table UnSupport set table properties");
    } else {
      icebergMetadata.setTableProperties(session, tableHandle, properties);
    }
  }

  @Override
  public void addColumn(
      ConnectorSession session, ConnectorTableHandle tableHandle, ColumnMetadata column) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "key table UnSupport add column");
    } else {
      icebergMetadata.addColumn(session, tableHandle, column);
    }
  }

  @Override
  public void dropColumn(
      ConnectorSession session, ConnectorTableHandle tableHandle, ColumnHandle column) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "key table UnSupport drop column");
    } else {
      icebergMetadata.dropColumn(session, tableHandle, column);
    }
  }

  @Override
  public Optional<ConnectorTableHandle> applyDelete(
      ConnectorSession session, ConnectorTableHandle handle) {
    if (handle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "key table UnSupport apply delete");
    } else {
      return icebergMetadata.applyDelete(session, handle);
    }
  }

  @Override
  public OptionalLong executeDelete(ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "key table UnSupport execute delete");
    } else {
      return icebergMetadata.executeDelete(session, tableHandle);
    }
  }

  @Override
  public Optional<ConstraintApplicationResult<ConnectorTableHandle>> applyFilter(
      ConnectorSession session, ConnectorTableHandle handle, Constraint constraint) {
    if (handle instanceof KeyedTableHandle) {
      return keyedConnectorMetadata.applyFilter(session, handle, constraint);
    } else {
      return icebergMetadata.applyFilter(session, handle, constraint);
    }
  }

  @Override
  public Optional<ProjectionApplicationResult<ConnectorTableHandle>> applyProjection(
      ConnectorSession session,
      ConnectorTableHandle handle,
      List<ConnectorExpression> projections,
      Map<String, ColumnHandle> assignments) {
    if (handle instanceof KeyedTableHandle) {
      return keyedConnectorMetadata.applyProjection(session, handle, projections, assignments);
    } else {
      return icebergMetadata.applyProjection(session, handle, projections, assignments);
    }
  }

  @Override
  public TableStatistics getTableStatistics(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      return keyedConnectorMetadata.getTableStatistics(session, tableHandle);
    } else {
      return icebergMetadata.getTableStatistics(session, tableHandle);
    }
  }

  @Override
  public ConnectorAnalyzeMetadata getStatisticsCollectionMetadata(
      ConnectorSession session,
      ConnectorTableHandle tableHandle,
      Map<String, Object> analyzeProperties) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "This connector does not support analyze");
    } else {
      return icebergMetadata.getStatisticsCollectionMetadata(
          session, tableHandle, analyzeProperties);
    }
  }

  @Override
  public ConnectorTableHandle beginStatisticsCollection(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "This connector does not support analyze");
    } else {
      return icebergMetadata.beginStatisticsCollection(session, tableHandle);
    }
  }

  @Override
  public ColumnHandle getMergeRowIdColumnHandle(
      ConnectorSession session, ConnectorTableHandle tableHandle) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "Key table does not support modifying table rows");
    } else {
      return icebergMetadata.getMergeRowIdColumnHandle(session, tableHandle);
    }
  }

  @Override
  public ConnectorMergeTableHandle beginMerge(
      ConnectorSession session, ConnectorTableHandle tableHandle, RetryMode retryMode) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "Key table does not support beginMerge");
    } else {
      return icebergMetadata.beginMerge(session, tableHandle, retryMode);
    }
  }

  @Override
  public void finishMerge(
      ConnectorSession session,
      ConnectorMergeTableHandle tableHandle,
      Collection<Slice> fragments,
      Collection<ComputedStatistics> computedStatistics) {
    if (tableHandle instanceof KeyedTableHandle) {
      throw new TrinoException(NOT_SUPPORTED, "Key table does not support finishMerge");
    } else {
      icebergMetadata.finishMerge(session, tableHandle, fragments, computedStatistics);
    }
  }

  public void rollback() {
    // TODO: cleanup open transaction
  }

  public MixedTable getMixedTable(SchemaTableName schemaTableName) {
    return tableCache.computeIfAbsent(
        schemaTableName,
        ignore -> mixedFormatCatalog.loadTable(getTableIdentifier(schemaTableName)));
  }

  private TableIdentifier getTableIdentifier(SchemaTableName schemaTableName) {
    return TableIdentifier.of(
        mixedFormatCatalog.name(), schemaTableName.getSchemaName(), schemaTableName.getTableName());
  }

  private List<String> listNamespaces(ConnectorSession session, Optional<String> namespace) {
    if (namespace.isPresent()) {
      if (isHiveSystemSchema(namespace.get())) {
        return ImmutableList.of();
      }
      return ImmutableList.of(namespace.get());
    }
    return listSchemaNames(session);
  }
}
