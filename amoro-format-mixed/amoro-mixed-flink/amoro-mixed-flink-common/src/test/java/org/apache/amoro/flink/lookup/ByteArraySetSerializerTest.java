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

package org.apache.amoro.flink.lookup;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ByteArraySetSerializerTest {
  private static final Logger LOG = LoggerFactory.getLogger(ByteArraySetSerializerTest.class);

  @Test
  public void testByteArraySetSerializer() {
    Set<ByteArrayWrapper> byteSet = new HashSet<>();
    byte[] data = "Hello".getBytes();
    byteSet.add(new ByteArrayWrapper(data, data.length));
    data = "World".getBytes();
    byteSet.add(new ByteArrayWrapper(data, data.length));
    byte[] serialized = ByteArraySetSerializer.serialize(byteSet);
    Set<ByteArrayWrapper> actualSet = ByteArraySetSerializer.deserialize(serialized);
    Assert.assertEquals(byteSet.size(), actualSet.size());
    Assert.assertEquals(byteSet, actualSet);
  }

  @Test
  public void testPerformance() {
    Set<ByteArrayWrapper> byteArraySet = new HashSet<>();
    StringBuilder sb = new StringBuilder();
    int num = 10000;
    long start = System.currentTimeMillis();
    int totalSize = 4;
    for (int i = 0; i < num; i++) {
      sb.append(i);
      byte[] tmp = sb.toString().getBytes();
      byteArraySet.add(new ByteArrayWrapper(tmp, tmp.length));
      totalSize += 4 + tmp.length;
    }
    LOG.info("added {} items process time: {}", num, System.currentTimeMillis() - start);
    Assert.assertEquals(num, byteArraySet.size());

    start = System.currentTimeMillis();
    byte[] serialized = ByteArraySetSerializer.serialize(byteArraySet);
    long cost = System.currentTimeMillis() - start;
    assert serialized != null;
    Assert.assertEquals(totalSize, serialized.length);
    LOG.info(
        "serialized cost: {}, num= {}, result byte array size={}.", cost, num, serialized.length);

    start = System.currentTimeMillis();
    Set<ByteArrayWrapper> actualSet = ByteArraySetSerializer.deserialize(serialized);
    cost = System.currentTimeMillis() - start;
    LOG.info("deserialized cost: {}, num= {}, set size={}.", cost, num, actualSet.size());
    Assert.assertEquals(byteArraySet, actualSet);

    // exists
    sb = new StringBuilder();
    start = System.currentTimeMillis();
    for (int i = 0; i < num; i++) {
      sb.append(i);
      Assert.assertTrue(
          actualSet.contains(
              new ByteArrayWrapper(sb.toString().getBytes(), sb.toString().getBytes().length)));
    }
    long end = System.currentTimeMillis();
    LOG.info("contains process time:{}", end - start);
  }
}
