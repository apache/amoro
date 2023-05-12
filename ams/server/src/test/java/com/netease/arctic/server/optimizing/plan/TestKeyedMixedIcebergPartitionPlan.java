package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestKeyedMixedIcebergPartitionPlan extends TableTestBase {

  public TestKeyedMixedIcebergPartitionPlan(CatalogTestHelper catalogTestHelper,
                                            TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testEmpty() {

  }
}