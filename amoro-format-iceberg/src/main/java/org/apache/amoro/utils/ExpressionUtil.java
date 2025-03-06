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

import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.amoro.table.MixedTable;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.UnboundTerm;
import org.apache.iceberg.types.Types;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Utility class for working with {@link Expression}. */
public class ExpressionUtil {

  /**
   * Convert partition data to data filter.
   *
   * @param table the {@link MixedTable} table
   * @param specId the partition spec id
   * @param partitions the collection of partition data
   * @return data filter converted from partition data
   */
  public static Expression convertPartitionDataToDataFilter(
      MixedTable table, int specId, Collection<StructLike> partitions) {
    Expression filter = Expressions.alwaysFalse();
    for (StructLike partition : partitions) {
      filter = Expressions.or(filter, convertPartitionDataToDataFilter(table, specId, partition));
    }
    return filter;
  }

  /**
   * Convert partition data to data filter.
   *
   * @param table the {@link MixedTable} table
   * @param specId the partition spec id
   * @param partition the partition data
   * @return data filter converted from partition data
   */
  public static Expression convertPartitionDataToDataFilter(
      MixedTable table, int specId, StructLike partition) {
    PartitionSpec spec = MixedTableUtil.getMixedTablePartitionSpecById(table, specId);
    Schema schema = table.schema();
    Expression filter = Expressions.alwaysTrue();
    for (int i = 0; i < spec.fields().size(); i++) {
      PartitionField partitionField = spec.fields().get(i);
      Types.NestedField sourceField = schema.findField(partitionField.sourceId());
      UnboundTerm transform = Expressions.transform(sourceField.name(), partitionField.transform());

      Class<?> resultType =
          partitionField.transform().getResultType(sourceField.type()).typeId().javaClass();
      Object partitionValue = partition.get(i, resultType);
      if (partitionValue != null) {
        filter = Expressions.and(filter, Expressions.equal(transform, partitionValue));
      } else {
        filter = Expressions.and(filter, Expressions.isNull(transform));
      }
    }
    return filter;
  }

  /**
   * Convert SQL filter to Iceberg {@link Expression}.
   *
   * @param sqlFilter the SQL filter
   * @param tableColumns the list of table columns
   * @return the Iceberg {@link Expression}
   */
  public static Expression convertSqlFilterToIcebergExpression(
      String sqlFilter, List<Types.NestedField> tableColumns) {
    if (StringUtils.isEmpty(StringUtils.trim(sqlFilter))) {
      return Expressions.alwaysTrue();
    }

    try {
      Select statement = (Select) CCJSqlParserUtil.parse("SELECT * FROM dummy WHERE " + sqlFilter);
      PlainSelect select = statement.getPlainSelect();
      return convertSparkExpressionToIceberg(select.getWhere(), tableColumns);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse where condition: " + sqlFilter, e);
    }
  }

  private static Expression convertSparkExpressionToIceberg(
      net.sf.jsqlparser.expression.Expression whereExpr, List<Types.NestedField> tableColumns) {
    if (whereExpr instanceof IsNullExpression) {
      IsNullExpression isNull = (IsNullExpression) whereExpr;
      Types.NestedField column = getColumn(isNull.getLeftExpression(), tableColumns);
      return isNull.isNot()
          ? Expressions.notNull(column.name())
          : Expressions.isNull(column.name());
    } else if (whereExpr instanceof EqualsTo) {
      EqualsTo eq = (EqualsTo) whereExpr;
      Types.NestedField column = getColumn(eq.getLeftExpression(), tableColumns);
      return Expressions.equal(column.name(), getValue(eq.getRightExpression(), column));
    } else if (whereExpr instanceof NotEqualsTo) {
      NotEqualsTo ne = (NotEqualsTo) whereExpr;
      Types.NestedField column = getColumn(ne.getLeftExpression(), tableColumns);
      return Expressions.notEqual(column.name(), getValue(ne.getRightExpression(), column));
    } else if (whereExpr instanceof GreaterThan) {
      GreaterThan gt = (GreaterThan) whereExpr;
      Types.NestedField column = getColumn(gt.getLeftExpression(), tableColumns);
      return Expressions.greaterThan(column.name(), getValue(gt.getRightExpression(), column));
    } else if (whereExpr instanceof GreaterThanEquals) {
      GreaterThanEquals ge = (GreaterThanEquals) whereExpr;
      Types.NestedField column = getColumn(ge.getLeftExpression(), tableColumns);
      return Expressions.greaterThanOrEqual(
          column.name(), getValue(ge.getRightExpression(), column));
    } else if (whereExpr instanceof MinorThan) {
      MinorThan lt = (MinorThan) whereExpr;
      Types.NestedField column = getColumn(lt.getLeftExpression(), tableColumns);
      return Expressions.lessThan(column.name(), getValue(lt.getRightExpression(), column));
    } else if (whereExpr instanceof MinorThanEquals) {
      MinorThanEquals le = (MinorThanEquals) whereExpr;
      Types.NestedField column = getColumn(le.getLeftExpression(), tableColumns);
      return Expressions.lessThanOrEqual(column.name(), getValue(le.getRightExpression(), column));
    } else if (whereExpr instanceof InExpression) {
      InExpression in = (InExpression) whereExpr;
      Types.NestedField column = getColumn(in.getLeftExpression(), tableColumns);
      net.sf.jsqlparser.expression.Expression rightExpr = in.getRightExpression();
      List<Object> values = new ArrayList<>();
      if (rightExpr instanceof ExpressionList) {
        for (net.sf.jsqlparser.expression.Expression expr : ((ExpressionList<?>) rightExpr)) {
          values.add(getValue(expr, column));
        }
      } else {
        throw new UnsupportedOperationException("Subquery IN not supported");
      }
      return in.isNot()
          ? Expressions.notIn(column.name(), values)
          : Expressions.in(column.name(), values);
    } else if (whereExpr instanceof NotExpression) {
      NotExpression not = (NotExpression) whereExpr;
      return Expressions.not(convertSparkExpressionToIceberg(not.getExpression(), tableColumns));
    } else if (whereExpr instanceof AndExpression) {
      AndExpression and = (AndExpression) whereExpr;
      return Expressions.and(
          convertSparkExpressionToIceberg(and.getLeftExpression(), tableColumns),
          convertSparkExpressionToIceberg(and.getRightExpression(), tableColumns));
    } else if (whereExpr instanceof OrExpression) {
      OrExpression or = (OrExpression) whereExpr;
      return Expressions.or(
          convertSparkExpressionToIceberg(or.getLeftExpression(), tableColumns),
          convertSparkExpressionToIceberg(or.getRightExpression(), tableColumns));
    }
    throw new UnsupportedOperationException("Unsupported expression: " + whereExpr);
  }

