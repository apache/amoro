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

import org.apache.amoro.hive.io.TestHiveTaskReader;
import org.apache.amoro.hive.io.TestHiveTaskWriter;
import org.apache.amoro.io.TestTaskReader;
import org.apache.amoro.io.TestTaskWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/** Runs the shared JUnit 4 I/O fixtures with the Iceberg dependency selected by AMS. */
public class TestMaintenanceIcebergCompatibility {

  @ParameterizedTest
  @ValueSource(
      classes = {
        TestTaskReader.class,
        TestTaskWriter.class,
        TestHiveTaskReader.class,
        TestHiveTaskWriter.class
      })
  public void testSharedIO(Class<?> testClass) {
    Result result = JUnitCore.runClasses(testClass);
    Assertions.assertTrue(result.getRunCount() > 0, "No shared I/O tests were executed");
    Assertions.assertTrue(result.wasSuccessful(), () -> result.getFailures().toString());
  }
}
