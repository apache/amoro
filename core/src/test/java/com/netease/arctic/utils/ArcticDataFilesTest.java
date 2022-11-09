package com.netease.arctic.utils;

import com.netease.arctic.iceberg.optimize.InternalRecordWrapper;
import com.netease.arctic.iceberg.optimize.StructLikeWrapper;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class ArcticDataFilesTest {

  private static final Schema SCHEMA = new Schema(
      Types.NestedField.required(1, "dt", Types.TimestampType.withoutZone())
  );

  @Test
  public void testMonthPartition() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).month("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt",
        LocalDateTime.parse("2022-11-11T11:00:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = ArcticDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testDaysPartition() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).day("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt",
        LocalDateTime.parse("2022-11-11T11:00:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = ArcticDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testHoursPartition() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).hour("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt",
        LocalDateTime.parse("2022-11-11T11:00:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = ArcticDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testBucketPartition() {
     Schema schema = new Schema(
        Types.NestedField.required(1, "dt", Types.IntegerType.get())
    );
    PartitionSpec spec = PartitionSpec.builderFor(schema).bucket("dt", 2).build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt", 1)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = ArcticDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testTruncatePartition() {
    Schema schema = new Schema(
        Types.NestedField.required(1, "dt", Types.IntegerType.get())
    );
    PartitionSpec spec = PartitionSpec.builderFor(schema).truncate("dt", 2).build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt", 1)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = ArcticDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }
}
