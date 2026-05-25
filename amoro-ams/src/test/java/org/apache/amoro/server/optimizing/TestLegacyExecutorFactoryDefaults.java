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

package org.apache.amoro.server.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.amoro.TableFormat;
import org.apache.amoro.hive.optimizing.MixedHiveRewriteExecutorFactory;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.MixedIcebergRewriteExecutorFactory;
import org.junit.jupiter.api.Test;

/**
 * Exercises the legacy-row compensation lookup in isolation. The goal of these tests is to pin the
 * format→factory mapping that {@link OptimizingQueue} relies on when restoring pre-0.9 Iceberg task
 * rows whose {@code properties} column pre-dates the multi-format refactor (C3).
 *
 * <p>Kept as pure unit tests (no AMS bootstrap) because {@link LegacyExecutorFactoryDefaults} is a
 * stateless static util — coupling it to a heavier harness would only obscure what the test locks
 * down.
 */
public class TestLegacyExecutorFactoryDefaults {

  @Test
  public void legacyIcebergRowInjectsIcebergFactory() {
    assertEquals(
        IcebergRewriteExecutorFactory.class.getName(),
        LegacyExecutorFactoryDefaults.resolveDefaultExecutorFactoryImpl(TableFormat.ICEBERG));
  }

  @Test
  public void legacyMixedIcebergRowInjectsMixedIcebergFactory() {
    assertEquals(
        MixedIcebergRewriteExecutorFactory.class.getName(),
        LegacyExecutorFactoryDefaults.resolveDefaultExecutorFactoryImpl(TableFormat.MIXED_ICEBERG));
  }

  @Test
  public void legacyMixedHiveRowInjectsMixedHiveFactory() {
    assertEquals(
        MixedHiveRewriteExecutorFactory.class.getName(),
        LegacyExecutorFactoryDefaults.resolveDefaultExecutorFactoryImpl(TableFormat.MIXED_HIVE));
  }

  @Test
  public void paimonFormatReturnsNull() {
    // Paimon tasks always carry their own factory impl in properties (set by the Paimon planner at
    // plan time), so back-filling from format alone would be guesswork — we skip and log.
    assertNull(
        LegacyExecutorFactoryDefaults.resolveDefaultExecutorFactoryImpl(TableFormat.PAIMON),
        "Paimon has no legacy rows to compensate for; caller must skip the task");
  }

  @Test
  public void unknownFormatReturnsNull() {
    // Simulate a future-registered format (or a renamed one) that this util has never been
    // updated for. We must not crash the restore path — just log and skip.
    TableFormat custom = TableFormat.register("TEST_UNKNOWN_FORMAT_FOR_LEGACY_DEFAULTS");
    assertNull(LegacyExecutorFactoryDefaults.resolveDefaultExecutorFactoryImpl(custom));
  }
}
