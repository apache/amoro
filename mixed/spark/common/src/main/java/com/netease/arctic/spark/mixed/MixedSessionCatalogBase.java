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

package com.netease.arctic.spark.mixed;

import com.netease.arctic.spark.SessionCatalogBase;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

public abstract class MixedSessionCatalogBase<T extends TableCatalog & SupportsNamespaces>
    extends SessionCatalogBase<T> {

  public static final String LEGACY_MIXED_FORMAT_PROVIDER = "arctic";

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
    return LEGACY_MIXED_FORMAT_PROVIDER.equalsIgnoreCase(provider);
  }
}
