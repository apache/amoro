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

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class TestOptimizingProcess {
  @Test
  void testCloseWithCommitPreference() {
    OptimizingProcess process = mock(OptimizingProcess.class, CALLS_REAL_METHODS);

    process.close(true);
    verify(process).close(true, null);

    process.close(false);
    verify(process).close(false, null);
  }

  @Test
  void testCloseWithReasonDoesNotCommit() {
    OptimizingProcess process = mock(OptimizingProcess.class, CALLS_REAL_METHODS);

    process.close("Unsupported Iceberg format version: 3");

    verify(process).close(false, "Unsupported Iceberg format version: 3");
  }
}
