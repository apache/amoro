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

package org.apache.amoro.resource;

/**
 * The interface of internal resource container For {@link InternalResourceContainer}, resources are
 * managed by AMS, and resources are decoupled from processes, which means one process could run any
 * number of resources and a resource could run any number of processes. For {@link
 * ExternalResourceContainer} resources are managed outside(by some JobManager or application
 * master), resources and processes are one-to-one mapping.
 */
public interface InternalResourceContainer extends ResourceContainer {

  /**
   * Start a new optimizer.
   *
   * @param resource resource information to start the optimizer
   */
  void requestResource(Resource resource);

  /**
   * Release a optimizer
   *
   * @param resource resource information to release the optimizer
   */
  void releaseResource(Resource resource);

  /**
   * Whether AMS should drive auto-restart (re-invoking {@link #requestResource}) for this container
   * when an optimizer is detected as orphaned.
   *
   * <p>Containers that already have their own process-level self-healing should return {@code
   * false}. A Kubernetes Deployment is the canonical example: its ReplicaSet recreates crashed
   * Pods, so AMS-driven restart would race against the controller. Flink/Spark containers may also
   * want to return {@code false} when the underlying engine is configured with JobManager HA, since
   * the engine will resurrect the driver on its own.
   *
   * <p>When this returns {@code false}, the orphaned resource is logged once and then left alone:
   * AMS will not re-request the resource, will not consume retry attempts, and will not delete the
   * resource DB row after the max-retry threshold. The implication is that if the external
   * self-healing itself fails permanently (e.g. an ImagePullBackOff), the row will accumulate in
   * the DB until an operator cleans it up manually.
   *
   * <p>Returns {@code true} by default. Override in containers that manage lifecycle externally.
   */
  default boolean supportsAutoRestart() {
    return true;
  }
}
