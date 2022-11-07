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

package com.netease.arctic.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.StructLikeMap;

import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Utils to handle table properties.
 */
public class TablePropertyUtil {

  public static final StructLike EMPTY_STRUCT = GenericRecord.create(new Schema());

  /**
   * Decode string to max transaction id map of each partition.
   *
   * @param spec table partition spec
   * @param value string value
   * @return max transaction id map of each partition
   */
  public static StructLikeMap<Long> decodePartitionMaxTxId(PartitionSpec spec, String value) {
    try {
      StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
      TypeReference<Map<String, Long>> typeReference = new TypeReference<Map<String, Long>>() {};
      Map<String, Long> map = new ObjectMapper().readValue(value, typeReference);
      for (String key : map.keySet()) {
        if (spec.isUnpartitioned()) {
          results.put(null, map.get(key));
        } else {
          StructLike partitionData = DataFiles.data(spec, key);
          results.put(partitionData, map.get(key));
        }
      }
      return results;
    } catch (JsonProcessingException e) {
      throw new UnsupportedOperationException("Failed to decode partition max txId ", e);
    }
  }

  public static StructLikeMap<Map<String, String>> decodePartitionProperties(PartitionSpec spec, String value) {
    try {
      StructLikeMap<Map<String, String>> results = StructLikeMap.create(spec.partitionType());
      TypeReference<Map<String, Map<String, String>>> typeReference =
          new TypeReference<Map<String, Map<String, String>>>() {};
      Map<String, Map<String, String>> map = new ObjectMapper().readValue(value, typeReference);
      for (String key : map.keySet()) {
        if (spec.isUnpartitioned()) {
          results.put(EMPTY_STRUCT, map.get(key));
        } else {
          StructLike partitionData = DataFiles.data(spec, key);
          results.put(partitionData, map.get(key));
        }
      }
      return results;
    } catch (JsonProcessingException e) {
      throw new UnsupportedOperationException("Failed to decode partition max txId ", e);
    }
  }

  public static String encodePartitionProperties(PartitionSpec spec,
      StructLikeMap<Map<String, String>> partitionProperties) {
    Map<String, Map<String, String>> stringKeyMap = Maps.newHashMap();
    for (StructLike pd : partitionProperties.keySet()) {
      String pathLike = spec.partitionToPath(pd);
      stringKeyMap.put(pathLike, partitionProperties.get(pd));
    }
    String value;
    try {
      value = new ObjectMapper().writeValueAsString(stringKeyMap);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
    return value;
  }

  public static StructLikeMap<Long> getPartitionMaxTransactionId(KeyedTable keyedTable) {
    StructLikeMap<Long> baseTableMaxTransactionId = StructLikeMap.create(keyedTable.spec().partitionType());

    StructLikeMap<Map<String, String>> partitionProperty = keyedTable.asKeyedTable().baseTable().partitionProperty();
    partitionProperty.forEach((partitionKey, propertyValue) -> {
      Long maxTxId = (propertyValue == null ||
          propertyValue.get(TableProperties.BASE_TABLE_MAX_TRANSACTION) == null) ?
          0 : Long.parseLong(propertyValue.get(TableProperties.BASE_TABLE_MAX_TRANSACTION));
      baseTableMaxTransactionId.put(partitionKey, maxTxId);
    });

    return baseTableMaxTransactionId;
  }
  
  public static long allocateMaxTransactionId(KeyedTable keyedTable) {
    ChangeTable changeTable = keyedTable.changeTable();
    changeTable.refresh();
    Snapshot snapshot = changeTable.currentSnapshot();
    return snapshot == null ? 0L : snapshot.sequenceNumber();
  }
}
