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

package org.apache.amoro.spark.mixed;

import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet;
import org.apache.amoro.spark.SessionCatalogBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.Set;

public abstract class MixedSessionCatalogBase<T extends TableCatalog & SupportsNamespaces>
    extends SessionCatalogBase<T> {

  /** Using {@link #MIXED_ICEBERG_PROVIDER} or {@link #MIXED_HIVE_PROVIDER} instead. */
  @Deprecated public static final String LEGACY_MIXED_FORMAT_PROVIDER = "arctic";

  /** Provider when creating a mixed-iceberg table in session catalog */
  public static final String MIXED_ICEBERG_PROVIDER = "mixed_iceberg";

  /** Provider when creating a mixed-hive table in session catalog. */
  public static final String MIXED_HIVE_PROVIDER = "mixed_hive";

  /** Supported providers */
  public static final Set<String> SUPPORTED_PROVIDERS =
      ImmutableSet.of(LEGACY_MIXED_FORMAT_PROVIDER, MIXED_ICEBERG_PROVIDER, MIXED_HIVE_PROVIDER);

  /**
   * build mixed-format catalog instance.
   *
   * @param name spark catalog name
   * @param options catalog initialize options
   * @return mixed format spark catalog.
   */
  protected abstract MixedSparkCatalogBase buildTargetCatalog(
      String name, CaseInsensitiveStringMap options);

  @Override
  protected boolean isManagedTable(Table table) {
    return MixedFormatSparkUtil.isMixedFormatTable(table);
  }

  @Override
  protected boolean isManagedSubTable(Identifier ident) {
    if (ident.namespace().length != 2) {
      return false;
    }
    return MixedTableStoreType.from(ident.name()) != null;
  }

  @Override
  protected boolean isManagedProvider(String provider) {
    return StringUtils.isNotBlank(provider) && SUPPORTED_PROVIDERS.contains(provider);
  }
}
