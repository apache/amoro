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

import static org.apache.amoro.properties.CatalogMetaProperties.TABLE_FORMATS;

import org.apache.amoro.flink.catalog.FlinkUnifiedCatalog;
import org.apache.amoro.flink.catalog.MixedCatalog;
import org.apache.flink.annotation.Internal;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;

/** {@link ConfigOption}s for {@link MixedCatalog} and {@link FlinkUnifiedCatalog}. */
@Internal
public class CatalogFactoryOptions {
  public static final String MIXED_ICEBERG_IDENTIFIER = "mixed_iceberg";
  public static final String MIXED_HIVE_IDENTIFIER = "mixed_hive";
  @Deprecated public static final String LEGACY_MIXED_IDENTIFIER = "arctic";
  public static final String UNIFIED_IDENTIFIER = "unified";

  public static final ConfigOption<String> METASTORE_URL =
      ConfigOptions.key("metastore.url").stringType().noDefaultValue();

  public static final ConfigOption<String> FLINK_TABLE_FORMATS =
      ConfigOptions.key(TABLE_FORMATS)
          .stringType()
          .noDefaultValue()
          .withDescription("This illustrates the table format contained in the catalog.");
}
