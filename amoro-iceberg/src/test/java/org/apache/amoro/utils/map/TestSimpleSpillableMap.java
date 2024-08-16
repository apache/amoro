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

import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.lucene.util.RamUsageEstimator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TestSimpleSpillableMap {

  private static final Random random = new Random(100000);

  private SimpleSpillableMap<Key, Value> map;
  private long keySize;
  private long valueSize;

  @Before
  public void initSizes() {
    keySize = RamUsageEstimator.sizeOfObject(new Key(), 0);
    valueSize = RamUsageEstimator.sizeOfObject(new Value(), 0);
  }

  @Test
  public void testMemoryMap() {
    SimpleSpillableMap<Key, Value> map = testMap(10, 10);
    Assert.assertEquals(0, map.getSizeOfFileOnDiskInBytes());
    map.close();
  }

  @Test
  public void testSpilledMap() {
    SimpleSpillableMap<Key, Value> map = testMap(0, 20);
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() > 0);
    map.close();
  }

  @Test
  public void testSpillableMap() {
    SimpleSpillableMap<Key, Value> map = testMap(10, 20);
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() > 0);
    map.close();
  }

  @Test
  public void testSpillableMapConsistency() {
    SimpleSpillableMap<Key, Value> actualMap =
        new SimpleSpillableMap<>(
            5 * (keySize + valueSize),
            null,
            new DefaultSizeEstimator<>(),
            new DefaultSizeEstimator<>());

    Map<Key, Value> expectedMap = Maps.newHashMap();
    for (int i = 0; i < 10; i++) {
      Key key = new Key();
      Value value = new Value();
      expectedMap.put(key, value);
      actualMap.put(key, value);
    }
    Assert.assertTrue(actualMap.getSizeOfFileOnDiskInBytes() > 0);
    Assert.assertTrue(actualMap.getMemoryMapSize() < expectedMap.size());
    assertSimpleMaps(actualMap, expectedMap);

    // update new value
    Sets.newHashSet(expectedMap.keySet())
        .forEach(
            k -> {
              Value newValue = new Value();
              actualMap.put(k, newValue);
              expectedMap.put(k, newValue);
              assertSimpleMaps(actualMap, expectedMap);
            });

    Sets.newHashSet(expectedMap.keySet())
        .forEach(
            k -> {
              actualMap.delete(k);
              expectedMap.remove(k);
              assertSimpleMaps(actualMap, expectedMap);
            });
  }

  @Test
  public void testSpillableMapRePut() {
    SimpleSpillableMap<Key, Value> actualMap =
        new SimpleSpillableMap<>(
            (keySize + valueSize),
            null,
            new DefaultSizeEstimator<>(),
            new DefaultSizeEstimator<>());

    Key k1 = new Key();
    Value v1 = new Value();
    Key k2 = new Key();
    Value v2 = new Value();

    actualMap.put(k1, v1);
    actualMap.put(k2, v2);

    // remove k1 from memory
    actualMap.delete(k1);
    Assert.assertEquals(v2, actualMap.get(k2));
    // put a new value for k2
    Value v3 = new Value();
    actualMap.put(k2, v3);
    Assert.assertEquals(v3, actualMap.get(k2));

    actualMap.delete(k2);
    // should not exist in memory or on disk
    Assert.assertNull(actualMap.get(k2));
  }

  private SimpleSpillableMap<Key, Value> testMap(long expectMemorySize, int expectKeyCount) {
    SimpleSpillableMap<Key, Value> actualMap =
        new SimpleSpillableMap<>(
            expectMemorySize * (keySize + valueSize),
            null,
            new DefaultSizeEstimator<>(),
            new DefaultSizeEstimator<>());
    Assert.assertEquals(0, actualMap.getSizeOfFileOnDiskInBytes());
    Map<Key, Value> expectedMap = Maps.newHashMap();
    for (int i = 0; i < expectKeyCount; i++) {
      Key key = new Key();
      Value value = new Value();
      expectedMap.put(key, value);
      actualMap.put(key, value);
    }
    assertSimpleMaps(actualMap, expectedMap);
    Assert.assertEquals(expectMemorySize, actualMap.getMemoryMapSize());
    Assert.assertEquals(
        expectMemorySize * (keySize + valueSize), actualMap.getMemoryMapSpaceSize());
    return actualMap;
  }

  private <K, V> void assertSimpleMaps(SimpleMap<K, V> actualMap, Map<K, V> expectedMap) {
    for (K key : expectedMap.keySet()) {
      Assert.assertEquals(expectedMap.get(key), actualMap.get(key));
    }
  }

  private static class Key implements Serializable {
    String id = UUID.randomUUID().toString();

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return id.equals(((Key) obj).id);
    }
  }

  private static class Value implements Serializable {
    Long value = random.nextLong();
    String[] values = new String[10];

    Value() {
      for (int i = 0; i < values.length; i++) {
        values[i] = UUID.randomUUID().toString();
      }
    }

    @Override
    public boolean equals(Object obj) {
      return value == (long) ((Value) obj).value;
    }

    @Override
    public String toString() {
      return Long.toString(value);
    }
  }
}
