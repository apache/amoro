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

package org.apache.amoro.flink.catalog.factories;

import static org.apache.amoro.Constants.THRIFT_TABLE_SERVICE_NAME;
import static org.apache.amoro.flink.table.OptionsUtil.getCatalogProperties;
import static org.apache.amoro.properties.CatalogMetaProperties.TABLE_FORMATS;

import org.apache.amoro.CommonUnifiedCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.UnifiedCatalogLoader;
import org.apache.amoro.client.AmsThriftUrl;
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.catalog.FlinkUnifiedCatalog;
import org.apache.amoro.flink.catalog.MixedCatalog;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CommonCatalogOptions;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.util.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.flink.FlinkCatalogFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** Factory for {@link FlinkUnifiedCatalog}. */
public class FlinkUnifiedCatalogFactory implements CatalogFactory {

  public static final Set<TableFormat> SUPPORTED_FORMATS =
      Sets.newHashSet(
          TableFormat.MIXED_ICEBERG,
          TableFormat.MIXED_HIVE,
          TableFormat.ICEBERG,
          TableFormat.PAIMON);

  @Override
  public String factoryIdentifier() {
    return CatalogFactoryOptions.UNIFIED_IDENTIFIER;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return Collections.emptySet();
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    return Collections.emptySet();
  }

  @Override
  public Catalog createCatalog(Context context) {

    final String defaultDatabase =
        context
            .getOptions()
            .getOrDefault(CommonCatalogOptions.DEFAULT_DATABASE_KEY, MixedCatalog.DEFAULT_DB);
    final String metastoreUrl = context.getOptions().get(CatalogFactoryOptions.METASTORE_URL.key());
    final Map<String, String> catalogProperties = getCatalogProperties(context.getOptions());

    UnifiedCatalog unifiedCatalog;
    if (metastoreUrl != null) {
      String amoroCatalogName =
          AmsThriftUrl.parse(metastoreUrl, THRIFT_TABLE_SERVICE_NAME).catalogName();
      unifiedCatalog =
          UnifiedCatalogLoader.loadUnifiedCatalog(
              metastoreUrl, amoroCatalogName, catalogProperties);
    } else {
      String metastoreType = catalogProperties.get(FlinkCatalogFactory.ICEBERG_CATALOG_TYPE);
      Preconditions.checkArgument(metastoreType != null, "Catalog type cannot be empty");
      TableMetaStore tableMetaStore =
          TableMetaStore.builder()
              .withConfiguration(
                  InternalCatalogBuilder.clusterHadoopConf(metastoreType, catalogProperties))
              .build();
      unifiedCatalog =
          new CommonUnifiedCatalog(
              context.getName(), metastoreType, catalogProperties, tableMetaStore);
    }
    Configuration hadoopConf = unifiedCatalog.authenticationContext().getConfiguration();
    Set<TableFormat> tableFormats =
        CatalogUtil.tableFormats(unifiedCatalog.metastoreType(), unifiedCatalog.properties());
    validate(tableFormats);

    return new FlinkUnifiedCatalog(
        metastoreUrl, defaultDatabase, unifiedCatalog, context, hadoopConf);
  }

  private void validate(Set<TableFormat> expectedFormats) {
    if (expectedFormats.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "The table formats must be specified in the catalog properties: [%s]",
              TABLE_FORMATS));
    }
    if (!SUPPORTED_FORMATS.containsAll(expectedFormats)) {
      throw new IllegalArgumentException(
          String.format(
              "The table formats [%s] are not supported in the unified catalog, the supported table formats are [%s].",
              expectedFormats, SUPPORTED_FORMATS));
    }
  }
}
