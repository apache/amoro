package com.netease.arctic.mixed;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.HTTPClient;
import org.apache.iceberg.rest.RESTCatalog;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MixedIcebergAmoroCatalog extends BasicMixedIcebergCatalog {

  public static final String HTTP_HEADER_LIST_TABLE_FILTER = "LIST-TABLE-FILTER";

  @Override
  protected Catalog buildIcebergCatalog(
      String name,
      String metastoreType,
      Map<String, String> properties,
      Configuration hadoopConf) {
    Preconditions.checkArgument(CatalogMetaProperties.CATALOG_TYPE_AMS.equalsIgnoreCase(metastoreType),
        this.getClass().getName() + " support internal catalog only.");
    Preconditions.checkNotNull(properties.containsKey(CatalogProperties.URI),
        "lack required properties: %s", CatalogProperties.URI);

    properties = Maps.newHashMap(properties);
    properties.put(CatalogProperties.WAREHOUSE_LOCATION, name);

    // add table-filter to http header.
    // list table only return mixed-iceberg table.
    Map<String, String> headers = Maps.newHashMap();
    headers.put(HTTP_HEADER_LIST_TABLE_FILTER, TableFormat.MIXED_ICEBERG.name());
    String uri = properties.get(CatalogProperties.URI);
    Catalog catalog = new RESTCatalog(config -> HTTPClient.builder(config).uri(uri).withHeaders(headers).build());
    catalog.initialize(name, properties);
    return catalog;
  }

  @Override
  public List<com.netease.arctic.table.TableIdentifier> listTables(String database) {
    // Amoro iceberg rest catalog only return base store of mixed-format
    return icebergCatalog().listTables(Namespace.of(database))
        .stream().map(id -> com.netease.arctic.table.TableIdentifier.of(this.name(), database, id.name()))
        .collect(Collectors.toList());
  }

  @Override
  protected MixedTables newMixedTables(TableMetaStore metaStore, CatalogMeta meta, Catalog icebergCatalog) {
    return new InternalMixedTables(metaStore, meta, icebergCatalog);
  }

  static class InternalMixedTables extends MixedTables {

    public InternalMixedTables(TableMetaStore tableMetaStore, CatalogMeta catalogMeta, Catalog catalog) {
      super(tableMetaStore, catalogMeta, catalog);
    }

    @Override
    protected TableIdentifier generateChangeStoreIdentifier(
        TableIdentifier baseIdentifier) {
      return TableIdentifier.of(baseIdentifier.namespace(), baseIdentifier.name() + "@change");
    }
  }
}
