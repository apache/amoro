package com.netease.arctic.spark.io;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.writer.ChangeTaskWriter;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.spark.SparkInternalRowCastWrapper;
import com.netease.arctic.spark.SparkInternalRowWrapper;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArcticSparkInsertIntoWriter extends ChangeTaskWriter<InternalRow> {
  private final Schema schema;
  private final StructType dsSchema;
  protected ArcticSparkInsertIntoWriter(FileFormat format,
                                        FileAppenderFactory<InternalRow> appenderFactory,
                                        OutputFileFactory outputFileFactory, ArcticFileIO io, long targetFileSize,
                                        long mask, Schema schema, StructType dsSchema, PartitionSpec spec, PrimaryKeySpec primaryKeySpec) {
    super(format, appenderFactory, outputFileFactory, io, targetFileSize, mask, schema, spec, primaryKeySpec);
    this.schema = schema;
    this.dsSchema = dsSchema;
  }

  @Override
  protected StructLike asStructLike(InternalRow data) {
    return new SparkInternalRowWrapper(SparkSchemaUtil.convert(schema)).wrap(data);
  }

  @Override
  protected InternalRow appendMetaColumns(InternalRow data, Long fileOffset) {
    List<DataType> dataTypeList = Arrays.stream(dsSchema.fields()).map(StructField::dataType).collect(Collectors.toList());
    List<Object> rows = new ArrayList<>(dsSchema.size() + 1);
    for (int i = 0; i < dsSchema.size(); i++) {
      rows.add(data.get(i, dataTypeList.get(i)));
    }
    rows.add(fileOffset);
    return new GenericInternalRow(rows.toArray());
  }

  @Override
  protected ChangeAction action(InternalRow data) {
    return ChangeAction.INSERT;
  }

}
