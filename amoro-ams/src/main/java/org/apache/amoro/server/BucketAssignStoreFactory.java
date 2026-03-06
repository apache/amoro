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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.shade.zookeeper3.org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating BucketAssignStore implementations based on HA configuration.
 *
 * <p>Supports different storage backends (ZK, database) according to HA type.
 */
public final class BucketAssignStoreFactory {
  private static final Logger LOG = LoggerFactory.getLogger(BucketAssignStoreFactory.class);

  private BucketAssignStoreFactory() {}

  /**
   * Creates a BucketAssignStore based on the given HA configuration and container.
   *
   * @param haContainer the HA container
   * @param conf service configuration
   * @return a BucketAssignStore implementation according to HA type
   * @throws IllegalArgumentException if HA type is unsupported
   * @throws RuntimeException if the ZK store cannot be created
   */
  public static BucketAssignStore create(
      HighAvailabilityContainer haContainer, Configurations conf) {
    String haType = conf.getString(AmoroManagementConf.HA_TYPE).toLowerCase();
    String clusterName = conf.getString(AmoroManagementConf.HA_CLUSTER_NAME);

    switch (haType) {
      case AmoroManagementConf.HA_TYPE_ZK:
        if (haContainer instanceof org.apache.amoro.server.ha.ZkHighAvailabilityContainer) {
          org.apache.amoro.server.ha.ZkHighAvailabilityContainer zkHaContainer =
              (org.apache.amoro.server.ha.ZkHighAvailabilityContainer) haContainer;
          CuratorFramework zkClient = zkHaContainer.getZkClient();
          if (zkClient != null) {
            LOG.info("Creating ZkBucketAssignStore for cluster: {}", clusterName);
            return new ZkBucketAssignStore(zkClient, clusterName);
          }
        }
        throw new RuntimeException(
            "Cannot create ZkBucketAssignStore: ZK client not available or invalid container type");

      case AmoroManagementConf.HA_TYPE_DATABASE:
        LOG.info("Creating DataBaseBucketAssignStore for cluster: {}", clusterName);
        // TODO: Implement DataBaseBucketAssignStore when ready
        throw new UnsupportedOperationException("DataBaseBucketAssignStore is not yet implemented");

      default:
        throw new IllegalArgumentException(
            "Unsupported ha.type: " + haType + ", only 'zk' or 'database' are allowed");
    }
  }
}
