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

package com.netease.arctic.flink.read.hybrid.reader;

import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import org.apache.flink.api.connector.source.SourceSplit;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * If using arctic table as build-table, FirstSplits can record the first splits planned by Enumerator.
 * After
 */
public class FirstSplits implements Serializable {

  public static final long serialVersionUID = 1L;
  public static final Logger LOGGER = LoggerFactory.getLogger(FirstSplits.class);

  private Map<String, Boolean> splits;
  private long unfinishedCount;
  private volatile boolean finished = false;

  public FirstSplits(Collection<ArcticSplit> splits) {
    Preconditions.checkNotNull(splits, "plan splits should not be null");
    this.splits = splits.stream().map(SourceSplit::splitId).collect(Collectors.toMap((k) -> k, (i) -> false));

    unfinishedCount = this.splits.size();
    LOGGER.info("init splits at {}, size:{}", LocalDateTime.now(), unfinishedCount);
  }

  public boolean getFinished() {
    return finished;
  }

  public synchronized void addSplitsBack(Collection<ArcticSplit> splits) {
    if (this.splits == null || CollectionUtil.isNullOrEmpty(splits)) {
      return;
    }
    splits.forEach((p) -> {
      Boolean finished = this.splits.get(p.splitId());
      if (finished == null || !finished) {
        return;
      }
      unfinishedCount++;
      LOGGER.debug("add back split:{} at {}", p, LocalDateTime.now());
      this.splits.put(p.splitId(), false);
    });
  }

  /**
   * Remove finished splits. If all splits are finished, then return true, otherwise, return false.
   * @param finishedSplitIds
   * @return
   */
  public synchronized boolean removeAndReturnIfAllFinished(Collection<String> finishedSplitIds) {
    if (splits == null) {
      return true;
    }
    if (CollectionUtil.isNullOrEmpty(finishedSplitIds)) {
      return unfinishedCount == 0;
    }

    finishedSplitIds.forEach((p) -> {
      Boolean finished = this.splits.get(p);
      if (finished == null || finished) {
        return;
      }
      unfinishedCount--;
      this.splits.put(p, true);
      LOGGER.debug("finish split:{} at {}", p, LocalDateTime.now());
    });
    if (unfinishedCount == 0) {
      clear();
      LOGGER.info("finish all splits at {}", LocalDateTime.now());
    }
    return unfinishedCount == 0;
  }

  public synchronized void clear() {
    this.splits = null;
    this.finished = true;
  }
}
