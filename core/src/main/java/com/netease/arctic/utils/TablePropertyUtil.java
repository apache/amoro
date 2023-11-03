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
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.StructLikeMap;

import java.io.UncheckedIOException;
import java.util.Map;

/** Utils to handle table properties. */
public class TablePropertyUtil {

  public static final StructLike EMPTY_STRUCT = GenericRecord.create(new Schema());

  public static StructLikeMap<Map<String, String>> decodePartitionProperties(
      PartitionSpec spec, String value) {
    try {
      StructLikeMap<Map<String, String>> results = StructLikeMap.create(spec.partitionType());
      TypeReference<Map<String, Map<String, String>>> typeReference =
          new TypeReference<Map<String, Map<String, String>>>() {};
      Map<String, Map<String, String>> map = new ObjectMapper().readValue(value, typeReference);
      for (String key : map.keySet()) {
        if (spec.isUnpartitioned()) {
          results.put(EMPTY_STRUCT, map.get(key));
        } else {
          StructLike partitionData = ArcticDataFiles.data(spec, key);
          results.put(partitionData, map.get(key));
        }
      }
      return results;
    } catch (JsonProcessingException e) {
      throw new UnsupportedOperationException("Failed to decode partition max txId ", e);
    }
  }

  public static String encodePartitionProperties(
      PartitionSpec spec, StructLikeMap<Map<String, String>> partitionProperties) {
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

  public static StructLikeMap<Long> getPartitionBaseOptimizedTime(KeyedTable keyedTable) {
    return getPartitionLongProperties(
        keyedTable.baseTable(), TableProperties.PARTITION_BASE_OPTIMIZED_TIME);
  }

  public static StructLikeMap<Long> getPartitionOptimizedSequence(KeyedTable keyedTable) {
    return getPartitionLongProperties(
        keyedTable.baseTable(), TableProperties.PARTITION_OPTIMIZED_SEQUENCE);
  }

  public static StructLikeMap<Long> getPartitionLongProperties(
      UnkeyedTable unkeyedTable, String key) {
    StructLikeMap<Long> result = StructLikeMap.create(unkeyedTable.spec().partitionType());

    StructLikeMap<Map<String, String>> partitionProperty = unkeyedTable.partitionProperty();
    partitionProperty.forEach(
        (partitionKey, propertyValue) -> {
          Long longValue =
              (propertyValue == null || propertyValue.get(key) == null)
                  ? null
                  : Long.parseLong(propertyValue.get(key));
          if (longValue != null) {
            result.put(partitionKey, longValue);
          }
        });

    return result;
  }

  public static Map<String, String> getPartitionProperties(
      ArcticTable arcticTable, String partitionPath) {
    return getPartitionProperties(
        arcticTable.isKeyedTable()
            ? arcticTable.asKeyedTable().baseTable()
            : arcticTable.asUnkeyedTable(),
        partitionPath);
  }

  public static Map<String, String> getPartitionProperties(
      UnkeyedTable unkeyedTable, String partitionPath) {
    StructLike partitionData;
    if (unkeyedTable.spec().isUnpartitioned()) {
      partitionData = TablePropertyUtil.EMPTY_STRUCT;
    } else {
      partitionData = ArcticDataFiles.data(unkeyedTable.spec(), partitionPath);
    }
    return getPartitionProperties(unkeyedTable, partitionData);
  }

  public static Map<String, String> getPartitionProperties(
      UnkeyedTable unkeyedTable, StructLike partitionData) {
    Map<String, String> result = Maps.newHashMap();
    StructLikeMap<Map<String, String>> partitionProperty = unkeyedTable.partitionProperty();
    if (partitionProperty.containsKey(partitionData)) {
      result = partitionProperty.get(partitionData);
    }
    return result;
  }

  public static StructLikeMap<Long> getLegacyPartitionMaxTransactionId(KeyedTable keyedTable) {
    StructLikeMap<Long> baseTableMaxTransactionId =
        StructLikeMap.create(keyedTable.spec().partitionType());

    StructLikeMap<Map<String, String>> partitionProperty =
        keyedTable.asKeyedTable().baseTable().partitionProperty();
    partitionProperty.forEach(
        (partitionKey, propertyValue) -> {
          Long maxTxId =
              (propertyValue == null
                      || propertyValue.get(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID) == null)
                  ? null
                  : Long.parseLong(
                      propertyValue.get(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID));
          if (maxTxId != null) {
            baseTableMaxTransactionId.put(partitionKey, maxTxId);
          }
        });

    return baseTableMaxTransactionId;
  }

  public static long getTableWatermark(Map<String, String> properties) {
    String watermarkValue = properties.get(TableProperties.WATERMARK_TABLE);
    if (watermarkValue == null) {
      return -1;
    } else {
      return Long.parseLong(watermarkValue);
    }
  }

  /**
   * Check if the given table properties is the base store of mixed-format,
   *
   * @param properties properties of the table.
   * @param format - {@link TableFormat#MIXED_ICEBERG} or {@link TableFormat#MIXED_HIVE}
   * @return true if this is a base store of mixed-format.
   */
  public static boolean isBaseStore(Map<String, String> properties, TableFormat format) {
    String tableFormat = properties.get(TableProperties.TABLE_FORMAT);
    String tableStore = properties.get(TableProperties.MIXED_FORMAT_TABLE_STORE);
    return format.name().equalsIgnoreCase(tableFormat)
        && TableProperties.MIXED_FORMAT_TABLE_STORE_BASE.equalsIgnoreCase(tableStore);
  }

  /**
   * parse change store table identifier for base store table properties
   *
   * @param properties - table properties of base store
   * @return - table identifier of change store.
   */
  public static TableIdentifier parseChangeIdentifier(Map<String, String> properties) {
    Preconditions.checkArgument(
        properties.containsKey(TableProperties.MIXED_FORMAT_CHANGE_STORE_IDENTIFIER),
        "can read change store identifier from base store properties");
    String change = properties.get(TableProperties.MIXED_FORMAT_CHANGE_STORE_IDENTIFIER);
    return TableIdentifier.parse(change);
  }


  public static Map<String, String> commonMixedProperties(PrimaryKeySpec keySpec, TableFormat format) {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(TableProperties.TABLE_FORMAT, format.name());
    if (keySpec.primaryKeyExisted()) {
      String fields = Joiner.on(",").join(keySpec.fieldNames());
      properties.put(TableProperties.MIXED_FORMAT_PRIMARY_KEY_FIELDS, fields);
    }

    properties.put(TableProperties.TABLE_CREATE_TIME, String.valueOf(System.currentTimeMillis()));
    properties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    properties.put(org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED, "true");
    properties.put("flink.max-continuous-empty-commits", String.valueOf(Integer.MAX_VALUE));
    return properties;
  }


  public static Map<String, String> baseStoreProperties(
      PrimaryKeySpec keySpec, TableIdentifier changeIdentifier, TableFormat format) {
    Map<String, String> properties = commonMixedProperties(keySpec, format);
    properties.put(TableProperties.MIXED_FORMAT_TABLE_STORE,
        TableProperties.MIXED_FORMAT_TABLE_STORE_BASE);
    if (keySpec.primaryKeyExisted()) {
      properties.put(TableProperties.MIXED_FORMAT_CHANGE_STORE_IDENTIFIER, changeIdentifier.toString());
    }
    return properties;
  }

  public static Map<String, String> changeStoreProperties(PrimaryKeySpec keySpec, TableFormat format) {
    Map<String, String> properties = commonMixedProperties(keySpec, format);
    properties.put(TableProperties.MIXED_FORMAT_TABLE_STORE,
        TableProperties.MIXED_FORMAT_TABLE_STORE_CHANGE);
    return properties;
  }
}
