package com.netease.arctic.server.table.executor;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static com.netease.arctic.BasicTableTestHelper.SPEC;

@RunWith(Parameterized.class)
public class TestDataExpireIceberg extends TestDataExpire {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
        // Iceberg format partitioned by timestamp filed
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
         new BasicTableTestHelper(false, true, getDefaultProp())},
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
         new BasicTableTestHelper(false, false, getDefaultProp())},
        // Iceberg format partitioned by timestampz filed
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
         new BasicTableTestHelper(TABLE_SCHEMA1, PrimaryKeySpec.noPrimaryKey(), SPEC, getDefaultProp())},
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
         new BasicTableTestHelper(TABLE_SCHEMA1, PrimaryKeySpec.noPrimaryKey(), PartitionSpec.unpartitioned(),
             getDefaultProp())},
        // Iceberg format partitioned by date string filed
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
         new BasicTableTestHelper(TABLE_SCHEMA2, PrimaryKeySpec.noPrimaryKey(), SPEC2, getDefaultProp())},
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
         new BasicTableTestHelper(TABLE_SCHEMA2, PrimaryKeySpec.noPrimaryKey(), PartitionSpec.unpartitioned(),
             getDefaultProp())}
    };
  }

  public TestDataExpireIceberg(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }
}