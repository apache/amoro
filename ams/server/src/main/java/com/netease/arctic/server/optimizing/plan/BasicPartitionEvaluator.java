package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class BasicPartitionEvaluator extends PartitionEvaluator {
  private final Set<String> deleteFileSet = Sets.newHashSet();
  private final OptimizingConfig config;
  private final TableRuntime tableRuntime;
  private final long fragmentSize;
  private final long planTime;

  // fragment files
  protected int fragmentFileCount = 0;
  protected long fragmentFileSize = 0;

  // segment files
  protected int segmentFileCount = 0;
  protected long segmentFileSize = 0;
  protected int rewriteSegmentFileCount = 0;
  protected long rewriteSegmentFileSize = 0L;
  protected int rewritePosSegmentFileCount = 0;
  protected long rewritePosSegmentFileSize = 0L;

  // delete files
  protected int equalityDeleteFileCount = 0;
  protected long equalityDeleteFileSize = 0L;
  protected int posDeleteFileCount = 0;
  protected long posDeleteFileSize = 0L;

  private long cost = -1;

  public BasicPartitionEvaluator(TableRuntime tableRuntime, String partition, long planTime) {
    super(partition);
    this.tableRuntime = tableRuntime;
    this.config = tableRuntime.getOptimizingConfig();
    this.fragmentSize = config.getTargetSize() / config.getFragmentRatio();
    this.planTime = planTime;
  }

  protected boolean isFragmentFile(IcebergDataFile dataFile) {
    return dataFile.fileSizeInBytes() <= fragmentSize;
  }

  @Override
  public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    if (isFragmentFile(dataFile)) {
      addFragmentFile(dataFile, deletes);
    } else {
      addSegmentFile(dataFile, deletes);
    }
  }

  private boolean isDuplicateDelete(IcebergContentFile<?> delete) {
    boolean deleteExist = deleteFileSet.contains(delete.path().toString());
    if (!deleteExist) {
      deleteFileSet.add(delete.path().toString());
    }
    return deleteExist;
  }

  private void addFragmentFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    fragmentFileSize += dataFile.fileSizeInBytes();
    fragmentFileCount += 1;

    for (IcebergContentFile<?> delete : deletes) {
      addDelete(delete);
    }
  }

  private void addSegmentFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    segmentFileSize += dataFile.fileSizeInBytes();
    segmentFileCount += 1;

    long deleteRecord = 0;
    boolean equalityDeleteExist = false;
    int posDeleteCount = 0;
    for (IcebergContentFile<?> delete : deletes) {
      addDelete(delete);
      deleteRecord += delete.recordCount();
      if (delete.content() == FileContent.POSITION_DELETES) {
        posDeleteCount++;
      } else {
        equalityDeleteExist = true;
      }
    }
    if (deleteRecord >= dataFile.recordCount() * config.getMajorDuplicateRatio()) {
      rewriteSegmentFileSize += dataFile.fileSizeInBytes();
      rewriteSegmentFileCount += 1;
    } else if (equalityDeleteExist || posDeleteCount > 1) {
      rewritePosSegmentFileSize += dataFile.fileSizeInBytes();
      rewritePosSegmentFileCount += 1;
    }
  }

  private void addDelete(IcebergContentFile<?> delete) {
    if (isDuplicateDelete(delete)) {
      return;
    }
    if (delete.content() == FileContent.DATA || delete.content() == FileContent.EQUALITY_DELETES) {
      equalityDeleteFileCount += 1;
      equalityDeleteFileSize += delete.fileSizeInBytes();
    } else {
      posDeleteFileCount += 1;
      posDeleteFileSize += delete.fileSizeInBytes();
    }
  }

  @Override
  public boolean isNecessary() {
    return isFullNecessary() || isMajorNecessary() || isMinorNecessary();
  }

  @Override
  public long getCost() {
    if (cost < 0) {
      // TODO check
      cost = rewriteSegmentFileSize * 4 + fragmentFileSize * 4 +
          rewritePosSegmentFileSize / 10 + posDeleteFileSize + equalityDeleteFileSize;
    }
    return cost;
  }

  @Override
  public OptimizingType getOptimizingType() {
    return isFullNecessary() ? OptimizingType.FULL_MAJOR :
        isMajorNecessary() ? OptimizingType.MAJOR : OptimizingType.MINOR;
  }

  public boolean isMajorNecessary() {
    return rewriteSegmentFileSize > 0;
  }

  public boolean isMinorNecessary() {
    int sourceFileCount = fragmentFileCount + equalityDeleteFileCount;
    return sourceFileCount >= config.getMinorLeastFileCount() ||
        (sourceFileCount > 1 &&
            planTime - tableRuntime.getLastMinorOptimizingTime() > config.getMinorLeastInterval());
  }

  public boolean isFullNecessary() {
    return config.getFullTriggerInterval() > 0 &&
        planTime - tableRuntime.getLastFullOptimizingTime() > config.getFullTriggerInterval();
  }

  public int getFragmentFileCount() {
    return fragmentFileCount;
  }

  public long getFragmentFileSize() {
    return fragmentFileSize;
  }

  public int getSegmentFileCount() {
    return segmentFileCount;
  }

  public long getSegmentFileSize() {
    return segmentFileSize;
  }

  public int getRewriteSegmentFileCount() {
    return rewriteSegmentFileCount;
  }

  public long getRewriteSegmentFileSize() {
    return rewriteSegmentFileSize;
  }

  public int getRewritePosSegmentFileCount() {
    return rewritePosSegmentFileCount;
  }

  public long getRewritePosSegmentFileSize() {
    return rewritePosSegmentFileSize;
  }

  public int getEqualityDeleteFileCount() {
    return equalityDeleteFileCount;
  }

  public long getEqualityDeleteFileSize() {
    return equalityDeleteFileSize;
  }

  public int getPosDeleteFileCount() {
    return posDeleteFileCount;
  }

  public long getPosDeleteFileSize() {
    return posDeleteFileSize;
  }
}
