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

package com.netease.arctic.optimizer.common;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.util.UUID;

public class TestOptimizerConfig {

  @Test
  public void testParseArguments() throws CmdLineException {
    String cmd = "-a thrift://127.0.0.1:1260 -p 11 -g g1 -hb 2000 -eds -dsp /tmp/arctic -msz 512";
    String[] args = cmd.split(" ");
    OptimizerConfig optimizerConfig = new OptimizerConfig(args);
    Assert.assertEquals("thrift://127.0.0.1:1260", optimizerConfig.getAmsUrl());
    Assert.assertEquals(11, optimizerConfig.getExecutionParallel());
    Assert.assertEquals("g1", optimizerConfig.getGroupName());
    Assert.assertEquals(2000, optimizerConfig.getHeartBeat());
    Assert.assertTrue(optimizerConfig.isExtendDiskStorage());
    Assert.assertEquals("/tmp/arctic", optimizerConfig.getDiskStoragePath());
    Assert.assertEquals(512, optimizerConfig.getMemoryStorageSize());
  }

  @Test
  public void testSetAndGet() {
    OptimizerConfig config = new OptimizerConfig();
    String amsUrl = "thrift://127.0.0.1:1260";
    int executionParallel = 4;
    String groupName = "testGroup";
    long heartBeat = 20000;
    String diskStoragePath = "/tmp";
    long memoryStorageSize = 1024;
    String resourceId = UUID.randomUUID().toString();

    config.setAmsUrl(amsUrl);
    config.setExecutionParallel(executionParallel);
    config.setGroupName(groupName);
    config.setHeartBeat(heartBeat);
    config.setExtendDiskStorage(true);
    config.setDiskStoragePath(diskStoragePath);
    config.setMemoryStorageSize(memoryStorageSize);
    config.setResourceId(resourceId);

    Assert.assertEquals(amsUrl, config.getAmsUrl());
    Assert.assertEquals(executionParallel, config.getExecutionParallel());
    Assert.assertEquals(groupName, config.getGroupName());
    Assert.assertEquals(heartBeat, config.getHeartBeat());
    Assert.assertTrue(config.isExtendDiskStorage());
    Assert.assertEquals(diskStoragePath, config.getDiskStoragePath());
    Assert.assertEquals(memoryStorageSize, config.getMemoryStorageSize());
    Assert.assertEquals(resourceId, config.getResourceId());
  }

  @Test(expected = CmdLineException.class)
  public void testMissingRequiredArgs() throws CmdLineException {
    String[] args = {"-a", "thrift://127.0.0.1:1260", "-p", "4"};
    new OptimizerConfig(args);
  }

  @Test(expected = CmdLineException.class)
  public void testInvalidArgs() throws CmdLineException {
    String[] args = {"-a", "thrift://127.0.0.1:1260", "-p", "invalid", "-g", "testGroup"};
    new OptimizerConfig(args);
  }

  @Test(expected = CmdLineException.class)
  public void testMissingValueArgs() throws CmdLineException {
    String[] args = {"-a", "thrift://127.0.0.1:1260", "-p", "-g", "testGroup"};
    new OptimizerConfig(args);
  }
}
