package com.netease.arctic.formats.paimon;

import com.netease.arctic.FormatCatalogFactory;
import com.netease.arctic.ams.api.TableFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.options.Options;

import java.util.Map;

public class PaimonCatalogFactory implements FormatCatalogFactory {
  @Override
  public PaimonCatalog create(
      String name, String metastoreType, Map<String, String> properties, Configuration configuration) {

    Options options = Options.fromMap(properties);

    CatalogContext catalogContext = CatalogContext.create(options, configuration);

    return new PaimonCatalog(CatalogFactory.createCatalog(catalogContext), name);
  }

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }
}
