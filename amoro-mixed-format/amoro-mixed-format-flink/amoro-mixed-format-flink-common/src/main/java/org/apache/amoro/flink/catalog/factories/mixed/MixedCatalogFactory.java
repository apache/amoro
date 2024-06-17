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

import static org.apache.amoro.flink.table.KafkaConnectorOptionsUtil.getKafkaParams;
import static org.apache.flink.table.factories.FactoryUtil.PROPERTY_VERSION;

import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.catalog.MixedCatalog;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.table.factories.FactoryUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Factory for {@link MixedCatalog} */
public class MixedCatalogFactory implements CatalogFactory {

  @Override
  public String factoryIdentifier() {
    return CatalogFactoryOptions.LEGACY_MIXED_IDENTIFIER;
  }

  @Override
  public Catalog createCatalog(Context context) {

    final FactoryUtil.CatalogFactoryHelper helper =
        FactoryUtil.createCatalogFactoryHelper(this, context);
    helper.validate();

    final String defaultDatabase = helper.getOptions().get(CatalogFactoryOptions.DEFAULT_DATABASE);
    String metastoreUrl = helper.getOptions().get(CatalogFactoryOptions.METASTORE_URL);
    final Map<String, String> mixedCatalogProperties = getKafkaParams(context.getOptions());
    final Map<String, String> catalogProperties = Maps.newHashMap(mixedCatalogProperties);

    Optional<String> tableFormatsOptional =
        helper.getOptions().getOptional(CatalogFactoryOptions.FLINK_TABLE_FORMATS);
    tableFormatsOptional.ifPresent(
        tableFormats ->
            catalogProperties.put(CatalogFactoryOptions.FLINK_TABLE_FORMATS.key(), tableFormats));

    return new MixedCatalog(
        context.getName(),
        defaultDatabase,
        InternalCatalogBuilder.builder().metastoreUrl(metastoreUrl).properties(catalogProperties));
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return Collections.emptySet();
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    final Set<ConfigOption<?>> options = new HashSet<>();
    options.add(PROPERTY_VERSION);
    options.add(CatalogFactoryOptions.METASTORE_URL);
    options.add(CatalogFactoryOptions.DEFAULT_DATABASE);

    // authorization config
    options.add(CatalogFactoryOptions.AUTH_AMS_CONFIGS_DISABLE);
    options.add(CatalogFactoryOptions.AUTH_METHOD);
    options.add(CatalogFactoryOptions.SIMPLE_USER_NAME);
    options.add(CatalogFactoryOptions.KEYTAB_LOGIN_USER);
    options.add(CatalogFactoryOptions.KRB5_CONF_PATH);
    options.add(CatalogFactoryOptions.KRB5_CONF_ENCODE);
    options.add(CatalogFactoryOptions.KEYTAB_PATH);
    options.add(CatalogFactoryOptions.KEYTAB_ENCODE);

    options.add(CatalogFactoryOptions.FLINK_TABLE_FORMATS);
    return options;
  }
}
