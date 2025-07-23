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

package org.apache.amoro.server.utils;

/** SnowflakeId generator */
public class SnowflakeIdGenerator {
  // Base timestamp (e.g., the start time of the service)
  private static final long EPOCH_SECONDS = 0L;

  // Given that the maximum int value in javascript is 2^53-1, here the length of the SnowflakeId is
  // compressed to 54 bits (fixed header 1 bit + timestamp part + machine ID part + sequence part)
  // Number of bits allocated for the timestamp part
  private static final long TIMESTAMP_BITS = 40L;

  // Number of bits allocated for the machine ID part
  private static final long MACHINE_ID_BITS = 5L;

  // Number of bits allocated for the sequence number part
  private static final long SEQUENCE_BITS = 8L;

  // Left shift amount for the timestamp part
  private static final long TIMESTAMP_LEFT_SHIFT = MACHINE_ID_BITS + SEQUENCE_BITS;

  // Left shift amount for the machine ID part
  private static final long MACHINE_ID_LEFT_SHIFT = SEQUENCE_BITS;

  // Maximum value for the machine ID
  private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);

  // Maximum value for the sequence number
  private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

  // Machine ID and Sequence
  private final long machineId;
  private static final long DEFAULT_MACHINE_ID = 0L;
  private long sequence = 0L;
  private long lastTimestamp = -1L;

  /** Constructor with default machine ID */
  public SnowflakeIdGenerator() {
    this(DEFAULT_MACHINE_ID);
  }

  /**
   * Constructor to set Machine ID
   *
   * @param machineId Machine ID, must be between 0 and 32
   */
  public SnowflakeIdGenerator(long machineId) {
    if (machineId > MAX_MACHINE_ID || machineId < 0) {
      throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
    }
    this.machineId = machineId;
  }

  /** Generate a unique Snowflake ID */
  public synchronized long generateId() {
    long timestamp = currentTime();

    if (timestamp < lastTimestamp) {
      throw new RuntimeException("Clock moved backwards!");
    }

    // If the timestamp is the same as the last generated, increment sequence number
    if (timestamp == lastTimestamp) {
      sequence = (sequence + 1) & SEQUENCE_MASK;
      if (sequence == 0) {
        timestamp =
            waitForNextMillis(lastTimestamp); // If sequence overflows, wait for next millisecond
      }
    } else {
      sequence = 0;
    }

    lastTimestamp = timestamp;

    // Return Snowflake ID by shifting the parts to the correct positions
    return ((timestamp - EPOCH_SECONDS) << TIMESTAMP_LEFT_SHIFT)
        | (machineId << MACHINE_ID_LEFT_SHIFT)
        | sequence;
  }

  /** Get the current timestamp in 10ms */
  private long currentTime() {
    return System.currentTimeMillis() / 10;
  }

  private long waitForNextMillis(long lastTimestamp) {
    long timestamp = currentTime();
    while (timestamp <= lastTimestamp) {
      timestamp = currentTime();
    }
    return timestamp;
  }

  /**
   * Get the minimum Snowflake ID for a specific timestamp (for example, 1735689600
   * (2025-01-01T00:00:00Z))
   *
   * @param timestamp Specified timestamp (in milliseconds or seconds)
   * @return Minimum Snowflake ID
   */
  public static long getMinSnowflakeId(long timestamp) {
    // if timestamp is milliseconds, compress it to 10ms
    if (timestamp < 10000000000L) {
      timestamp *= 100; // Convert seconds to 10ms
    } else {
      timestamp /= 10; // Convert milliseconds to 10ms
    }

    return (timestamp << TIMESTAMP_LEFT_SHIFT);
  }

  /**
   * Extract the timestamp part from a Snowflake ID
   *
   * @param snowflakeId Snowflake ID
   * @return Timestamp part in seconds
   */
  public static long extractTimestamp(long snowflakeId) {
    return (snowflakeId >> TIMESTAMP_LEFT_SHIFT) / 100 + EPOCH_SECONDS;
  }
}
