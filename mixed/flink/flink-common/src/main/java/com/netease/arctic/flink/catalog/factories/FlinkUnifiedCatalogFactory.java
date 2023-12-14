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

package com.netease.arctic.flink.catalog.factories;

import static com.netease.arctic.ams.api.Constants.THRIFT_TABLE_SERVICE_NAME;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.TABLE_FORMATS;
import static com.netease.arctic.flink.catalog.factories.CatalogFactoryOptions.DEFAULT_DATABASE;
import static com.netease.arctic.flink.catalog.factories.CatalogFactoryOptions.FLINK_TABLE_FORMATS;
import static org.apache.flink.table.factories.FactoryUtil.PROPERTY_VERSION;

import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.UnifiedCatalogLoader;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.client.ArcticThriftUrl;
import com.netease.arctic.flink.catalog.FlinkUnifiedCatalog;
import com.netease.arctic.flink.catalog.factories.iceberg.IcebergFlinkCatalogFactory;
import com.netease.arctic.flink.catalog.factories.mixed.MixedCatalogFactory;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Factory for {@link FlinkUnifiedCatalog}. */
public class FlinkUnifiedCatalogFactory implements CatalogFactory {

  private static final Set<TableFormat> SUPPORTED_FORMATS =
      Sets.newHashSet(TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE, TableFormat.ICEBERG);

  @Override
  public String factoryIdentifier() {
    return CatalogFactoryOptions.UNIFIED_IDENTIFIER;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    Set<ConfigOption<?>> requiredOptions = new HashSet<>();
    requiredOptions.add(CatalogFactoryOptions.METASTORE_URL);
    return requiredOptions;
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    final Set<ConfigOption<?>> options = new HashSet<>();
    options.add(PROPERTY_VERSION);
    options.add(DEFAULT_DATABASE);
    return options;
  }

  @Override
  public Catalog createCatalog(Context context) {
    final FactoryUtil.CatalogFactoryHelper helper =
        FactoryUtil.createCatalogFactoryHelper(this, context);
    helper.validate();

    final String defaultDatabase = helper.getOptions().get(DEFAULT_DATABASE);
    String metastoreUrl = helper.getOptions().get(CatalogFactoryOptions.METASTORE_URL);

    String amoroCatalogName =
        ArcticThriftUrl.parse(metastoreUrl, THRIFT_TABLE_SERVICE_NAME).catalogName();
    UnifiedCatalog unifiedCatalog =
        UnifiedCatalogLoader.loadUnifiedCatalog(metastoreUrl, amoroCatalogName, Maps.newHashMap());
    Configuration hadoopConf = unifiedCatalog.authenticationContext().getConfiguration();

    TableFormat catalogTableFormat =
        TableFormat.valueOf(unifiedCatalog.properties().get(TABLE_FORMATS));

    Map<TableFormat, AbstractCatalog> availableCatalogs = Maps.newHashMap();
    SUPPORTED_FORMATS.forEach(
        tableFormat -> {
          if (!availableCatalogs.containsKey(tableFormat)) {
            if (catalogTableFormat == TableFormat.ICEBERG
                && (tableFormat.in(TableFormat.MIXED_HIVE, TableFormat.MIXED_ICEBERG))) {
              // Mixed catalog couldn't load the iceberg table, so specify the table formats to the
              // mixed catalog
              context.getOptions().put(FLINK_TABLE_FORMATS.key(), tableFormat.toString());
            }
            availableCatalogs.put(tableFormat, createCatalog(context, tableFormat, hadoopConf));
          }
        });

    return new FlinkUnifiedCatalog(
        metastoreUrl, context.getName(), defaultDatabase, unifiedCatalog, availableCatalogs);
  }

  private AbstractCatalog createCatalog(
      Context context, TableFormat tableFormat, Configuration hadoopConf) {
    CatalogFactory catalogFactory;

    switch (tableFormat) {
      case MIXED_ICEBERG:
      case MIXED_HIVE:
        catalogFactory = new MixedCatalogFactory();
        break;
      case ICEBERG:
        catalogFactory = new IcebergFlinkCatalogFactory(hadoopConf);
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported table format: [%s] in the amoro catalog." + tableFormat));
    }

    try {
      return (AbstractCatalog) catalogFactory.createCatalog(context);
    } catch (CatalogException e) {
      if (e.getMessage().contains("must implement createCatalog(Context)")) {
        return (AbstractCatalog)
            catalogFactory.createCatalog(context.getName(), context.getOptions());
      }
      throw e;
    }
  }
}
