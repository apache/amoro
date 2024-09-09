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

package org.apache.amoro.server;

import static org.apache.amoro.server.AmoroServiceContainer.LOG;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class AmoroAMSTest {
  @ClassRule public static final Network NETWORK = Network.newNetwork();

  private static final String AMS_IMAGE_VERSION = "apache/amoro:master-snapshot";

  public GenericContainer AMSContainer =
      new GenericContainer(DockerImageName.parse(AMS_IMAGE_VERSION))
          .withExposedPorts(1630, 1630)
          .withCommand("ams")
          .withNetwork(NETWORK)
          .waitingFor(Wait.forHttp("/"));

  @Test
  public void testAMS() throws Exception {

    LOG.info("Starting AMS standalone containers...");

    AMSContainer.start();
    AMSContainer.waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

    Assert.assertTrue(AMSContainer.isRunning());
    Wait.forHealthcheck();

    LOG.info("Containers are started.");
  }
}
