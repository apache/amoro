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

package org.apache.amoro.formats;

import org.apache.amoro.AmoroCatalog;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.hudi.HudiCatalogFactory;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedCatalogUtil;

import java.util.Map;

public class HudiCatalogTestHelper extends AbstractFormatCatalogTestHelper<FormatCatalog> {

  protected HudiCatalogTestHelper(String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, catalogProperties);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.HUDI;
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    HudiCatalogFactory catalogFactory = new HudiCatalogFactory();
    TableMetaStore metaStore = MixedCatalogUtil.buildMetaStore(getCatalogMeta());
    Map<String, String> convertedCatalogProperties =
        catalogFactory.convertCatalogProperties(
            catalogName, getMetastoreType(), getCatalogMeta().getCatalogProperties());
    return catalogFactory.create(
        catalogName, getMetastoreType(), convertedCatalogProperties, metaStore);
  }

  @Override
  public FormatCatalog originalCatalog() {
    return null;
  }

  @Override
  public void setTableProperties(String db, String tableName, String key, String value) {}

  @Override
  public void removeTableProperties(String db, String tableName, String key) {}

  @Override
  public void clean() {}

  @Override
  public void createTable(String db, String tableName) throws Exception {}

  @Override
  public void createDatabase(String database) throws Exception {}
}
