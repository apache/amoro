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

package org.apache.amoro.flink.write;

import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.AUTO_EMIT_LOGSTORE_WATERMARK_GAP;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.LOG_STORE_CATCH_UP;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.table.MixedTable;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.iceberg.UpdateProperties;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class TestAutomaticDoubleWriteStatus extends FlinkTestBase {
  public MixedFormatTableLoader tableLoader;

  public TestAutomaticDoubleWriteStatus() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Test
  public void testTableProperties() {
    tableLoader = MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder);
    tableLoader.open();
    MixedTable mixedTable = tableLoader.loadMixedFormatTable();
    UpdateProperties up = mixedTable.updateProperties();
    up.set(AUTO_EMIT_LOGSTORE_WATERMARK_GAP.key(), "10");
    up.commit();
    AutomaticDoubleWriteStatus status =
        new AutomaticDoubleWriteStatus(tableLoader, Duration.ofSeconds(10));
    status.open();

    Assert.assertFalse(status.isDoubleWrite());
    status.processWatermark(new Watermark(System.currentTimeMillis() - 11 * 1000));
    Assert.assertFalse(status.isDoubleWrite());
    Assert.assertFalse(Boolean.parseBoolean(mixedTable.properties().get(LOG_STORE_CATCH_UP.key())));
    status.processWatermark(new Watermark(System.currentTimeMillis() - 9 * 1000));
    Assert.assertTrue(status.isDoubleWrite());
    Assert.assertTrue(status.isDoubleWrite());

    mixedTable.refresh();
    Assert.assertTrue(Boolean.parseBoolean(mixedTable.properties().get(LOG_STORE_CATCH_UP.key())));
  }
}
