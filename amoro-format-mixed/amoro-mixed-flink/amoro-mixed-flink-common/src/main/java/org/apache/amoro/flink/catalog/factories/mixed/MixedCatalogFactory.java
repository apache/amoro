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

package org.apache.amoro.flink.catalog.factories.mixed;

import static org.apache.amoro.flink.table.OptionsUtil.getCatalogProperties;

import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.catalog.MixedCatalog;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CommonCatalogOptions;
import org.apache.flink.table.factories.CatalogFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** Factory for {@link MixedCatalog} */
public class MixedCatalogFactory implements CatalogFactory {

  @Override
  public String factoryIdentifier() {
    return CatalogFactoryOptions.LEGACY_MIXED_IDENTIFIER;
  }

  @Override
  public Catalog createCatalog(Context context) {

    final String defaultDatabase =
        context
            .getOptions()
            .getOrDefault(CommonCatalogOptions.DEFAULT_DATABASE_KEY, MixedCatalog.DEFAULT_DB);
    final String metastoreUrl = context.getOptions().get(CatalogFactoryOptions.METASTORE_URL.key());
    final Map<String, String> catalogProperties = getCatalogProperties(context.getOptions());

    final InternalCatalogBuilder catalogBuilder =
        InternalCatalogBuilder.builder()
            .metastoreUrl(metastoreUrl)
            .catalogName(context.getName())
            .properties(catalogProperties);

    return new MixedCatalog(context.getName(), defaultDatabase, catalogBuilder);
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return Collections.emptySet();
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    return Collections.emptySet();
  }
}
