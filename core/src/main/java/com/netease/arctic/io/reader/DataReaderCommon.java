package com.netease.arctic.io.reader;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.table.MetadataColumns;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.PartitionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DataReaderCommon {

  protected static Map<Integer, ?> getIdToConstant(FileScanTask task, Schema projectedSchema,
      BiFunction<Type, Object, Object> convertConstant) {
    Schema partitionSchema = TypeUtil.select(projectedSchema, task.spec().identitySourceIds());
    Map<Integer, Object> idToConstant = new HashMap<>();
    if (!partitionSchema.columns().isEmpty()) {
      idToConstant.putAll(PartitionUtil.constantsMap(task, convertConstant));
    }
    idToConstant.put(org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId(),
        convertConstant.apply(Types.StringType.get(), task.file().path().toString()));

    if (task instanceof ArcticFileScanTask) {
      ArcticFileScanTask arcticFileScanTask = (ArcticFileScanTask)task;
      idToConstant.put(
          MetadataColumns.TRANSACTION_ID_FILED_ID,
          convertConstant.apply(Types.LongType.get(), arcticFileScanTask.file().transactionId()));

      if (arcticFileScanTask.fileType() == DataFileType.BASE_FILE) {
        idToConstant.put(
            MetadataColumns.FILE_OFFSET_FILED_ID,
            convertConstant.apply(Types.LongType.get(), Long.MAX_VALUE));
      }
      if (arcticFileScanTask.fileType() == DataFileType.EQ_DELETE_FILE) {
        idToConstant.put(MetadataColumns.CHANGE_ACTION_ID, convertConstant.apply(
            Types.StringType.get(),
            ChangeAction.DELETE.toString()));
      } else if (arcticFileScanTask.fileType() == DataFileType.INSERT_FILE) {
        idToConstant.put(MetadataColumns.CHANGE_ACTION_ID, convertConstant.apply(
            Types.StringType.get(),
            ChangeAction.INSERT.toString()));
      }
    }
    return idToConstant;
  }
}
