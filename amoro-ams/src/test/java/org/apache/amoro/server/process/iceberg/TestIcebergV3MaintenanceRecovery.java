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

package org.apache.amoro.server.process.iceberg;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.formats.iceberg.IcebergV3MaintenanceFixture;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableOrphanFilesCleaningMetrics;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.Collections;

class TestIcebergV3MaintenanceRecovery {
  @TempDir private Path temp;

  @ParameterizedTest(name = "recover {0} on V{1}")
  @CsvSource({
    "snapshot,3",
    "dangling,3",
    "data,3",
    "orphan,3",
    "tag,3",
    "snapshot,2",
    "dangling,2",
    "data,2",
    "orphan,2",
    "tag,2"
  })
  void testRecoveredMaintenanceChecksVersion(String action, int version) throws Exception {
    IcebergV3MaintenanceFixture fixture =
        new IcebergV3MaintenanceFixture(temp.resolve(action), version, action);
    DefaultTableRuntime runtime = mock(DefaultTableRuntime.class);
    doReturn(ServerTableIdentifier.of("test_catalog", "test_database", action, TableFormat.ICEBERG))
        .when(runtime)
        .getTableIdentifier();
    doReturn(fixture.configuration).when(runtime).getTableConfiguration();
    doReturn(OptimizingStatus.IDLE).when(runtime).getOptimizingStatus();
    doReturn(mock(TableOrphanFilesCleaningMetrics.class))
        .when(runtime)
        .getOrphanFilesCleaningMetrics();
    doReturn(
            IcebergTable.newIcebergTable(
                fixture.table.id(),
                fixture.table,
                TableMetaStore.builder().withConfiguration(new Configuration()).build(),
                Collections.emptyMap()))
        .when(runtime)
        .loadTable();
    LocalExecutionEngine engine = mock(LocalExecutionEngine.class);
    doReturn(LocalExecutionEngine.ENGINE_NAME).when(engine).name();
    IcebergProcessFactory factory = new IcebergProcessFactory();
    factory.availableExecuteEngines(Collections.singletonList(engine));
    TableProcessStore store = mock(TableProcessStore.class);
    doReturn(action(action)).when(store).getAction();
    LocalProcess recovered = (LocalProcess) factory.recover(runtime, store);
    if (version == 3) {
      assertAll(
          () -> {
            RuntimeException failure = assertThrows(RuntimeException.class, recovered::run);
            Throwable cause = failure;
            while (cause.getCause() != null) {
              cause = cause.getCause();
            }
            assertTrue(cause instanceof IllegalArgumentException);
            assertTrue(cause.getMessage().contains("Unsupported Iceberg format version: 3"));
          },
          fixture::assertPreserved);
    } else {
      recovered.run();
      fixture.assertChanged();
    }
  }

  private Action action(String name) {
    switch (name) {
      case "snapshot":
        return IcebergActions.EXPIRE_SNAPSHOTS;
      case "dangling":
        return IcebergActions.CLEAN_DANGLING_DELETE;
      case "data":
        return IcebergActions.EXPIRE_DATA;
      case "orphan":
        return IcebergActions.CLEAN_ORPHAN;
      case "tag":
        return IcebergActions.AUTO_CREATE_TAGS;
      default:
        throw new IllegalArgumentException(name);
    }
  }
}
