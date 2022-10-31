/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.hive.io;

import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.LocationKind;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetReaders;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.junit.Assert;

public class TestIOUtils {

  public static void testWrite(ArcticTable table, LocationKind locationKind, List<Record> records, String pathFeature) throws IOException {
    testWrite(table, locationKind, records, pathFeature, null, null);
  }

  public static void testWrite(
      ArcticTable table, LocationKind locationKind, List<Record> records, String pathFeature,
      Expression expression, List<Record> readRecords) throws IOException {
    AdaptHiveGenericTaskWriterBuilder builder = AdaptHiveGenericTaskWriterBuilder
        .builderFor(table)
        .withTransactionId(table.isKeyedTable() ? 1L : null);

    TaskWriter<Record> changeWrite = builder.buildWriter(locationKind);
    for (Record record: records) {
      changeWrite.write(record);
    }
    WriteResult complete = changeWrite.complete();
    Arrays.stream(complete.dataFiles()).forEach(s -> Assert.assertTrue(s.path().toString().contains(pathFeature)));
    CloseableIterable<Record> concat =
        CloseableIterable.concat(Arrays.stream(complete.dataFiles()).map(s -> readParquet(
            table.schema(),
            s.path().toString(), expression)).collect(Collectors.toList()));
    Set<Record> result = new HashSet<>();
    Iterators.addAll(result, concat.iterator());
    if (readRecords == null) {
      Assert.assertEquals(result, new HashSet<>(records));
    } else {
      Assert.assertEquals(result, new HashSet<>(readRecords));
    }
  }

  private static CloseableIterable<Record> readParquet(Schema schema, String path, Expression expression){
    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
            Files.localInput(path))
        .project(schema)
        .filter(expression == null ? Expressions.alwaysTrue() : expression)
        .createReaderFunc(fileSchema -> AdaptHiveGenericParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<Record> iterable = builder.build();
    return iterable;
  }
}
