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

import org.apache.amoro.AmoroIOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TestRocksDBBackend {

  public static final String CF_NAME = "TEST";

  @Before
  public void setup() {}

  @Test
  public void testAddAndDropColumnFamily() throws Exception {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    int originalCfCount = rocksDBBackend.listColumnFamilies().size();
    rocksDBBackend.addColumnFamily(CF_NAME);
    Assert.assertEquals(originalCfCount + 1, rocksDBBackend.listColumnFamilies().size());
    rocksDBBackend.dropColumnFamily(CF_NAME);
    Assert.assertEquals(originalCfCount, rocksDBBackend.listColumnFamilies().size());
  }

  @Test
  public void testPutGetDelete() {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    rocksDBBackend.addColumnFamily(CF_NAME);
    rocksDBBackend.put(CF_NAME, "name", "mj");
    rocksDBBackend.put(CF_NAME, 2, "zjs");
    rocksDBBackend.put(CF_NAME, 4556, "zyx");
    Assert.assertEquals("zyx", rocksDBBackend.get(CF_NAME, 4556));
    Assert.assertEquals("zjs", rocksDBBackend.get(CF_NAME, 2));
    Assert.assertEquals("mj", rocksDBBackend.get(CF_NAME, "name"));
    rocksDBBackend.delete(CF_NAME, 4556);
    rocksDBBackend.delete(CF_NAME, "name");
    Assert.assertNull(rocksDBBackend.get(CF_NAME, 4556));
    Assert.assertNull(rocksDBBackend.get(CF_NAME, "name"));
    rocksDBBackend.put(CF_NAME, 2, "mj");
    Assert.assertEquals("mj", rocksDBBackend.get(CF_NAME, 2));
    rocksDBBackend.put(CF_NAME, "name", "mj");
    Assert.assertEquals("mj", rocksDBBackend.get(CF_NAME, "name"));
    rocksDBBackend.dropColumnFamily(CF_NAME);
    try {
      rocksDBBackend.get(CF_NAME, "name");
      Assert.fail();
    } catch (Throwable t) {
      Assert.assertTrue(t instanceof AmoroIOException);
    }
  }

  @Test
  public void testIterator() {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    rocksDBBackend.addColumnFamily(CF_NAME);
    List<String> expect = Arrays.asList("mj", "zjs", "zyx");
    rocksDBBackend.put(CF_NAME, "name", expect.get(0));
    rocksDBBackend.put(CF_NAME, 2, expect.get(1));
    rocksDBBackend.put(CF_NAME, 4556, expect.get(2));
    Iterator<String> values = rocksDBBackend.valuesForTest(CF_NAME);
    List<String> valueList = new ArrayList<>();
    while (values.hasNext()) {
      valueList.add(values.next());
    }
    Collections.sort(expect);
    Collections.sort(valueList);
    Assert.assertEquals(expect.size(), valueList.size());
    Assert.assertArrayEquals(expect.toArray(), valueList.toArray());

    rocksDBBackend.delete(CF_NAME, "name");
    valueList = new ArrayList<>();
    values = rocksDBBackend.valuesForTest(CF_NAME);
    while (values.hasNext()) {
      valueList.add(values.next());
    }
    Assert.assertEquals(2, valueList.size());
    rocksDBBackend.dropColumnFamily(CF_NAME);
  }

  @Test
  public void testClose() {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    File baseFile = new File(rocksDBBackend.getRocksDBBasePath());
    Assert.assertTrue(baseFile.exists());
    Assert.assertTrue(baseFile.isDirectory());
    rocksDBBackend.close();
    Assert.assertFalse(baseFile.exists());
  }
}
