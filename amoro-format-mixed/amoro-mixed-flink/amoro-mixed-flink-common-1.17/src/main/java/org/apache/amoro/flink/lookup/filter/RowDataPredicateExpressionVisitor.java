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

import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.AND;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.DIVIDE;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.EQUALS;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.GREATER_THAN;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.GREATER_THAN_OR_EQUAL;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.IS_NOT_NULL;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.IS_NULL;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.LESS_THAN;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.LESS_THAN_OR_EQUAL;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.MINUS;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.NOT_EQUALS;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.OR;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.PLUS;
import static org.apache.amoro.flink.lookup.filter.RowDataPredicate.Opt.TIMES;

import org.apache.flink.table.expressions.CallExpression;
import org.apache.flink.table.expressions.Expression;
import org.apache.flink.table.expressions.ExpressionDefaultVisitor;
import org.apache.flink.table.expressions.FieldReferenceExpression;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.expressions.TypeLiteralExpression;
import org.apache.flink.table.expressions.ValueLiteralExpression;
import org.apache.flink.table.functions.BuiltInFunctionDefinitions;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class implements the visitor pattern for traversing expressions and building a {@link
 * RowDataPredicate} out of them.
 *
 * <p>It supports a limited set of built-in functions, such as EQUALS, LESS_THAN, GREATER_THAN,
 * NOT_EQUALS, etc.
 */
