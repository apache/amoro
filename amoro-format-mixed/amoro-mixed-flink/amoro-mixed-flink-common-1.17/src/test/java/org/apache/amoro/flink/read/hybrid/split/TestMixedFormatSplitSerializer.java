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

package org.apache.amoro.flink.read.hybrid.split;

import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.reader.TestRowDataReaderFunction;
import org.apache.flink.util.FlinkRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestMixedFormatSplitSerializer extends TestRowDataReaderFunction {

  @Test
  public void testSerAndDes() {
    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.planFullTable(testKeyedTable, new AtomicInteger(0));
    assertSerializedSplitEquals(mixedFormatSplits);
  }

  @Test
  public void testSerAndDesMoRSplit() {
    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.mergeOnReadPlan(testKeyedTable, null, new AtomicInteger(0));
    assertSerializedSplitEquals(mixedFormatSplits);
  }

  private void assertSerializedSplitEquals(List<MixedFormatSplit> expected) {
    MixedFormatSplitSerializer serializer = new MixedFormatSplitSerializer();
    List<byte[]> contents =
        expected.stream()
            .map(
                split -> {
                  try {
                    return serializer.serialize(split);
                  } catch (IOException e) {
                    e.printStackTrace();
                    return new byte[0];
                  }
                })
            .collect(Collectors.toList());

    Assert.assertArrayEquals(
        expected.toArray(new MixedFormatSplit[0]),
        contents.stream()
            .map(
                data -> {
                  if (data.length == 0) {
                    throw new FlinkRuntimeException("failed cause data length is 0.");
                  }
                  try {
                    return serializer.deserialize(1, data);
                  } catch (IOException e) {
                    throw new FlinkRuntimeException(e);
                  }
                })
            .toArray(MixedFormatSplit[]::new));
  }

  @Test
  public void testNullableSplit() throws IOException {
    MixedFormatSplitSerializer serializer = new MixedFormatSplitSerializer();
    byte[] ser = serializer.serialize(null);

    MixedFormatSplit actual = serializer.deserialize(1, ser);

    Assert.assertNull(actual);
  }
}
