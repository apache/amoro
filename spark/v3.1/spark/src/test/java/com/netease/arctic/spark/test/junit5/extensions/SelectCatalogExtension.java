package com.netease.arctic.spark.test.junit5.extensions;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.test.junit5.SparkTestBase;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;

public class SelectCatalogExtension implements BeforeEachMethodAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(SelectCatalogExtension.class);

  @Override
  public void invokeBeforeEachMethod(ExtensionContext context, ExtensionRegistry registry) throws Throwable {
    Preconditions.condition(
        context.getTestInstance().isPresent()
            && context.getRequiredTestInstance() instanceof SparkTestBase,
        () -> "This is not a SparkTest");


    String sparkCatalog = selectCatalogByFormat(context, registry);
    if (sparkCatalog != null) {
      SparkTestBase instance = (SparkTestBase) context.getRequiredTestInstance();
      System.out.println("setup catalog :" + sparkCatalog);
      LOG.info("Set catalog from test: " + context.getDisplayName() + ", SparkCatalog=" + sparkCatalog);
      instance.setCurrentCatalog(sparkCatalog);
    }

  }


  private String selectCatalogByFormat(ExtensionContext context, ExtensionRegistry registry) {
    TableFormat format = formatFromMethodArgs(context, registry);
    return chooseCatalogForFormatTest(format);
  }

  private TableFormat formatFromMethodArgs(ExtensionContext context, ExtensionRegistry registry) {
    ParameterResolver resolver = registry.stream(ParameterResolver.class)
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


  private static String chooseCatalogForFormatTest(TableFormat format) {
    switch (format) {
      case MIXED_HIVE:
        return SparkTestBase.HIVE_CATALOG;
      case MIXED_ICEBERG:
        return SparkTestBase.INTERNAL_CATALOG;
      default:
        throw new IllegalArgumentException("Un-supported table format type for test:" + format);
    }
  }


}