  private static Types.NestedField getColumn(
      net.sf.jsqlparser.expression.Expression expr, List<Types.NestedField> tableColumns) {
    if (expr instanceof Column) {
      String columnName = ((Column) expr).getColumnName();
      Optional<Types.NestedField> column =
          tableColumns.stream().filter(c -> c.name().equals(columnName)).findFirst();
      if (column.isPresent()) {
        return column.get();
      }
      throw new IllegalArgumentException("Column not found: " + columnName);
    }

    throw new IllegalArgumentException("Expected column reference, got: " + expr);
  }

  private static Object getValue(
      net.sf.jsqlparser.expression.Expression expr, Types.NestedField column) {
    try {
      return convertValue(expr, column);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to convert value: " + expr, e);
    }
  }

  private static Object convertValue(
      net.sf.jsqlparser.expression.Expression expr, Types.NestedField column) {
    switch (column.type().typeId()) {
      case BOOLEAN:
        return Boolean.valueOf(((Column) expr).getColumnName());
      case STRING:
        return ((StringValue) expr).getValue();
      case INTEGER:
      case LONG:
        return ((LongValue) expr).getValue();
      case FLOAT:
      case DOUBLE:
        return ((DoubleValue) expr).getValue();
      case DATE:
        String dateStr = getDateTimeLiteralStr(expr, "date");
        if (dateStr != null) {
          return Date.valueOf(dateStr).toLocalDate().toEpochDay();
        }
        break;
      case TIME:
        String timeStr = getDateTimeLiteralStr(expr, "time");
        if (timeStr != null) {
          return Time.valueOf(timeStr).toLocalTime().getLong(ChronoField.MICRO_OF_DAY);
        }
        break;
      case TIMESTAMP:
        String timestampStr = getDateTimeLiteralStr(expr, "timestamp");
        if (timestampStr != null) {
          long timestamp;
          if (column.type().equals(Types.TimestampType.withZone())) {
            timestamp = OffsetDateTime.parse(timestampStr).toEpochSecond();
          } else {
            timestamp = LocalDateTime.parse(timestampStr).toEpochSecond(ZoneOffset.ofHours(0));
          }
          return timestamp * 1_000_000L;
        }
        break;
    }
    throw new IllegalArgumentException(
        expr + " can not be converted to column type: " + column.type());
  }

  private static String getDateTimeLiteralStr(
      net.sf.jsqlparser.expression.Expression expr, String type) {
    String timestampStr = null;
    if (expr instanceof StringValue) {
      timestampStr = ((StringValue) expr).getValue();
    } else if (expr instanceof DateTimeLiteralExpression
        && ((DateTimeLiteralExpression) expr).getType().name().equalsIgnoreCase(type)) {
      timestampStr = ((DateTimeLiteralExpression) expr).getValue().replaceAll("^'(.*)'$", "$1");
    }
    return timestampStr;
  }
}
