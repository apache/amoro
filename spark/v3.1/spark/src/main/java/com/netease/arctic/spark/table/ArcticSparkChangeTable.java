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

package com.netease.arctic.spark.table;

import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.op.OverwriteBaseFiles;
import com.netease.arctic.op.RewritePartitions;
import com.netease.arctic.scan.KeyedTableScan;
import com.netease.arctic.table.BaseKeyedTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.MetadataColumns;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableSet;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.connector.catalog.TableCapability;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ArcticSparkChangeTable implements KeyedTable, HasTableOperations {

  private BaseKeyedTable table;

  public ArcticSparkChangeTable(BaseKeyedTable table) {
    this.table = table;
  }

  private static final Set<TableCapability> CAPABILITIES = ImmutableSet.of(
      TableCapability.BATCH_READ
      );

  public Set<TableCapability> capabilities() {
    return CAPABILITIES;
  }

  @Override
  public void refresh() {
    table.changeTable().refresh();
  }

  @Override
  public UpdateSchema updateSchema() {
    return table.updateSchema();
  }

  @Override
  public UpdateProperties updateProperties() {
    return table.updateProperties();
  }

  @Override
  public KeyedTableScan newScan() {
    return table.newScan();
  }

  @Override
  public PrimaryKeySpec primaryKeySpec() {
    return table.primaryKeySpec();
  }

  @Override
  public String baseLocation() {
    return table.baseLocation();
  }

  @Override
  public String changeLocation() {
    return table.changeLocation();
  }

  @Override
  public BaseTable baseTable() {
    return table.baseTable();
  }

  @Override
  public ChangeTable changeTable() {
    return table.changeTable();
  }

  @Override
  public String name() {
    return table.name();
  }

  @Override
  public PartitionSpec spec() {
    return table.spec();

  }

  @Override
  public Map<String, String> properties() {
    return table.properties();
  }

  @Override
  public String location() {
    return table.location();
  }

  @Override
  public ArcticFileIO io() {
    return table.io();
  }

  @Override
  public long beginTransaction(String signature) {
    return table.beginTransaction(signature);
  }

  @Override
  public RewritePartitions newRewritePartitions() {
    return table.newRewritePartitions();
  }

  @Override
  public OverwriteBaseFiles newOverwriteBaseFiles() {
    return table.newOverwriteBaseFiles();
  }

  @Override
  public TableIdentifier id() {
    return table.changeTable().id();
  }

  @Override
  public Schema schema() {
    Schema schema = table.changeTable().schema();
    List<Types.NestedField> columns = schema.columns().stream().collect(Collectors.toList());
    columns.add(MetadataColumns.TRANSACTION_ID_FILED);
    columns.add(MetadataColumns.FILE_OFFSET_FILED);
    columns.add(MetadataColumns.CHANGE_ACTION_FIELD);
    return new Schema(columns);
  }

  @Override
  public TableOperations operations() {
    return null;
  }
}
