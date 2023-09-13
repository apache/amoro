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
    } else if ("hive".equalsIgnoreCase(metastoreType)) {
      type = "hive";
    } else {
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
