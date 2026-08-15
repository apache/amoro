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

package org.apache.amoro.optimizing.plan;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class TestCommonPartitionEvaluator {

  private static final long DEFAULT_TARGET_SIZE = 128L << 20;

  @Test
  public void nonPositiveFragmentRatioIsClampedBeforeEvaluation() {
    assertUsesClampedFragmentThreshold(0);
    assertUsesClampedFragmentThreshold(-2);
  }

  private void assertUsesClampedFragmentThreshold(int fragmentRatio) {
    OptimizingConfig config =
        new OptimizingConfig()
            .setEnabled(true)
            .setTargetSize(DEFAULT_TARGET_SIZE)
            .setFragmentRatio(fragmentRatio)
            .setMinTargetSizeRatio(0.75)
            .setMajorDuplicateRatio(0.5)
            .setFullTriggerInterval(-1);
    Assert.assertEquals(1, config.getFragmentRatio());
    Pair<Integer, StructLike> partition = Pair.of(0, null);
    CommonPartitionEvaluator evaluator =
        new CommonPartitionEvaluator(
            ServerTableIdentifier.of(1L, "catalog", "database", "table", TableFormat.ICEBERG),
            config,
            partition,
            System.currentTimeMillis(),
            0L,
            0L,
            0L);

    Assert.assertTrue(
        evaluator.addFile(
            dataFile("fragment-" + fragmentRatio, DEFAULT_TARGET_SIZE), Collections.emptyList()));
    Assert.assertFalse(
        evaluator.addFile(
            dataFile("non-fragment-" + fragmentRatio, DEFAULT_TARGET_SIZE + 1),
            Collections.emptyList()));
    Assert.assertEquals(1, evaluator.getFragmentFileCount());
  }

  private DataFile dataFile(String name, long size) {
    return DataFiles.builder(PartitionSpec.unpartitioned())
        .withPath("/tmp/" + name + ".parquet")
        .withFileSizeInBytes(size)
        .withRecordCount(1)
        .build();
  }
}
