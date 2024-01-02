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
  public void testCreateHourlyTag() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "0")
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD, "hourly")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime now = fromEpochMillis(snapshot.timestampMillis());
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
    checkTag(table, "tag-" + formatDateTime(now.minusHours(1)), snapshot);

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
    checkTag(table, "tag-" + formatDate(now.minusDays(2)), snapshot);

    // Offset -1 minute to make the snapshot exceed the offset to create tag
    offsetMinutesOfToday--;
    table
        .updateProperties()
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES, offsetMinutesOfToday + "")
        .commit();
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 2);
    checkTag(table, "tag-" + formatDate(now.minusDays(1)), snapshot);

    // should not recreate tag
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 2);
  }

  @Test
  public void testCreateHourlyOffsetTag() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "0")
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD, "hourly")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime testDateTime = fromEpochMillis(snapshot.timestampMillis());
    long offsetMinutesOfHour = getOffsetMinutesOfHour(snapshot.timestampMillis()) + 1;
    table
        .updateProperties()
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES, offsetMinutesOfHour + "")
        .commit();
    newAutoCreateIcebergTagAction(table, testDateTime).execute();
    checkTag(table, "tag-" + formatDateTime(testDateTime.minusHours(2)), snapshot);

    offsetMinutesOfHour--;
    table
        .updateProperties()
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES, offsetMinutesOfHour + "")
        .commit();
    newAutoCreateIcebergTagAction(table, testDateTime).execute();
    checkTagCount(table, 2);
    checkTag(table, "tag-" + formatDateTime(testDateTime.minusHours(1)), snapshot);

    newAutoCreateIcebergTagAction(table, testDateTime).execute();
    checkTagCount(table, 2);
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
  public void testNotCreateDelayHourlyTag() {
    Table table = getArcticTable().asUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true")
        .set(TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD, "hourly")
        .set(TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES, "60")
        .commit();
    table.newAppend().commit();
    checkSnapshots(table, 1);
    checkNoTag(table);

    Snapshot snapshot = table.currentSnapshot();
    LocalDateTime now = fromEpochMillis(snapshot.timestampMillis());
    LocalDateTime lastHour = now.minusHours(1);

    // should not create last hour tag
    newAutoCreateIcebergTagAction(table, lastHour).execute();
    checkNoTag(table);

    // should create this hour tag
    newAutoCreateIcebergTagAction(table, now).execute();
    checkTagCount(table, 1);
    checkTag(table, "tag-" + formatDateTime(now.minusHours(1)), snapshot);
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
        .set(TableProperties.AUTO_CREATE_TAG_FORMAT, "'custom-tag-'yyyyMMdd'-auto'")
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
    testTagTimePeriodHourly("2022-08-08T11:40:00", 30, "2022-08-08T11:00:00");
    testTagTimePeriodHourly("2022-08-08T23:40:00", 15, "2022-08-08T23:00:00");
    testTagTimePeriodHourly("2022-08-09T00:10:00", 30, "2022-08-08T23:00:00");

    testTagTimePeriodDaily("2022-08-08T03:40:00", 30, "2022-08-08T00:00:00");
    testTagTimePeriodDaily("2022-08-08T23:40:00", 15, "2022-08-08T00:00:00");
    testTagTimePeriodDaily("2022-08-09T00:10:00", 30, "2022-08-08T00:00:00");
  }

  private void testTagTimePeriodHourly(
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
        TagConfiguration.Period.HOURLY
            .getTagTime(checkTime, offsetMinutes)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

    Assert.assertEquals(expectedTriggerTime, actualTriggerTime);
  }

  private void testTagTimePeriodDaily(
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
        TagConfiguration.Period.DAILY
            .getTagTime(checkTime, offsetMinutes)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

    Assert.assertEquals(expectedTriggerTime, actualTriggerTime);
  }

  private long getOffsetMinutesOfToday(long millis) {
    LocalDateTime now = fromEpochMillis(millis);
    LocalDateTime today = LocalDateTime.of(now.toLocalDate(), LocalTime.ofSecondOfDay(0));
    Duration between = Duration.between(today, now);
    return between.toMinutes();
  }

  private long getOffsetMinutesOfHour(long millis) {
    LocalDateTime now = fromEpochMillis(millis);
    return now.getMinute();
  }

  private LocalDateTime fromEpochMillis(long millis) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
  }

  private String formatDate(LocalDateTime localDateTime) {
    return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  private String formatDateTime(LocalDateTime localDateTime) {
    return localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
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
