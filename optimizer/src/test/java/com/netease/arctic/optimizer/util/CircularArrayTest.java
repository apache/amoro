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

package com.netease.arctic.optimizer.util;

import org.junit.Assert;
import org.junit.Test;

public class CircularArrayTest {

  @Test
  public void testEmpty() {
    CircularArray<Integer> array = new CircularArray<>(10);
    for (Integer i : array) {
      Assert.fail("should be empty");
    }
    Assert.assertFalse(array.iterator().hasNext());
    Assert.assertEquals(0, array.size());
    testClear(array);
  }

  @Test
  public void test1Element() {
    CircularArray<Integer> array = new CircularArray<>(10);
    array.add(1000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      Assert.assertEquals(1000, i.intValue());
    }
    Assert.assertEquals(1, cnt);
    Assert.assertEquals(1, array.size());
    testClear(array);
  }

  @Test
  public void test2Element() {
    CircularArray<Integer> array = new CircularArray<>(10);
    array.add(1000);
    array.add(2000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      if (cnt == 1) {
        Assert.assertEquals(2000, i.intValue());
      } else {
        Assert.assertEquals(1000, i.intValue());
      }
    }
    Assert.assertEquals(2, cnt);
    Assert.assertEquals(2, array.size());
    testClear(array);
  }

  @Test
  public void test1Full() {
    CircularArray<Integer> array = new CircularArray<>(1);
    array.add(1000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      Assert.assertEquals(1000, i.intValue());
    }
    Assert.assertEquals(1, cnt);
    Assert.assertEquals(1, array.size());
    testClear(array);
  }

  @Test
  public void test1Spill() {
    CircularArray<Integer> array = new CircularArray<>(1);
    array.add(1000);
    array.add(2000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      Assert.assertEquals(2000, i.intValue());
    }
    Assert.assertEquals(1, cnt);
    Assert.assertEquals(1, array.size());
    testClear(array);
  }

  @Test
  public void test1SpillTwice() {
    CircularArray<Integer> array = new CircularArray<>(1);
    array.add(1000);
    array.add(2000);
    array.add(3000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      Assert.assertEquals(3000, i.intValue());
    }
    Assert.assertEquals(1, cnt);
    Assert.assertEquals(1, array.size());
    testClear(array);
  }

  @Test
  public void test2Full() {
    CircularArray<Integer> array = new CircularArray<>(2);
    array.add(1000);
    array.add(2000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      if (cnt == 1) {
        Assert.assertEquals(2000, i.intValue());
      } else {
        Assert.assertEquals(1000, i.intValue());
      }
    }
    Assert.assertEquals(2, cnt);
    Assert.assertEquals(2, array.size());
    testClear(array);
  }

  @Test
  public void test2Spill() {
    CircularArray<Integer> array = new CircularArray<>(2);
    array.add(1000);
    array.add(2000);
    array.add(3000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      if (cnt == 1) {
        Assert.assertEquals(3000, i.intValue());
      } else {
        Assert.assertEquals(2000, i.intValue());
      }
    }
    Assert.assertEquals(2, cnt);
    Assert.assertEquals(2, array.size());
    testClear(array);
  }

  @Test
  public void test2SpillTwice() {
    CircularArray<Integer> array = new CircularArray<>(2);
    array.add(1000);
    array.add(2000);
    array.add(3000);
    array.add(4000);
    array.add(5000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      if (cnt == 1) {
        Assert.assertEquals(5000, i.intValue());
      } else {
        Assert.assertEquals(4000, i.intValue());
      }
    }
    Assert.assertEquals(2, cnt);
    Assert.assertEquals(2, array.size());
    testClear(array);
  }

  @Test
  public void testAfterClear() {
    CircularArray<Integer> array = new CircularArray<>(2);
    array.add(1000);
    array.add(2000);
    array.add(3000);
    array.add(4000);
    array.add(5000);
    int cnt = 0;
    for (Integer i : array) {
      cnt++;
      if (cnt == 1) {
        Assert.assertEquals(5000, i.intValue());
      } else {
        Assert.assertEquals(4000, i.intValue());
      }
    }
    Assert.assertEquals(2, cnt);
    Assert.assertEquals(2, array.size());
    testClear(array);

    array.add(1000);
    array.add(2000);
    array.add(3000);
    array.add(4000);
    array.add(5000);
    int cnt2 = 0;
    for (Integer i : array) {
      cnt2++;
      if (cnt2 == 1) {
        Assert.assertEquals(5000, i.intValue());
      } else {
        Assert.assertEquals(4000, i.intValue());
      }
    }
    Assert.assertEquals(2, cnt2);
    Assert.assertEquals(2, array.size());
    testClear(array);
  }
  
  private void testClear(CircularArray<Integer> array) {
    array.clear();
    for (Integer i : array) {
      Assert.fail("should be empty");
    }
    Assert.assertFalse(array.iterator().hasNext());
    Assert.assertEquals(0, array.size());
  }
}