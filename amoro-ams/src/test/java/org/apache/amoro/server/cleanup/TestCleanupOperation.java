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

package org.apache.amoro.server.cleanup;

import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.junit.Assert;
import org.junit.Test;

public class TestCleanupOperation {
  @Test
  public void testCleanupOperationFromCode() {
    // Test that all cleanup operations can be retrieved by their codes
    Assert.assertEquals(
        CleanupOperation.DANGLING_DELETE_FILES_CLEANING, CleanupOperation.fromCode(11));
    Assert.assertEquals(CleanupOperation.ORPHAN_FILES_CLEANING, CleanupOperation.fromCode(22));
    Assert.assertEquals(CleanupOperation.DATA_EXPIRING, CleanupOperation.fromCode(33));
    Assert.assertEquals(CleanupOperation.SNAPSHOTS_EXPIRING, CleanupOperation.fromCode(44));
    Assert.assertEquals(CleanupOperation.NONE, CleanupOperation.fromCode(-1));

    // Test that unknown codes return NONE
    Assert.assertEquals(CleanupOperation.NONE, CleanupOperation.fromCode(999));
  }
}
