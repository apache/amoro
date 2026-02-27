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

import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.LOOKUP_CACHE_TTL_AFTER_WRITE;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.ROCKSDB_WRITING_THREADS;
import static org.junit.Assert.assertEquals;

import org.apache.amoro.flink.lookup.filter.RowDataPredicate;
import org.apache.amoro.flink.lookup.filter.RowDataPredicateExpressionVisitor;
import org.apache.amoro.flink.lookup.filter.TestRowDataPredicateBase;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.metrics.groups.UnregisteredMetricsGroup;
import org.apache.flink.shaded.guava31.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava31.com.google.common.cache.CacheBuilder;
import org.apache.flink.shaded.guava31.com.google.common.collect.Lists;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.catalog.UniqueConstraint;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.binary.BinaryRowData;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.runtime.typeutils.BinaryRowDataSerializer;
import org.apache.flink.table.runtime.typeutils.RowDataSerializer;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RunWith(value = Parameterized.class)
public class TestKVTable extends TestRowDataPredicateBase {
  private static final Logger LOG = LoggerFactory.getLogger(TestKVTable.class);
  @Rule public TemporaryFolder temp = new TemporaryFolder();
  @Rule public TestName name = new TestName();
  private final Configuration config = new Configuration();
  private final List<String> primaryKeys = Lists.newArrayList("id", "grade");
  private final List<String> primaryKeysDisorder = Lists.newArrayList("grade", "num", "id");

  private final boolean guavaCacheEnabled;

