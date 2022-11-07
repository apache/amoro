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
   * set transactionId for current operation
   * @param transactionId table transactionId
   * @return this for method chaining
   */
  RewritePartitions withMaxTransactionId(long transactionId);

}
