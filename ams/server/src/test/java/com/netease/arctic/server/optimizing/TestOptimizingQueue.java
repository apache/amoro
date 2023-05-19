package com.netease.arctic.server.optimizing;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.server.table.AMSTableTestBase;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;

@RunWith(Parameterized.class)
public class TestOptimizingQueue extends AMSTableTestBase {

  public TestOptimizingQueue(CatalogTestHelper catalogTestHelper,
                             TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, true);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
            new BasicTableTestHelper(false, true)}};
  }

  @Test
  public void testPollNoTask() {
    TableRuntimeMeta tableRuntimeMeta = getTableRuntimeMeta(OptimizingStatus.PENDING);
    OptimizingQueue queue = buildOptimizingQueue(tableRuntimeMeta);
    OptimizingTask optimizingTask = queue.pollTask("", 1);
    Assert.assertNull(optimizingTask);
  }

  @NotNull
  private OptimizingQueue buildOptimizingQueue(TableRuntimeMeta tableRuntimeMeta) {
    OptimizingQueue queue =
        new OptimizingQueue(tableService(), new ResourceGroup.Builder("test", "local").build(),
            Collections.singletonList(tableRuntimeMeta));
    return queue;
  }

  private TableRuntimeMeta getTableRuntimeMeta(OptimizingStatus status) {
    ArcticTable arcticTable = tableService().loadTable(serverTableIdentifier());
    TableRuntimeMeta tableRuntimeMeta = new TableRuntimeMeta();
    tableRuntimeMeta.setCatalogName(serverTableIdentifier().getCatalog());
    tableRuntimeMeta.setDbName(serverTableIdentifier().getDatabase());
    tableRuntimeMeta.setTableName(serverTableIdentifier().getTableName());
    tableRuntimeMeta.setTableId(serverTableIdentifier().getId());
    tableRuntimeMeta.setTableStatus(status);
    tableRuntimeMeta.setTableConfig(TableConfiguration.parseConfig(arcticTable.properties()));
    tableRuntimeMeta.constructTableRuntime(tableService());
    return tableRuntimeMeta;
  }
}
