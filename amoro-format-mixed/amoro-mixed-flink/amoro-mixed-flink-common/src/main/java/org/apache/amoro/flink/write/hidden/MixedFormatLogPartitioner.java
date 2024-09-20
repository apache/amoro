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

package org.apache.amoro.flink.write.hidden;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

import org.apache.amoro.flink.shuffle.ShuffleHelper;
import org.apache.amoro.log.LogData;
import org.apache.commons.lang.ArrayUtils;
import org.apache.flink.table.data.RowData;
import org.apache.kafka.common.utils.Utils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/** This is a log message partitioner that makes sure the record is without out-of-order. */
public class MixedFormatLogPartitioner<T> implements Serializable {
  private static final long serialVersionUID = 9184708069203854226L;
  private final AtomicInteger counter = new AtomicInteger(0);
  private final ShuffleHelper helper;

  public MixedFormatLogPartitioner(ShuffleHelper shuffleHelper) {
    this.helper = shuffleHelper;
  }

  public int partition(LogData<T> logData, int[] partitions) {
    checkNotNull(logData, "record is null");
    checkArgument(ArrayUtils.isNotEmpty(partitions), "Partitions of the target topic is empty.");

    int partition;
    if (helper == null || !helper.isPrimaryKeyExist()) {
      int nextValue = nextValue();
      int part = Utils.toPositive(nextValue) % partitions.length;
      partition = partitions[part];
    } else {
      helper.open();
      long hash = helper.hashKeyValue((RowData) logData.getActualValue());
      partition = partitions[(int) (hash % partitions.length)];
    }
    return partition;
  }

  private int nextValue() {
    return counter.getAndIncrement();
  }
}
