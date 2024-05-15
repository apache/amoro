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

package org.apache.amoro.trino;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedCatalogUtil;
import io.trino.spi.classloader.ThreadContextClassLoader;

import javax.inject.Inject;

import java.util.Collections;

/** A factory to generate {@link MixedFormatCatalog} */
public class DefaultAmoroCatalogFactory implements AmoroCatalogFactory {

  private final AmoroConfig amoroConfig;

  private volatile MixedFormatCatalog amoroCatalog;
  private volatile TableMetaStore tableMetaStore;

  @Inject
  public DefaultAmoroCatalogFactory(AmoroConfig amoroConfig) {
    this.amoroConfig = amoroConfig;
  }

  public MixedFormatCatalog getAmoroCatalog() {
    if (amoroCatalog == null) {
      synchronized (this) {
        if (amoroCatalog == null) {
          try (ThreadContextClassLoader ignored =
              new ThreadContextClassLoader(this.getClass().getClassLoader())) {
            this.amoroCatalog =
                new AmoroCatalogSupportTableSuffix(
                    CatalogLoader.load(amoroConfig.getCatalogUrl(), Collections.emptyMap()));
          }
        }
      }
    }
    return amoroCatalog;
  }

  @Override
  public TableMetaStore getTableMetastore() {
    if (this.tableMetaStore == null) {
      synchronized (this) {
        if (this.tableMetaStore == null) {
          try (ThreadContextClassLoader ignored =
              new ThreadContextClassLoader(this.getClass().getClassLoader())) {
            CatalogMeta meta = CatalogLoader.loadMeta(amoroConfig.getCatalogUrl());
            this.tableMetaStore = MixedCatalogUtil.buildMetaStore(meta);
          }
        }
      }
    }
    return this.tableMetaStore;
  }
}
