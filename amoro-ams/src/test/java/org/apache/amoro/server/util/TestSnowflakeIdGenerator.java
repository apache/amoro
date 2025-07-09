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

package org.apache.amoro.server.util;

import org.apache.amoro.server.utils.SnowflakeIdGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSnowflakeIdGenerator {
  private SnowflakeIdGenerator generator;

  private static final long TEST_TIMESTAMP = 1735689600;
  private static final long TEST_MACHINE_ID = 0;
  private static final long TEST_SEQUENCE = 0;
  private static final long TEST_ID =
      (TEST_TIMESTAMP << 22) | (TEST_MACHINE_ID << 12) | TEST_SEQUENCE;
  private static final long TEST_MIN_ID = 7280009832038400L;

  @Before
  public void setUp() {
    generator = new SnowflakeIdGenerator(0);
  }

  @Test
  public void testConstructor_InvalidMachineId_ThrowsException() {
    Assert.assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(1024));
  }

  @Test
  public void testGenerateId() {
    long id1 = generator.generateId();
    long id2 = generator.generateId();
    Assert.assertTrue(id2 > id1);
  }

  @Test
  public void testGetMinSnowflakeId() {
    long minId = SnowflakeIdGenerator.getMinSnowflakeId(TEST_TIMESTAMP);
    Assert.assertEquals(TEST_MIN_ID, minId);
  }

  @Test
  public void testExtractTimestamp() {
    long extractedTimestamp = SnowflakeIdGenerator.extractTimestamp(TEST_ID);
    Assert.assertEquals(TEST_TIMESTAMP, extractedTimestamp);
  }
}
