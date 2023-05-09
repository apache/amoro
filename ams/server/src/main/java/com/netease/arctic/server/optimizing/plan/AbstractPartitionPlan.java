package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;

import java.util.List;

public abstract class AbstractPartitionPlan {
  public static final int INVALID_SEQUENCE = -1;

  protected final String partition;
  protected final OptimizingConfig config;
  protected final TableRuntime tableRuntime;
  protected final long fragementSize;

  protected ArcticTable tableObject;
  private long fromSequence = INVALID_SEQUENCE;
  private long toSequence = INVALID_SEQUENCE;

  public AbstractPartitionPlan(TableRuntime tableRuntime, ArcticTable table, String partition) {
    this.tableObject = table;
    this.partition = partition;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.fragementSize = config.getTargetSize() / config.getFragmentRatio();
  }

  public String getPartition() {
    return partition;
  }

  public abstract void addFile(DataFile dataFile, List<DeleteFile> deletes);

  public abstract void addFile(DataFile dataFile, List<DeleteFile> deletes, List<IcebergDataFile> changeDeletes);

  public abstract boolean isNecessary();

  public abstract long getCost();

  public abstract OptimizingType getOptimizingType();

  public List<TaskDescriptor> splitTasks(int targetTaskCount) {
    throw new UnsupportedOperationException();
  }

  protected void markSequence(long sequence) {
    if (fromSequence == INVALID_SEQUENCE || fromSequence > sequence) {
      fromSequence = sequence;
    }
    if (toSequence == INVALID_SEQUENCE || toSequence < sequence) {
      toSequence = sequence;
    }
  }
  
  public long getFromSequence() {
    return fromSequence;
  }
  
  public long getToSequence() {
    return toSequence;
  }
}
