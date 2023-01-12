package com.netease.arctic.ams.server.service;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.blocker.BaseBlocker;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_CATALOG_NAME;

public class TestTableBlockerService extends TableTestBase {
  TableIdentifier tableIdentifier = TableIdentifier.of(AMS_TEST_CATALOG_NAME, "test", "test");
  
  @Test
  public void testBlock() throws OperationConflictException {
    List<BaseBlocker> blockers = ServiceContainer.getTableBlockerService().getBlockers(tableIdentifier);
    Assert.assertEquals(0, blockers.size());
    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);
    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations);
    Assert.assertEquals(2, block.operations().size());
    Assert.assertTrue(block.operations().contains(BlockableOperation.BATCH_WRITE));
    Assert.assertTrue(block.operations().contains(BlockableOperation.OPTIMIZE));

    long timeout = ArcticMetaStoreConf.BLOCKER_TIMEOUT.defaultValue();
    Assert.assertEquals(timeout, block.getExpirationTime() - block.getCreateTime());

    blockers = ServiceContainer.getTableBlockerService().getBlockers(tableIdentifier);
    Assert.assertEquals(1, blockers.size());

    ServiceContainer.getTableBlockerService().renew(tableIdentifier, block.blockerId());
    blockers = ServiceContainer.getTableBlockerService().getBlockers(tableIdentifier);
    Assert.assertEquals(1, blockers.size());
    
    ServiceContainer.getTableBlockerService().release(tableIdentifier, block.blockerId());
    blockers = ServiceContainer.getTableBlockerService().getBlockers(tableIdentifier);
    Assert.assertEquals(0, blockers.size());
  }
}
