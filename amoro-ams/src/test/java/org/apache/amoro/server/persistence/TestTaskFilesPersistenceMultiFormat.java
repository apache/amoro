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

package org.apache.amoro.server.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Round-trip tests for {@link TaskFilesPersistence} after C4 widened both the persist and load
 * sides to {@link BaseOptimizingInput}.
 *
 * <p>Three scenarios are covered, each exercising the full serialization path (Java serialization +
 * gzip) through a real Derby-backed mapper — if the type signatures regress back to {@code
 * RewriteFilesInput}-only, these tests fail to compile, and if the serializer ever drops Paimon
 * fields, the deserialized input will fail the field-level asserts.
 *
 * <ul>
 *   <li><b>Iceberg</b>: guard that the existing Iceberg path is untouched.
 *   <li><b>Paimon</b>: guard that {@code PaimonCompactionInput} survives round-trip with every
 *       non-transient field (commitUser, taskBytes, serializerVersion, targetSnapshotId,
 *       partitionPath).
 *   <li><b>Mixed</b>: two different {@code processId}s, one Iceberg and one Paimon, asserted to
 *       round-trip independently — which demonstrates that the storage shape does not tag the blob
 *       by format.
 * </ul>
 *
 * <p>The test does <b>not</b> boot the full AMS stack; it only stands up an in-memory Derby and
 * lets {@link TaskFilesPersistence} hit its mapper directly. A light local helper inserts the
 * pre-existing {@code optimizing_process_state} row ({@code updateProcessInputFiles} is an UPDATE,
 * not an UPSERT).
 */
@DisplayName("TaskFilesPersistence multi-format round-trip")
class TestTaskFilesPersistenceMultiFormat {

  private static TemporaryFolder derbyFolder;
  private static final long TABLE_ID = 42L;

  @BeforeAll
  static void setUpDerby() throws Exception {
    derbyFolder = new TemporaryFolder();
    derbyFolder.create();
    String derbyPath = derbyFolder.newFolder("derby").getPath();
    String derbyUrl = String.format("jdbc:derby:%s/derby;create=true", derbyPath);
    Configurations configurations = new Configurations();
    configurations.set(AmoroManagementConf.DB_CONNECTION_URL, derbyUrl);
    configurations.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_DERBY);
    configurations.set(
        AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
    DataSource ds = DataSourceFactory.createDataSource(configurations);
    SqlSessionFactoryProvider.getInstance().init(ds);
    truncateProcessStateRows();
  }

  @AfterAll
  static void tearDownDerby() {
    if (derbyFolder != null) {
      derbyFolder.delete();
    }
  }

  @Test
  @DisplayName("Iceberg RewriteFilesInput round-trips unchanged — regression guard")
  void icebergRoundTrip() {
    long processId = 1001L;
    insertProcessStateRow(processId);

    RewriteFilesInput iceberg = newIcebergInput();
    Map<Integer, BaseOptimizingInput> toPersist = new HashMap<>();
    toPersist.put(0, iceberg);
    TestHelper.persistDirect(processId, toPersist);

    Map<Integer, BaseOptimizingInput> loaded = TaskFilesPersistence.loadTaskInputs(processId);

    assertEquals(1, loaded.size());
    BaseOptimizingInput raw = loaded.get(0);
    assertInstanceOf(
        RewriteFilesInput.class,
        raw,
        "Iceberg descriptor must round-trip as RewriteFilesInput, not the base type");
    RewriteFilesInput restored = (RewriteFilesInput) raw;
    // Empty-array fields survive the round-trip — this is enough to prove the blob is well-formed
    // without dragging a real Iceberg table into the test.
    assertNotNull(restored.rewrittenDataFiles());
    assertEquals(0, restored.rewrittenDataFiles().length);
    assertEquals(0, restored.rePosDeletedDataFiles().length);
    assertEquals(0, restored.readOnlyDeleteFiles().length);
    assertEquals(0, restored.rewrittenDeleteFiles().length);
  }

  @Test
  @DisplayName("Paimon PaimonCompactionInput round-trips with every field intact")
  void paimonRoundTrip() {
    long processId = 2002L;
    insertProcessStateRow(processId);

    byte[] taskBytes = new byte[] {1, 2, 3, 4, 5};
    String commitUser = "paimon-commit-user-uuid";
    int serializerVersion = 3;
    long targetSnapshotId = 9876543210L;
    String partitionPath = "dt=2026-04-18/region=us";

    PaimonCompactionInput paimon =
        new PaimonCompactionInput(
            null, taskBytes, serializerVersion, commitUser, partitionPath, targetSnapshotId);

    Map<Integer, BaseOptimizingInput> toPersist = new HashMap<>();
    toPersist.put(7, paimon);
    TestHelper.persistDirect(processId, toPersist);

    Map<Integer, BaseOptimizingInput> loaded = TaskFilesPersistence.loadTaskInputs(processId);

    assertEquals(1, loaded.size());
    BaseOptimizingInput raw = loaded.get(7);
    assertInstanceOf(
        PaimonCompactionInput.class,
        raw,
        "Paimon descriptor must round-trip as PaimonCompactionInput, not the base type");
    PaimonCompactionInput restored = (PaimonCompactionInput) raw;
    assertArrayEquals(
        taskBytes,
        restored.getTaskBytes(),
        "taskBytes carries the Paimon AppendCompactTask payload — must survive bit-for-bit");
    assertEquals(serializerVersion, restored.getSerializerVersion());
    assertEquals(commitUser, restored.getCommitUser());
    assertEquals(targetSnapshotId, restored.getTargetSnapshotId());
    assertEquals(partitionPath, restored.getPartitionPath());
  }

