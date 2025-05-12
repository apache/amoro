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
}
