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

package org.apache.amoro.flink.lookup;

import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.time.Duration;

/** This class is used to configure lookup options. */
public class LookupOptions implements Serializable {
  private static final long serialVersionUID = -1L;

  private final long lruMaximumSize;
  private final int writeRecordThreadNum;
  private final Duration ttlAfterWrite;
  private final long blockCacheCapacity;
  private final int blockCacheNumShardBits;

  private LookupOptions(Builder builder) {
    this.lruMaximumSize = builder.lruMaximumSize;
    this.writeRecordThreadNum = builder.writeRecordThreadNum;
    this.ttlAfterWrite = builder.ttlAfterWrite;
    this.blockCacheCapacity = builder.blockCacheCapacity;
    this.blockCacheNumShardBits = builder.blockCacheNumShardBits;
  }

  public long lruMaximumSize() {
    return lruMaximumSize;
  }

  public int writeRecordThreadNum() {
    return writeRecordThreadNum;
  }

  public Duration ttlAfterWrite() {
    return ttlAfterWrite;
  }

  public boolean isTTLAfterWriteValidated() {
    return ttlAfterWrite.compareTo(Duration.ZERO) > 0;
  }

  public long blockCacheCapacity() {
    return blockCacheCapacity;
  }

  public int numShardBits() {
    return blockCacheNumShardBits;
  }

  @Override
  public String toString() {
    return "LookupOptions{"
        + "lruMaximumSize="
        + lruMaximumSize
        + ", writeRecordThreadNum="
        + writeRecordThreadNum
        + ", ttlAfterWrite="
        + ttlAfterWrite
        + ", blockCacheCapacity="
        + blockCacheCapacity
        + ", blockCacheNumShardBits="
        + blockCacheNumShardBits
        + "}";
  }

  public static class Builder {
    private long lruMaximumSize;
    private int writeRecordThreadNum;
    private Duration ttlAfterWrite;
    private long blockCacheCapacity;
    private int blockCacheNumShardBits;

    /** LRU cache max size. */
    public Builder lruMaximumSize(long lruMaximumSize) {
      Preconditions.checkArgument(lruMaximumSize >= 0, "lruMaximumSize must not be negative");
      this.lruMaximumSize = lruMaximumSize;
      return this;
    }

    /** Write record thread num. */
    public Builder writeRecordThreadNum(int writeRecordThreadNum) {
      Preconditions.checkArgument(
          writeRecordThreadNum > 0, "writeRecordThreadNum must be greater than 0");
      this.writeRecordThreadNum = writeRecordThreadNum;
      return this;
    }

    /** Clean expired records after write. */
    public Builder ttlAfterWrite(Duration ttlAfterWrite) {
      Preconditions.checkArgument(
          !ttlAfterWrite.isNegative(), "ttlAfterWrite must not be negative");
      this.ttlAfterWrite = ttlAfterWrite;
      return this;
    }

    public Builder blockCacheCapacity(long blockCacheCapacity) {
      Preconditions.checkArgument(
          blockCacheCapacity > 0, "blockCacheCapacity must be greater than 0");
      this.blockCacheCapacity = blockCacheCapacity;
      return this;
    }

    public Builder blockCacheNumShardBits(int blockCacheNumShardBits) {
      Preconditions.checkArgument(
          blockCacheNumShardBits >= -1,
          "blockCacheNumShardBits must be greater than or equal to -1");
      this.blockCacheNumShardBits = blockCacheNumShardBits;
      return this;
    }

    public LookupOptions build() {
      return new LookupOptions(this);
    }
  }
}
