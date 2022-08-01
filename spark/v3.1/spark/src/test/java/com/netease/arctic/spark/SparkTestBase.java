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

package com.netease.arctic.spark;

import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * test base class for normal spark tests.
 *
 * test base class contain no code expect beforeClass && afterClass
 */
public class SparkTestBase extends SparkTestContext {

  @Rule
  public TestName testName = new TestName();
  protected long begin ;

  @Before
  public void testBegin(){
    System.out.println("==================================");
    System.out.println("  Test Begin: " + testName.getMethodName());
    System.out.println("==================================");
    begin = System.currentTimeMillis();
  }

  @After
  public void after() {
    long cost = System.currentTimeMillis() - begin;
    System.out.println("==================================");
    System.out.println("  Test End: " + testName.getMethodName() + ", total cost: " + cost + " ms");
    System.out.println("==================================");
  }

  public void assertDescResult(List<Object[]> rows, List<String> primaryKeys) {
    boolean primaryKeysBlock = false;
    List<String> descPrimaryKeys = Lists.newArrayList();
    for (Object[] row : rows) {
      if (StringUtils.equalsIgnoreCase("# Primary keys", row[0].toString())) {
        primaryKeysBlock = true;
      } else if (StringUtils.startsWith(row[0].toString(), "# ") && primaryKeysBlock) {
        primaryKeysBlock = false;
      } else if (primaryKeysBlock){
        descPrimaryKeys.add(row[0].toString());
      }
    }

    Assert.assertEquals(primaryKeys.size(), descPrimaryKeys.size());
    Assert.assertArrayEquals(primaryKeys.stream().sorted().distinct().toArray(),
        descPrimaryKeys.stream().sorted().distinct().toArray());
  }
}