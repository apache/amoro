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

package org.apache.amoro.utils;

import static org.apache.amoro.utils.ExpressionUtil.convertSqlFilterToIcebergExpression;

import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class TestExpressionUtil {
  @Test
  public void testConvertSqlToIcebergExpression() {
    List<Types.NestedField> fields = new ArrayList<>();
    fields.add(Types.NestedField.optional(1, "column_a", Types.IntegerType.get()));
    assertEqualExpressions(
        Expressions.isNull("column_a"),
        convertSqlFilterToIcebergExpression("column_a IS NULL", fields));
    assertEqualExpressions(
        Expressions.notNull("column_a"),
        convertSqlFilterToIcebergExpression("column_a IS NOT NULL", fields));

    assertEqualExpressions(
        Expressions.alwaysTrue(), convertSqlFilterToIcebergExpression("", fields));

    testConvertSqlToIcebergExpressionByType("1", "'1'", Types.StringType.get());
    testConvertSqlToIcebergExpressionByType(1, "1", Types.IntegerType.get());
    testConvertSqlToIcebergExpressionByType(1L, "1", Types.LongType.get());
    testConvertSqlToIcebergExpressionByType(1.15f, "1.15", Types.FloatType.get());
    testConvertSqlToIcebergExpressionByType(1.15, "1.15", Types.DoubleType.get());
    testConvertSqlToIcebergExpressionByType(true, "true", Types.BooleanType.get());
    testConvertSqlToIcebergExpressionByType(false, "false", Types.BooleanType.get());

    long epochDay = Date.valueOf("2022-01-01").toLocalDate().toEpochDay();
    testConvertSqlToIcebergExpressionByType(epochDay, "DATE '2022-01-01'", Types.DateType.get());
    testConvertSqlToIcebergExpressionByType(epochDay, "'2022-01-01'", Types.DateType.get());

    long microOfDay = Time.valueOf("12:12:12").toLocalTime().getLong(ChronoField.MICRO_OF_DAY);
    testConvertSqlToIcebergExpressionByType(microOfDay, "TIME '12:12:12'", Types.TimeType.get());
    testConvertSqlToIcebergExpressionByType(microOfDay, "'12:12:12'", Types.TimeType.get());

    long epochMicroSecond =
        Timestamp.valueOf("2022-01-01 12:12:12")
                .toLocalDateTime()
                .toEpochSecond(ZoneOffset.ofHours(0))
            * 1_000_000L;
    testConvertSqlToIcebergExpressionByType(
        epochMicroSecond, "TIMESTAMP '2022-01-01T12:12:12'", Types.TimestampType.withoutZone());
    testConvertSqlToIcebergExpressionByType(
        epochMicroSecond, "'2022-01-01T12:12:12'", Types.TimestampType.withoutZone());
  }

  public <T> void testConvertSqlToIcebergExpressionByType(T exprValue, String sqlValue, Type type) {
    List<Types.NestedField> fields = new ArrayList<>();
    fields.add(Types.NestedField.optional(1, "column_a", type));
    fields.add(Types.NestedField.optional(2, "column_b", type));

    assertEqualExpressions(
        Expressions.equal("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a = " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.notEqual("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a != " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.greaterThan("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a > " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.greaterThanOrEqual("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a >= " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.lessThan("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a < " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.lessThanOrEqual("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a <= " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.in("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a IN (" + sqlValue + ")", fields));
    assertEqualExpressions(
        Expressions.notIn("column_a", exprValue),
        convertSqlFilterToIcebergExpression("column_a NOT IN (" + sqlValue + ")", fields));
    assertEqualExpressions(
        Expressions.not(Expressions.equal("column_a", exprValue)),
        convertSqlFilterToIcebergExpression("NOT column_a = " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.and(
            Expressions.equal("column_a", exprValue), Expressions.equal("column_b", exprValue)),
        convertSqlFilterToIcebergExpression(
            "column_a = " + sqlValue + " AND column_b = " + sqlValue, fields));
    assertEqualExpressions(
        Expressions.or(
            Expressions.equal("column_a", exprValue), Expressions.equal("column_b", exprValue)),
        convertSqlFilterToIcebergExpression(
            "column_a = " + sqlValue + " OR column_b = " + sqlValue, fields));
  }

  private void assertEqualExpressions(Expression exp1, Expression exp2) {
    Assert.assertEquals(exp1.toString(), exp2.toString());
  }
}
