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

package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.server.table.TagConfiguration;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/** Action to auto create tag for Iceberg Table. */
public class AutoCreateIcebergTagAction {
  private static final Logger LOG = LoggerFactory.getLogger(AutoCreateIcebergTagAction.class);

  private final Table table;
  private final TagConfiguration tagConfig;
  private final LocalDateTime triggerTime;
  private final String tagName;

  public AutoCreateIcebergTagAction(
      Table table, TagConfiguration tagConfig, LocalDateTime checkTime) {
    this.table = table;
    this.tagConfig = tagConfig;

    LocalDateTime tagTime =
        tagConfig.getTriggerPeriod().getTagTime(checkTime, tagConfig.getTriggerOffsetMinutes());
    // triggerTime = TagTime + triggerOffset
    // The trigger time of the tag, which is the time when the tag is expected to be created.
    this.triggerTime = tagTime.plusMinutes(tagConfig.getTriggerOffsetMinutes());
    this.tagName = tagConfig.getTriggerPeriod().generateTagName(tagTime, tagConfig.getTagFormat());
  }

  public void execute() {
    if (!tagConfig.isAutoCreateTag()) {
      return;
    }
    LOG.debug("Start checking the automatic creation of tags for {}", table.name());
    if (tagExist()) {
      LOG.debug("Found the expected tag on {}, skip", table.name());
      return;
    }
    boolean success = createTag();
    if (success) {
      LOG.info("Created a tag successfully on {}", table.name());
    } else {
      LOG.info("Skipped tag creation on {}", table.name());
    }
  }

  private boolean tagExist() {
    String tag =
        table.refs().entrySet().stream()
            .filter(entry -> entry.getValue().isTag())
            .map(Map.Entry::getKey)
            .filter(tagName::equals)
            .findFirst()
            .orElse(null);
    return tag != null;
  }

  private boolean createTag() {
    long tagTriggerTimestampMillis = getTagTriggerTimestampMillis();
    Snapshot snapshot = findSnapshot(table, tagTriggerTimestampMillis);
    if (snapshot == null) {
      LOG.info("Found no snapshot at {} for {}", tagTriggerTimestampMillis, table.name());
      return false;
    }
    if (exceedMaxDelay(snapshot)) {
      LOG.info(
          "{}'s snapshot {} at {} exceeds max delay {}, and the expected trigger time is {}",
          table.name(),
          snapshot.snapshotId(),
          snapshot.timestampMillis(),
          tagConfig.getMaxDelayMinutes(),
          tagTriggerTimestampMillis);
      return false;
    }
    table.manageSnapshots().createTag(tagName, snapshot.snapshotId()).commit();
    LOG.info(
        "Created a tag {} for {} on snapshot {} at {}",
        tagName,
        table.name(),
        snapshot.snapshotId(),
        snapshot.timestampMillis());
    return true;
  }

  private boolean exceedMaxDelay(Snapshot snapshot) {
    if (tagConfig.getMaxDelayMinutes() <= 0) {
      return false;
    }
    long delay = snapshot.timestampMillis() - getTagTriggerTimestampMillis();
    return delay > tagConfig.getMaxDelayMinutes() * 60_000L;
  }

  private long getTagTriggerTimestampMillis() {
    return triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  private static Snapshot findSnapshot(Table table, long tagTriggerTime) {
    Iterable<Snapshot> snapshots = table.snapshots();
    for (Snapshot snapshot : snapshots) {
      long waterMark = getWaterMark(table, snapshot);
      if (waterMark > tagTriggerTime) {
        return snapshot;
      }
    }
    return null;
  }

  private static long getWaterMark(Table table, Snapshot snapshot) {
    // TODO get water mark from snapshot level
    return snapshot.timestampMillis();
  }
}
