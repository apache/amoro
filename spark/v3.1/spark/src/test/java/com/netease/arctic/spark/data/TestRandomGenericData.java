package com.netease.arctic.spark.data;

import org.junit.Assert;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.junit.Test;

import java.util.List;

import static org.apache.iceberg.types.Types.NestedField.optional;
import static org.apache.iceberg.types.Types.NestedField.required;

public class TestRandomGenericData {
  private static final Types.StructType SCHEMA = Types.StructType.of(
      required(100, "id", Types.LongType.get()),
      optional(101, "data", Types.StringType.get()),
      required(102, "b", Types.BooleanType.get()),
      optional(103, "i", Types.IntegerType.get()),
      required(104, "l", Types.LongType.get()),
      optional(105, "f", Types.FloatType.get()),
      required(106, "d", Types.DoubleType.get()),
      optional(107, "date", Types.DateType.get()),
      required(108, "ts_tz", Types.TimestampType.withZone()),
      required(110, "s", Types.StringType.get()),
      required(112, "fixed", Types.FixedType.ofLength(7)),
      optional(113, "bytes", Types.BinaryType.get()),
      required(114, "dec_9_0", Types.DecimalType.of(9, 0)),
      required(115, "dec_11_2", Types.DecimalType.of(11, 2)),
      required(116, "dec_38_10", Types.DecimalType.of(38, 10))
  );

  @Test
  public void generateRandomData() {
    List<Record> generate = RandomGenericData.generate(new Schema(SCHEMA.fields()), 100, 200);
    generate.forEach(System.out::println);
    Assert.assertEquals(generate.size(), 100);
  }
}
