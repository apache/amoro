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

import static org.apache.flink.table.types.logical.utils.LogicalTypeChecks.getPrecision;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.DecimalType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A predicate to be used in a filter operation on a {@link RowData} object. It can be constructed
 * from various comparison operators on a field or from boolean operators with other predicates.
 *
 * <p>The {@code test} method will apply the predicate to a {@link RowData} object, returning true
 * if the predicate is satisfied by the given data.
 */
public class RowDataPredicate implements Predicate<RowData>, Serializable {
  private static final long serialVersionUID = 1L;
  private Opt opt;
  private final String fieldName;
  private final int fieldIndex;
  private final DataType dataType;
  private Serializable[] parameters;
  private final RowDataPredicate[] leftPredicates;
  private final RowDataPredicate[] rightPredicates;

  /** Constructor used for testing purposes. */
  public RowDataPredicate(
      Opt opt,
      String fieldName,
      int fieldIndex,
      DataType dataType,
      Serializable[] parameters,
      RowDataPredicate[] leftPredicates,
      RowDataPredicate[] rightPredicates) {
    this.opt = opt;
    this.fieldName = fieldName;
    this.fieldIndex = fieldIndex;
    this.dataType = dataType;
    this.parameters = parameters;
    this.leftPredicates = leftPredicates;
    this.rightPredicates = rightPredicates;
  }

  /**
   * Constructor for logical operation, when left and right side of the operation is not a simple
   * comparison.
   */
  public RowDataPredicate(
      Opt opt, RowDataPredicate[] leftPredicates, RowDataPredicate[] rightPredicates) {
    this(opt, null, -1, null, null, leftPredicates, rightPredicates);
  }

  /** Constructor for simple comparison operator. */
  public RowDataPredicate(String fieldName, int fieldIndex, DataType dataType) {
    this(null, fieldName, fieldIndex, dataType, null, null, null);
  }

  /** Constructor for comparing value to a fixed value or NULL value. */
  public RowDataPredicate(Serializable[] parameters) {
    this(null, null, -1, null, parameters, null, null);
  }

  /** Test if the RowData record satisfies this predicate. */
  @Override
  public boolean test(RowData rowData) {
    boolean result;
    Object val;
    switch (opt) {
      case EQUALS:
        val = getter(rowData);
        result = compareEquals(val);
        break;
      case NOT_EQUALS:
        val = getter(rowData);
        result = !compareEquals(val);
        break;
      case GREATER_THAN:
        val = getter(rowData);
        result = compareGreaterThan(val);
        break;
      case GREATER_THAN_OR_EQUAL:
        val = getter(rowData);
        result = compareGreaterThanOrEqual(val);
        break;
      case LESS_THAN:
        val = getter(rowData);
        result = compareLessThan(val);
        break;
      case LESS_THAN_OR_EQUAL:
        val = getter(rowData);
        result = compareLessThanOrEqual(val);
        break;
      case IS_NOT_NULL:
        val = getter(rowData);
        result = compareIsNotNull(val);
        break;
      case IS_NULL:
        val = getter(rowData);
        result = compareIsNull(val);
        break;
      case AND:
        Preconditions.checkNotNull(leftPredicates);
        Preconditions.checkNotNull(rightPredicates);
        result = Arrays.stream(leftPredicates).allMatch(p -> p.test(rowData));
        if (!result) {
          return false;
        }
        result = Arrays.stream(rightPredicates).allMatch(p -> p.test(rowData));
        break;
      case OR:
        Preconditions.checkNotNull(leftPredicates);
        Preconditions.checkNotNull(rightPredicates);
        result = Arrays.stream(leftPredicates).allMatch(p -> p.test(rowData));
        if (result) {
          return true;
        }
        result = Arrays.stream(rightPredicates).allMatch(p -> p.test(rowData));
        break;
      default:
        throw new IllegalArgumentException("Unsupported opt: " + opt);
    }

    return result;
  }

  public Serializable[] parameters() {
    return parameters;
  }

  /**
   * Combines this RowDataPredicate with another using the specified operator.
   *
   * @param operator the operator to use for the combination
   * @param that the other RowDataPredicate to combine with this one
   * @return the combined RowDataPredicate
   */
  public RowDataPredicate combine(Opt operator, RowDataPredicate that) {
    this.opt = operator;
    if (that == null) {
      this.parameters = null;
    } else {
      this.parameters = that.parameters;
    }
    return this;
  }

