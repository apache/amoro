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

import static com.netease.arctic.flink.catalog.factories.ArcticCatalogFactoryOptions.DEFAULT_DATABASE;
import static org.apache.flink.table.factories.FactoryUtil.PROPERTY_VERSION;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.flink.catalog.FlinkUnifiedCatalog;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.table.catalog.AbstractCatalog;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Factory for {@link FlinkUnifiedCatalog}. */
public class FlinkCatalogFactory implements CatalogFactory {

  private static final Set<TableFormat> SUPPORTED_FORMATS =
      Sets.newHashSet(TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE);

  @Override
  public String factoryIdentifier() {
    return ArcticCatalogFactoryOptions.AMORO_IDENTIFIER;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    Set<ConfigOption<?>> requiredOptions = new HashSet<>();
    requiredOptions.add(ArcticCatalogFactoryOptions.METASTORE_URL);
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
    String metastoreUrl = helper.getOptions().get(ArcticCatalogFactoryOptions.METASTORE_URL);

    Map<TableFormat, AbstractCatalog> availableCatalogs = Maps.newHashMap();
    SUPPORTED_FORMATS.forEach(
        tableFormat -> {
          if (!availableCatalogs.containsKey(tableFormat)) {
            availableCatalogs.put(tableFormat, createCatalog(context, tableFormat));
          }
        });

    return new FlinkUnifiedCatalog(
        metastoreUrl, context.getName(), defaultDatabase, availableCatalogs);
  }

  private AbstractCatalog createCatalog(Context context, TableFormat tableFormat) {
    CatalogFactory catalogFactory;

    switch (tableFormat) {
      case MIXED_ICEBERG:
      case MIXED_HIVE:
        catalogFactory = new ArcticCatalogFactory();
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported table format: [%s] in the amoro catalog." + tableFormat));
    }

    return (AbstractCatalog) catalogFactory.createCatalog(context);
  }
}
