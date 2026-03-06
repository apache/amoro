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

package org.apache.amoro.flink.catalog.factories.paimon;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.paimon.catalog.FileSystemCatalogFactory;
import org.apache.paimon.flink.FlinkCatalog;
import org.apache.paimon.flink.FlinkCatalogFactory;
import org.apache.paimon.options.CatalogOptions;

import java.util.Map;

/** Creating Paimon FlinkCatalogFactory with properties which stored in the AMS */
public class PaimonFlinkCatalogFactory extends FlinkCatalogFactory {
  private final Map<String, String> options;
  private final String metastoreType;

  public PaimonFlinkCatalogFactory(Map<String, String> options, String metastoreType) {
    this.options = options;
    this.metastoreType = metastoreType;
  }

  @Override
  public FlinkCatalog createCatalog(Context context) {
    context.getOptions().putAll(options);
    addMetastoreType(context);
    return super.createCatalog(context);
  }

  private void addMetastoreType(Context context) {
    String type;
    if (CatalogMetaProperties.CATALOG_TYPE_HADOOP.equalsIgnoreCase(metastoreType)) {
      type = FileSystemCatalogFactory.IDENTIFIER;
    } else {
      type = metastoreType;
    }
    context.getOptions().put(CatalogOptions.METASTORE.key(), type);
  }
}
