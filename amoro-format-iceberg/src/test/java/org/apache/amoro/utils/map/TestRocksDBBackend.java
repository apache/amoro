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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TestRocksDBBackend {

  public static final String CF_NAME = "TEST";

  @BeforeEach
  public void setup() {}

  @Test
  public void testAddAndDropColumnFamily() throws Exception {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    int originalCfCount = rocksDBBackend.listColumnFamilies().size();
    rocksDBBackend.addColumnFamily(CF_NAME);
    Assertions.assertEquals(originalCfCount + 1, rocksDBBackend.listColumnFamilies().size());
    rocksDBBackend.dropColumnFamily(CF_NAME);
    Assertions.assertEquals(originalCfCount, rocksDBBackend.listColumnFamilies().size());
  }

  @Test
  public void testPutGetDelete() {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    rocksDBBackend.addColumnFamily(CF_NAME);
    rocksDBBackend.put(CF_NAME, "name", "mj");
    rocksDBBackend.put(CF_NAME, 2, "zjs");
    rocksDBBackend.put(CF_NAME, 4556, "zyx");
    Assertions.assertEquals("zyx", rocksDBBackend.get(CF_NAME, 4556));
    Assertions.assertEquals("zjs", rocksDBBackend.get(CF_NAME, 2));
    Assertions.assertEquals("mj", rocksDBBackend.get(CF_NAME, "name"));
    rocksDBBackend.delete(CF_NAME, 4556);
    rocksDBBackend.delete(CF_NAME, "name");
    Assertions.assertNull(rocksDBBackend.get(CF_NAME, 4556));
    Assertions.assertNull(rocksDBBackend.get(CF_NAME, "name"));
    rocksDBBackend.put(CF_NAME, 2, "mj");
    Assertions.assertEquals("mj", rocksDBBackend.get(CF_NAME, 2));
    rocksDBBackend.put(CF_NAME, "name", "mj");
    Assertions.assertEquals("mj", rocksDBBackend.get(CF_NAME, "name"));
    rocksDBBackend.dropColumnFamily(CF_NAME);
    try {
      rocksDBBackend.get(CF_NAME, "name");
      Assertions.fail();
    } catch (Throwable t) {
      Assertions.assertTrue(t instanceof AmoroIOException);
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
    Assertions.assertEquals(expect.size(), valueList.size());
    Assertions.assertArrayEquals(expect.toArray(), valueList.toArray());

    rocksDBBackend.delete(CF_NAME, "name");
    valueList = new ArrayList<>();
    values = rocksDBBackend.valuesForTest(CF_NAME);
    while (values.hasNext()) {
      valueList.add(values.next());
    }
    Assertions.assertEquals(2, valueList.size());
    rocksDBBackend.dropColumnFamily(CF_NAME);
  }

  @Test
  public void testClose() {
    RocksDBBackend rocksDBBackend = RocksDBBackend.getOrCreateInstance();
    File baseFile = new File(rocksDBBackend.getRocksDBBasePath());
    Assertions.assertTrue(baseFile.exists());
    Assertions.assertTrue(baseFile.isDirectory());
    rocksDBBackend.close();
    Assertions.assertFalse(baseFile.exists());
  }
}
