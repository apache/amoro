package com.netease.arctic.op;


import org.apache.iceberg.DataFile;
import org.apache.iceberg.PendingUpdate;
import org.apache.iceberg.util.StructLikeMap;

public interface RewritePartitions extends PendingUpdate<StructLikeMap<Long>> {

  /**
   * Add a {@link DataFile} to the table.
   *
   * @param file a data file
   * @return this for method chaining
   */
  RewritePartitions addDataFile(DataFile file);

  /**
   * Rewrite the change files whose sequence <= changeSequence in the partition.
   * Only the files in the changed partitions are affected.
   *
   * @param changeSequence table changeSequence
   * @return this for method chaining
   */
  RewritePartitions rewriteChangeBefore(long changeSequence);

}
