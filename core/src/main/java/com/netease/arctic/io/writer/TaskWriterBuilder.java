package com.netease.arctic.io.writer;

import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.OperateKinds;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.TaskWriter;

public interface TaskWriterBuilder<T> {

  TaskWriter<T> buildWriter(OperateKinds operateKinds);

  TaskWriter<T> buildWriter(LocationKind locationKind);

  SortedPosDeleteWriter<T> buildBasePosDeleteWriter(long mask, long index, StructLike partitionKey);

}
