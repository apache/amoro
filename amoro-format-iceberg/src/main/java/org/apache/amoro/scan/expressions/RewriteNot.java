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

package org.apache.amoro.scan.expressions;

import org.apache.iceberg.expressions.BoundPredicate;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.ExpressionVisitors;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.UnboundPredicate;

/** Copy form iceberg-api 0.13.2 to assume that there are no NOT nodes in the expression tree. */
class RewriteNot extends ExpressionVisitors.ExpressionVisitor<Expression> {
  private static final RewriteNot INSTANCE = new RewriteNot();

  static RewriteNot get() {
    return INSTANCE;
  }

  private RewriteNot() {}

  @Override
  public Expression alwaysTrue() {
    return Expressions.alwaysTrue();
  }

  @Override
  public Expression alwaysFalse() {
    return Expressions.alwaysFalse();
  }

  @Override
  public Expression not(Expression result) {
    return result.negate();
  }

  @Override
  public Expression and(Expression leftResult, Expression rightResult) {
    return Expressions.and(leftResult, rightResult);
  }

  @Override
  public Expression or(Expression leftResult, Expression rightResult) {
    return Expressions.or(leftResult, rightResult);
  }

  @Override
  public <T> Expression predicate(BoundPredicate<T> pred) {
    return pred;
  }

  @Override
  public <T> Expression predicate(UnboundPredicate<T> pred) {
    return pred;
  }
}