public class RowDataPredicateExpressionVisitor
    extends ExpressionDefaultVisitor<Optional<RowDataPredicate>> {

  /**
   * A map from field names to their respective indices in the input row.
   *
   * <p>Start from 0.
   */
  private final Map<String, Integer> fieldIndexMap;
  /** A map from field names to their respective data types */
  private final Map<String, DataType> fieldDataTypeMap;

  public RowDataPredicateExpressionVisitor(
      Map<String, Integer> fieldIndexMap, Map<String, DataType> fieldDataTypeMap) {
    this.fieldIndexMap = fieldIndexMap;
    this.fieldDataTypeMap = fieldDataTypeMap;
  }

  /**
   * Visits a {@link CallExpression} and renders it as a {@link RowDataPredicate}.
   *
   * @param call the call expression to visit
   * @return an optional {@link RowDataPredicate}
   */
  @Override
  public Optional<RowDataPredicate> visit(CallExpression call) {
    if (BuiltInFunctionDefinitions.EQUALS.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(EQUALS, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.LESS_THAN.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(LESS_THAN, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.LESS_THAN_OR_EQUAL.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(LESS_THAN_OR_EQUAL, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.GREATER_THAN.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(GREATER_THAN, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.GREATER_THAN_OR_EQUAL.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(GREATER_THAN_OR_EQUAL, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.NOT_EQUALS.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(NOT_EQUALS, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.OR.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(OR, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.AND.equals(call.getFunctionDefinition())) {
      return renderBinaryOperator(AND, call.getResolvedChildren());
    }
    if (BuiltInFunctionDefinitions.IS_NULL.equals(call.getFunctionDefinition())) {
      return renderUnaryOperator(IS_NULL, call.getResolvedChildren().get(0));
    }
    if (BuiltInFunctionDefinitions.IS_NOT_NULL.equals(call.getFunctionDefinition())) {
      return renderUnaryOperator(IS_NOT_NULL, call.getResolvedChildren().get(0));
    }
    if (BuiltInFunctionDefinitions.PLUS.equals(call.getFunctionDefinition())) {
      return arithmeticOperator(PLUS, call);
    }
    if (BuiltInFunctionDefinitions.MINUS.equals(call.getFunctionDefinition())) {
      return arithmeticOperator(MINUS, call);
    }
    if (BuiltInFunctionDefinitions.TIMES.equals(call.getFunctionDefinition())) {
      return arithmeticOperator(TIMES, call);
    }
    if (BuiltInFunctionDefinitions.DIVIDE.equals(call.getFunctionDefinition())) {
      return arithmeticOperator(DIVIDE, call);
    }
    if (BuiltInFunctionDefinitions.CAST.equals(call.getFunctionDefinition())) {
      return castOperator(call);
    }
    throw new IllegalArgumentException(
        String.format(
            "Not supported build-in function: %s, CallExpression: %s, for RowDataPredicateExpressionVisitor",
            call.getFunctionDefinition(), call));
  }

  @Override
  public Optional<RowDataPredicate> visit(ValueLiteralExpression valueLiteralExpression) {
    LogicalType tpe = valueLiteralExpression.getOutputDataType().getLogicalType();
    Serializable[] params = new Serializable[1];
    switch (tpe.getTypeRoot()) {
      case CHAR:
      case VARCHAR:
        params[0] = valueLiteralExpression.getValueAs(String.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case BOOLEAN:
        params[0] = valueLiteralExpression.getValueAs(Boolean.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case DECIMAL:
        params[0] = valueLiteralExpression.getValueAs(BigDecimal.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case TINYINT:
        params[0] = valueLiteralExpression.getValueAs(Byte.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case SMALLINT:
        params[0] = valueLiteralExpression.getValueAs(Short.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case INTEGER:
        params[0] = valueLiteralExpression.getValueAs(Integer.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case BIGINT:
        params[0] = valueLiteralExpression.getValueAs(Long.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case FLOAT:
        params[0] = valueLiteralExpression.getValueAs(Float.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case DOUBLE:
        params[0] = valueLiteralExpression.getValueAs(Double.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case DATE:
        params[0] =
            valueLiteralExpression.getValueAs(LocalDate.class).map(Date::valueOf).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case TIME_WITHOUT_TIME_ZONE:
        params[0] = valueLiteralExpression.getValueAs(java.sql.Time.class).orElse(null);
        return Optional.of(new RowDataPredicate(params));
      case TIMESTAMP_WITHOUT_TIME_ZONE:
        params[0] =
            valueLiteralExpression
                .getValueAs(LocalDateTime.class)
                .map(Timestamp::valueOf)
                .orElse(null);
        return Optional.of(new RowDataPredicate(params));
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<RowDataPredicate> visit(FieldReferenceExpression fieldReferenceExpression) {
    String fieldName = fieldReferenceExpression.getName();
    int fieldIndex = fieldIndexMap.get(fieldName);
    DataType dataType = fieldDataTypeMap.get(fieldName);
    return Optional.of(new RowDataPredicate(fieldName, fieldIndex, dataType));
  }

  @Override
  protected Optional<RowDataPredicate> defaultMethod(Expression expression) {
    return Optional.empty();
  }

  protected Optional<RowDataPredicate> arithmeticOperator(
      RowDataPredicate.Opt arithmeticOpt, CallExpression call) {
    List<ResolvedExpression> resolvedChildren = call.getResolvedChildren();
    Optional<RowDataPredicate> leftPredicate = resolvedChildren.get(0).accept(this);
    Optional<RowDataPredicate> rightPredicate = resolvedChildren.get(1).accept(this);
    Serializable left = leftPredicate.get().parameters()[0];
    Serializable right = rightPredicate.get().parameters()[0];
    if (left instanceof Number && right instanceof Number) {
      Serializable result;
      switch (arithmeticOpt) {
        case MINUS:
          result = ((Number) left).longValue() - ((Number) right).longValue();
          break;
        case TIMES:
          result = ((Number) left).longValue() * ((Number) right).longValue();
          break;
        case PLUS:
          result = ((Number) left).longValue() + ((Number) right).longValue();
          break;
        case DIVIDE:
          result = ((Number) left).longValue() / ((Number) right).longValue();
          break;
        default:
          throw new IllegalArgumentException(
              String.format(
                  "Not supported arithmetic opt: %s, call expression: %s", arithmeticOpt, call));
      }
      return Optional.of(new RowDataPredicate(new Serializable[] {result}));
    }
    throw new IllegalArgumentException(
        String.format(
            "arithmetic operator: %s only supported numerical parameters, call expression: %s",
            arithmeticOpt, call));
  }

  protected Optional<RowDataPredicate> castOperator(CallExpression call) {
    List<ResolvedExpression> resolvedChildren = call.getResolvedChildren();
    Optional<RowDataPredicate> leftPredicate = resolvedChildren.get(0).accept(this);
    if (resolvedChildren.size() != 2) {
      throw new IllegalArgumentException(
          String.format(
              "cast operator's children expressions should be 2. call expression: %s", call));
    }
    if (resolvedChildren.get(1) instanceof TypeLiteralExpression) {
      Class<?> type = resolvedChildren.get(1).getOutputDataType().getConversionClass();
      Serializable se = (Serializable) type.cast(leftPredicate.get().parameters()[0]);
      return Optional.of(new RowDataPredicate(new Serializable[] {se}));
    }
    throw new IllegalArgumentException(
        String.format(
            "cast operator's children expressions should be 2. call expression: %s", call));
  }

  protected Optional<RowDataPredicate> renderUnaryOperator(
      RowDataPredicate.Opt opt, ResolvedExpression resolvedExpression) {
    if (resolvedExpression instanceof FieldReferenceExpression) {
      Optional<RowDataPredicate> leftPredicate = resolvedExpression.accept(this);
      return leftPredicate.map(rowDataPredicate -> rowDataPredicate.combine(opt, null));
    }
    return Optional.empty();
  }

  protected Optional<RowDataPredicate> renderBinaryOperator(
      RowDataPredicate.Opt opt, List<ResolvedExpression> resolvedExpressions) {
    Optional<RowDataPredicate> leftPredicate = resolvedExpressions.get(0).accept(this);

    Optional<RowDataPredicate> rightPredicate = resolvedExpressions.get(1).accept(this);

    if (AND.equals(opt) || OR.equals(opt)) {
      Preconditions.checkArgument(leftPredicate.isPresent());
      Preconditions.checkArgument(rightPredicate.isPresent());
      return Optional.of(
          new RowDataPredicate(
              opt,
              new RowDataPredicate[] {leftPredicate.get()},
              new RowDataPredicate[] {rightPredicate.get()}));
    }

    return leftPredicate.flatMap(left -> rightPredicate.map(right -> left.combine(opt, right)));
  }
}
