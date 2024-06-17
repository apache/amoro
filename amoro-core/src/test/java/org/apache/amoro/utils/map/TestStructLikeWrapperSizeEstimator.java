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

package org.apache.amoro.utils.map;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.data.ChangedLsn;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeWrapper;
import org.apache.lucene.util.RamUsageEstimator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TestStructLikeWrapperSizeEstimator {

  @Test
  public void testSizeEstimator() {
    Record record1 = MixedDataTestHelpers.createRecord(1, "name1", 0, "2022-08-30T12:00:00");
    Record record2 = MixedDataTestHelpers.createRecord(2, "test2", 1, "2023-06-29T13:00:00");

    Map<StructLike, ChangedLsn> map = Maps.newHashMap();
    ChangedLsn changedLsn = ChangedLsn.of(1, 2);
    map.put(record1, changedLsn);
    long oldSize = RamUsageEstimator.sizeOfObject(map, 0);
    map.put(record2, changedLsn);
    long newSize = RamUsageEstimator.sizeOfObject(map, 0);
    long record2Size = newSize - oldSize;
    StructLikeWrapper wrapper =
        StructLikeWrapper.forType(BasicTableTestHelper.TABLE_SCHEMA.asStruct()).set(record2);

    // Because the size of map also will increase, so the record2Size should a little bigger than
    // the size of the record
    long estimateSize = new StructLikeWrapperSizeEstimator().sizeEstimate(wrapper);
    Assert.assertEquals(1, record2Size / estimateSize);
  }

  @Test
  public void testSizeEstimatorWithNullField() {
    final Schema schema =
        new Schema(
            Types.NestedField.required(1, "id", Types.IntegerType.get()),
            Types.NestedField.required(2, "name", Types.StringType.get()),
            Types.NestedField.optional(3, "ts", Types.LongType.get()));

    Record record1 = MixedDataTestHelpers.createRecord(schema, 1, "name1", 0);
    // Set the ts field to null
    Record record2 = MixedDataTestHelpers.createRecord(schema, 1, "name1", null);
    Map<StructLike, ChangedLsn> map = Maps.newHashMap();
    ChangedLsn changedLsn = ChangedLsn.of(1, 2);
    map.put(record1, changedLsn);
    long oldSize = RamUsageEstimator.sizeOfObject(map, 0);
    map.put(record2, changedLsn);
    long newSize = RamUsageEstimator.sizeOfObject(map, 0);
    long record2Size = newSize - oldSize;

    StructLikeWrapper wrapper = StructLikeWrapper.forType(schema.asStruct()).set(record2);

    long estimateSize = new StructLikeWrapperSizeEstimator().sizeEstimate(wrapper);
    Assert.assertEquals(1, record2Size / estimateSize);
  }
}
