package com.netease.arctic.spark.reader;

import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.reader.BaseIcebergDataReader;
import com.netease.arctic.spark.SparkInternalRowWrapper;
import com.netease.arctic.spark.util.ArcticSparkUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.parquet.schema.MessageType;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;

import java.util.Map;
import java.util.function.Function;

public class ArcticSparkUnkeyedDataReader extends BaseIcebergDataReader<InternalRow> {
  public ArcticSparkUnkeyedDataReader(
      ArcticFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      String nameMapping,
      boolean caseSensitive) {
    super(fileIO, tableSchema, projectedSchema, nameMapping, caseSensitive,
        ArcticSparkUtil::convertConstant, false);
  }

  @Override
  protected Function<MessageType, ParquetValueReader<?>> getNewReaderFunction(
      Schema projectedSchema,
      Map<Integer, ?> idToConstant) {
    return fileSchema -> SparkParquetV2Readers.buildReader(projectedSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<Schema, Function<InternalRow, StructLike>> toStructLikeFunction() {
    return schema -> {
      final StructType structType = SparkSchemaUtil.convert(schema);
      return row -> new SparkInternalRowWrapper(structType).wrap(row);
    };
  }
}
