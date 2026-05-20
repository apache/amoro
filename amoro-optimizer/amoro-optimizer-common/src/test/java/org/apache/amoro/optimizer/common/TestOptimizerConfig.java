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

package org.apache.amoro.optimizer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;

import java.util.UUID;

public class TestOptimizerConfig {

  @Test
  public void testParseArguments() throws CmdLineException {
    String cmd = "-a thrift://127.0.0.1:1260 -p 11 -g g1 -hb 2000 -eds -dsp /tmp/amoro -msz 512";
    String[] args = cmd.split(" ");
    OptimizerConfig optimizerConfig = new OptimizerConfig(args);
    Assertions.assertEquals("thrift://127.0.0.1:1260", optimizerConfig.getAmsUrl());
    Assertions.assertEquals(11, optimizerConfig.getExecutionParallel());
    Assertions.assertEquals("g1", optimizerConfig.getGroupName());
    Assertions.assertEquals(2000, optimizerConfig.getHeartBeat());
    Assertions.assertTrue(optimizerConfig.isExtendDiskStorage());
    Assertions.assertEquals("/tmp/amoro", optimizerConfig.getDiskStoragePath());
    Assertions.assertEquals(512, optimizerConfig.getMemoryStorageSize());
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

    Assertions.assertEquals(amsUrl, config.getAmsUrl());
    Assertions.assertEquals(executionParallel, config.getExecutionParallel());
    Assertions.assertEquals(groupName, config.getGroupName());
    Assertions.assertEquals(heartBeat, config.getHeartBeat());
    Assertions.assertTrue(config.isExtendDiskStorage());
    Assertions.assertEquals(diskStoragePath, config.getDiskStoragePath());
    Assertions.assertEquals(memoryStorageSize, config.getMemoryStorageSize());
    Assertions.assertEquals(resourceId, config.getResourceId());
  }

  @Test
  public void testMissingRequiredArgs() {
    String[] args = {"-a", "thrift://127.0.0.1:1260", "-p", "4"};
    Assertions.assertThrows(CmdLineException.class, () -> new OptimizerConfig(args));
  }

  @Test
  public void testInvalidArgs() {
    String[] args = {"-a", "thrift://127.0.0.1:1260", "-p", "invalid", "-g", "testGroup"};
    Assertions.assertThrows(CmdLineException.class, () -> new OptimizerConfig(args));
  }

  @Test
  public void testMissingValueArgs() {
    String[] args = {"-a", "thrift://127.0.0.1:1260", "-p", "-g", "testGroup"};
    Assertions.assertThrows(CmdLineException.class, () -> new OptimizerConfig(args));
  }
}