  private final Schema mixedTableSchema =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "grade", Types.StringType.get()),
          Types.NestedField.required(3, "num", Types.IntegerType.get()));

  private String dbPath;

  @Parameterized.Parameters(name = "guavaCacheEnabled = {0}")
  public static Object[][] parameters() {
    return new Object[][] {{true}, {false}};
  }

  public TestKVTable(boolean guavaCacheEnabled) {
    this.guavaCacheEnabled = guavaCacheEnabled;
  }

  @Before
  public void before() throws IOException {
    dbPath = temp.newFolder().getPath();
    if (!guavaCacheEnabled) {
      config.set(MixedFormatValidator.LOOKUP_CACHE_MAX_ROWS, 0L);
    }
  }

  @Test
  public void testRowDataSerializer() throws IOException {
    BinaryRowDataSerializer binaryRowDataSerializer = new BinaryRowDataSerializer(3);

    GenericRowData genericRowData = (GenericRowData) row(1, "2", 3);
    RowType rowType = FlinkSchemaUtil.convert(mixedTableSchema);
    RowDataSerializer rowDataSerializer = new RowDataSerializer(rowType);
    BinaryRowData record = rowDataSerializer.toBinaryRow(genericRowData);

    DataOutputSerializer view = new DataOutputSerializer(32);
    binaryRowDataSerializer.serialize(record, view);
    System.out.println(Arrays.toString(view.getCopyOfBuffer()));

    BinaryRowData desRowData =
        binaryRowDataSerializer.deserialize(new DataInputDeserializer(view.getCopyOfBuffer()));
    Assert.assertNotNull(desRowData);
    Assert.assertEquals(record.getInt(0), desRowData.getInt(0));
    Assert.assertEquals(record.getInt(1), desRowData.getInt(1));
    Assert.assertEquals(record.getInt(2), desRowData.getInt(2));

    // test join key rowData
    binaryRowDataSerializer = new BinaryRowDataSerializer(2);
    List<String> keys = Lists.newArrayList("id", "grade");
    Schema keySchema = mixedTableSchema.select(keys);
    rowType = FlinkSchemaUtil.convert(keySchema);
    rowDataSerializer = new RowDataSerializer(rowType);
    KeyRowData keyRowData = new KeyRowData(new int[] {0, 1}, row(2, "3", 4));
    KeyRowData keyRowData1 = new KeyRowData(new int[] {0, 1}, row(2, "3", 4));

    BinaryRowData binaryRowData = rowDataSerializer.toBinaryRow(keyRowData);
    view.clear();
    binaryRowDataSerializer.serialize(binaryRowData, view);
    byte[] rowBytes = view.getCopyOfBuffer();

    BinaryRowData binaryRowData1 = rowDataSerializer.toBinaryRow(keyRowData1);
    view.clear();
    binaryRowDataSerializer.serialize(binaryRowData1, view);
    byte[] rowBytes1 = view.getCopyOfBuffer();
    Assert.assertArrayEquals(rowBytes1, rowBytes);
  }

  @Test
  public void testInitialUniqueKeyTable() throws IOException {
    config.setInteger(ROCKSDB_WRITING_THREADS, 5);
    List<String> joinKeys = Lists.newArrayList("id", "grade");
    try (UniqueIndexTable uniqueIndexTable = (UniqueIndexTable) createTable(joinKeys)) {
      uniqueIndexTable.open();

      // During the initialization phase, the Merge-on-Read approach is used to retrieve data,
      // which will only return INSERT data.
      // When there are multiple entries with the same primary key, only one entry will be returned.
      initTable(
          uniqueIndexTable,
          upsertStream(
              row(RowKind.INSERT, 1, "1", 1),
              row(RowKind.INSERT, 2, "2", 2),
              row(RowKind.INSERT, 2, "3", 3),
              row(RowKind.INSERT, 2, "4", 4),
              row(RowKind.INSERT, 2, "5", 5)));

      if (!uniqueIndexTable.initialized()) {
        uniqueIndexTable.waitInitializationCompleted();
      }

      assertTable(
          uniqueIndexTable,
          row(1, "1"),
          row(1, "1", 1),
          row(2, "2"),
          row(2, "2", 2),
          row(2, "3"),
          row(2, "3", 3),
          row(2, "4"),
          row(2, "4", 4),
          row(2, "5"),
          row(2, "5", 5));

      // upsert table
      upsertTable(
          uniqueIndexTable,
          upsertStream(
              row(RowKind.DELETE, 1, "1", 1),
              row(RowKind.INSERT, 2, "2", 2),
              row(RowKind.DELETE, 2, "2", 2),
              row(RowKind.UPDATE_BEFORE, 3, "3", 4),
              row(RowKind.UPDATE_AFTER, 3, "3", 5),
              row(RowKind.INSERT, 4, "4", 4)));

      assertTable(
          uniqueIndexTable,
          row(1, "1"),
          null,
          row(2, "2"),
          null,
          row(3, "3"),
          row(3, "3", 5),
          row(4, "4"),
          row(4, "4", 4));
    }
  }

  @Test
  public void testSecondaryKeysMapping() throws IOException {
    // primary keys are id and grade.
    List<String> joinKeys = Lists.newArrayList("grade", "id");
    try (SecondaryIndexTable secondaryIndexTable =
        (SecondaryIndexTable) createTableWithDisorderPK(joinKeys)) {
      secondaryIndexTable.open();

      initTable(
          secondaryIndexTable,
          upsertStream(
              row(RowKind.INSERT, 1, "1", 1),
              row(RowKind.INSERT, 2, "2", 2),
              row(RowKind.INSERT, 2, "3", 3),
              row(RowKind.INSERT, 2, "3", 4),
              row(RowKind.INSERT, 2, "5", 5)));

      if (!secondaryIndexTable.initialized()) {
        secondaryIndexTable.waitInitializationCompleted();
      }

      assertTableSet(secondaryIndexTable, row("1", 1), row(1, "1", 1));
      assertTableSet(secondaryIndexTable, row("2", 2), row(2, "2", 2));

      upsertTable(
          secondaryIndexTable,
          upsertStream(
              row(RowKind.DELETE, 1, "1", 1),
              row(RowKind.INSERT, 2, "2", 2),
              row(RowKind.DELETE, 2, "2", 2),
              row(RowKind.UPDATE_BEFORE, 3, "3", 4),
              row(RowKind.UPDATE_AFTER, 3, "3", 5),
              row(RowKind.INSERT, 3, "4", 4)));

      assertTableSet(secondaryIndexTable, row("1", 1), null);
      assertTableSet(secondaryIndexTable, row("3", 2), row(2, "3", 3), row(2, "3", 4));
      assertTableSet(secondaryIndexTable, row("4", 3), row(3, "4", 4));
    }
  }

  @Test
  public void testInitialSecondaryKeyTable() throws IOException {
    config.setInteger(ROCKSDB_WRITING_THREADS, 10);
    config.set(LOOKUP_CACHE_TTL_AFTER_WRITE, Duration.ofMinutes(1000));
    // primary keys are id and grade.
    List<String> joinKeys = Lists.newArrayList("id");
    try (SecondaryIndexTable secondaryIndexTable = (SecondaryIndexTable) createTable(joinKeys)) {
      writeAndAssert(secondaryIndexTable);
    }
  }

  private void writeAndAssert(SecondaryIndexTable secondaryIndexTable) throws IOException {
    secondaryIndexTable.open();

    initTable(
        secondaryIndexTable,
        upsertStream(
            row(RowKind.INSERT, 1, "1", 1),
            row(RowKind.INSERT, 2, "2", 2),
            row(RowKind.INSERT, 2, "3", 3),
            row(RowKind.INSERT, 2, "4", 4),
            row(RowKind.INSERT, 2, "5", 5)));

    if (!secondaryIndexTable.initialized()) {
      secondaryIndexTable.waitInitializationCompleted();
    }

    assertTableSet(secondaryIndexTable, row(1), row(1, "1", 1));
    assertTableSet(
        secondaryIndexTable,
        row(2),
        row(2, "2", 2),
        row(2, "3", 3),
        row(2, "4", 4),
        row(2, "5", 5));

    upsertTable(
        secondaryIndexTable,
        upsertStream(
            row(RowKind.DELETE, 1, "1", 1),
            row(RowKind.INSERT, 2, "2", 2),
            row(RowKind.DELETE, 2, "2", 2),
            row(RowKind.UPDATE_BEFORE, 3, "3", 4),
            row(RowKind.UPDATE_AFTER, 3, "3", 5),
            row(RowKind.INSERT, 3, "4", 4)));

    assertTableSet(secondaryIndexTable, row(1), null);
    assertTableSet(secondaryIndexTable, row(2), row(2, "3", 3), row(2, "4", 4), row(2, "5", 5));
    assertTableSet(secondaryIndexTable, row(3), row(3, "3", 5), row(3, "4", 4));
  }

  @Test
  public void testCacheExpired() throws InterruptedException {
    Cache<Integer, Integer> cache =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(1)).build();
    cache.put(1, 1);
    cache
        .asMap()
        .compute(
            2,
            (k, v) -> {
              if (v == null) {
                return k;
              }
              return v;
            });
    Assert.assertEquals(Integer.valueOf(1), cache.getIfPresent(1));
    Assert.assertEquals(Integer.valueOf(2), cache.getIfPresent(2));
    Thread.sleep(1001);
    Assert.assertEquals(2, cache.size());
    Assert.assertNull(cache.getIfPresent(1));
    Assert.assertNull(cache.getIfPresent(2));
    cache.cleanUp();
    cache.put(3, 3);
    Assert.assertEquals(1, cache.size());
    Assert.assertNull(cache.getIfPresent(1));
    Assert.assertEquals(Integer.valueOf(3), cache.getIfPresent(3));
  }

  @Test
  public void testPredicate() throws IOException {
    String filter = "id >= 2 and num < 5 and num > 2";
    Optional<RowDataPredicate> rowDataPredicate = generatePredicate(filter);

    KVTable<RowData> uniqueIndexTable =
        createTable(Lists.newArrayList("id", "grade"), rowDataPredicate);
    uniqueIndexTable.open();
    initTable(
        uniqueIndexTable,
        upsertStream(
            row(RowKind.INSERT, 1, "1", 1),
            row(RowKind.INSERT, 2, "2", 2),
            row(RowKind.INSERT, 2, "3", 3),
            row(RowKind.INSERT, 3, "4", 4),
            row(RowKind.INSERT, 3, "5", 5)));

    if (!uniqueIndexTable.initialized()) {
      uniqueIndexTable.waitInitializationCompleted();
    }

    assertTable(
        uniqueIndexTable,
        row(1, "1"),
        null,
        row(2, "2"),
        null,
        row(2, "3"),
        row(2, "3", 3),
        row(3, "4"),
        row(3, "4", 4),
        row(3, "5"),
        null);

    // upsert table
    upsertTable(
        uniqueIndexTable,
        upsertStream(
            row(RowKind.DELETE, 1, "1", 1),
            row(RowKind.INSERT, 2, "2", 2),
            row(RowKind.DELETE, 2, "2", 2),
            row(RowKind.UPDATE_BEFORE, 2, "3", 3),
            row(RowKind.UPDATE_AFTER, 2, "3", 5),
            row(RowKind.INSERT, 4, "4", 4)));

    assertTable(
        uniqueIndexTable,
        row(1, "1"),
        null,
        row(2, "2"),
        null,
        row(2, "3"),
        null,
        row(4, "4"),
        row(4, "4", 4));
  }

  @Test
  public void testSecondaryIndexPredicate() throws IOException {
    String filter = "id >= 2 and num < 5 and num > 2";
    Optional<RowDataPredicate> rowDataPredicate = generatePredicate(filter);

    // primary keys are id and grade.
    List<String> joinKeys = Lists.newArrayList("id");
    try (SecondaryIndexTable secondaryIndexTable =
        (SecondaryIndexTable) createTable(joinKeys, rowDataPredicate)) {
      secondaryIndexTable.open();

      initTable(
          secondaryIndexTable,
          upsertStream(
              row(RowKind.INSERT, 1, "1", 1),
              row(RowKind.INSERT, 2, "2", 2),
              row(RowKind.INSERT, 2, "3", 3),
              row(RowKind.INSERT, 2, "4", 4),
              row(RowKind.INSERT, 2, "5", 5)));

      if (!secondaryIndexTable.initialized()) {
        secondaryIndexTable.waitInitializationCompleted();
      }

      assertTableSet(secondaryIndexTable, row(1), null);
      assertTableSet(secondaryIndexTable, row(2), row(2, "3", 3), row(2, "4", 4));

      upsertTable(
          secondaryIndexTable,
          upsertStream(
              row(RowKind.DELETE, 1, "1", 1),
              row(RowKind.INSERT, 2, "2", 2),
              row(RowKind.DELETE, 2, "2", 2),
              row(RowKind.UPDATE_BEFORE, 3, "3", 4),
              row(RowKind.UPDATE_AFTER, 3, "3", 5),
              row(RowKind.INSERT, 3, "4", 4)));

      assertTableSet(secondaryIndexTable, row(1), null);
      assertTableSet(secondaryIndexTable, row(2), row(2, "3", 3), row(2, "4", 4));
      assertTableSet(secondaryIndexTable, row(3), row(3, "4", 4));
    }
  }

  private Optional<RowDataPredicate> generatePredicate(String filterSql) {
    Map<String, Integer> fieldIndexMap = new HashMap<>();
    Map<String, DataType> fieldTypeMap = new HashMap<>();
    List<Types.NestedField> fields = mixedTableSchema.asStruct().fields();
    List<Column> columns = new ArrayList<>(fields.size());
    for (int i = 0; i < fields.size(); i++) {
      String name = fields.get(i).name();
      DataType dataType =
          TypeConversions.fromLogicalToDataType(FlinkSchemaUtil.convert(fields.get(i).type()));
      fieldIndexMap.put(name, i);
      fieldTypeMap.put(name, dataType);
      columns.add(i, Column.physical(name, dataType));
    }
    ResolvedSchema schema =
        new ResolvedSchema(
            columns, Collections.emptyList(), UniqueConstraint.primaryKey("", primaryKeys));

    RowDataPredicateExpressionVisitor rowDataPredicateExpressionVisitor =
        new RowDataPredicateExpressionVisitor(fieldIndexMap, fieldTypeMap);

    List<ResolvedExpression> expressions = resolveSQLFilterToExpression(filterSql, schema);
    assertEquals(1, expressions.size());
    return expressions.get(0).accept(rowDataPredicateExpressionVisitor);
  }

  private KVTable<RowData> createTableWithDisorderPK(List<String> joinKeys) {
    return createTable(joinKeys, Optional.empty(), true);
  }

  private KVTable<RowData> createTable(
      List<String> joinKeys, Optional<RowDataPredicate> rowDataPredicate) {
    return createTable(joinKeys, rowDataPredicate, false);
  }

  private KVTable<RowData> createTable(
      List<String> joinKeys, Optional<RowDataPredicate> rowDataPredicate, boolean isDisorderPK) {
    return KVTableFactory.INSTANCE.create(
        new RowDataStateFactory(dbPath, new UnregisteredMetricsGroup()),
        isDisorderPK ? primaryKeysDisorder : primaryKeys,
        joinKeys,
        mixedTableSchema,
        config,
        rowDataPredicate.orElse(null));
  }

  private KVTable<RowData> createTable(List<String> joinKeys) {
    return createTable(joinKeys, Optional.empty());
  }

  private void initTable(KVTable<RowData> table, Iterator<RowData> initStream) throws IOException {
    if (initStream != null) {
      table.initialize(initStream);
    }
  }

  private void upsertTable(KVTable<RowData> table, Iterator<RowData> upsertStream, RowData... rows)
      throws IOException {
    if (upsertStream != null) {
      table.upsert(upsertStream);
    }
  }

  private void assertTable(KVTable<RowData> table, RowData... rows) throws IOException {
    // Loop through the rows array in steps of 2
    for (int i = 0; i < rows.length; i = i + 2) {
      // Get the key and expected value at the current index and the next index
      RowData key = rows[i], expected = rows[i + 1];

      List<RowData> values = table.get(key);
      Assert.assertNotNull(values);
      if (expected == null) {
        Assert.assertEquals(0, values.size());
        continue;
      }
      Assert.assertEquals(expected.toString(), 1, values.size());
      RowData actual = values.get(0);
      assertRecord(expected, actual);
    }
  }

  private void assertTableSet(KVTable<RowData> table, RowData key, RowData... expects)
      throws IOException {
    List<RowData> values = table.get(key);
    if (expects == null) {
      Assert.assertEquals(0, values.size());
      return;
    }
    Assert.assertEquals(expects.length, values.size());
    values = values.stream().sorted(compare()).collect(Collectors.toList());
    List<RowData> expectsAfterSort =
        Arrays.stream(expects).sorted(compare()).collect(Collectors.toList());
    for (int i = 0; i < expects.length; i = i + 1) {
      // Get the key and expected value at the current index and the next index
      RowData expected = expectsAfterSort.get(i);

      RowData actual = values.get(i);
      assertRecord(expected, actual);
    }
  }

  private Comparator<RowData> compare() {
    return Comparator.comparingInt((RowData o) -> o.getInt(0))
        .thenComparing(o -> o.getString(1))
        .thenComparingInt(o -> o.getInt(2));
  }

  private void assertRecord(RowData expected, RowData actual) {
    if (!(actual instanceof BinaryRowData)) {
      throw new IllegalArgumentException("Only support BinaryRowData");
    }
    BinaryRowData binaryRowData = (BinaryRowData) actual;
    for (int j = 0; j < binaryRowData.getArity(); j++) {
      switch (j) {
        case 0:
        case 2:
          Assert.assertEquals(
              String.format("expected:%s, actual:%s.", expected.toString(), actual),
              expected.getInt(j),
              binaryRowData.getInt(j));
          break;
        case 1:
          Assert.assertEquals(
              String.format("expected:%s, actual:%s.", expected, actual),
              expected.getString(j),
              binaryRowData.getString(j));
          break;
      }
    }
  }

  RowData row(RowKind rowKind, Object... objects) {
    return GenericRowData.ofKind(rowKind, wrapStringData(objects));
  }

  RowData row(Object... objects) {
    return GenericRowData.of(wrapStringData(objects));
  }

  Object[] wrapStringData(Object... objects) {
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] instanceof String) {
        objects[i] = StringData.fromString(objects[i].toString());
      }
    }
    return objects;
  }

  Iterator<RowData> upsertStream(RowData... rows) {
    return Lists.newArrayList(rows).iterator();
  }
}
