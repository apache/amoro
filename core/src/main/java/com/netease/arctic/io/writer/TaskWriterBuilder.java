package com.netease.arctic.io.writer;

import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.WriteOperationKind;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.TaskWriter;

/**
 * A factory to gen {@link TaskWriter}，
 * User don't need to care the detail of data write to which file location with which Parquet style
 */
public interface TaskWriterBuilder<T> {

  TaskWriter<T> buildWriter(WriteOperationKind writeOperationKind);

  TaskWriter<T> buildWriter(LocationKind locationKind);

  SortedPosDeleteWriter<T> buildBasePosDeleteWriter(long mask, long index, StructLike partitionKey);

}
