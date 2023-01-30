package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.model.TableBlocker;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.blocker.BaseBlocker;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_CATALOG_NAME;

public class TestTableBlockerService extends TableTestBase {
  private final TableIdentifier tableIdentifier = TableIdentifier.of(AMS_TEST_CATALOG_NAME, "test", "test");
  private final Map<String, String> properties = new HashMap<>();

  {
    properties.put("test_key", "test_value");
    properties.put("2", "2");
  }

  @Test
  public void testBlockAndRelease() throws OperationConflictException {
    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    assertBlockerCnt(0);
    assertNotBlocked(BlockableOperation.OPTIMIZE);
    assertNotBlocked(BlockableOperation.BATCH_WRITE);

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations, properties);
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

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations, properties);

    Assert.assertThrows("should be conflict", OperationConflictException.class,
        () -> ServiceContainer.getTableBlockerService().block(tableIdentifier, operations, properties));

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

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations, properties);

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

  @Test
  public void testExpire() {
    TableBlocker tableBlocker = new TableBlocker();
    tableBlocker.setTableIdentifier(tableIdentifier);
    tableBlocker.setExpirationTime(System.currentTimeMillis() - 10);
    tableBlocker.setCreateTime(System.currentTimeMillis() - 20);
    tableBlocker.setOperations(Collections.singletonList(BlockableOperation.OPTIMIZE.name()));
    ServiceContainer.getTableBlockerService().insertTableBlocker(tableBlocker);

    TableBlocker tableBlocker2 = new TableBlocker();
    tableBlocker2.setTableIdentifier(tableIdentifier);
    tableBlocker2.setExpirationTime(System.currentTimeMillis() + 100000);
    tableBlocker2.setCreateTime(System.currentTimeMillis() - 20);
    tableBlocker2.setOperations(Collections.singletonList(BlockableOperation.BATCH_WRITE.name()));
    ServiceContainer.getTableBlockerService().insertTableBlocker(tableBlocker2);

    int deleted = ServiceContainer.getTableBlockerService().expireBlockers(tableIdentifier);
    Assert.assertEquals(1, deleted);

    ServiceContainer.getTableBlockerService().release(tableIdentifier, tableBlocker2.getBlockerId() + "");
    assertBlockerCnt(0);
  }

  @Test
  public void testClear() {
    TableBlocker tableBlocker = new TableBlocker();
    tableBlocker.setTableIdentifier(tableIdentifier);
    tableBlocker.setExpirationTime(System.currentTimeMillis() - 10);
    tableBlocker.setCreateTime(System.currentTimeMillis() - 20);
    tableBlocker.setOperations(Collections.singletonList(BlockableOperation.OPTIMIZE.name()));
    ServiceContainer.getTableBlockerService().insertTableBlocker(tableBlocker);

    TableBlocker tableBlocker2 = new TableBlocker();
    tableBlocker2.setTableIdentifier(tableIdentifier);
    tableBlocker2.setExpirationTime(System.currentTimeMillis() + 100000);
    tableBlocker2.setCreateTime(System.currentTimeMillis() - 20);
    tableBlocker2.setOperations(Collections.singletonList(BlockableOperation.BATCH_WRITE.name()));
    ServiceContainer.getTableBlockerService().insertTableBlocker(tableBlocker2);

    int deleted = ServiceContainer.getTableBlockerService().clearBlockers(tableIdentifier);
    Assert.assertEquals(2, deleted);
    assertBlockerCnt(0);
  }

  @Test
  public void testAutoIncrementBlockerId() throws OperationConflictException {
    List<BlockableOperation> operations = new ArrayList<>();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);

    BaseBlocker block = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations, properties);

    ServiceContainer.getTableBlockerService().release(tableIdentifier, block.blockerId());

    BaseBlocker block2 = ServiceContainer.getTableBlockerService().block(tableIdentifier, operations, properties);

    Assert.assertEquals(Long.parseLong(block2.blockerId()) - Long.parseLong(block.blockerId()), 1);

    ServiceContainer.getTableBlockerService().release(tableIdentifier, block2.blockerId());
  }

  private void assertBlocker(BaseBlocker block, List<BlockableOperation> operations) {
    Assert.assertEquals(operations.size(), block.operations().size());
    operations.forEach(operation -> Assert.assertTrue(block.operations().contains(operation)));
    Assert.assertEquals(properties.size(), block.properties().size());
    block.properties().forEach((key, value) -> Assert.assertEquals(properties.get(key), value));

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
