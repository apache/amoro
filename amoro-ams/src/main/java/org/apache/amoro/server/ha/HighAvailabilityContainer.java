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

import org.apache.amoro.client.AmsServerInfo;

import java.util.List;

/**
 * Common interface for high availability (HA) containers.
 *
 * <p>Provides a unified contract for AMS HA implementations (ZK/DATABASE) to wait for leadership,
 * wait for follower state, and close resources.
 */
public interface HighAvailabilityContainer {

  /**
   * Blocks until this node gains leadership.
   *
   * @throws Exception if waiting fails or the underlying implementation throws an error
   */
  void waitLeaderShip() throws Exception;

  /**
   * Blocks until this node becomes a follower (for example, when leadership is lost).
   *
   * @throws Exception if waiting fails or the underlying implementation throws an error
   */
  void waitFollowerShip() throws Exception;

  /**
   * In master-slave mode, AMS startup requires registration and participation in the master node
   * election, which is a non-blocking process.
   *
   * @throws Exception if waiting fails or the underlying implementation throws an error
   */
  void registAndElect() throws Exception;

  /**
   * Returns whether the current node is the leader.
   *
   * @return true/false
   */
  boolean hasLeadership();

  /**
   * Returns the current AMS node information{@link AmsServerInfo}.
   *
   * @return AmsServerInfo
   */
  AmsServerInfo getOptimizingServiceServerInfo();

  /**
   * Returns the current AMS node information{@link AmsServerInfo}.
   *
   * @return AmsServerInfo
   */
  AmsServerInfo getLeaderNodeInfo();

  /**
   * Get list of alive nodes. Only the leader node should call this method.
   *
   * @return List of alive node information
   * @throws Exception if retrieval fails
   */
  List<AmsServerInfo> getAliveNodes() throws Exception;

  /** Closes the container and releases resources. */
  void close();
}
