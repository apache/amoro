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

package org.apache.amoro.optimizing;

import org.apache.amoro.io.reader.GenericCombinedIcebergDataReader;
import org.apache.amoro.io.writer.GenericIcebergPartitionedFanoutWriter;
import org.apache.amoro.io.writer.IcebergFanoutPosDeleteWriter;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.UnpartitionedWriter;

import java.util.UUID;

/** OptimizingExecutor for iceberg format. */
public class IcebergRewriteExecutor extends AbstractRewriteFilesExecutor {

  public IcebergRewriteExecutor(
      RewriteFilesInput input, MixedTable table, StructLikeCollections structLikeCollections) {
    super(input, table, structLikeCollections);
  }

  @Override
  protected OptimizingDataReader dataReader() {
    return new GenericCombinedIcebergDataReader(
        io,
        table.schema(),
        table.spec(),
        table.asUnkeyedTable().encryption(),
        table.properties().get(TableProperties.DEFAULT_NAME_MAPPING),
        false,
        IdentityPartitionConverters::convertConstant,
        false,
        structLikeCollections,
        input);
  }

  @Override
  protected FileWriter<PositionDelete<Record>, DeleteWriteResult> posWriter() {
    return new IcebergFanoutPosDeleteWriter<>(
        fullMetricAppenderFactory(fileSpec()),
        deleteFileFormat(),
        partition(),
        table.io(),
        table.asUnkeyedTable().encryption(),
        UUID.randomUUID().toString());
  }

  @Override
  protected TaskWriter<Record> dataWriter() {
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table.asUnkeyedTable(), table.spec().specId(), 0).build();

    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec());
    appenderFactory.setAll(table.properties());

    if (table.spec().isUnpartitioned()) {
      return new UnpartitionedWriter<>(
          table.spec(), dataFileFormat(), appenderFactory, outputFileFactory, io, targetSize());
    } else {
      return new GenericIcebergPartitionedFanoutWriter(
          table.schema(),
          table.spec(),
          dataFileFormat(),
          appenderFactory,
          outputFileFactory,
          io,
          targetSize());
    }
  }

  private PartitionSpec fileSpec() {
    return table.asUnkeyedTable().specs().get(input.allFiles()[0].specId());
  }
}
