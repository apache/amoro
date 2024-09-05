/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.io.reader;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MetadataColumns;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.PartitionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class DataReaderCommon {

  public static Map<Integer, ?> getIdToConstant(
      FileScanTask task, Schema projectedSchema, BiFunction<Type, Object, Object> convertConstant) {
    Schema partitionSchema = TypeUtil.select(projectedSchema, task.spec().identitySourceIds());
    Map<Integer, Object> idToConstant = new HashMap<>();
    if (!partitionSchema.columns().isEmpty()) {
      idToConstant.putAll(PartitionUtil.constantsMap(task, convertConstant));
    }
    idToConstant.put(
        org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId(),
        convertConstant.apply(Types.StringType.get(), task.file().path().toString()));

    if (task instanceof MixedFileScanTask) {
      MixedFileScanTask mixedFileScanTask = (MixedFileScanTask) task;
      idToConstant.put(
          MetadataColumns.TRANSACTION_ID_FILED_ID,
          convertConstant.apply(Types.LongType.get(), mixedFileScanTask.file().transactionId()));

      idToConstant.put(
          MetadataColumns.TREE_NODE_ID,
          convertConstant.apply(Types.LongType.get(), mixedFileScanTask.file().node().getId()));

      if (mixedFileScanTask.fileType() == DataFileType.BASE_FILE) {
        idToConstant.put(
            MetadataColumns.FILE_OFFSET_FILED_ID,
            convertConstant.apply(Types.LongType.get(), Long.MAX_VALUE));
      }
      if (mixedFileScanTask.fileType() == DataFileType.EQ_DELETE_FILE) {
        idToConstant.put(
            MetadataColumns.CHANGE_ACTION_ID,
            convertConstant.apply(Types.StringType.get(), ChangeAction.DELETE.toString()));
      } else if (mixedFileScanTask.fileType() == DataFileType.INSERT_FILE) {
        idToConstant.put(
            MetadataColumns.CHANGE_ACTION_ID,
            convertConstant.apply(Types.StringType.get(), ChangeAction.INSERT.toString()));
      }
    }
    return idToConstant;
  }

  protected static Map<Integer, ?> getIdToConstant(
      DataFile dataFile,
      Schema projectedSchema,
      PartitionSpec spec,
      BiFunction<Type, Object, Object> convertConstant) {

    Map<Integer, Object> idToConstant =
        new HashMap<>(partitionMap(dataFile, spec, convertConstant));

    idToConstant.put(
        org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId(),
        convertConstant.apply(Types.StringType.get(), dataFile.path().toString()));

    idToConstant.put(
        MetadataColumns.TRANSACTION_ID_FILED_ID,
        convertConstant.apply(Types.LongType.get(), dataFile.dataSequenceNumber()));

    return idToConstant;
  }

  public static Map<Integer, ?> partitionMap(
      DataFile dataFile, PartitionSpec spec, BiFunction<Type, Object, Object> convertConstant) {
    StructLike partitionData = dataFile.partition();

    // use java.util.HashMap because partition data may contain null values
    Map<Integer, Object> idToConstant = Maps.newHashMap();

    List<Types.NestedField> partitionFields = spec.partitionType().fields();
    List<PartitionField> fields = spec.fields();
    for (int pos = 0; pos < fields.size(); pos += 1) {
      PartitionField field = fields.get(pos);
      if (field.transform().isIdentity()) {
        Object converted =
            convertConstant.apply(
                partitionFields.get(pos).type(), partitionData.get(pos, Object.class));
        idToConstant.put(field.sourceId(), converted);
      }
    }

    return idToConstant;
  }
}