  private boolean compareLessThanOrEqual(Object val) {
    return compareLiteral(dataType, parameters[0], val) >= 0;
  }

  private boolean compareLessThan(Object val) {
    return compareLiteral(dataType, parameters[0], val) > 0;
  }

  private boolean compareGreaterThanOrEqual(Object val) {
    return compareLiteral(dataType, parameters[0], val) <= 0;
  }

  private boolean compareIsNotNull(Object val) {
    return val != null;
  }

  private boolean compareIsNull(Object val) {
    return val == null;
  }

  private boolean compareGreaterThan(Object val) {
    return compareLiteral(dataType, parameters[0], val) < 0;
  }

  private boolean compareEquals(Object val) {
    if (parameters[0] == null && val == null) {
      return true;
    }
    if (parameters[0] == null || val == null) {
      return false;
    }
    return compareLiteral(dataType, parameters[0], val) == 0;
  }

  Object getter(RowData rowData) {
    int pos = fieldIndex;
    if (rowData.isNullAt(pos)) {
      return null;
    }
    Preconditions.checkNotNull(dataType);
    LogicalType logicalType = dataType.getLogicalType();
    switch (logicalType.getTypeRoot()) {
      case CHAR:
      case VARCHAR:
        return rowData.getString(pos).toString();
      case BOOLEAN:
        return rowData.getBoolean(pos);
      case BINARY:
      case VARBINARY:
        return rowData.getBinary(pos);
      case DECIMAL:
        DecimalType decimalType = (DecimalType) logicalType;
        return rowData
            .getDecimal(pos, decimalType.getPrecision(), decimalType.getScale())
            .toBigDecimal();
      case TINYINT:
        return rowData.getByte(pos);
      case SMALLINT:
        return rowData.getShort(pos);
      case INTEGER:
      case DATE:
      case INTERVAL_YEAR_MONTH:
      case TIME_WITHOUT_TIME_ZONE:
        return rowData.getInt(pos);
      case BIGINT:
      case INTERVAL_DAY_TIME:
        return rowData.getLong(pos);
      case FLOAT:
        return rowData.getFloat(pos);
      case DOUBLE:
        return rowData.getDouble(pos);
      case TIMESTAMP_WITHOUT_TIME_ZONE:
      case TIMESTAMP_WITH_LOCAL_TIME_ZONE:
        final int timestampPrecision = getPrecision(logicalType);
        return rowData.getTimestamp(pos, timestampPrecision).getMillisecond();
      default:
        throw new IllegalArgumentException(
            String.format("Not supported datatype: %s, field: %s", dataType, fieldName));
    }
  }

  private static int compareLiteral(DataType type, Object v1, Object v2) {
    if (v1 instanceof Comparable) {
      return ((Comparable<Object>) v1).compareTo(v2);
    } else {
      throw new RuntimeException(String.format("Unsupported type: %s, val: %s", type, v1));
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(RowDataPredicate.class)
        .add("\n\topt", opt)
        .add("\n\tfieldName", fieldName)
        .add("\n\tfieldIndex", fieldIndex)
        .add("\n\tdataType", dataType)
        .add("\n\tparameters", parameters)
        .add(
            "\n\tleftPredicates",
            leftPredicates == null
                ? "[]"
                : Iterables.toString(
                    Arrays.stream(leftPredicates)
                        .map(predicate -> predicate.toString().replaceAll("\n", "\n\t"))
                        .collect(Collectors.toList())))
        .add(
            "\n\trightPredicates",
            rightPredicates == null
                ? "[]"
                : Iterables.toString(
                    Arrays.stream(rightPredicates)
                        .map(predicate -> predicate.toString().replaceAll("\n", "\n\t"))
                        .collect(Collectors.toList())))
        .toString();
  }

  public enum Opt {
    AND,
    OR,
    EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    NOT_EQUALS,
    IS_NULL,
    IS_NOT_NULL,
    TO_TIMESTAMP,
    MINUS,
    PLUS,
    DIVIDE,
    TIMES
  }
}
