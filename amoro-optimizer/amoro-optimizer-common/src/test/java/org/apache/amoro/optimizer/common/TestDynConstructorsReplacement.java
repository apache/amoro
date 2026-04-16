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

package org.apache.amoro.optimizer.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.utils.DynConstructors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

@DisplayName("Test amoro-common DynConstructors as replacement for Iceberg DynConstructors")
public class TestDynConstructorsReplacement {

  @Test
  @DisplayName("Should load OptimizingExecutorFactory by class name")
  void testLoadFactoryByClassName() throws Exception {
    String className = TestOptimizerExecutor.TestOptimizingExecutorFactory.class.getName();
    DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
        DynConstructors.builder(OptimizingExecutorFactory.class).impl(className).buildChecked();
    OptimizingExecutorFactory<?> factory = ctor.newInstance();
    assertNotNull(factory);
  }

  @Test
  @DisplayName("Should throw when loading non-existent class")
  void testLoadNonExistentClassThrows() {
    assertThrows(
        NoSuchMethodException.class,
        () ->
            DynConstructors.builder(OptimizingExecutorFactory.class)
                .impl("com.nonexistent.FakeFactory")
                .buildChecked());
  }

  @Test
  @DisplayName("Should create executor from loaded factory")
  @SuppressWarnings({"rawtypes", "unchecked"})
  void testLoadedFactoryCreateExecutor() throws Exception {
    String className = TestOptimizerExecutor.TestOptimizingExecutorFactory.class.getName();
    DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
        DynConstructors.builder(OptimizingExecutorFactory.class).impl(className).buildChecked();
    OptimizingExecutorFactory factory = ctor.newInstance();
    factory.initialize(new HashMap<>());

    TestOptimizerExecutor.TestOptimizingInput input =
        TestOptimizerExecutor.TestOptimizingInput.successInput(42);
    OptimizingExecutor<?> executor = factory.createExecutor(input);
    assertNotNull(executor);
  }
}
