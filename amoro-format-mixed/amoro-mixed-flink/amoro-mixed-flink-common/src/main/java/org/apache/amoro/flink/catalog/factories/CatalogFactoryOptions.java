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

import static org.apache.amoro.flink.catalog.MixedCatalog.DEFAULT_DB;
import static org.apache.amoro.properties.CatalogMetaProperties.TABLE_FORMATS;

import org.apache.amoro.flink.catalog.FlinkUnifiedCatalog;
import org.apache.amoro.flink.catalog.MixedCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.flink.annotation.Internal;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.table.catalog.CommonCatalogOptions;

/** {@link ConfigOption}s for {@link MixedCatalog} and {@link FlinkUnifiedCatalog}. */
@Internal
public class CatalogFactoryOptions {
  public static final String MIXED_ICEBERG_IDENTIFIER = "mixed_iceberg";
  public static final String MIXED_HIVE_IDENTIFIER = "mixed_hive";
  @Deprecated public static final String LEGACY_MIXED_IDENTIFIER = "arctic";
  public static final String UNIFIED_IDENTIFIER = "unified";

  public static final ConfigOption<String> DEFAULT_DATABASE =
      ConfigOptions.key(CommonCatalogOptions.DEFAULT_DATABASE_KEY)
          .stringType()
          .defaultValue(DEFAULT_DB);

  public static final String PROPERTIES_PREFIX = "properties";

  public static final ConfigOption<String> METASTORE_URL =
      ConfigOptions.key("metastore.url").stringType().noDefaultValue();

  // authorization configs
  public static final ConfigOption<String> AUTH_AMS_CONFIGS_DISABLE =
      ConfigOptions.key(PROPERTIES_PREFIX + "." + CatalogMetaProperties.LOAD_AUTH_FROM_AMS)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> AUTH_METHOD =
      ConfigOptions.key(PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> SIMPLE_USER_NAME =
      ConfigOptions.key(
              PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> KEYTAB_LOGIN_USER =
      ConfigOptions.key(PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> KRB5_CONF_PATH =
      ConfigOptions.key(PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB_PATH)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> KRB5_CONF_ENCODE =
      ConfigOptions.key(PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB_ENCODE)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> KEYTAB_PATH =
      ConfigOptions.key(
              PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB_PATH)
          .stringType()
          .noDefaultValue();
  public static final ConfigOption<String> KEYTAB_ENCODE =
      ConfigOptions.key(
              PROPERTIES_PREFIX + "." + CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB_ENCODE)
          .stringType()
          .noDefaultValue();

  public static final ConfigOption<String> FLINK_TABLE_FORMATS =
      ConfigOptions.key(TABLE_FORMATS)
          .stringType()
          .noDefaultValue()
          .withDescription("This illustrates the table format contained in the catalog.");
}
