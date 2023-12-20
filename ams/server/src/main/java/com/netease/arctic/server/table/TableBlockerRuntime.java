package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.server.exception.BlockerConflictException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableBlockerMapper;
import com.netease.arctic.table.blocker.RenewableBlocker;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TableBlockerRuntime extends PersistentBase {

  private final Lock tableLock = new ReentrantLock();
  private final ServerTableIdentifier tableIdentifier;

  public TableBlockerRuntime(ServerTableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  /**
   * Get all valid blockers.
   *
   * @return all valid blockers
   */
  public List<TableBlocker> getBlockers() {
    tableLock.lock();
    try {
      return getAs(
          TableBlockerMapper.class,
          mapper -> mapper.selectBlockers(tableIdentifier, System.currentTimeMillis()));
    } finally {
      tableLock.unlock();
    }
  }

  /**
   * Block some operations for table.
   *
   * @param operations - operations to be blocked
   * @param properties -
   * @param blockerTimeout -
   * @return TableBlocker if success
   */
  public TableBlocker block(
      List<BlockableOperation> operations,
      @Nonnull Map<String, String> properties,
      long blockerTimeout) {
    Preconditions.checkNotNull(operations, "operations should not be null");
    Preconditions.checkArgument(!operations.isEmpty(), "operations should not be empty");
    Preconditions.checkArgument(blockerTimeout > 0, "blocker timeout must > 0");
    tableLock.lock();
    try {
      long now = System.currentTimeMillis();
      List<TableBlocker> tableBlockers =
          getAs(TableBlockerMapper.class, mapper -> mapper.selectBlockers(tableIdentifier, now));
      if (conflict(operations, tableBlockers)) {
        throw new BlockerConflictException(operations + " is conflict with " + tableBlockers);
      }
      TableBlocker tableBlocker =
          buildTableBlocker(tableIdentifier, operations, properties, now, blockerTimeout);
      doAs(TableBlockerMapper.class, mapper -> mapper.insertBlocker(tableBlocker));
      return tableBlocker;
    } finally {
      tableLock.unlock();
    }
  }

  /**
   * Renew blocker.
   *
   * @param blockerId - blockerId
   * @param blockerTimeout - timeout
   * @throws IllegalStateException if blocker not exist
   */
  public long renew(String blockerId, long blockerTimeout) {
    tableLock.lock();
    try {
      long now = System.currentTimeMillis();
      TableBlocker tableBlocker =
          getAs(
              TableBlockerMapper.class,
              mapper -> mapper.selectBlocker(Long.parseLong(blockerId), now));
      if (tableBlocker == null) {
        throw new ObjectNotExistsException("Blocker " + blockerId + " of " + tableIdentifier);
      }
      long expirationTime = now + blockerTimeout;
      doAs(
          TableBlockerMapper.class,
          mapper -> mapper.updateBlockerExpirationTime(Long.parseLong(blockerId), expirationTime));
      return expirationTime;
    } finally {
      tableLock.unlock();
    }
  }

  /**
   * Release blocker, succeed when blocker not exist.
   *
   * @param blockerId - blockerId
   */
  public void release(String blockerId) {
    tableLock.lock();
    try {
      doAs(TableBlockerMapper.class, mapper -> mapper.deleteBlocker(Long.parseLong(blockerId)));
    } finally {
      tableLock.unlock();
    }
  }

  /**
   * Check if operation are blocked now.
   *
   * @param operation - operation to check
   * @return true if blocked
   */
  public boolean isBlocked(BlockableOperation operation) {
    tableLock.lock();
    try {
      List<TableBlocker> tableBlockers =
          getAs(
              TableBlockerMapper.class,
              mapper -> mapper.selectBlockers(tableIdentifier, System.currentTimeMillis()));
      return conflict(operation, tableBlockers);
    } finally {
      tableLock.unlock();
    }
  }

  private boolean conflict(
      List<BlockableOperation> blockableOperations, List<TableBlocker> blockers) {
    return blockableOperations.stream().anyMatch(operation -> conflict(operation, blockers));
  }

  private boolean conflict(BlockableOperation blockableOperation, List<TableBlocker> blockers) {
    return blockers.stream()
        .anyMatch(blocker -> blocker.getOperations().contains(blockableOperation.name()));
  }

  private TableBlocker buildTableBlocker(
      ServerTableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties,
      long now,
      long blockerTimeout) {
    TableBlocker tableBlocker = new TableBlocker();
    tableBlocker.setTableIdentifier(tableIdentifier);
    tableBlocker.setCreateTime(now);
    tableBlocker.setExpirationTime(now + blockerTimeout);
    tableBlocker.setOperations(
        operations.stream().map(BlockableOperation::name).collect(Collectors.toList()));
    HashMap<String, String> propertiesOfTableBlocker = new HashMap<>(properties);
    propertiesOfTableBlocker.put(RenewableBlocker.BLOCKER_TIMEOUT, blockerTimeout + "");
    tableBlocker.setProperties(propertiesOfTableBlocker);
    return tableBlocker;
  }
}
