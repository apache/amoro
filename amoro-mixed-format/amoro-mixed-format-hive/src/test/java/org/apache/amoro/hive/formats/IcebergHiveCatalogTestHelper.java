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

package org.apache.amoro.hive.formats;

import org.apache.amoro.formats.IcebergHadoopCatalogTestHelper;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.iceberg.CatalogProperties;

import java.util.HashMap;
import java.util.Map;

public class IcebergHiveCatalogTestHelper extends IcebergHadoopCatalogTestHelper {

  public IcebergHiveCatalogTestHelper(String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, catalogProperties);
  }

  @Override
  public void initHiveConf(Configuration hiveConf) {
    PROPERTIES.put(CatalogProperties.URI, hiveConf.get(HiveConf.ConfVars.METASTOREURIS.varname));
  }

  protected String getMetastoreType() {
    return CatalogMetaProperties.CATALOG_TYPE_HIVE;
  }

  public static IcebergHiveCatalogTestHelper defaultHelper() {
    return new IcebergHiveCatalogTestHelper("test_iceberg_catalog", new HashMap<>());
  }
}
