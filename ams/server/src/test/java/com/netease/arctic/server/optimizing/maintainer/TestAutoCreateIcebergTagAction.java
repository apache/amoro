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

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.server.table.TagConfiguration;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.Table;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TestAutoCreateIcebergTagAction extends TableTestBase {

  public TestAutoCreateIcebergTagAction() {
    super(new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true));
  }

  @Test
  public void testDefaultNotEnableCreateTag() {
    Table table = getArcticTable().asUnkeyedTable();
    checkNoTag(table);
    newAutoCreateIcebergTagAction(table, LocalDateTime.now()).execute();
    checkNoTag(table);
  }

  @NotNull
  private AutoCreateIcebergTagAction newAutoCreateIcebergTagAction(Table table, LocalDateTime now) {
    TagConfiguration tagConfig = TagConfiguration.parse(table.properties());
    return new AutoCreateIcebergTagAction(table, tagConfig, now);
  }

  @Test
  public void testCreateDailyTag() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "0")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime now = fromEpochMillis(snapshot.timestampMillis());
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
    checkTag(table, "tag-" + formatDate(now.minusDays(1)), snapshot);

    // should not recreate tag
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
  }

  @Test
  public void testCreateDailyOffsetTag() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "0")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime now = fromEpochMillis(snapshot.timestampMillis());
    // We calculate the offset from the snapshot timestamp, and +1 minute to ensure the snapshot
    // won't exceed the offset to create tag
    long offsetMinutesOfToday = getOffsetMinutesOfToday(snapshot.timestampMillis()) + 1;
    table
        .updateProperties()
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES, offsetMinutesOfToday + "")
        .commit();
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 0);

    // Offset -1 minute to make the snapshot exceed the offset to create tag
    offsetMinutesOfToday--;
    table
        .updateProperties()
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES, offsetMinutesOfToday + "")
        .commit();
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
    checkTag(table, "tag-" + formatDate(now.minusDays(1)), snapshot);

    // should not recreate tag
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
  }

  @Test
  public void testNotCreateDelayDailyTag() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "1440")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime now = fromEpochMillis(snapshot.timestampMillis());
    LocalDateTime yesterday = now.minusDays(1);

    // should not create yesterday tag
    newAutoCreateIcebergTagAction(table, yesterday).execute();
    checkNoTag(table);

    // should create today tag
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
    checkTag(table, "tag-" + formatDate(now.minusDays(1)), snapshot);
  }

  @Test
  public void testTagFormat() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "0")
        .commit();
    table
        .updateProperties()
        .set(TableProperties.AUTO_CREATE_TAG_DAILY_FORMAT, "'custom-tag-'yyyyMMdd'-auto'")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime now = fromEpochMillis(snapshot.timestampMillis());
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
    checkTag(table, "custom-tag-" + formatDate(now.minusDays(1)) + "-auto", snapshot);

    // should not recreate tag
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
  }

  @Test
  public void testTriggerTimePeriod() {
    testTagTriggerTimePeriodHourly("2022-08-08T11:40:00", 30, "2022-08-08T11:30:00");
    testTagTriggerTimePeriodHourly("2022-08-08T23:40:00", 15, "2022-08-08T23:15:00");
    testTagTriggerTimePeriodHourly("2022-08-09T00:10:00", 30, "2022-08-08T23:30:00");

    testTagTriggerTimePeriodDaily("2022-08-08T03:40:00", 30, "2022-08-08T00:30:00");
    testTagTriggerTimePeriodDaily("2022-08-08T23:40:00", 15, "2022-08-08T00:15:00");
    testTagTriggerTimePeriodDaily("2022-08-09T00:10:00", 30, "2022-08-08T00:30:00");
  }

  private void testTagTriggerTimePeriodHourly(
      String checkTimeStr, int offsetMinutes, String expectedResultStr) {
    LocalDateTime checkTime = LocalDateTime.parse(checkTimeStr);
    Long expectedTriggerTime =
        (expectedResultStr == null)
            ? null
            : LocalDateTime.parse(expectedResultStr)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

    Long actualTriggerTime =
        TagConfiguration.Period.HOURLY.getTagTriggerTime(checkTime, offsetMinutes);

    Assert.assertEquals(expectedTriggerTime, actualTriggerTime);
  }

  private void testTagTriggerTimePeriodDaily(
      String checkTimeStr, int offsetMinutes, String expectedResultStr) {
    LocalDateTime checkTime = LocalDateTime.parse(checkTimeStr);
    Long expectedTriggerTime =
        (expectedResultStr == null)
            ? null
            : LocalDateTime.parse(expectedResultStr)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

    Long actualTriggerTime =
        TagConfiguration.Period.DAILY.getTagTriggerTime(checkTime, offsetMinutes);

    Assert.assertEquals(expectedTriggerTime, actualTriggerTime);
  }

  private long getOffsetMinutesOfToday(long millis) {
    LocalDateTime now = fromEpochMillis(millis);
    LocalDateTime today = LocalDateTime.of(now.toLocalDate(), LocalTime.ofSecondOfDay(0));
    Duration between = Duration.between(today, now);
    return between.toMinutes();
  }

  private LocalDateTime fromEpochMillis(long millis) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
  }

  private String formatDate(LocalDateTime localDateTime) {
    return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  private void checkNoTag(Table table) {
    Assert.assertFalse(table.refs().values().stream().anyMatch(SnapshotRef::isTag));
  }

  private void checkTagCount(Table table, int count) {
    Assert.assertEquals(count, table.refs().values().stream().filter(SnapshotRef::isTag).count());
  }

  private void checkTag(Table table, String tagName, Snapshot snapshot) {
    SnapshotRef snapshotRef = table.refs().get(tagName);
    Assert.assertNotNull(snapshotRef);
    Assert.assertTrue(snapshotRef.isTag());
    Assert.assertEquals(snapshot.snapshotId(), snapshotRef.snapshotId());
  }

  private void checkSnapshots(Table table, int count) {
    Assert.assertEquals(Iterables.size(table.snapshots()), count);
  }
}
