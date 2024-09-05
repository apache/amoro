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

package org.apache.amoro.spark.test.extensions;

import org.apache.amoro.TableFormat;
import org.apache.amoro.spark.test.SparkTestBase;
import org.apache.amoro.spark.test.SparkTestContext;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class EnableCatalogSelectExtension implements BeforeEachMethodAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(EnableCatalogSelectExtension.class);

  public EnableCatalogSelectExtension() {
    LOG.info("extension created");
  }

  @Override
  public void invokeBeforeEachMethod(ExtensionContext context, ExtensionRegistry registry)
      throws Throwable {
    Preconditions.condition(
        context.getTestInstance().isPresent()
            && context.getRequiredTestInstance() instanceof SparkTestBase,
        () -> "This is not a SparkTest");

    String sparkCatalog = selectSparkCatalog(context, registry);
    if (sparkCatalog != null) {
      SparkTestBase instance = (SparkTestBase) context.getRequiredTestInstance();
      System.out.println("setup catalog :" + sparkCatalog);
      LOG.info(
          "Set catalog from test: " + context.getDisplayName() + ", SparkCatalog=" + sparkCatalog);
      instance.setCurrentCatalog(sparkCatalog);
    }
  }

  private String selectSparkCatalog(ExtensionContext context, ExtensionRegistry registry) {
    EnableCatalogSelect.SelectCatalog selector = findAnnotation(context);
    if (selector == null) {
      return "spark_catalog";
    }
    if (StringUtils.isNotEmpty(selector.use())) {
      return selector.use();
    } else if (selector.byTableFormat() && !selector.unifiedCatalog()) {
      return selectMixedCatalogByFormat(context, registry);
    } else if (selector.byTableFormat()) {
      return selectUnifiedCatalogByFormat(context, registry);
    } else {
      throw new IllegalArgumentException("can't determine the spark catalog");
    }
  }

  private EnableCatalogSelect.SelectCatalog findAnnotation(ExtensionContext context) {
    Method method = context.getRequiredTestMethod();
    EnableCatalogSelect.SelectCatalog selector =
        method.getAnnotation(EnableCatalogSelect.SelectCatalog.class);
    if (selector != null) {
      return selector;
    }

    Class<?> testClass = context.getRequiredTestClass();
    selector = testClass.getAnnotation(EnableCatalogSelect.SelectCatalog.class);
    return selector;
  }

  private String selectMixedCatalogByFormat(ExtensionContext context, ExtensionRegistry registry) {
    TableFormat format = formatFromMethodArgs(context, registry);
    Preconditions.condition(
        format == TableFormat.MIXED_ICEBERG || format == TableFormat.MIXED_HIVE,
        "must be a mixed-format");
    switch (format) {
      case MIXED_ICEBERG:
        return SparkTestContext.SparkCatalogNames.MIXED_ICEBERG;
      case MIXED_HIVE:
        return SparkTestContext.SparkCatalogNames.MIXED_HIVE;
      default:
        throw new IllegalArgumentException("must be a mixed-format");
    }
  }

  private String selectUnifiedCatalogByFormat(
      ExtensionContext context, ExtensionRegistry registry) {
    TableFormat format = formatFromMethodArgs(context, registry);
    switch (format) {
      case MIXED_ICEBERG:
        return SparkTestContext.SparkCatalogNames.UNIFIED_MIXED_ICEBERG;
      case MIXED_HIVE:
        return SparkTestContext.SparkCatalogNames.UNIFIED_MIXED_HIVE;
      case ICEBERG:
        return SparkTestContext.SparkCatalogNames.UNIFIED_ICEBERG;
      case PAIMON:
        return SparkTestContext.SparkCatalogNames.UNIFIED_PAIMON;
      default:
        throw new IllegalArgumentException("unknown format");
    }
  }

  private TableFormat formatFromMethodArgs(ExtensionContext context, ExtensionRegistry registry) {
    ParameterResolver resolver =
        registry.stream(ParameterResolver.class)
            .filter(r -> r.getClass().getName().contains("ParameterizedTestParameterResolver"))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("It' not a @ParameterizedTest"));

    DefaultParameterContext parameterContext = null;
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int i = 0; i < parameters.length; i++) {
      Parameter p = parameters[i];
      if (parameterContext == null && p.getType().equals(TableFormat.class)) {
        parameterContext = new DefaultParameterContext(p, i, context.getTestInstance());
      } else if (parameterContext != null && p.getType().equals(TableFormat.class)) {
        throw new IllegalArgumentException("The test with multi-parameters are TableFormat type.");
      }
    }
    if (parameterContext == null) {
      throw new IllegalArgumentException("The test has not parameter is TableFormat type");
    }
    return (TableFormat) resolver.resolveParameter(parameterContext, context);
  }
}
