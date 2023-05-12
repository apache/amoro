package com.netease.arctic.spark.test.extensions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class EachParameterResolver implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return isBeforeOrAfterEachMethod(parameterContext.getDeclaringExecutable())
        && isParameterTypeSupported(parameterContext.getDeclaringExecutable());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    if (type.isAssignableFrom(ExtensionContext.class)){
      return extensionContext;
    }
    return null;
  }

  private boolean isBeforeOrAfterEachMethod(Executable executable) {
    return executable.getAnnotation(BeforeEach.class) != null
        || executable.getAnnotation(AfterEach.class) != null;
  }

  private boolean isParameterTypeSupported(Executable executable) {
    Parameter[] parameters = executable.getParameters();
    for (Parameter parameter: parameters){
      Class<?> type = parameter.getType();
      if (!type.isAssignableFrom(ExtensionContext.class)){
        return false;
      }
    }
    return true;
  }
}
