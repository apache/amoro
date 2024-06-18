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

package org.apache.amoro.server.catalog;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.server.exception.IllegalMetadataException;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;

import java.util.List;

public abstract class ServerCatalog extends PersistentBase {

  private volatile CatalogMeta metadata;

  protected ServerCatalog(CatalogMeta metadata) {
    this.metadata = metadata;
  }

  public String name() {
    return metadata.getCatalogName();
  }

  public CatalogMeta getMetadata() {
    return metadata;
  }

  public void updateMetadata(CatalogMeta metadata) {
    doAs(CatalogMetaMapper.class, mapper -> mapper.updateCatalog(metadata));
    this.metadata = metadata;
  }

  public abstract boolean databaseExists(String database);

  public abstract boolean tableExists(String database, String tableName);

  public abstract List<String> listDatabases();

  public abstract List<TableIDWithFormat> listTables();

  public abstract List<TableIDWithFormat> listTables(String database);

  public abstract AmoroTable<?> loadTable(String database, String tableName);

  public void dispose() {
    doAsTransaction(
        () ->
            doAsExisted(
                CatalogMetaMapper.class,
                mapper -> mapper.deleteCatalog(name()),
                () ->
                    new IllegalMetadataException(
                        "Catalog " + name() + " has more than one database or table")));
  }
}
