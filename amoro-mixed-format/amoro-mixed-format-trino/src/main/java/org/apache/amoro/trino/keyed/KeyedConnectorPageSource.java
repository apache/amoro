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

import static io.trino.plugin.iceberg.IcebergErrorCode.ICEBERG_BAD_DATA;
import static java.util.Objects.requireNonNull;
import static org.apache.amoro.shade.guava32.com.google.common.base.Preconditions.checkArgument;
import static org.apache.amoro.shade.guava32.com.google.common.base.Throwables.throwIfInstanceOf;

import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.table.MetadataColumns;
import org.apache.amoro.trino.delete.TrinoDeleteFile;
import org.apache.amoro.trino.delete.TrinoRow;
import org.apache.amoro.trino.unkeyed.IcebergPageSourceProvider;
import org.apache.amoro.trino.unkeyed.IcebergSplit;
import io.trino.plugin.iceberg.IcebergColumnHandle;
import io.trino.plugin.iceberg.IcebergFileFormat;
import io.trino.spi.Page;
import io.trino.spi.TrinoException;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorPageSource;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.DynamicFilter;
import io.trino.spi.type.Type;
import io.trino.spi.type.TypeManager;
import org.apache.amoro.hive.io.reader.AdaptHiveMixedDeleteFilter;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.joda.time.DateTimeZone;
import org.apache.amoro.trino.AmoroErrorCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.TimeZone;
import java.util.stream.Collectors;

/** ConnectorPageSource for Keyed Table */
public class KeyedConnectorPageSource implements ConnectorPageSource {

  private final IcebergPageSourceProvider icebergPageSourceProvider;
  private final ConnectorTransactionHandle transaction;
  private final ConnectorSession session;
  private final KeyedConnectorSplit split;
  private final KeyedTableHandle table;
  private final List<IcebergColumnHandle> expectedColumns;
  private final List<IcebergColumnHandle> requiredColumns;
  private final DynamicFilter dynamicFilter;
  private final TypeManager typeManager;
  private final AdaptHiveMixedDeleteFilter<TrinoRow> mixedDeleteFilter;

  private final List<ColumnHandle> requireColumnsDummy;
  private final Type[] requireColumnTypes;
  private final int[] expectedColumnIndexes;
  private final Iterator<MixedFileScanTask> dataTasksIt;

  private boolean close;
  long completedPositions;
  long completedBytes;
  long readTimeNanos;

  public KeyedConnectorPageSource(
      List<IcebergColumnHandle> expectedColumns,
      List<IcebergColumnHandle> requiredColumns,
      IcebergPageSourceProvider icebergPageSourceProvider,
      ConnectorTransactionHandle transaction,
      ConnectorSession session,
      KeyedConnectorSplit split,
      KeyedTableHandle table,
      DynamicFilter dynamicFilter,
      TypeManager typeManager,
      AdaptHiveMixedDeleteFilter<TrinoRow> mixedDeleteFilter) {
    this.expectedColumns = expectedColumns;
    this.icebergPageSourceProvider = icebergPageSourceProvider;
    this.transaction = transaction;
    this.session = session;
    this.split = split;
    this.table = table;
    this.requiredColumns = requiredColumns;
    this.dynamicFilter = dynamicFilter;
    this.typeManager = typeManager;
    this.mixedDeleteFilter = mixedDeleteFilter;

    this.requireColumnsDummy =
        requiredColumns.stream().map(ColumnHandle.class::cast).collect(Collectors.toList());
    this.expectedColumnIndexes = new int[expectedColumns.size()];
    for (int i = 0; i < expectedColumns.size(); i++) {
      checkArgument(
          expectedColumns.get(i).equals(requiredColumns.get(i)),
          "Expected columns must be a prefix of required columns");
      expectedColumnIndexes[i] = i;
    }

    this.requireColumnTypes =
        requiredColumns.stream().map(IcebergColumnHandle::getType).toArray(Type[]::new);

    this.dataTasksIt = split.getKeyedTableScanTask().dataTasks().iterator();
  }

  @Override
  public OptionalLong getCompletedPositions() {
    if (current == null) {
      return OptionalLong.empty();
    }
    OptionalLong optionalLong = current.getCompletedPositions();
    if (optionalLong.isPresent()) {
      return OptionalLong.of(completedPositions + optionalLong.getAsLong());
    }
    return OptionalLong.of(completedPositions);
  }

