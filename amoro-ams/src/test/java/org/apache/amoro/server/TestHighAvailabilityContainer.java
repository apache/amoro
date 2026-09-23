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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.server.ha.ZkHighAvailabilityContainer;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Test for HighAvailabilityContainer leader election using mocked ZK. */
public class TestHighAvailabilityContainer {

  private Configurations serviceConfig;
  private HighAvailabilityContainer haContainer;
  private CuratorFramework mockZkClient;
  private LeaderLatch mockLeaderLatch;

  @Before
  public void setUp() throws Exception {
    mockZkClient = createMockZkClient();
    mockLeaderLatch = createMockLeaderLatch();

    serviceConfig = new Configurations();
    serviceConfig.setString(AmoroManagementConf.SERVER_EXPOSE_HOST, "127.0.0.1");
    serviceConfig.setInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, 1260);
    serviceConfig.setInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT, 1261);
    serviceConfig.setInteger(AmoroManagementConf.HTTP_SERVER_PORT, 1630);
    serviceConfig.setBoolean(AmoroManagementConf.HA_ENABLE, true);
    serviceConfig.setString(AmoroManagementConf.HA_ZOOKEEPER_ADDRESS, "127.0.0.1:2181");
    serviceConfig.setString(AmoroManagementConf.HA_CLUSTER_NAME, "test-cluster");
  }

  @After
  public void tearDown() throws Exception {
    if (haContainer != null) {
      haContainer.close();
    }
  }

  @Test
  public void testHasLeadership() throws Exception {
    serviceConfig.setBoolean(AmoroManagementConf.HA_USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(false);
    haContainer = createContainerWithMockZk();

    Assert.assertFalse("Should not be leader initially", haContainer.hasLeadership());

    mockLeaderLatch = createMockLeaderLatch(true);
    haContainer = createContainerWithMockZk();

    Assert.assertTrue("Should be leader", haContainer.hasLeadership());
  }

  @Test
  public void testCreateWithoutHAEnabled() throws Exception {
    serviceConfig.setBoolean(AmoroManagementConf.HA_ENABLE, false);
    serviceConfig.setBoolean(AmoroManagementConf.HA_USE_MASTER_SLAVE_MODE, true);
    haContainer = new ZkHighAvailabilityContainer(serviceConfig);
  }

  @Test
  public void testGetTableServiceServerInfo() throws Exception {
    serviceConfig.setBoolean(AmoroManagementConf.HA_USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(true);
    haContainer = createContainerWithMockZk();

    AmsServerInfo tableServiceInfo = haContainer.getTableServiceServerInfo();
    Assert.assertEquals("127.0.0.1", tableServiceInfo.getHost());
    Assert.assertEquals(Integer.valueOf(1260), tableServiceInfo.getThriftBindPort());
  }

  @Test
  public void testGetOptimizingServiceServerInfo() throws Exception {
    serviceConfig.setBoolean(AmoroManagementConf.HA_USE_MASTER_SLAVE_MODE, true);
    mockLeaderLatch = createMockLeaderLatch(true);
    haContainer = createContainerWithMockZk();

    AmsServerInfo optimizingServiceInfo = haContainer.getOptimizingServiceServerInfo();
    Assert.assertEquals("127.0.0.1", optimizingServiceInfo.getHost());
    Assert.assertEquals(Integer.valueOf(1261), optimizingServiceInfo.getThriftBindPort());
  }

  /** Create HighAvailabilityContainer with mocked ZK components using reflection. */
  private HighAvailabilityContainer createContainerWithMockZk() throws Exception {
    // Build with HA disabled to avoid real ZK connection, then inject mocks via reflection
    Configurations tempConfig = new Configurations(serviceConfig);
    tempConfig.setBoolean(AmoroManagementConf.HA_ENABLE, false);
    HighAvailabilityContainer container = new ZkHighAvailabilityContainer(tempConfig);

    // Inject mock ZK client
    java.lang.reflect.Field zkClientField =
        ZkHighAvailabilityContainer.class.getDeclaredField("zkClient");
    zkClientField.setAccessible(true);
    zkClientField.set(container, mockZkClient);

    // Inject mock leader latch
    java.lang.reflect.Field leaderLatchField =
        ZkHighAvailabilityContainer.class.getDeclaredField("leaderLatch");
    leaderLatchField.setAccessible(true);
    leaderLatchField.set(container, mockLeaderLatch);

    // Inject server info (null when HA disabled, but tests need it)
    AmsServerInfo tableServiceInfo = new AmsServerInfo();
    tableServiceInfo.setHost(serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST));
    tableServiceInfo.setThriftBindPort(
        serviceConfig.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT));
    tableServiceInfo.setRestBindPort(
        serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));

    AmsServerInfo optimizingServiceInfo = new AmsServerInfo();
    optimizingServiceInfo.setHost(serviceConfig.getString(AmoroManagementConf.SERVER_EXPOSE_HOST));
    optimizingServiceInfo.setThriftBindPort(
        serviceConfig.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT));
    optimizingServiceInfo.setRestBindPort(
        serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT));

    java.lang.reflect.Field tableServiceField =
        ZkHighAvailabilityContainer.class.getDeclaredField("tableServiceServerInfo");
    tableServiceField.setAccessible(true);
    tableServiceField.set(container, tableServiceInfo);

    java.lang.reflect.Field optimizingServiceField =
        ZkHighAvailabilityContainer.class.getDeclaredField("optimizingServiceServerInfo");
    optimizingServiceField.setAccessible(true);
    optimizingServiceField.set(container, optimizingServiceInfo);

    return container;
  }

  @SuppressWarnings("unchecked")
  private CuratorFramework createMockZkClient() throws Exception {
    CuratorFramework mockClient = mock(CuratorFramework.class);
    doAnswer(invocation -> null).when(mockClient).start();
    doAnswer(invocation -> null).when(mockClient).close();
    return mockClient;
  }

  private LeaderLatch createMockLeaderLatch() throws Exception {
    return createMockLeaderLatch(true);
  }

  private LeaderLatch createMockLeaderLatch(boolean hasLeadership) throws Exception {
    LeaderLatch mockLatch = mock(LeaderLatch.class);
    when(mockLatch.hasLeadership()).thenReturn(hasLeadership);
    doAnswer(invocation -> null).when(mockLatch).addListener(any());
    doAnswer(invocation -> null).when(mockLatch).start();
    doAnswer(invocation -> null).when(mockLatch).close();
    doAnswer(invocation -> null).when(mockLatch).await();
    return mockLatch;
  }
}
