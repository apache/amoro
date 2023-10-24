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

package com.netease.arctic.server.table.executor;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.formats.iceberg.IcebergTable;
import com.netease.arctic.server.table.TableManager;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TagsCheckingExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(TagsCheckingExecutor.class);

  private static final long INTERVAL = 60 * 1000L; // 1min

  protected TagsCheckingExecutor(TableManager tableManager, int poolSize) {
    super(tableManager, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return tableRuntime.getTableConfiguration().isAutoCreateTagEnabled();
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    try {
      AmoroTable<?> amoroTable = loadTable(tableRuntime);
      new Checker(
              amoroTable, TagConfig.fromTableProperties(amoroTable.properties()), LocalDate.now())
          .checkAndCreateTodayTag();
    } catch (Throwable t) {
      LOG.error("unexpected tag checking error of table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  public static class Checker {
    private final AmoroTable<?> amoroTable;
    private final TagConfig tagConfig;
    private final LocalDate now;

    public Checker(AmoroTable<?> amoroTable, TagConfig tagConfig, LocalDate now) {
      this.amoroTable = amoroTable;
      this.tagConfig = tagConfig;
      this.now = now;
    }

    public void checkAndCreateTodayTag() {
      if (!tagConfig.isAutoCreateTag()) {
        return;
      }
      // check today's tag/branch exist
      if (findRefOfToday(getTable(), false) != null || findRefOfToday(getTable(), true) != null) {
        LOG.debug("{} find today's tag or branch, skip", amoroTable.name());
        return;
      }
      // create today's tag/branch if not exist
      boolean success;
      if (amoroTable.originalTable() instanceof IcebergTable) {
        success = createIcebergTableRef((Table) amoroTable.originalTable());
      } else if (amoroTable.originalTable() instanceof ArcticTable) {
        ArcticTable arcticTable = (ArcticTable) amoroTable.originalTable();
        if (arcticTable.isKeyedTable()) {
          success = createKeyedTableRef(arcticTable.asKeyedTable());
        } else {
          success = createIcebergTableRef(arcticTable.asUnkeyedTable());
        }
      } else {
        // not support other table format
        return;
      }
      LOG.info("{} today's ref creation {}", amoroTable.name(), success ? "succeed" : "skipped");
    }

    private Table getTable() {
      if (amoroTable.originalTable() instanceof IcebergTable) {
        return (Table) amoroTable.originalTable();
      } else if (amoroTable.originalTable() instanceof ArcticTable) {
        ArcticTable arcticTable = (ArcticTable) amoroTable.originalTable();
        return arcticTable.isKeyedTable()
            ? arcticTable.asKeyedTable().baseTable()
            : arcticTable.asUnkeyedTable();
      } else {
        throw new UnsupportedOperationException("only support Iceberg/Mixed Format");
      }
    }

    private String findRefOfToday(Table table, boolean isBranch) {
      DateTimeFormatter formatter;
      if (isBranch) {
        formatter = DateTimeFormatter.ofPattern(tagConfig.getBranchFormat());
      } else {
        formatter = DateTimeFormatter.ofPattern(tagConfig.getTagFormat());
      }
      String name = now.minusDays(1).format(formatter);
      return table.refs().entrySet().stream()
          .filter(entry -> isBranch == entry.getValue().isBranch())
          .map(Map.Entry::getKey)
          .filter(name::equals)
          .findFirst()
          .orElse(null);
    }

    private boolean createIcebergTableRef(Table table) {
      Snapshot snapshot = findSnapshot(table, getTagTriggerTime());
      if (snapshot == null) {
        LOG.info("{} no snapshot found at {}", amoroTable.name(), getTagTriggerTime());
        return false;
      }
      if (!tagConfig.isOptimizingTag()) {
        table.manageSnapshots().createTag(getTodayTagName(), snapshot.snapshotId()).commit();
        LOG.info(
            "{} create today's tag {} on snapshot {} {}",
            amoroTable.name(),
            getTodayTagName(),
            snapshot.snapshotId(),
            snapshot.timestampMillis());
      } else {
        table.manageSnapshots().createBranch(getTodayBranchName(), snapshot.snapshotId()).commit();
        LOG.info(
            "{} create today's branch {} on snapshot {} {}",
            amoroTable.name(),
            getTodayBranchName(),
            snapshot.snapshotId(),
            snapshot.timestampMillis());
      }
      return true;
    }

    private boolean createKeyedTableRef(KeyedTable table) {
      BaseTable baseStore = table.baseTable();
      ChangeTable changeStore = table.changeTable();
      if (!tagConfig.isOptimizingTag()) {
        return createIcebergTableRef(baseStore);
      } else {
        // 1.create change branch
        Snapshot changeSnapshot;
        String branchOfChange = findRefOfToday(changeStore, true);
        if (branchOfChange != null) {
          // branch on change store already exists
          SnapshotRef snapshotRef = changeStore.refs().get(branchOfChange);
          long changeSnapshotId = snapshotRef.snapshotId();
          changeSnapshot = changeStore.snapshot(changeSnapshotId);
        } else {
          // branch on change store not exists
          changeSnapshot = findSnapshot(changeStore, getTagTriggerTime());
          if (changeSnapshot == null) {
            LOG.debug("{} find no snapshot on change store, skip", amoroTable.name());
            return false;
          }
        }
        // 2.create base branch
        Snapshot baseSnapshot = null;
        if (!baseStore.snapshots().iterator().hasNext()) {
          // create an empty base snapshot if there are no snapshot on base store
          baseStore.newAppend().commit();
          baseSnapshot = baseStore.currentSnapshot();
        } else {
          // find the latest base snapshot before changeSnapshot
          for (Snapshot snapshot : baseStore.snapshots()) {
            if (snapshot.timestampMillis() > changeSnapshot.timestampMillis()) {
              break;
            }
            baseSnapshot = snapshot;
          }
        }
        if (baseSnapshot == null) {
          LOG.debug("{} find no snapshot on base store, skip", amoroTable.name());
          return false;
        }
        if (branchOfChange == null) {
          // create change branch if branch not exists
          changeStore
              .manageSnapshots()
              .createBranch(getTodayBranchName(), changeSnapshot.snapshotId())
              .commit();
          LOG.info(
              "{} create today's change branch {} on snapshot {} {}",
              amoroTable.name(),
              getTodayBranchName(),
              changeSnapshot.snapshotId(),
              changeSnapshot.timestampMillis());
        }
        baseStore
            .manageSnapshots()
            .createBranch(getTodayBranchName(), baseSnapshot.snapshotId())
            .commit();
        LOG.info(
            "{} create today's base branch {} on snapshot {} {}",
            amoroTable.name(),
            getTodayBranchName(),
            baseSnapshot.snapshotId(),
            baseSnapshot.timestampMillis());
        return true;
      }
    }

    private String getTodayTagName() {
      String tagFormat = tagConfig.getTagFormat();
      return now.minusDays(1).format(DateTimeFormatter.ofPattern(tagFormat));
    }

    private String getTodayBranchName() {
      String branchFormat = tagConfig.getBranchFormat();
      return now.minusDays(1).format(DateTimeFormatter.ofPattern(branchFormat));
    }

    private long getTagTriggerTime() {
      String triggerDayTime = tagConfig.getTriggerDayTime();
      LocalTime localTime = LocalTime.parse(triggerDayTime, DateTimeFormatter.ofPattern("HH:mm"));
      LocalDateTime localDateTime = LocalDateTime.of(now, localTime);
      // TODO support time zone
      return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
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

  static class TagConfig {
    private boolean autoCreateTag;
    private String tagFormat;
    private String branchFormat;
    private String triggerDayTime;
    private boolean optimizingTag;

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

    public String getBranchFormat() {
      return branchFormat;
    }

    public void setBranchFormat(String branchFormat) {
      this.branchFormat = branchFormat;
    }

    public String getTriggerDayTime() {
      return triggerDayTime;
    }

    public void setTriggerDayTime(String triggerDayTime) {
      this.triggerDayTime = triggerDayTime;
    }

    public boolean isOptimizingTag() {
      return optimizingTag;
    }

    public void setOptimizingTag(boolean optimizingTag) {
      this.optimizingTag = optimizingTag;
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
      tagConfig.setTriggerDayTime(
          CompatiblePropertyUtil.propertyAsString(
              tableProperties,
              TableProperties.AUTO_CREATE_TAG_TRIGGER_DAY_TIME,
              TableProperties.AUTO_CREATE_TAG_TRIGGER_DAY_TIME_DEFAULT));
      tagConfig.setOptimizingTag(
          CompatiblePropertyUtil.propertyAsBoolean(
              tableProperties,
              TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED,
              TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED_DEFAULT));
      tagConfig.setBranchFormat(
          CompatiblePropertyUtil.propertyAsString(
              tableProperties,
              TableProperties.AUTO_CREATE_TAG_BRANCH_FORMAT,
              TableProperties.AUTO_CREATE_TAG_BRANCH_FORMAT_DEFAULT));
      return tagConfig;
    }
  }
}
