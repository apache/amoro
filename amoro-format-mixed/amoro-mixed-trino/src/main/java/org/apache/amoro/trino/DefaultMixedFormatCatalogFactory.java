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

import io.trino.spi.classloader.ThreadContextClassLoader;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;

import javax.inject.Inject;

import java.util.Collections;

/** A factory to generate {@link MixedFormatCatalog} */
public class DefaultMixedFormatCatalogFactory implements MixedFormatCatalogFactory {

  private final MixedFormatConfig mixedFormatConfig;
  private volatile MixedFormatCatalog mixedFormatCatalog;
  private volatile TableMetaStore tableMetaStore;

  @Inject
  public DefaultMixedFormatCatalogFactory(MixedFormatConfig mixedFormatConfig) {
    this.mixedFormatConfig = mixedFormatConfig;
  }

  public MixedFormatCatalog getMixedFormatCatalog() {
    if (mixedFormatCatalog == null) {
      synchronized (this) {
        if (mixedFormatCatalog == null) {
          try (ThreadContextClassLoader ignored =
              new ThreadContextClassLoader(this.getClass().getClassLoader())) {
            this.mixedFormatCatalog =
                new MixedFormatCatalogSupportTableSuffix(
                    CatalogLoader.load(mixedFormatConfig.getCatalogUrl(), Collections.emptyMap()));
          }
        }
      }
    }
    return mixedFormatCatalog;
  }

  @Override
  public TableMetaStore getTableMetastore() {
    if (this.tableMetaStore == null) {
      synchronized (this) {
        if (this.tableMetaStore == null) {
          try (ThreadContextClassLoader ignored =
              new ThreadContextClassLoader(this.getClass().getClassLoader())) {
            CatalogMeta meta = CatalogLoader.loadMeta(mixedFormatConfig.getCatalogUrl());
            this.tableMetaStore = CatalogUtil.buildMetaStore(meta);
          }
        }
      }
    }
    return this.tableMetaStore;
  }
}
