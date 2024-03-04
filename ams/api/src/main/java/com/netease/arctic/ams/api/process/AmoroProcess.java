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

package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.Action;

/**
 * AmoroProcess is a process the whole lifecycle of which is managed by Amoro. The process is
 * submitted by user or system and handled by Amoro. The process could be a minor optimizing, a
 * major optimizing, an external optimizing, a snapshot refresh, a snapshot expire, a clean orphaned
 * files or a hive commit sync.
 *
 * @param <T> the state type of the process
 */
public interface AmoroProcess<T extends ProcessState> {

  /**
   * Submit the process to Amoro. The process will be handled by Amoro after submitted. If the
   * process is already submitted, this method will do nothing. For external optimizing, the process
   * will be submitted to external resources like Yarn.
   */
  void submit();

  /**
   * return submit future of the process This method always returns the same future object even if
   * submit() has not been called
   *
   * @return submit future of the process
   */
  SimpleFuture getSubmitFuture();

  /**
   * return complete future of the process This method always returns the same future object even if
   * submit() has not been called
   *
   * @return complete future of the process
   */
  SimpleFuture getCompleteFuture();

  /**
   * Cancel and close this process, related resources will be released. This method will block until
   * getStatus() return CLOSED, but related resource could be released later.
   */
  void close();

  /**
   * Get the summary of the process
   *
   * @return the summary of the process
   */
  String getSummary();

  /**
   * Get {@link ProcessState} of the process
   *
   * @return the state of the process
   */
  T getState();

  /**
   * Get {@link ProcessStatus} of the process
   *
   * @return the status of the process
   */
  default ProcessStatus getStatus() {
    return getState().getStatus();
  }

  /**
   * Check if the process is closed
   *
   * @return true if the process is closed, false otherwise
   */
  default boolean isClosed() {
    return getState().getStatus() == ProcessStatus.CLOSED;
  }

  /**
   * Get the id of the process
   *
   * @return the id of the process
   */
  default long getId() {
    return getState().getId();
  }

  /**
   * Get the {@link Action} of the process
   *
   * @return the action of the process
   */
  default Action getAction() {
    return getState().getAction();
  }
}
