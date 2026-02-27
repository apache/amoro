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

import io.trino.plugin.iceberg.catalog.TrinoCatalog;
import io.trino.plugin.iceberg.catalog.TrinoCatalogFactory;
import io.trino.spi.security.ConnectorIdentity;

import javax.inject.Inject;

/** Factory to generate TrinoCatalog */
public class MixedFormatTrinoCatalogFactory implements TrinoCatalogFactory {

  private final MixedFormatCatalogFactory mixedFormatCatalogFactory;

  @Inject
  public MixedFormatTrinoCatalogFactory(MixedFormatCatalogFactory mixedFormatCatalogFactory) {
    this.mixedFormatCatalogFactory = mixedFormatCatalogFactory;
  }

  @Override
  public TrinoCatalog create(ConnectorIdentity identity) {
    return new MixedFormatTrinoCatalog(mixedFormatCatalogFactory.getMixedFormatCatalog());
  }
}
