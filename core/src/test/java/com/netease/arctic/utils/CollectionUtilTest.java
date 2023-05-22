/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class CollectionUtilTest {

  @Test
  public void zipTests() {
    List<Integer> a = Lists.newArrayList(1, 2, 3);
    List<Integer> b = Lists.newArrayList(4, 5, 6);
    zipAssert(CollectionUtil.zip(a, b), a, b);

    a = Lists.newArrayList(1, 2);
    b = Lists.newArrayList(4, 5, 6);
    zipAssert(CollectionUtil.zip(a, b), a, b);

    a = Lists.newArrayList(1, 2, 3);
    b = Lists.newArrayList(4, 5);
    zipAssert(CollectionUtil.zip(a, b), a, b);
  }

  private <A, B> void zipAssert(List<Pair<A, B>> zipResult, List<A> inputA, List<B> inputB) {
    Assert.assertEquals(zipResult.size(), Math.min(inputA.size(), inputB.size()));
    for (int i = 0; i < zipResult.size(); i++) {
      Pair<A, B> varZip = zipResult.get(i);
      A varA = inputA.get(i);
      B varB = inputB.get(i);
      Assert.assertEquals(varA, varZip.getLeft());
      Assert.assertEquals(varB, varZip.getRight());
    }
  }


  @Test
  public void asMapTest() {
    Map<String, String> map = CollectionUtil.asMap("k1", "v1", "k2", "v2");
    Assert.assertEquals(2, map.size());
    Assert.assertEquals("v1", map.get("k1"));
    Assert.assertEquals("v2", map.get("k2"));

    Assert.assertThrows(IllegalArgumentException.class, () -> {
      CollectionUtil.asMap("k1", "v1", "k2");
    });
  }
}
