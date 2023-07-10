/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License,
Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.flink.lookup;

import com.netease.arctic.flink.read.hybrid.reader.DataIteratorReaderFunction;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.hive.io.reader.AbstractAdaptHiveArcticDataReader;
import com.netease.arctic.table.ArcticTable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.TableFunction;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class ArcticRowDataLookupFunction extends TableFunction<RowData> {
  private static final long serialVersionUID = -7694050999266540499L;
  private final BasicLookupFunction<RowData> basicLookupFunction;

  public ArcticRowDataLookupFunction(
      TableFactory<RowData> tableFactory,
      ArcticTable arcticTable,
      List<String> joinKeys,
      Schema projectSchema,
      List<Expression> filters,
      ArcticTableLoader tableLoader,
      Configuration config,
      Predicate<RowData> predicate,
      AbstractAdaptHiveArcticDataReader<RowData> flinkArcticMORDataReader,
      DataIteratorReaderFunction<RowData> readerFunction) {
    this.basicLookupFunction = new BasicLookupFunction<>(tableFactory,
        arcticTable,
        joinKeys,
        projectSchema,
        filters,
        tableLoader,
        config,
        predicate,
        flinkArcticMORDataReader,
        readerFunction);
  }

  @Override
  public void open(FunctionContext context) throws IOException {
    basicLookupFunction.open(context);
  }

  public void eval(Object rowKey) throws IOException {
    List<RowData> results = basicLookupFunction.lookup(rowKey);
    results.forEach(this::collect);
  }

  @Override
  public void close() throws Exception {
    basicLookupFunction.close();
  }
}
