package com.netease.arctic.server.catalog;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_AMS;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_CUSTOM;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_GLUE;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;

import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public class CatalogBuilder {

  /** matrix of catalog type and supported table formats */
  private static final Map<String, Set<TableFormat>> formatSupportedMatrix =
      ImmutableMap.of(
          CATALOG_TYPE_HADOOP,
              Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.PAIMON),
          CATALOG_TYPE_GLUE, Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
          CATALOG_TYPE_CUSTOM, Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
          CATALOG_TYPE_HIVE,
              Sets.newHashSet(
                  TableFormat.ICEBERG,
                  TableFormat.MIXED_ICEBERG,
                  TableFormat.MIXED_HIVE,
                  TableFormat.PAIMON),
          CATALOG_TYPE_AMS, Sets.newHashSet(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG));

  public static ServerCatalog buildServerCatalog(
      CatalogMeta catalogMeta, Configurations serverConfiguration) {
    String type = catalogMeta.getCatalogType();
    Set<TableFormat> tableFormats = CatalogUtil.tableFormats(catalogMeta);

    Preconditions.checkState(
        formatSupportedMatrix.containsKey(type), "unsupported catalog type: %s", type);
    Preconditions.checkState(
        tableFormats.size() == 1,
        "only 1 types format is supported: %s",
        Joiner.on(",").join(tableFormats));

    Set<TableFormat> supportedFormats = formatSupportedMatrix.get(type);
    TableFormat tableFormat = tableFormats.iterator().next();
    Preconditions.checkState(
        supportedFormats.contains(tableFormat),
        "Table format %s is not supported for metastore type: %s",
        tableFormat,
        type);

    switch (type) {
      case CATALOG_TYPE_HADOOP:
      case CATALOG_TYPE_GLUE:
      case CATALOG_TYPE_CUSTOM:
        return new ExternalCatalog(catalogMeta);
      case CATALOG_TYPE_HIVE:
        if (tableFormat.equals(TableFormat.MIXED_HIVE)) {
          return new MixedHiveCatalogImpl(catalogMeta);
        }
        return new ExternalCatalog(catalogMeta);
      case CATALOG_TYPE_AMS:
        if (tableFormat.equals(TableFormat.MIXED_ICEBERG)) {
          return new InternalMixedCatalogImpl(catalogMeta, serverConfiguration);
        } else if (tableFormat.equals(TableFormat.ICEBERG)) {
          return new InternalIcebergCatalogImpl(catalogMeta, serverConfiguration);
        } else {
          throw new IllegalStateException("AMS catalog support iceberg/mixed-iceberg table only.");
        }
      default:
        throw new IllegalStateException("unsupported catalog type:" + type);
    }
  }
}