  @Test
  @DisplayName("Two processIds: Iceberg + Paimon persisted independently load their own type")
  void crossProcessMixedLoad() {
    long icebergProcessId = 3003L;
    long paimonProcessId = 3004L;
    insertProcessStateRow(icebergProcessId);
    insertProcessStateRow(paimonProcessId);

    RewriteFilesInput iceberg = newIcebergInput();
    PaimonCompactionInput paimon =
        new PaimonCompactionInput(
            null, new byte[] {9, 8, 7}, 1, "other-commit-user", "dt=2026-04-18", 1234567890L);

    TestHelper.persistDirect(icebergProcessId, Collections.singletonMap(0, iceberg));
    TestHelper.persistDirect(paimonProcessId, Collections.singletonMap(5, paimon));

    Map<Integer, BaseOptimizingInput> icebergLoaded =
        TaskFilesPersistence.loadTaskInputs(icebergProcessId);
    Map<Integer, BaseOptimizingInput> paimonLoaded =
        TaskFilesPersistence.loadTaskInputs(paimonProcessId);

    assertEquals(1, icebergLoaded.size());
    assertEquals(1, paimonLoaded.size());
    assertInstanceOf(
        RewriteFilesInput.class,
        icebergLoaded.get(0),
        "Iceberg processId must load back as RewriteFilesInput — cross-processId bleed would break recovery");
    assertInstanceOf(
        PaimonCompactionInput.class,
        paimonLoaded.get(5),
        "Paimon processId must load back as PaimonCompactionInput — cross-processId bleed would break recovery");

    PaimonCompactionInput restoredPaimon = (PaimonCompactionInput) paimonLoaded.get(5);
    assertArrayEquals(new byte[] {9, 8, 7}, restoredPaimon.getTaskBytes());
    assertEquals("other-commit-user", restoredPaimon.getCommitUser());
  }

  @Test
  @DisplayName("loadTaskInputs for an unknown processId returns an empty map (not null)")
  void loadAbsentProcessIdReturnsEmpty() {
    // No insert — the row simply does not exist. Selecting returns an empty list which
    // TaskFilesPersistence must collapse to Collections.emptyMap() rather than blowing up.
    Map<Integer, BaseOptimizingInput> loaded = TaskFilesPersistence.loadTaskInputs(9_999_999L);
    assertNotNull(loaded);
    assertTrue(loaded.isEmpty());
  }

  // ---------- Fixtures ---------------------------------------------------------------------------

  private static RewriteFilesInput newIcebergInput() {
    return new RewriteFilesInput(
        new DataFile[0],
        new DataFile[0],
        new ContentFile<?>[0],
        new ContentFile<?>[0],
        /* table= */ null);
  }

  /**
   * {@code updateProcessInputFiles} is an UPDATE; we need a pre-existing row to target. We insert
   * via a direct JDBC statement to keep the fixture independent of the table_process mapper (which
   * belongs to a different concern entirely).
   */
  private static void insertProcessStateRow(long processId) {
    try (Connection connection =
        SqlSessionFactoryProvider.getInstance().get().openSession().getConnection()) {
      try (Statement stmt = connection.createStatement()) {
        stmt.execute(
            "INSERT INTO optimizing_process_state "
                + "(process_id, table_id, target_snapshot_id, target_change_snapshot_id) "
                + "VALUES ("
                + processId
                + ", "
                + TABLE_ID
                + ", 0, 0)");
      }
      connection.commit();
    } catch (Exception e) {
      throw new RuntimeException("insert optimizing_process_state row failed", e);
    }
  }

  private static void truncateProcessStateRows() {
    try (Connection connection =
        SqlSessionFactoryProvider.getInstance().get().openSession().getConnection()) {
      try (Statement stmt = connection.createStatement()) {
        stmt.execute("DELETE FROM optimizing_process_state");
      }
      connection.commit();
    } catch (Exception e) {
      throw new RuntimeException("truncate optimizing_process_state failed", e);
    }
  }

  /**
   * Test-side bridge into {@code TaskFilesPersistence}'s package-private storage layer. We cannot
   * call {@code TaskFilesPersistence.persistTaskInputs(processId, tasks)} directly because that API
   * expects {@code TaskRuntime} wrappers; in this test we care about the serialization layer alone,
   * not the {@code TaskRuntime} lifecycle. So we drive the mapper in the same package.
   */
  private static final class TestHelper extends PersistentBase {

    private static final TestHelper INSTANCE = new TestHelper();

    static void persistDirect(long processId, Map<Integer, BaseOptimizingInput> inputs) {
      INSTANCE.doAs(
          OptimizingProcessMapper.class,
          mapper -> mapper.updateProcessInputFiles(processId, inputs));
    }

    @SuppressWarnings("unused")
    static List<byte[]> selectBytes(long processId) {
      return INSTANCE.getAs(
          OptimizingProcessMapper.class, mapper -> mapper.selectProcessInputFiles(processId));
    }
  }
}
