package com.netease.arctic.server.catalog;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.CatalogProperties;

import java.util.Arrays;
import java.util.Set;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_AMS;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_CUSTOM;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;

public class CatalogBuilder {

  //TODO: use internal or external concepts
  public static ServerCatalog buildServerCatalog(CatalogMeta catalogMeta, Configurations serverConfiguration) {
    String type = catalogMeta.getCatalogType();
    Set<TableFormat> tableFormats = CatalogUtil.tableFormats(catalogMeta);

    switch (type) {
      case CATALOG_TYPE_HADOOP:
        Preconditions.checkArgument(
            includeFormatCheck(tableFormats, TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
            "Hadoop catalog support iceberg/mixed-iceberg table only.");
        return new CommonExternalCatalogImpl(catalogMeta);
      case CATALOG_TYPE_HIVE:
        if (includeFormatCheck(tableFormats, TableFormat.MIXED_HIVE)) {
          return new MixedHiveCatalogImpl(catalogMeta);
        } else if (includeFormatCheck(tableFormats, TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG)) {
          return new CommonExternalCatalogImpl(catalogMeta);
        } else {
          throw new IllegalArgumentException("Hive Catalog support iceberg/mixed-iceberg table or mixed-hive table.");
        }

      case CATALOG_TYPE_AMS:
        Preconditions.checkArgument(
            includeFormatCheck(tableFormats, TableFormat.MIXED_ICEBERG),
            "AMS catalog support mixed iceberg table only.");
        if (tableFormat.equals(TableFormat.MIXED_ICEBERG)) {
          return new MixedCatalogImpl(catalogMeta);
        } else if (tableFormat.equals(TableFormat.ICEBERG)) {
          return new InternalIcebergCatalogImpl(catalogMeta, serverConfiguration);
        } else {
          throw new IllegalStateException("AMS catalog support iceberg/mixed-iceberg table only.");
        }
        return new MixedCatalogImpl(catalogMeta);
      case CATALOG_TYPE_CUSTOM:
        Preconditions.checkArgument(
            includeFormatCheck(tableFormats, TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
            "Custom catalog support iceberg table only.");
        Preconditions.checkArgument(catalogMeta.getCatalogProperties().containsKey(CatalogProperties.CATALOG_IMPL),
            "Custom catalog properties must contains " + CatalogProperties.CATALOG_IMPL);
        return new CommonExternalCatalogImpl(catalogMeta);
      default:
        throw new IllegalStateException("unsupported catalog type:" + type);
    }
  }


  private static boolean includeFormatCheck(Set<TableFormat> formats, TableFormat... supports) {
    if (formats.size() <= supports.length) {
      Set<TableFormat> subSet =  Sets.newHashSet(formats);
      Arrays.stream(supports).forEach(subSet::remove);
      return subSet.isEmpty();
    }
    return false;
  }

}
