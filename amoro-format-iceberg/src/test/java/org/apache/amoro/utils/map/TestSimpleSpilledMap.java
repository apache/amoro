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

import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

public class TestSimpleSpilledMap {

  private SimpleSpillableMap.SimpleSpilledMap map;

  @BeforeEach
  public void createMap() {
    SimpleSpillableMap spillableMap =
        new SimpleSpillableMap(
            100L, null, new StructLikeWrapperSizeEstimator(), new DefaultSizeEstimator<>());
    map =
        spillableMap
        .new SimpleSpilledMap(
            SerializationUtil.createJavaSimpleSerializer(),
            SerializationUtil.createJavaSimpleSerializer(),
            null);
  }

  @AfterEach
  public void disposeMap() {
    map.close();
    map = null;
  }

  @Test
  public void testPutGetRemove() {
    Key key = new Key();
    Value value = new Value();
    map.put("name", 555);
    map.put(2, "zjs");
    map.put(4556, "zyx");
    map.put(key, value);
    Assertions.assertEquals(555, map.get("name"));
    Assertions.assertEquals("zjs", map.get(2));
    Assertions.assertEquals("zyx", map.get(4556));
    Assertions.assertEquals(value, map.get(key));
    map.delete(4556);
    Assertions.assertNull(map.get(4556));
    map.put(4556, value);
    Assertions.assertEquals(value, map.get(4556));
  }

  @Test
  public void testPutNull() {
    Key key = new Key();
    Value value = new Value();
    map.put(key, value);
    Assertions.assertEquals(value, map.get(key));
    Assertions.assertThrows(Exception.class, () -> map.put(key, null));
    Assertions.assertThrows(Exception.class, () -> map.put(null, value));
  }

  private static class Key implements Serializable {
    String key = "Key";

    @Override
    public boolean equals(Object obj) {
      return ((Key) obj).key == key;
    }
  }

  private class Value implements Serializable {
    int value = 666;

    @Override
    public boolean equals(Object obj) {
      return ((Value) obj).value == value;
    }
  }
}
