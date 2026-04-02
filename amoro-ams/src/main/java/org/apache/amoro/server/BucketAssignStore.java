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

import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.exception.BucketAssignStoreException;

import java.util.List;
import java.util.Map;

/**
 * Interface for storing and retrieving bucket ID assignments to AMS nodes. Different
 * implementations can use different storage backends (e.g., ZooKeeper, database).
 */
public interface BucketAssignStore {

  /**
   * Save bucket ID assignments for a node.
   *
   * @param nodeInfo The node information
   * @param bucketIds List of bucket IDs assigned to this node
   * @throws BucketAssignStoreException If save operation fails
   */
  void saveAssignments(AmsServerInfo nodeInfo, List<String> bucketIds)
      throws BucketAssignStoreException;

  /**
   * Get bucket ID assignments for a node.
   *
   * @param nodeInfo The node information
   * @return List of bucket IDs assigned to this node, empty list if not found
   * @throws BucketAssignStoreException If retrieval operation fails
   */
  List<String> getAssignments(AmsServerInfo nodeInfo) throws BucketAssignStoreException;

  /**
   * Remove bucket ID assignments for a node.
   *
   * @param nodeInfo The node information
   * @throws BucketAssignStoreException If removal operation fails
   */
  void removeAssignments(AmsServerInfo nodeInfo) throws BucketAssignStoreException;

  /**
   * Get all bucket ID assignments for all nodes.
   *
   * @return Map of node info to list of bucket IDs
   * @throws BucketAssignStoreException If retrieval operation fails
   */
  Map<AmsServerInfo, List<String>> getAllAssignments() throws BucketAssignStoreException;

  /**
   * Get all alive AMS nodes that have bucket assignments. Used by optimizers in master-slave mode
   * to discover all AMS optimizing endpoints.
   *
   * @return List of AmsServerInfo for all nodes with bucket assignments, empty list if none
   * @throws BucketAssignStoreException If retrieval operation fails
   */
  List<AmsServerInfo> getAliveNodes() throws BucketAssignStoreException;

  /**
   * Get the last update time for a node's assignments.
   *
   * @param nodeInfo The node information
   * @return Last update timestamp in milliseconds, 0 if not found
   * @throws BucketAssignStoreException If retrieval operation fails
   */
  long getLastUpdateTime(AmsServerInfo nodeInfo) throws BucketAssignStoreException;

  /**
   * Update the last update time for a node's assignments.
   *
   * @param nodeInfo The node information
   * @throws BucketAssignStoreException If update operation fails
   */
  void updateLastUpdateTime(AmsServerInfo nodeInfo) throws BucketAssignStoreException;
}
