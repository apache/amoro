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

package org.apache.amoro.flink.lookup.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.amoro.flink.util.DateTimeUtils;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.data.DecimalData;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.types.DataType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/** Test for {@link RowDataPredicate}. */
public class TestRowDataPredicateAllFieldTypes extends TestRowDataPredicateBase {
  protected RowDataPredicateExpressionVisitor visitor;
  protected final Map<String, Integer> fieldIndexMap = new HashMap<>();
  protected final Map<String, DataType> fieldDataTypeMap = new HashMap<>();
  List<Column> columns = new ArrayList<>();
  protected ResolvedSchema schema;

  @Before
  public void setUp() {
    columns.add(0, Column.physical("f0", DataTypes.INT()));
    columns.add(1, Column.physical("f1", DataTypes.STRING()));
    columns.add(2, Column.physical("f2", DataTypes.CHAR(1)));
    columns.add(3, Column.physical("f3", DataTypes.BOOLEAN()));
    columns.add(4, Column.physical("f4", DataTypes.BINARY(1)));
    columns.add(5, Column.physical("f5", DataTypes.VARBINARY(10)));
    columns.add(6, Column.physical("f6", DataTypes.DECIMAL(38, 10)));
    columns.add(7, Column.physical("f7", DataTypes.TINYINT()));
    columns.add(8, Column.physical("f8", DataTypes.SMALLINT()));
    columns.add(9, Column.physical("f9", DataTypes.BIGINT()));
    columns.add(10, Column.physical("f10", DataTypes.FLOAT()));
    columns.add(11, Column.physical("f11", DataTypes.DOUBLE()));
    columns.add(12, Column.physical("f12", DataTypes.DATE()));
    columns.add(13, Column.physical("f13", DataTypes.TIME()));
    columns.add(14, Column.physical("f14", DataTypes.TIMESTAMP(3)));
    columns.add(15, Column.physical("f15", DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE(3)));
    schema = new ResolvedSchema(columns, Collections.emptyList(), null);
    for (int i = 0; i < columns.size(); i++) {
      Column column = columns.get(i);
      fieldDataTypeMap.put(column.getName(), column.getDataType());
      fieldIndexMap.put(column.getName(), i);
    }
    visitor = new RowDataPredicateExpressionVisitor(fieldIndexMap, fieldDataTypeMap);
  }

  @Test
  public void testInt() {
    String equalExpr = "f0 = 2";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f0", 2)));
    assertFalse(predicate.test(generateRowData("f0", 1)));
  }

  @Test
  public void testString() {
    String equalExpr = "f1 = 'a'";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f1", StringData.fromString("a"))));
    assertFalse(predicate.test(GenericRowData.of("f1", StringData.fromString("b"))));
  }

  @Test
  public void testChar() {
    String equalExpr = "f2 = 'a'";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f2", StringData.fromString("a"))));
    assertFalse(predicate.test(generateRowData("f2", StringData.fromString("b"))));
  }

  @Test
  public void testBoolean() {
    String equalExpr = "f3 = true";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f3", Boolean.TRUE)));
    assertFalse(predicate.test(generateRowData("f3", Boolean.FALSE)));
  }

  //    @Test
  public void testBinary() {
    String equalExpr = "f4 = '1'"; // byte[]
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f4", (byte) 1)));
    assertFalse(predicate.test(generateRowData("f4", (byte) 2)));
  }

  @Test
  public void testDecimal() {
    String equalExpr = "f6 = 1.1";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(
        predicate.test(
            generateRowData("f6", DecimalData.fromBigDecimal(BigDecimal.valueOf(1.1d), 38, 1))));
    assertFalse(
        predicate.test(
            generateRowData("f6", DecimalData.fromBigDecimal(BigDecimal.valueOf(1.2d), 38, 1))));
  }

  //    @Test
  public void testTinyint() {
    String equalExpr = "f7 = cast('1' as tinyint)"; // byte
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f7", 1)));
    assertFalse(predicate.test(generateRowData("f7", 0)));
  }

  //    @Test
  public void testSmallint() {
    String equalExpr = "f8 = 1"; // short
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f8", (short) 1)));
    assertFalse(predicate.test(generateRowData("f8", (short) 0)));
  }

  @Test
  public void testBigint() {
    String equalExpr = "f9 = 1"; // long
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f9", 1L)));
    assertFalse(predicate.test(generateRowData("f9", 0L)));
  }

  //    @Test
  public void testFloat() {
    String equalExpr = "f10 = 1.1";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f10", 1.1f)));
    assertFalse(predicate.test(generateRowData("f10", 1.2f)));
  }

  @Test
  public void testDouble() {
    String equalExpr = "f11 = 1.1";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f11", 1.1d)));
    assertFalse(predicate.test(generateRowData("f11", 1.2d)));
  }

  //  @Test
  public void testTimestamp() {
    String equalExpr = "f14 = TO_TIMESTAMP('2020-01-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(
        predicate.test(
            generateRowData(
                "f14", TimestampData.fromTimestamp(Timestamp.valueOf("2020-01-01 00:00:00")))));
    assertFalse(
        predicate.test(
            generateRowData(
                "f14", TimestampData.fromTimestamp(Timestamp.valueOf("2020-01-01 00:00:01")))));
  }

  //  @Test
  public void testUnixTimestamp() {
    String equalExpr = "f1 = cast(from_unixtime(unix_timestamp(),'yyyy-MM-dd') as String)";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    String format = "yyyy-MM-dd";
    String current =
        DateTimeUtils.formatUnixTimestamp(
            System.currentTimeMillis() / 1000, format, TimeZone.getDefault());
    assertTrue(predicate.test(generateRowData("f1", StringData.fromString(current))));
    assertFalse(predicate.test(generateRowData("f1", StringData.fromString("2020-01-01-01"))));
  }

  //  @Test
  public void testFromUnixTimestampMinus() {
    String equalExpr = "f1 = from_unixtime(unix_timestamp()- 3 * 3600,'yyyy-MM-dd')";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    String format = "yyyy-MM-dd";
    String current =
        DateTimeUtils.formatUnixTimestamp(
            System.currentTimeMillis() / 1000 - 3 * 3600, format, TimeZone.getDefault());
    assertTrue(predicate.test(generateRowData("f1", StringData.fromString(current))));
    assertFalse(predicate.test(generateRowData("f1", StringData.fromString("2020-01-01-01"))));
  }

  @Test
  public void testArithmetic() {
    // bigint type
    String arithmeticExpr = "f9 = (1514356320000 + 1) * 10 / 2";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(arithmeticExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate predicate = resolved.get(0).accept(visitor).get();
    assertTrue(predicate.test(generateRowData("f9", 7571781600005L)));
    assertFalse(predicate.test(generateRowData("f9", 7571781600004L)));
  }

  protected RowData generateRowData(String fieldName, Object val) {
    int index = Integer.parseInt(fieldName.substring(1));
    Object[] objects = new Object[columns.size()];
    objects[index] = val;
    return GenericRowData.of(objects);
  }
}
