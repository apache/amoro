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

package com.netease.arctic.ams.api.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * An incrementing counter metric.
 */
public class Counter implements Metric {
  private final LongAdder count = new LongAdder();

  /**
   * Increment the counter by one.
   */
  public void inc() {
    inc(1);
  }

  /**
   * Increment the counter by {@code n}.
   *
   * @param n the amount by which the counter will be increased
   */
  public void inc(long n) {
    count.add(n);
  }

  /**
   * Returns the counter's current value.
   *
   * @return the counter's current value
   */
  public long getCount() {
    return count.sum();
  }
}
