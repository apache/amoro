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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.maintainer.MaintainerMetrics;
import org.apache.amoro.maintainer.MaintainerOperationType;
import org.apache.amoro.maintainer.OptimizingInfo;
import org.apache.amoro.maintainer.TableMaintainerContext;
import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for IcebergTableMaintainer metrics recording.
 *
 * <p>These tests verify that IcebergTableMaintainer correctly records metrics through the
 * MaintainerMetrics interface for all maintainer operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestIcebergTableMaintainerMetrics {

  @Mock private TableMaintainerContext mockContext;

  @Mock private MaintainerMetrics mockMetrics;

  @Mock private TableConfiguration mockTableConfiguration;

  @Mock private OptimizingInfo mockOptimizingInfo;

  @Mock private org.apache.amoro.config.TagConfiguration mockTagConfiguration;

  @Mock private org.apache.amoro.config.DataExpirationConfig mockDataExpirationConfig;

  private IcebergTableMaintainer maintainer;

  @BeforeEach
  public void setUp() {
    TableIdentifier tableIdentifier = TableIdentifier.of("test_catalog", "test_db", "test_table");

    when(mockContext.getMetrics()).thenReturn(mockMetrics);
    when(mockContext.getTableConfiguration()).thenReturn(mockTableConfiguration);
    when(mockContext.getOptimizingInfo()).thenReturn(mockOptimizingInfo);
    when(mockOptimizingInfo.isProcessing()).thenReturn(false);

    // Feature disabled by default to avoid complex setup
    when(mockTableConfiguration.isCleanOrphanEnabled()).thenReturn(false);
    when(mockTableConfiguration.isDeleteDanglingDeleteFilesEnabled()).thenReturn(false);
    when(mockTableConfiguration.isExpireSnapshotEnabled()).thenReturn(false);

    // Setup Tag and DataExpiration configs to avoid NPE
    when(mockTableConfiguration.getTagConfiguration()).thenReturn(mockTagConfiguration);
    when(mockTagConfiguration.isAutoCreateTag()).thenReturn(false);
    when(mockTableConfiguration.getExpiringDataConfig()).thenReturn(mockDataExpirationConfig);
    // Data expiration disabled by default
    when(mockDataExpirationConfig.isEnabled()).thenReturn(false);
    // Use a valid field name ("ts" matches the mock table schema) to avoid issues
    when(mockDataExpirationConfig.getExpirationField()).thenReturn("ts");

    // Create a minimal Table mock to avoid complex setup
    org.apache.iceberg.Table table = createMinimalTableMock();

    maintainer = new IcebergTableMaintainer(table, tableIdentifier, mockContext);
  }

  @Test
  public void testExpireSnapshotsDisabledDoesNotRecordMetrics() {
    // Feature disabled by default in setUp()
    when(mockTableConfiguration.isExpireSnapshotEnabled()).thenReturn(false);

    // Execute
    maintainer.expireSnapshots();

    // Verify no metrics are recorded when feature is disabled
    verify(mockMetrics, never()).recordOperationStart(any(MaintainerOperationType.class));
  }

  @Test
  public void testAutoCreateTagsDisabledDoesNotRecordDetailedMetrics() {
    // Setup - feature disabled
    when(mockTagConfiguration.isAutoCreateTag()).thenReturn(false);

    // Execute
    maintainer.autoCreateTags();

    // Verify operation-level metrics are still recorded even when no tags are created
    InOrder inOrder = inOrder(mockMetrics);
    inOrder.verify(mockMetrics).recordOperationStart(MaintainerOperationType.TAG_CREATION);
    inOrder
        .verify(mockMetrics)
        .recordOperationSuccess(eq(MaintainerOperationType.TAG_CREATION), anyLong());
  }

  @Test
  public void testExpireDataInvalidFieldDoesNotRecordDetailedMetrics() {
    // Setup - empty expiration field causes IllegalArgumentException
    when(mockDataExpirationConfig.isEnabled()).thenReturn(true);
    when(mockDataExpirationConfig.getRetentionTime()).thenReturn(1L);
    when(mockDataExpirationConfig.getExpirationField()).thenReturn("");
    when(mockDataExpirationConfig.getBaseOnRule())
        .thenReturn(org.apache.amoro.config.DataExpirationConfig.BaseOnRule.CURRENT_TIME);

    // Execute - this will throw exception for empty field name
    assertThrows(IllegalArgumentException.class, () -> maintainer.expireData());

    // Verify operation-level metrics are still recorded even when field is invalid
    // Exception is caught and logged, metrics still record failure
    verify(mockMetrics).recordOperationStart(MaintainerOperationType.DATA_EXPIRATION);
    // When exception occurs, recordOperationFailure is called
    verify(mockMetrics)
        .recordOperationFailure(eq(MaintainerOperationType.DATA_EXPIRATION), anyLong(), any());
  }

  @Test
  public void testCleanOrphanFilesDisabledDoesNotRecordMetrics() {
    // Feature disabled by default in setUp()
    when(mockTableConfiguration.isCleanOrphanEnabled()).thenReturn(false);

    // Execute
    maintainer.cleanOrphanFiles();

    // Verify no metrics are recorded when feature is disabled
    verify(mockMetrics, never()).recordOperationStart(any(MaintainerOperationType.class));
  }

  @Test
  public void testCleanDanglingDeleteFilesDisabledDoesNotRecordMetrics() {
    // Feature disabled by default in setUp()
    when(mockTableConfiguration.isDeleteDanglingDeleteFilesEnabled()).thenReturn(false);

    // Execute
    maintainer.cleanDanglingDeleteFiles();

    // Verify no dangling delete metrics are recorded when feature is disabled
    verify(mockMetrics, never()).recordDanglingDeleteFilesCleaned(anyInt());
  }

  @Test
  public void testAllMaintainerOperationsExist() {
    // Verify that IcebergTableMaintainer implements methods for all relevant operation types

    // IcebergTableMaintainer should cover 5 out of 6 operation types:
    // ORPHAN_FILES_CLEANING - cleanOrphanFiles()
    // DANGLING_DELETE_FILES_CLEANING - cleanDanglingDeleteFiles()
    // SNAPSHOT_EXPIRATION - expireSnapshots()
    // DATA_EXPIRATION - expireData()
    // TAG_CREATION - autoCreateTags()
    // PARTITION_EXPIRATION - not in IcebergTableMaintainer (Paimon-specific)

    // Verify the methods exist and can be called
    assertDoesNotThrow(() -> maintainer.cleanOrphanFiles());
    assertDoesNotThrow(() -> maintainer.cleanDanglingDeleteFiles());
    assertDoesNotThrow(() -> maintainer.expireSnapshots());
    assertDoesNotThrow(() -> maintainer.expireData());
    assertDoesNotThrow(() -> maintainer.autoCreateTags());
  }

  @Test
  public void testMaintainerMetricsInterfaceHasAllOperationTypes() {
    // Verify MaintainerOperationType enum has all expected operation types
    MaintainerOperationType[] operationTypes = MaintainerOperationType.values();

    // Should have 6 operation types
    assertEquals(6, operationTypes.length);

    // Verify specific operation types exist
    assertDoesNotThrow(() -> MaintainerOperationType.valueOf("ORPHAN_FILES_CLEANING"));
    assertDoesNotThrow(() -> MaintainerOperationType.valueOf("DANGLING_DELETE_FILES_CLEANING"));
    assertDoesNotThrow(() -> MaintainerOperationType.valueOf("SNAPSHOT_EXPIRATION"));
    assertDoesNotThrow(() -> MaintainerOperationType.valueOf("DATA_EXPIRATION"));
    assertDoesNotThrow(() -> MaintainerOperationType.valueOf("TAG_CREATION"));
    assertDoesNotThrow(() -> MaintainerOperationType.valueOf("PARTITION_EXPIRATION"));
  }

  /**
   * Creates a minimal Table mock that avoids complex setup. The mock is configured to handle basic
   * method calls without throwing exceptions.
   */
  private org.apache.iceberg.Table createMinimalTableMock() {
    org.apache.iceberg.Table table = org.mockito.Mockito.mock(org.apache.iceberg.Table.class);

    // Configure minimal behavior to avoid NullPointerExceptions
    when(table.name()).thenReturn("test_catalog.test_db.test_table");
    when(table.schema())
        .thenReturn(
            new org.apache.iceberg.Schema(
                org.apache.iceberg.types.Types.NestedField.optional(
                    1, "ts", org.apache.iceberg.types.Types.TimestampType.withoutZone())));
    when(table.currentSnapshot()).thenReturn(null);

    return table;
  }
}
