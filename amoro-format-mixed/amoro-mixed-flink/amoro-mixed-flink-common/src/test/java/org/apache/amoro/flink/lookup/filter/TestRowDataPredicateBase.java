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

import org.apache.amoro.flink.planner.calcite.FlinkTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableException;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.bridge.java.internal.StreamTableEnvironmentImpl;
import org.apache.flink.table.catalog.CatalogManager;
import org.apache.flink.table.catalog.FunctionCatalog;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.expressions.resolver.ExpressionResolver;
import org.apache.flink.table.planner.calcite.FlinkContext;
import org.apache.flink.table.planner.calcite.FlinkTypeFactory;
import org.apache.flink.table.planner.delegation.PlannerBase;
import org.apache.flink.table.planner.expressions.RexNodeExpression;
import org.apache.flink.table.planner.plan.utils.RexNodeToExpressionConverter;
import org.apache.flink.table.types.logical.RowType;
import org.junit.Before;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

public abstract class TestRowDataPredicateBase {
  public static StreamExecutionEnvironment env;
  public static TableEnvironment tEnv;

  @Before
  public void init() {
    env = StreamExecutionEnvironment.getExecutionEnvironment();
    tEnv = StreamTableEnvironment.create(env);
  }

  /**
   * This method takes in an SQL filter expression and a ResolvedSchema object, and returns a List
   * of ResolvedExpression objects.
   */
  protected List<ResolvedExpression> resolveSQLFilterToExpression(
      String sqlExp, ResolvedSchema schema) {
    StreamTableEnvironmentImpl tbImpl = (StreamTableEnvironmentImpl) tEnv;

    FlinkContext ctx = ((PlannerBase) tbImpl.getPlanner()).getFlinkContext();
    CatalogManager catMan = tbImpl.getCatalogManager();
    FunctionCatalog funCat = ctx.getFunctionCatalog();
    RowType sourceType = (RowType) schema.toSourceRowDataType().getLogicalType();
    ClassLoader classLoader = tEnv.getClass().getClassLoader();
    FlinkTypeFactory typeFactory = new FlinkTypeFactory(classLoader, FlinkTypeSystem.INSTANCE);
    RexNodeToExpressionConverter converter =
        new RexNodeToExpressionConverter(
            new RexBuilder(typeFactory),
            sourceType.getFieldNames().toArray(new String[0]),
            funCat,
            catMan,
            TimeZone.getTimeZone(tEnv.getConfig().getLocalTimeZone()));

    RexNodeExpression rexExp =
        (RexNodeExpression) tbImpl.getParser().parseSqlExpression(sqlExp, sourceType, null);
    ResolvedExpression resolvedExp =
        rexExp
            .getRexNode()
            .accept(converter)
            .getOrElse(
                () -> {
                  throw new IllegalArgumentException(
                      "Cannot convert "
                          + rexExp.getRexNode()
                          + " to Expression, this likely "
                          + "means you used some function(s) not "
                          + "supported with this setup.");
                });
    ExpressionResolver resolver =
        ExpressionResolver.resolverFor(
                tEnv.getConfig(),
                classLoader,
                name -> Optional.empty(),
                funCat.asLookup(
                    str -> {
                      throw new TableException(
                          "We should not need to lookup any expressions at this point");
                    }),
                catMan.getDataTypeFactory(),
                (sqlExpression, inputRowType, outputType) -> {
                  throw new TableException(
                      "SQL expression parsing is not supported at this location.");
                })
            .build();
    return resolver.resolve(Collections.singletonList(resolvedExp));
  }
}
