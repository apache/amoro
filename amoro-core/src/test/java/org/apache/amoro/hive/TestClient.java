package org.apache.amoro.hive;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedCatalogUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClient {

  @Test
  public void test() throws TException, InterruptedException {
    HiveConf conf = new HiveConf();
    conf.set("hive.metastore.uris", "thrift://127.0.0.1:9083");
    Map<String, String> properties = Maps.newHashMap();
    properties.put(CatalogProperties.URI, conf.get(HiveConf.ConfVars.METASTOREURIS.varname));
    CatalogMeta meta = CatalogTestHelpers.buildHiveCatalogMeta(
        "hive", properties, conf, TableFormat.HUDI);
    TableMetaStore metaStore = MixedCatalogUtil.buildMetaStore(meta);
    CachedHiveClientPool client = new CachedHiveClientPool(metaStore, new HashMap<>());

//    try {
//      Database db = new Database();
//      db.setName("amoro");
//      client.run( c -> {
//        c.createDatabase(db);
//        return null;
//      });
//    }catch (Exception e) {
//      e.printStackTrace();
//    }
    List<String> list = client.run(c -> c.getAllDatabases());
    System.out.println(list);
    Database databases = client.run(c -> c.getDatabase("amoro"));
    System.out.println(databases);
  }
}
