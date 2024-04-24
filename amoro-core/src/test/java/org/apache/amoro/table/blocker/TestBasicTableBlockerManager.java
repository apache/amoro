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

package org.apache.amoro.table.blocker;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.OperationConflictException;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestBasicTableBlockerManager extends TableTestBase {

  private static final List<BlockableOperation> OPERATIONS = new ArrayList<>();

  static {
    OPERATIONS.add(BlockableOperation.OPTIMIZE);
    OPERATIONS.add(BlockableOperation.BATCH_WRITE);
  }

  public TestBasicTableBlockerManager() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Test
  public void testBlockAndRelease() throws OperationConflictException {
    TableBlockerManager tableBlockerManager =
        getMixedFormatCatalog().getTableBlockerManager(TableTestHelper.TEST_TABLE_ID);
    Assert.assertTrue(tableBlockerManager instanceof BasicTableBlockerManager);
    BasicTableBlockerManager blockerManager = (BasicTableBlockerManager) tableBlockerManager;

    Blocker block = blockerManager.block(OPERATIONS);

    blockerManager.release(block);
  }
}
