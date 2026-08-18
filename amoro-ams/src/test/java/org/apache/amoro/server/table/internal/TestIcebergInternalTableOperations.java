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

package org.apache.amoro.server.table.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class TestIcebergInternalTableOperations {

  @Test
  public void testCommitFailureIsPropagated() {
    FileIO io = mock(FileIO.class);
    CommitFailedException failure = new CommitFailedException("concurrent commit");
    when(io.newOutputFile(anyString())).thenThrow(failure);

    IcebergInternalTableOperations operations = newOperations(io);
    TableMetadata metadata = operations.current();

    CommitFailedException actual =
        assertThrows(CommitFailedException.class, () -> operations.commit(metadata, metadata));

    assertSame(failure, actual.getCause());
    verify(io).deleteFile(anyString());
  }

  @Test
  public void testCleanupFailureDoesNotMaskCommitFailure() {
    FileIO io = mock(FileIO.class);
    CommitFailedException failure = new CommitFailedException("concurrent commit");
    when(io.newOutputFile(anyString())).thenThrow(failure);
    doThrow(new RuntimeException("cleanup failed")).when(io).deleteFile(anyString());

    IcebergInternalTableOperations operations = newOperations(io);
    TableMetadata metadata = operations.current();

    CommitFailedException actual =
        assertThrows(CommitFailedException.class, () -> operations.commit(metadata, metadata));

    assertSame(failure, actual.getCause());
    verify(io).deleteFile(anyString());
  }

  private IcebergInternalTableOperations newOperations(FileIO io) {
    Schema schema = new Schema(Types.NestedField.required(1, "id", Types.LongType.get()));
    TableMetadata metadata =
        TableMetadata.newTableMetadata(
            schema,
            PartitionSpec.unpartitioned(),
            SortOrder.unsorted(),
            "file:/tmp/amoro-test-table",
            Collections.emptyMap());
    IcebergInternalTableOperations operations =
        spy(
            new IcebergInternalTableOperations(
                mock(ServerTableIdentifier.class),
                mock(org.apache.amoro.server.table.TableMetadata.class),
                io));
    doReturn(metadata).when(operations).current();
    return operations;
  }
}
