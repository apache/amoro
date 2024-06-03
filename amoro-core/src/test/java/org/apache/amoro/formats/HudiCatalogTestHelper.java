package org.apache.amoro.formats;

import org.apache.amoro.AmoroCatalog;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.hudi.HudiCatalogFactory;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedCatalogUtil;

import java.util.Map;

public class HudiCatalogTestHelper extends AbstractFormatCatalogTestHelper<FormatCatalog> {

  protected HudiCatalogTestHelper(String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, catalogProperties);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.HUDI;
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    HudiCatalogFactory catalogFactory = new HudiCatalogFactory();
    TableMetaStore metaStore = MixedCatalogUtil.buildMetaStore(getCatalogMeta());
    Map<String, String> convertedCatalogProperties =
        catalogFactory.convertCatalogProperties(
            catalogName, getMetastoreType(), getCatalogMeta().getCatalogProperties());
    return catalogFactory.create(
        catalogName, getMetastoreType(), convertedCatalogProperties, metaStore);
  }

  @Override
  public FormatCatalog originalCatalog() {
    return null;
  }

  @Override
  public void setTableProperties(String db, String tableName, String key, String value) {

  }

  @Override
  public void removeTableProperties(String db, String tableName, String key) {

  }

  @Override
  public void clean() {

  }

  @Override
  public void createTable(String db, String tableName) throws Exception {

  }

  @Override
  public void createDatabase(String database) throws Exception {

  }
}
