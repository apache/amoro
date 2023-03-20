/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.stream.Stream;

public class TestArgumentsProvider
    implements AnnotationConsumer<TestArguments>, ArgumentsProvider {
  TestArguments.TestArg[] args;

  @Override
  public void accept(TestArguments testArguments) {
    this.args = (testArguments).value();
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return Stream.of(args)
        .map(TestArguments.TestArg::value)
        .map(Arguments::of);
  }
}
