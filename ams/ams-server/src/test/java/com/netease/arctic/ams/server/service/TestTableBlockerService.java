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
  public void testBlockAndRelease() throws OperationConflictException {
    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations);
    assertBlocker(block, operations);
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);

    ServiceContainer.getTableBlockerService().release(tableIdentifier, block.blockerId());
    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);
  }

  @Test
  public void testBlockConflict() throws OperationConflictException {
    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations);

    Assert.assertThrows("should be conflict", OperationConflictException.class,
        () -> ServiceContainer.getTableBlockerService().block(tableIdentifier, operations));

    assertBlocker(block, operations);
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);

    ServiceContainer.getTableBlockerService().release(tableIdentifier, block.blockerId());
    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);
  }

  @Test
  public void testRenew() throws OperationConflictException {
    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations);

    ServiceContainer.getTableBlockerService().renew(tableIdentifier, block.blockerId());
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);

    assertBlocker(block, operations);
    assertBlockerCnt(1);
    assertBlocked(BlockableOperation.OPTIMIZE);
    assertBlocked(BlockableOperation.BATCH_WRITE);
    assertBlockerRenewed(ServiceContainer.getTableBlockerService().getBlockers(tableIdentifier).get(0));

    ServiceContainer.getTableBlockerService().release(tableIdentifier, block.blockerId());
    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);
  }

  private void assertBlocker(BaseBlocker block, List<BlockableOperation> operations) {
    Assert.assertEquals(operations.size(), block.operations().size());
    operations.forEach(operation -> Assert.assertTrue(block.operations().contains(operation)));

    long timeout = ArcticMetaStoreConf.BLOCKER_TIMEOUT.defaultValue();
    Assert.assertEquals(timeout, block.getExpirationTime() - block.getCreateTime());
  }

  private void assertBlockerRenewed(BaseBlocker block) {
    long timeout = ArcticMetaStoreConf.BLOCKER_TIMEOUT.defaultValue();
    Assert.assertTrue(block.getExpirationTime() - block.getCreateTime() > timeout);
  }

  private void assertNotBlocked(BlockableOperation optimize) {
    Assert.assertFalse(
        ServiceContainer.getTableBlockerService().isBlocked(tableIdentifier, optimize));
  }

  private void assertBlocked(BlockableOperation optimize) {
    Assert.assertTrue(
        ServiceContainer.getTableBlockerService().isBlocked(tableIdentifier, optimize));
  }

  private void assertBlockerCnt(int i) {
    List<BaseBlocker> blockers;
    blockers = ServiceContainer.getTableBlockerService().getBlockers(tableIdentifier);
    Assert.assertEquals(i, blockers.size());
  }


}
