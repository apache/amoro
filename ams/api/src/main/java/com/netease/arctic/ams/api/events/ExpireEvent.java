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

package com.netease.arctic.ams.api.events;

import org.immutables.value.Value;

/**
 * Amoro automatically maintains datalake metadata files(e.g. expire snapshots, clean orphan
 * files...), every maintenance process will generate an event which could be reported to {@link
 * com.netease.arctic.ams.api.events.EventListener EventListener}
 */
@Value.Immutable
public interface ExpireEvent extends TableEvent {
  /**
   * The expiring process id
   *
   * @return id
   */
  long processId();

  /**
   * Operation of expiring(e.g. expire snapshots)
   *
   * @return expiring operation
   */
  ExpireOperation operation();

  /**
   * Details of expiring event
   *
   * @return event detail
   */
  ExpireResult expireResult();

  /**
   * Get expiring event result as target specific Class
   *
   * @param clazz Subclass of ExpireResult
   * @return Specific Class
   * @param <R> Specific Class
   */
  default <R extends ExpireResult> R getExpireResultAs(Class<R> clazz) {
    return clazz.cast(expireResult());
  }
}
