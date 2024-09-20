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

package org.apache.amoro.spark.table;

import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.spark.reader.SparkScanBuilder;
import org.apache.amoro.spark.util.MixedFormatSparkUtils;
import org.apache.amoro.spark.writer.MixedFormatSparkWriteBuilder;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.Schema;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.analysis.NoSuchPartitionException;
import org.apache.spark.sql.catalyst.analysis.PartitionAlreadyExistsException;
import org.apache.spark.sql.connector.catalog.SupportsPartitionManagement;
import org.apache.spark.sql.connector.catalog.SupportsRead;
import org.apache.spark.sql.connector.catalog.SupportsWrite;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCapability;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.connector.read.ScanBuilder;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.WriteBuilder;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.Map;
import java.util.Set;

public class MixedSparkTable
    implements Table,
        SupportsRead,
        SupportsWrite,
        SupportsRowLevelOperator,
        SupportsPartitionManagement {
  private static final Set<String> RESERVED_PROPERTIES =
      Sets.newHashSet("provider", "format", "current-snapshot-id");
  private static final Set<TableCapability> CAPABILITIES =
      ImmutableSet.of(
          TableCapability.BATCH_READ,
          TableCapability.BATCH_WRITE,
          TableCapability.STREAMING_WRITE,
          TableCapability.OVERWRITE_BY_FILTER,
          TableCapability.OVERWRITE_DYNAMIC);

  private final MixedTable mixedTable;
  private final String sparkCatalogName;
  private StructType lazyTableSchema = null;
  private SparkSession lazySpark = null;
  private final MixedFormatCatalog catalog;

  public static Table ofMixedTable(
      MixedTable table, MixedFormatCatalog catalog, String sparkCatalogName) {
    if (table.isUnkeyedTable()) {
      if (!(table instanceof SupportHive)) {
        return new UnkeyedSparkTable(table.asUnkeyedTable(), false, sparkCatalogName);
      }
    }
    return new MixedSparkTable(table, catalog, sparkCatalogName);
  }

  public MixedSparkTable(
      MixedTable mixedTable, MixedFormatCatalog catalog, String sparkCatalogName) {
    this.mixedTable = mixedTable;
    this.sparkCatalogName = sparkCatalogName;
    this.catalog = catalog;
  }

  private SparkSession sparkSession() {
    if (lazySpark == null) {
      this.lazySpark = SparkSession.active();
    }

    return lazySpark;
  }

  public MixedTable table() {
    return mixedTable;
  }

  @Override
  public String name() {
    return sparkCatalogName
        + "."
        + mixedTable.id().getDatabase()
        + "."
        + mixedTable.id().getTableName();
  }

  @Override
  public StructType schema() {
    if (lazyTableSchema == null) {
      Schema tableSchema = mixedTable.schema();
      this.lazyTableSchema = SparkSchemaUtil.convert(tableSchema);
    }

    return lazyTableSchema;
  }

  @Override
  public Transform[] partitioning() {
    return Spark3Util.toTransforms(mixedTable.spec());
  }

  @Override
  public Map<String, String> properties() {
    ImmutableMap.Builder<String, String> propsBuilder = ImmutableMap.builder();

    if (!mixedTable.properties().containsKey(TableProperties.BASE_FILE_FORMAT)) {
      propsBuilder.put(TableProperties.BASE_FILE_FORMAT, TableProperties.BASE_FILE_FORMAT_DEFAULT);
    }

    if (!mixedTable.properties().containsKey(TableProperties.DELTA_FILE_FORMAT)) {
      propsBuilder.put(
          TableProperties.DELTA_FILE_FORMAT,
          mixedTable
              .properties()
              .getOrDefault(
                  TableProperties.CHANGE_FILE_FORMAT, TableProperties.CHANGE_FILE_FORMAT_DEFAULT));
    }
    propsBuilder.put("provider", MixedFormatSparkUtils.mixedTableProvider(table()));
    mixedTable.properties().entrySet().stream()
        .filter(entry -> !RESERVED_PROPERTIES.contains(entry.getKey()))
        .forEach(propsBuilder::put);

    return propsBuilder.build();
  }

  @Override
  public Set<TableCapability> capabilities() {
    return CAPABILITIES;
  }

  @Override
  public String toString() {
    return mixedTable.toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other == null || getClass() != other.getClass()) {
      return false;
    }

    // use only name in order to correctly invalidate Spark cache
    MixedSparkTable that = (MixedSparkTable) other;
    return mixedTable.id().equals(that.mixedTable.id());
  }

  @Override
  public int hashCode() {
    // use only name in order to correctly invalidate Spark cache
    return mixedTable.id().hashCode();
  }

  @Override
  public ScanBuilder newScanBuilder(CaseInsensitiveStringMap options) {
    return new SparkScanBuilder(sparkSession(), mixedTable, options);
  }

  @Override
  public WriteBuilder newWriteBuilder(LogicalWriteInfo info) {
    return new MixedFormatSparkWriteBuilder(mixedTable, info, catalog);
  }

  @Override
  public SupportsExtendIdentColumns newUpsertScanBuilder(CaseInsensitiveStringMap options) {
    return new SparkScanBuilder(sparkSession(), mixedTable, options);
  }

  @Override
  public boolean requireAdditionIdentifierColumns() {
    return true;
  }

  @Override
  public boolean appendAsUpsert() {
    return mixedTable.isKeyedTable()
        && Boolean.parseBoolean(
            mixedTable.properties().getOrDefault(TableProperties.UPSERT_ENABLED, "false"));
  }

  @Override
  public StructType partitionSchema() {
    return SparkSchemaUtil.convert(new Schema(table().spec().partitionType().fields()));
  }

  @Override
  public void createPartition(InternalRow ident, Map<String, String> properties)
      throws PartitionAlreadyExistsException, UnsupportedOperationException {
    throw new UnsupportedOperationException("not supported create partition");
  }

  @Override
  public boolean dropPartition(InternalRow ident) {
    return false;
  }

  @Override
  public void replacePartitionMetadata(InternalRow ident, Map<String, String> properties)
      throws NoSuchPartitionException, UnsupportedOperationException {
    throw new UnsupportedOperationException("not supported replace partition");
  }

  @Override
  public Map<String, String> loadPartitionMetadata(InternalRow ident)
      throws UnsupportedOperationException {
    return null;
  }

  @Override
  public InternalRow[] listPartitionIdentifiers(String[] names, InternalRow ident) {
    return new InternalRow[0];
  }
}
