/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats.paimon;

import com.netease.arctic.FormatCatalogFactory;
import com.netease.arctic.ams.api.TableFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.options.Options;

import java.util.Map;

public class PaimonCatalogFactory implements FormatCatalogFactory {
  @Override
  public PaimonCatalog create(
      String name, String metastoreType, Map<String, String> properties, Configuration configuration) {

    Options options = Options.fromMap(properties);

    String type;
    if ("hadoop".equalsIgnoreCase(metastoreType)) {
      type = "filesystem";
    } else  {
      type = metastoreType;
    }
    options.set(CatalogOptions.METASTORE, type);

    CatalogContext catalogContext = CatalogContext.create(options, configuration);

    return new PaimonCatalog(CatalogFactory.createCatalog(catalogContext), name);
  }

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }
}
