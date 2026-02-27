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

package org.apache.amoro.flink.util;

import org.apache.amoro.flink.lookup.LookupOptions;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.flink.configuration.Configuration;

public class LookupUtil {

  public static LookupOptions convertLookupOptions(Configuration config) {
    return new LookupOptions.Builder()
        .lruMaximumSize(config.get(MixedFormatValidator.LOOKUP_CACHE_MAX_ROWS))
        .writeRecordThreadNum(config.get(MixedFormatValidator.ROCKSDB_WRITING_THREADS))
        .ttlAfterWrite(config.get(MixedFormatValidator.LOOKUP_CACHE_TTL_AFTER_WRITE))
        .blockCacheCapacity(config.get(MixedFormatValidator.ROCKSDB_BLOCK_CACHE_CAPACITY))
        .blockCacheNumShardBits(config.get(MixedFormatValidator.ROCKSDB_BLOCK_CACHE_NUM_SHARD_BITS))
        .build();
  }
}
