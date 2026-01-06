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

package org.apache.amoro.server.ha;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating HA containers based on configuration.
 *
 * <p>Supports "zk" and "database". When ha.enabled=false, returns a no-op container.
 */
public final class HighAvailabilityContainerFactory {
  private static final Logger LOG = LoggerFactory.getLogger(HighAvailabilityContainerFactory.class);

  private HighAvailabilityContainerFactory() {}

  /**
   * Creates an HA container based on the given configuration.
   *
   * @param conf service configuration
   * @return a HA container implementation according to ha.enabled and ha.type
   * @throws IllegalArgumentException if ha.type is unsupported
   * @throws RuntimeException if the ZK container cannot be created
   */
  public static HighAvailabilityContainer create(Configurations conf) {
    boolean enabled = conf.getBoolean(AmoroManagementConf.HA_ENABLE);
    if (!enabled) {
      LOG.info("HA is disabled, use NoopHighAvailabilityContainer");
      return new NoopHighAvailabilityContainer();
    }
    String type = conf.getString(AmoroManagementConf.HA_TYPE).toLowerCase();
    switch (type) {
      case AmoroManagementConf.HA_TYPE_ZK:
        try {
          return new ZkHighAvailabilityContainer(conf);
        } catch (Exception e) {
          throw new RuntimeException("Failed to create ZkHighAvailabilityContainer", e);
        }
      case AmoroManagementConf.HA_TYPE_DATABASE:
        return new DataBaseHighAvailabilityContainer(conf);
      default:
        throw new IllegalArgumentException(
            "Unsupported ha.type: " + type + ", only 'zk' or 'database' are allowed");
    }
  }
}
