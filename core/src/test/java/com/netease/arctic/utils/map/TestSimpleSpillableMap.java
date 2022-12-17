package com.netease.arctic.utils.map;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jol.info.GraphLayout;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TestSimpleSpillableMap {

  private static final Random random = new Random(100000);

  private static class Key implements Serializable {
    String id = UUID.randomUUID().toString();

    Long num = random.nextLong();

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
      for (int i = 0; i < values.length; i++)
        values[i] = UUID.randomUUID().toString();
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

  private SimpleSpillableMap<Key, Value> map;

  private long keySize;
  private long valueSize;

  @Before
  public void createMap() {
    keySize = GraphLayout.parseInstance(new Key()).totalSize();
    keySize = GraphLayout.parseInstance(new Value()).totalSize();
  }

  @After
  public void disposeMap() {
    map.close();
    map = null;
  }
  @Test
  public void testMemoryMap() {
    map = new SimpleSpillableMap<>(100 * (keySize + valueSize),
            "TEST");
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() == 0);
    Map<Key, Value> expectedMap = Maps.newHashMap();
    for (int i = 0; i < 100; i++) {
      Key key = new Key();
      Value value = new Value();
      expectedMap.put(key, value);
      map.put(key, value);
    }
    for (Key key : expectedMap.keySet()) {
      Assert.assertEquals(expectedMap.get(key), map.get(key));
    }
    Assert.assertEquals(100, map.getMemoryMapSize());
    Assert.assertEquals(100 *(keySize + valueSize), map.getMemoryMapSpaceSize());
    Assert.assertEquals(0, map.getSizeOfFileOnDiskInBytes());
  }

  @Test
  public void testSpilledMap() {
    map = new SimpleSpillableMap(0L,
            "TEST");
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() == 0);
    Map<Key, Value> expectedMap = Maps.newHashMap();
    for (int i = 0; i < 100; i++) {
      Key key = new Key();
      Value value = new Value();
      expectedMap.put(key, value);
      map.put(key, value);
    }
    for (Key key : expectedMap.keySet()) {
      Assert.assertEquals(expectedMap.get(key), map.get(key));
    }
    Assert.assertEquals(0, map.getMemoryMapSize());
    Assert.assertEquals(0, map.getMemoryMapSpaceSize());
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() > 0);
  }

  @Test
  public void testSpillableMap() {
    map = new SimpleSpillableMap(10 * (keySize + valueSize),
            "TEST");
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() == 0);
    Map<Key, Value> expectedMap = Maps.newHashMap();
    for (int i = 0; i < 100; i++) {
      Key key = new Key();
      Value value = new Value();
      expectedMap.put(key, value);
      map.put(key, value);
    }
    for (Key key : expectedMap.keySet()) {
      Assert.assertEquals(expectedMap.get(key), map.get(key));
    }
    Assert.assertEquals(10, map.getMemoryMapSize());
    Assert.assertEquals(10 * (keySize + valueSize), map.getMemoryMapSpaceSize());
    Assert.assertTrue(map.getSizeOfFileOnDiskInBytes() > 0);
  }
}

