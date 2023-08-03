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

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.Table;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RunWith(Parameterized.class)
public class TestTagChecking extends ExecutorTestBase {
  private final String format;
  static final String CUSTOM_FORMAT = "'custom_format'-yyyy-MM-dd='ssss'";

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false), CUSTOM_FORMAT},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, true), null}};
  }

  public TestTagChecking(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, String format) {
    super(catalogTestHelper, tableTestHelper);
    this.format = format;
  }

  @Before
  public void before() {
    if (format != null) {
      getArcticTable().updateProperties()
          .set(TableProperties.AUTO_CREATE_TAG_FORMAT, format)
          .set(TableProperties.AUTO_CREATE_TAG_BRANCH_FORMAT, format)
          .commit();
    }
  }

  private String getCustomFormat(LocalDate date) {
    return "custom_format" + date.minusDays(1).format(DateTimeFormatter.ofPattern("-yyyy-MM-dd=")) + "ssss";
  }

  @Test
  public void testUnkeyedTableCreateTag() {
    Assume.assumeFalse(isKeyedTable());
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    table.newAppend().commit();
    Snapshot snapshot = table.currentSnapshot();
    LocalDate now = LocalDate.now();

    // not enabled
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertNoRef(table);

    // enabled and create a tag
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertTag(table, snapshot, now);
    
    // recheck and should not create new tag
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertTag(table, snapshot, now);
  }

  @Test
  public void testUnkeyedTableCreateBranch() {
    Assume.assumeFalse(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    getArcticTable().updateProperties().set(TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED, "true").commit();
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    table.newAppend().commit();
    Snapshot snapshot = table.currentSnapshot();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(table, snapshot, now);
  }

  @Test
  public void testUnkeyedTableNotCreateOldTag() {
    Assume.assumeFalse(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    table.newAppend().commit();
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        tomorrow).checkAndCreateTodayTag();
    assertNoRef(table);
  }

  @Test
  public void testKeyedTableCreateTag() {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    baseTable.newAppend().commit();
    Snapshot baseSnapshot = baseTable.currentSnapshot();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertTag(baseTable, baseSnapshot, now);

    // recheck and should not create new tag
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertTag(baseTable, baseSnapshot, now);
  }

  @Test
  public void testKeyedTableCreateBranch() {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    getArcticTable().updateProperties().set(TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED, "true").commit();
    UnkeyedTable changeTable = getArcticTable().asKeyedTable().changeTable();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    changeTable.newAppend().commit();
    Snapshot changeSnapshot = changeTable.currentSnapshot();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(changeTable, changeSnapshot, now);
    assertBranch(baseTable, baseTable.currentSnapshot(), now);

    // recheck and should not create new branch
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(changeTable, changeSnapshot, now);
    assertBranch(baseTable, baseTable.currentSnapshot(), now);
  }

  @Test
  public void testKeyedTableCreateBranch2() throws InterruptedException {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    getArcticTable().updateProperties().set(TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED, "true").commit();
    UnkeyedTable changeTable = getArcticTable().asKeyedTable().changeTable();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    baseTable.newAppend().commit();
    baseTable.newAppend().commit();
    Snapshot baseSnapshot = baseTable.currentSnapshot();
    Thread.sleep(2);
    changeTable.newAppend().commit();
    Snapshot changeSnapshot = changeTable.currentSnapshot();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(changeTable, changeSnapshot, now);
    assertBranch(baseTable, baseSnapshot, now);

    // recheck and should not create new branch
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(changeTable, changeSnapshot, now);
    assertBranch(baseTable, baseSnapshot, now);
  }

  @Test
  public void testKeyedTableNotCreateOldTag() {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    UnkeyedTable changeTable = getArcticTable().asKeyedTable().changeTable();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    changeTable.newAppend().commit();
    LocalDate now = LocalDate.now().plusDays(1);
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertNoRef(changeTable);
    assertNoRef(baseTable);
  }

  @Test
  public void testKeyedTableNotCreateBranch() throws InterruptedException {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    getArcticTable().updateProperties().set(TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED, "true").commit();
    UnkeyedTable changeTable = getArcticTable().asKeyedTable().changeTable();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    changeTable.newAppend().commit();
    Thread.sleep(2);
    baseTable.newAppend().commit();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertNoRef(changeTable);
    assertNoRef(baseTable);
  }

  @Test
  public void testKeyedTableNotCreateBranch2() {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    getArcticTable().updateProperties().set(TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED, "true").commit();
    UnkeyedTable changeTable = getArcticTable().asKeyedTable().changeTable();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    baseTable.newAppend().commit();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertNoRef(changeTable);
    assertNoRef(baseTable);
  }

  @Test
  public void testKeyedTableBranchCompensation() throws InterruptedException {
    Assume.assumeTrue(isKeyedTable());
    getArcticTable().updateProperties().set(TableProperties.ENABLE_AUTO_CREATE_TAG, "true").commit();
    getArcticTable().updateProperties().set(TableProperties.AUTO_CREATE_TAG_OPTIMIZE_ENABLED, "true").commit();
    UnkeyedTable changeTable = getArcticTable().asKeyedTable().changeTable();
    UnkeyedTable baseTable = getArcticTable().asKeyedTable().baseTable();
    baseTable.newAppend().commit();
    baseTable.newAppend().commit();
    Snapshot baseSnapshot = baseTable.currentSnapshot();
    Thread.sleep(2);
    changeTable.newAppend().commit();
    Snapshot changeSnapshot = changeTable.currentSnapshot();
    LocalDate now = LocalDate.now();
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(changeTable, changeSnapshot, now);
    assertBranch(baseTable, baseSnapshot, now);
    
    // remove branch on tag
    baseTable.manageSnapshots().removeBranch(branchName(now)).commit();
    assertNoRef(baseTable);

    // compensate branch
    new TagsCheckingExecutor.Checker(getArcticTable(),
        TagsCheckingExecutor.TagConfig.fromTableProperties(getArcticTable().properties()),
        now).checkAndCreateTodayTag();
    assertBranch(changeTable, changeSnapshot, now);
    assertBranch(baseTable, baseSnapshot, now);
  }
  
  private void assertNoRef(Table table) {
    Assert.assertTrue(table.refs().size() <= 1);
    if (table.refs().size() == 1) {
      Assert.assertEquals("main", table.refs().entrySet().iterator().next().getKey());
    }
  }

  private void assertTag(Table table, Snapshot snapshot, LocalDate now) {
    assertRef(table, snapshot, now, true);
  }

  private void assertRef(Table table, Snapshot snapshot, LocalDate now, boolean isTag) {
    Assert.assertEquals(2, table.refs().size());
    // check ref, whose name is not 'main'
    table.refs().entrySet().stream().filter(entry -> !entry.getKey().equals("main"))
        .forEach(entry -> {
          String name = entry.getKey();
          SnapshotRef snapshotRef = entry.getValue();
          Assert.assertEquals(isTag, snapshotRef.isTag());
          Assert.assertEquals(isTag ? tagName(now) : branchName(now), name);
          Assert.assertEquals(snapshot.snapshotId(), snapshotRef.snapshotId());
          Assert.assertNull(snapshotRef.minSnapshotsToKeep());
          Assert.assertNull(snapshotRef.maxSnapshotAgeMs());
          Assert.assertNull(snapshotRef.maxRefAgeMs());
        });
  }

  private void assertBranch(Table table, Snapshot snapshot, LocalDate now) {
    assertRef(table, snapshot, now, false);
  }

  String tagName(LocalDate date) {
    if (format == null) {
      return "auto-tag-" + date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    } else {
      return getCustomFormat(date);
    }
  }

  String branchName(LocalDate date) {
    if (format == null) {
      return "auto-branch-" + date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    } else {
      return getCustomFormat(date);
    }
  }

}
