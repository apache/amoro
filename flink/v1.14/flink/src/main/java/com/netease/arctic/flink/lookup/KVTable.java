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

package com.netease.arctic.flink.lookup;

import com.netease.arctic.utils.SchemaUtil;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * The KVTable interface is used for lookup join in Arctic on Flink.
 * It includes methods for initializing and updating the lookup table,
 * as well as getting results by key and cleaning up the cache.
 */
public interface KVTable extends Serializable, Closeable {
  /**
   * Initialize the lookup table
   */
  void open();

  /**
   * Get the result by the key.
   *
   * @throws IOException Serialize the rowData failed.
   */
  List<RowData> get(RowData key) throws IOException;

  /**
   * Upsert the {@link KVTable} by the Change table dataStream.
   *
   * @throws IOException Serialize the rowData failed.
   */
  void upsert(Iterator<RowData> dataStream) throws IOException;

  /**
   * Initial the {@link  KVTable} by the MoR dataStream.
   *
   * @param dataStream
   * @throws IOException
   */
  void initialize(Iterator<RowData> dataStream) throws IOException;

  /**
   * @return if the rowData is filtered, return true.
   */
  boolean filter(RowData value);

  /**
   * @return if initialization is completed, return true.
   */
  boolean initialized();

  /**
   * Waiting for the initialization completed, and enable auto compaction at the end of the initialization.
   */
  void waitInitializationCompleted();

  /**
   * Try to clean up the cache manually, due to the lookup_cache.ttl-after-write configuration.
   * <p>lookup_cache.ttl-after-writ</p> Only works in SecondaryIndexTable.
   */
  default void cleanUp() {
  }

  void close();

  default BinaryRowDataSerializerWrapper createKeySerializer(
      Schema arcticTableSchema, List<String> keys) {
    Schema keySchema = SchemaUtil.convertFieldsToSchema(arcticTableSchema, keys);
    return new BinaryRowDataSerializerWrapper(keySchema);
  }

  default BinaryRowDataSerializerWrapper createValueSerializer(Schema projectSchema) {
    return new BinaryRowDataSerializerWrapper(projectSchema);
  }
}
