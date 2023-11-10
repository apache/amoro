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

package com.netease.arctic.spark.procedures;

import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.iceberg.catalog.Procedure;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class SupportProcedures {
  private static final Map<String, Supplier<SupportProcedures.ProcedureBuilder<?>>> BUILDERS = initProcedureBuilders();

  private static Map<String, Supplier<SupportProcedures.ProcedureBuilder<?>>> initProcedureBuilders() {
    ImmutableMap.Builder<String, Supplier<SupportProcedures.ProcedureBuilder<?>>> mapBuilder = ImmutableMap.builder();
    mapBuilder.put("rewrite_data_files", RewriteDataFilesProcedure::builder);
    return mapBuilder.build();
  }

  public static SupportProcedures.ProcedureBuilder<?> newBuilder(String name) {
    // procedure resolution is case insensitive to match the existing Spark behavior for functions
    Supplier<SupportProcedures.ProcedureBuilder<?>> builderSupplier = BUILDERS.get(name.toLowerCase(Locale.ROOT));
    return builderSupplier != null ? builderSupplier.get() : null;
  }

  public interface ProcedureBuilder<T extends Procedure> {
    ProcedureBuilder<T> withTableCatalog(TableCatalog tableCatalog);

    T build();
  }
}
