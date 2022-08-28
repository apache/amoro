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

package com.netease.arctic.hive.table;

import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.hive.utils.HiveTypes;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HiveTable {
  private static final Set<String> IGNORE_ARCTIC_PROPERTIES =
      Sets.newHashSet(HiveTableProperties.ARCTIC_TABLE_FLAG, HiveTableProperties.ARCTIC_TABLE_PRIMARY_KEYS,
          HiveTableProperties.ARCTIC_SERVER_NAME);
  private final Table table;
  private List<String> primaryKeys;
  private Map<String, String> tableProperties;
  private Map<String, String> arcticProperties;

  private HiveTable(Table table) {
    this.table = table;
  }

  public static HiveTable of(Table table) {
    Preconditions.checkNotNull(table);
    return new HiveTable(table);
  }

  public static Builder newBuilder(TableIdentifier tableIdentifier, Schema schema) {
    return new Builder(tableIdentifier.getDatabase(), tableIdentifier.getTableName(), schema);
  }

  public static Builder newBuilder(String database, String tableName, Schema schema) {
    return new Builder(database, tableName, schema);
  }

  public String getTableLocation() {
    return table.getSd().getLocation();
  }

  public List<FieldSchema> getTableSchema() {
    return table.getSd().getCols();
  }

  public int getCreateTime() {
    return table.getCreateTime();
  }

  public Schema getIceBergTableSchema() {
    initTablePrimaryKeys();
    return HiveSchemaUtil.hiveTableSchemaToIceberg(this, primaryKeys);
  }

  public Map<String, String> getArcticProperties() {
    initTableProperties();
    return arcticProperties;
  }

  public Map<String, String> getHiveTableProperties() {
    initTableProperties();
    return tableProperties;
  }

  public List<FieldSchema> getHivePartitionKeys() {
    return table.getPartitionKeys();
  }

  public PartitionSpec getPartitionKeys() {
    List<FieldSchema> partitionKeys = table.getPartitionKeys();
    PartitionSpec.Builder builder = PartitionSpec.builderFor(getIceBergTableSchema());
    partitionKeys.stream().forEach(p -> builder.identity(p.getName()));
    return builder.build();
  }

  public void setPrimaryKeys(List<String> primaryKeys) {
    this.primaryKeys = primaryKeys;
  }

  public PrimaryKeySpec getPrimaryKeys() {
    initTablePrimaryKeys();
    PrimaryKeySpec.Builder builder = PrimaryKeySpec.builderFor(getIceBergTableSchema());
    primaryKeys.forEach(builder::addColumn);
    return builder.build();
  }

  private void initTableProperties() {
    if (tableProperties == null) {
      tableProperties = Maps.newHashMap();
      tableProperties.putAll(table.getParameters());
      arcticProperties = Maps.newHashMap();
      tableProperties.forEach((key, value) -> {
        if (HiveTableProperties.isArcticProperty(key) && !HiveTableProperties.isIgnoreArcticProperty(key)) {
          arcticProperties.put(HiveTableProperties.arcticPropertyName(key), value);
        }
      });
    }
  }

  private void initTablePrimaryKeys() {
    if (primaryKeys == null) {
      primaryKeys = Lists.newArrayList();
      initTableProperties();
      String primaryKeyDes = tableProperties.get(HiveTableProperties.ARCTIC_TABLE_PRIMARY_KEYS);

      if (StringUtils.isNotBlank(primaryKeyDes)) {
        primaryKeys.addAll(
            Arrays.asList(primaryKeyDes.split(PrimaryKeySpec.PRIMARY_KEY_COLUMN_JOIN_DELIMITER)));
      }
    }
  }

  public String getDatabase() {
    return table.getDbName();
  }

  public String getTableName() {
    return table.getTableName();
  }

  public StorageDescriptor getSd() {
    return table.getSd();
  }

  public Table getTable() {
    return table;
  }

  public Map<String, String> getParameters() {
    return table.getParameters();
  }

  public static class Builder {
    Table table = new Table();

    private Builder(String database, String tableName, Schema schema) {
      table.setDbName(database);
      table.setTableName(tableName);
      table.setSd(new StorageDescriptor());
      table.setParameters(new HashMap<>());

      if (schema != null) {
        List<FieldSchema> cols = schema.columns().stream()
            .map(c -> new FieldSchema(c.name(), HiveTypes.typeIcebergToHive(c.type().toString()), c.doc()))
            .collect(Collectors.toList());
        table.getSd().setCols(cols);
      }
    }

    public Builder withFormat(String inputFormat, String outputFormat) {
      table.getSd().setInputFormat(inputFormat);
      table.getSd().setOutputFormat(outputFormat);
      return this;
    }

    public Builder withSerDe(String serde) {
      SerDeInfo serDeInfo = new SerDeInfo();
      serDeInfo.setSerializationLib(serde);
      table.getSd().setSerdeInfo(serDeInfo);
      return this;
    }

    public Builder withLocation(String location) {
      table.getSd().setLocation(location);
      return this;
    }

    public Builder withType(String type) {
      table.setTableType(type);
      return this;
    }

    public Builder withParameter(String key, String value) {
      table.getParameters().put(key, value);
      return this;
    }

    public Builder withParameters(Map<String, String> parameters) {
      table.getParameters().putAll(parameters);
      return this;
    }

    public Builder withPartitions(List<FieldSchema> partitions) {
      table.setPartitionKeys(partitions);
      return this;
    }

    public HiveTable build() {
      long current = System.currentTimeMillis();
      table.setCreateTime((int) current / 1000);
      table.setLastAccessTime((int) current / 1000);
      table.setRetention(Integer.MAX_VALUE);
      return new HiveTable(table);
    }
  }
}