  @Override
  public long getCompletedBytes() {
    if (current != null) {
      return completedBytes + current.getCompletedBytes();
    }
    return completedBytes;
  }

  @Override
  public long getReadTimeNanos() {
    if (current != null) {
      return readTimeNanos + current.getReadTimeNanos();
    }
    return readTimeNanos;
  }

  @Override
  public boolean isFinished() {
    return close;
  }

  private ConnectorPageSource current;

  @Override
  public Page getNextPage() {
    try {
      Page page = getPage();
      if (page == null) {
        close();
        return null;
      }

      if (mixedDeleteFilter != null) {
        int positionCount = page.getPositionCount();
        int[] positionsToKeep = new int[positionCount];
        try (CloseableIterable<TrinoRow> filteredRows =
            mixedDeleteFilter.filter(
                CloseableIterable.withNoopClose(
                    TrinoRow.fromPage(requireColumnTypes, page, positionCount)))) {
          int positionsToKeepCount = 0;
          for (TrinoRow rowToKeep : filteredRows) {
            positionsToKeep[positionsToKeepCount] = rowToKeep.getPosition();
            positionsToKeepCount++;
          }
          page =
              page.getPositions(positionsToKeep, 0, positionsToKeepCount)
                  .getColumns(expectedColumnIndexes);
        } catch (IOException e) {
          throw new TrinoException(
              ICEBERG_BAD_DATA, "Failed to filter rows during merge-on-read operation", e);
        }
      }

      return page;
    } catch (Exception e) {
      closeWithSuppression(e);
      throwIfInstanceOf(e, TrinoException.class);
      throw new TrinoException(AmoroErrorCode.AMORO_BAD_DATA, e);
    }
  }

  @Override
  public long getMemoryUsage() {
    return current == null ? 0 : current.getMemoryUsage();
  }

  @Override
  public void close() throws IOException {
    close = true;
    if (current != null) {
      current.close();
    }
  }

  protected void closeWithSuppression(Throwable throwable) {
    requireNonNull(throwable, "throwable is null");
    try {
      close();
    } catch (Exception e) {
      // Self-suppression not permitted
      if (throwable != e) {
        throwable.addSuppressed(e);
      }
    }
  }

  private Page getPage() throws IOException {
    if (current == null) {
      if (dataTasksIt.hasNext()) {
        current = open(dataTasksIt.next());
      } else {
        return null;
      }
    }

    Page page = null;
    while (page == null) {
      page = current.getNextPage();
      if (page == null) {
        current.close();
        if (dataTasksIt.hasNext()) {
          completedPositions +=
              current.getCompletedPositions().isPresent()
                  ? current.getCompletedPositions().getAsLong()
                  : 0L;
          completedBytes += current.getCompletedBytes();
          readTimeNanos += current.getReadTimeNanos();
          current = open(dataTasksIt.next());
        } else {
          return null;
        }
      }
    }
    return page;
  }

  private ConnectorPageSource open(MixedFileScanTask mixedFileScanTask) {
    PrimaryKeyedFile primaryKeyedFile = mixedFileScanTask.file();
    Map<Integer, Optional<String>> idToConstant = new HashMap<>();
    idToConstant.put(
        MetadataColumns.TRANSACTION_ID_FILED_ID,
        Optional.of(primaryKeyedFile.transactionId().toString()));
    if (primaryKeyedFile.type() == DataFileType.BASE_FILE) {
      idToConstant.put(MetadataColumns.FILE_OFFSET_FILED_ID, Optional.of(Long.MAX_VALUE + ""));
    }

    mixedDeleteFilter.setCurrentDataPath(mixedFileScanTask.file().path().toString());

    return icebergPageSourceProvider.createPageSource(
        transaction,
        session,
        new IcebergSplit(
            primaryKeyedFile.path().toString(),
            0,
            primaryKeyedFile.fileSizeInBytes(),
            primaryKeyedFile.fileSizeInBytes(),
            primaryKeyedFile.recordCount(),
            IcebergFileFormat.fromIceberg(primaryKeyedFile.format()),
            ImmutableList.of(),
            split.getPartitionSpecJson(),
            split.getPartitionDataJson(),
            mixedFileScanTask.deletes().stream()
                .map(TrinoDeleteFile::copyOf)
                .collect(Collectors.toList()),
            null,
            null),
        table.getIcebergTableHandle(),
        requireColumnsDummy,
        dynamicFilter,
        idToConstant,
        false,
        DateTimeZone.forID(TimeZone.getDefault().getID()));
  }
}
