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

package com.netease.arctic.spark.test.helper;

import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.stream.Collectors;

public class TestSource {

  private List<GenericRecord> sources;
  private Schema schema;

  public TestSource(List<GenericRecord> sources, Schema schema) {
    this.schema = schema;
    this.sources = sources;
  }

  public Dataset<Row> toSparkDataset(SparkSession spark) {
    Dataset<Row> ds = spark.createDataFrame(
        sources.stream().map(TestTableHelper::recordToRow).collect(Collectors.toList()),
        SparkSchemaUtil.convert(schema));
    return ds;
  }
}
