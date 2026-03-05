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

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.types.DataType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class contains unit tests for the {@link RowDataPredicateExpressionVisitor} class. */
public class TestRowDataPredicateExpressionVisitor extends TestRowDataPredicateBase {

  RowDataPredicateExpressionVisitor visitor;
  final Map<String, Integer> fieldIndexMap = new HashMap<>();
  final Map<String, DataType> fieldDataTypeMap = new HashMap<>();
  List<Column> columns = new ArrayList<>();
  ResolvedSchema schema;

  @Before
  public void setUp() {
    columns.add(0, Column.physical("id", DataTypes.INT()));
    columns.add(1, Column.physical("name", DataTypes.STRING()));
    columns.add(2, Column.physical("age", DataTypes.INT()));
    schema = new ResolvedSchema(columns, Collections.emptyList(), null);
    for (int i = 0; i < columns.size(); i++) {
      Column column = columns.get(i);
      fieldDataTypeMap.put(column.getName(), column.getDataType());
      fieldIndexMap.put(column.getName(), i);
    }

    visitor = new RowDataPredicateExpressionVisitor(fieldIndexMap, fieldDataTypeMap);
  }

  @Test
  public void testVisitCallExpressionEquals() {
    String equalExpr = "id = NULL";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(equalExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 6)));
  }

  @Test
  public void testVisitCallExpressionNotEquals() {
    String notEqualExpr = "id <> 1";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(notEqualExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(2, StringData.fromString("2"), 6)));
    assertFalse(rowDataPredicate.test(GenericRowData.of(1, StringData.fromString("2"), 6)));
  }

  @Test
  public void testVisitCallExpressionGreaterThanOrEqual() {
    String greaterThanOrEqualExpr = "age >= 5";
    List<ResolvedExpression> resolved =
        resolveSQLFilterToExpression(greaterThanOrEqualExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 5)));
    assertFalse(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 4)));
  }

  @Test
  public void testVisitCallExpressionGreaterThan() {
    String greaterThanExpr = "age > 5";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(greaterThanExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertFalse(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 5)));
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 6)));
  }

  @Test
  public void testVisitCallExpressionLessThanOrEqual() {
    String lessThanOrEqualExpr = "age <= 5";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(lessThanOrEqualExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 5)));
    assertFalse(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 6)));
  }

  @Test
  public void testVisitCallExpressionLessThan() {
    String lessThanExpr = "age < 5";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(lessThanExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertFalse(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 5)));
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 4)));
  }

  @Test
  public void testVisitCallExpressionIsNotNull() {
    String isNotNullExpr = "id is not NULL";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(isNotNullExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(1, StringData.fromString("1"), 6)));
    assertFalse(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("2"), 6)));
  }

  @Test
  public void testVisitCallExpressionIsNull() {
    String isNullExpr = "id is NULL";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(isNullExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 6)));
  }

  @Test
  public void testVisitCallExpressionEqualsAndGreaterThan() {
    String andExpr = "id = NULL AND age > 5";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(andExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 6)));
  }

  @Test
  public void testVisitCallExpressionEqualsOrLessThan() {
    String orExpr = "id = NULL OR age < 5";
    List<ResolvedExpression> resolved = resolveSQLFilterToExpression(orExpr, schema);
    assertEquals(1, resolved.size());
    RowDataPredicate rowDataPredicate = resolved.get(0).accept(visitor).get();
    assertTrue(rowDataPredicate.test(GenericRowData.of(null, StringData.fromString("1"), 6)));
    assertFalse(rowDataPredicate.test(GenericRowData.of(1, StringData.fromString("2"), 5)));
    assertTrue(rowDataPredicate.test(GenericRowData.of(1, StringData.fromString("2"), 4)));
  }
}
