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

package org.apache.amoro.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestOptimizingConfig {

  @Test
  public void maxFragmentSizeDividesTargetSizeByRatio() {
    OptimizingConfig config = new OptimizingConfig().setTargetSize(128 << 20).setFragmentRatio(8);
    Assertions.assertEquals(16 << 20, config.maxFragmentSize());
  }

  @Test
  public void nonPositiveFragmentRatioIsClampedToOne() {
    OptimizingConfig zero = new OptimizingConfig().setTargetSize(128 << 20).setFragmentRatio(0);
    Assertions.assertEquals(1, zero.getFragmentRatio());
    Assertions.assertEquals(128 << 20, zero.maxFragmentSize());

    OptimizingConfig negative =
        new OptimizingConfig()
            .setTargetSize(128 << 20)
            .setFragmentRatio(-2)
            .setMajorDuplicateRatio(0.5);
    Assertions.assertEquals(1, negative.getFragmentRatio());
    Assertions.assertEquals(128 << 20, negative.maxFragmentSize());
    Assertions.assertEquals(64 << 20, negative.maxDuplicateSize());
  }
}
