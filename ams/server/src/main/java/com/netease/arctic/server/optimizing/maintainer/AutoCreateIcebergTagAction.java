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

import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.TagTriggerPeriod;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AutoCreateIcebergTagAction {
  private static final Logger LOG = LoggerFactory.getLogger(AutoCreateIcebergTagAction.class);

  private final Table table;
  private final TagConfig tagConfig;
  private final LocalDateTime now;

  public AutoCreateIcebergTagAction(Table table, LocalDateTime now) {
    this.table = table;
    this.tagConfig = TagConfig.fromTableProperties(table.properties());
    this.now = now;
  }

  public void execute() {
    if (!tagConfig.isAutoCreateTag()) {
      return;
    }
    if (tagExist()) {
      LOG.debug("{} find expect tag, skip", table.name());
      return;
    }
    boolean success = createTag();
    LOG.info("{} tag creation {}", table.name(), success ? "succeed" : "skipped");
  }

  private boolean tagExist() {
    if (TagTriggerPeriod.DAILY.propertyName().equals(tagConfig.getTriggerPeriod())) {
      return findTagOfToday() != null;
    } else {
      throw new IllegalArgumentException(
          "unsupported trigger period " + tagConfig.getTriggerPeriod());
    }
  }

  private String findTagOfToday() {
    String name = generateTagName()
    return table.refs().entrySet().stream()
        .filter(entry -> entry.getValue().isTag())
        .map(Map.Entry::getKey)
        .filter(name::equals)
        .findFirst()
        .orElse(null);
  }

  private boolean createTag() {
    Snapshot snapshot = findSnapshot(table, getTagTriggerTime());
    if (snapshot == null) {
      LOG.info("{} no snapshot found at {}", this.table.name(), getTagTriggerTime());
      return false;
    }
    String newTagName = generateTagName();
    table.manageSnapshots().createTag(newTagName, snapshot.snapshotId()).commit();
    LOG.info(
        "{} create tag {} on snapshot {} {}",
        this.table.name(),
        newTagName,
        snapshot.snapshotId(),
        snapshot.timestampMillis());
    return true;
  }

  private String generateTagName() {
    if (TagTriggerPeriod.DAILY.propertyName().equals(tagConfig.getTriggerPeriod())) {
      String tagFormat = tagConfig.getTagFormat();
      return now.minusDays(1).format(DateTimeFormatter.ofPattern(tagFormat));
    } else {
      throw new IllegalArgumentException(
          "unsupported trigger period " + tagConfig.getTriggerPeriod());
    }
  }

  private long getTagTriggerTime() {
    if (TagTriggerPeriod.DAILY.propertyName().equals(tagConfig.getTriggerPeriod())) {
      int triggerOffsetMinutes = tagConfig.getTriggerOffsetMinutes();
      LocalTime localTime = LocalTime.ofSecondOfDay(triggerOffsetMinutes * 60L);
      LocalDateTime localDateTime = LocalDateTime.of(now.toLocalDate(), localTime);
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    } else {
      throw new IllegalArgumentException(
          "unsupported trigger period " + tagConfig.getTriggerPeriod());
    }
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

  static class TagConfig {
    private boolean autoCreateTag;
    private String tagFormat;
    private String triggerPeriod;
    private int triggerOffsetMinutes;

    public boolean isAutoCreateTag() {
      return autoCreateTag;
    }

    public void setAutoCreateTag(boolean autoCreateTag) {
      this.autoCreateTag = autoCreateTag;
    }

    public String getTagFormat() {
      return tagFormat;
    }

    public void setTagFormat(String tagFormat) {
      this.tagFormat = tagFormat;
    }

    public String getTriggerPeriod() {
      return triggerPeriod;
    }

    public void setTriggerPeriod(String triggerPeriod) {
      this.triggerPeriod = triggerPeriod;
    }

    public int getTriggerOffsetMinutes() {
      return triggerOffsetMinutes;
    }

    public void setTriggerOffsetMinutes(int triggerOffsetMinutes) {
      this.triggerOffsetMinutes = triggerOffsetMinutes;
    }

    public static TagConfig fromTableProperties(Map<String, String> tableProperties) {
      TagConfig tagConfig = new TagConfig();
      tagConfig.setAutoCreateTag(
          CompatiblePropertyUtil.propertyAsBoolean(
              tableProperties,
              TableProperties.ENABLE_AUTO_CREATE_TAG,
              TableProperties.ENABLE_AUTO_CREATE_TAG_DEFAULT));
      tagConfig.setTagFormat(
          CompatiblePropertyUtil.propertyAsString(
              tableProperties,
              TableProperties.AUTO_CREATE_TAG_FORMAT,
              TableProperties.AUTO_CREATE_TAG_FORMAT_DEFAULT));
      tagConfig.setTriggerPeriod(
          CompatiblePropertyUtil.propertyAsString(
              tableProperties,
              TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD,
              TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD_DEFAULT));
      tagConfig.setTriggerOffsetMinutes(
          CompatiblePropertyUtil.propertyAsInt(
              tableProperties,
              TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES,
              TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES_DEFAULT));
      return tagConfig;
    }
  }
}
