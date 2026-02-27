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

package org.apache.amoro.hive.utils;

import java.time.Instant;

public class TimeUtil {

  public static final long MICROS_PER_SECOND = 1000_000L;

  public static final long NANO_PER_MICRO = 1000L;

  /**
   * Returns the microseconds between two instants. If jdk version less than 18 there is a bug in
   * calculating microsecond intervals between two times using methods like
   * 'ChronoUnit.MICROS.between(start, end)', this method is used instead. see: <a
   * href="https://bugs.openjdk.org/browse/JDK-8273369">...</a>
   */
  public static long microsBetween(Instant start, Instant end) {
    long secsDiff = Math.subtractExact(end.getEpochSecond(), start.getEpochSecond());
    long totalMicros = Math.multiplyExact(secsDiff, MICROS_PER_SECOND);
    return Math.addExact(totalMicros, (end.getNano() - start.getNano()) / NANO_PER_MICRO);
  }
}
