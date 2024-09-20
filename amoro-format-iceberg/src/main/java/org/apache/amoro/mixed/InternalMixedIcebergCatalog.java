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

package org.apache.amoro.mixed;

import org.apache.amoro.TableFormat;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.HTTPClient;
import org.apache.iceberg.rest.RESTCatalog;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Mixed-iceberg format catalog base on internal catalog. */
public class InternalMixedIcebergCatalog extends BasicMixedIcebergCatalog {

  public static final String CHANGE_STORE_SEPARATOR = "@";
  public static final String CHANGE_STORE_NAME = "change";

  public static final String HTTP_HEADER_LIST_TABLE_FILTER = "LIST-TABLE-FILTER";

  @Override
  protected Catalog buildIcebergCatalog(
      String name, Map<String, String> properties, Configuration hadoopConf) {
    Preconditions.checkNotNull(
        properties.containsKey(CatalogProperties.URI),
        "lack required properties: %s",
        CatalogProperties.URI);

    properties = Maps.newHashMap(properties);
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, name);

    // add table-filter to http header.
    // list table only return mixed-iceberg table.
    Map<String, String> headers = Maps.newHashMap();
    headers.put(HTTP_HEADER_LIST_TABLE_FILTER, TableFormat.MIXED_ICEBERG.name());
    String uri = properties.get(CatalogProperties.URI);
    Catalog catalog =
        new RESTCatalog(config -> HTTPClient.builder(config).uri(uri).withHeaders(headers).build());
    CatalogUtil.configureHadoopConf(catalog, hadoopConf);
    catalog.initialize(name, properties);
    return catalog;
  }

  @Override
  public List<org.apache.amoro.table.TableIdentifier> listTables(String database) {
    // Amoro iceberg rest catalog only return base store of mixed-format
    return icebergCatalog().listTables(Namespace.of(database)).stream()
        .map(id -> org.apache.amoro.table.TableIdentifier.of(this.name(), database, id.name()))
        .collect(Collectors.toList());
  }

  @Override
  protected MixedTables newMixedTables(
      TableMetaStore metaStore, Map<String, String> catalogProperties, Catalog icebergCatalog) {
    return new InternalMixedTables(metaStore, catalogProperties, icebergCatalog);
  }

  static class InternalMixedTables extends MixedTables {

    public InternalMixedTables(
        TableMetaStore tableMetaStore, Map<String, String> catalogProperties, Catalog catalog) {
      super(tableMetaStore, catalogProperties, catalog);
    }

    /**
     * For internal table, using {table-name}@change as change store identifier, this identifier
     * cloud be recognized by AMS. Due to '@' is an invalid character of table name, the change
     * store identifier will never be conflict with other table name.
     *
     * @param baseIdentifier base store table identifier.
     * @return change store iceberg table identifier.
     */
    @Override
    protected TableIdentifier generateChangeStoreIdentifier(TableIdentifier baseIdentifier) {
      return TableIdentifier.of(
          baseIdentifier.namespace(),
          baseIdentifier.name() + CHANGE_STORE_SEPARATOR + CHANGE_STORE_NAME);
    }

    /**
     * The change store will be created automatically by AMS when creating the base store, so we
     * just load change store from AMS
     */
    @Override
    protected Table createChangeStore(
        TableIdentifier baseIdentifier,
        TableIdentifier changeIdentifier,
        Schema schema,
        PartitionSpec partitionSpec,
        PrimaryKeySpec keySpec,
        Map<String, String> properties) {
      return tableMetaStore.doAs(() -> icebergCatalog.loadTable(changeIdentifier));
    }

    /**
     * The change store will be dropped automatically by AMS when dropping the base store, so we do
     * nothing here
     */
    @Override
    protected boolean dropChangeStore(TableIdentifier changStoreIdentifier, boolean purge) {
      // drop change store in AMS
      return true;
    }
  }
}
