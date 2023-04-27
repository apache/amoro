package com.netease.arctic.spark.test.helper;

import com.netease.arctic.utils.CollectionHelper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.junit.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class DataComparator  {
  private List<Record> expectRecords;
  private List<Record> actualRecords;
  private Comparator<Record> comparator;

  private Function<Object, Object> fieldValueTrans;

  protected DataComparator(List<Record> expectRecords, List<Record> actualRecords) {
    this.expectRecords = expectRecords;
    this.actualRecords = actualRecords;

    this.fieldValueTrans = x -> {
      if (x instanceof LocalDateTime){
        long mills = ((LocalDateTime)x).toInstant(ZoneOffset.UTC).toEpochMilli();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneOffset.UTC);
      }
      return x;
    };
  }



  public DataComparator ignoreOrder(Comparator<Record> comparator) {
    this.comparator = comparator;
    return this;
  }

  public void assertRecordsEqual() {
    Assert.assertEquals("records size is not expected.", expectRecords.size(), actualRecords.size());
    if (comparator != null) {
      expectRecords.sort(comparator);
      actualRecords.sort(comparator);
    }
    CollectionHelper.zip(expectRecords, actualRecords)
        .forEach( r -> assertRecord(r.getLeft(), r.getRight()));
  }

  private void assertRecord(Record expectRecord , Record actualRecord ) {
    Assert.assertEquals("The record has different schema",
        expectRecord.struct().fields().size(), actualRecord.struct().fields().size());
    Types.StructType structType = expectRecord.struct();
    for (int i = 0 ; i < structType.fields().size(); i++ ){


      Object expectValue = expectRecord.get(i);
      Object actualValue = actualRecord.get(i);

      Object transExpectValue = fieldValueTrans.apply(expectValue);
      Object transActualValue = fieldValueTrans.apply(actualValue);

      Assert.assertEquals("field values are different", transExpectValue, transActualValue);
    }
  }



  public static  DataComparator build(List<Record> expectRecords, List<Record> actualRecords){
    return new DataComparator(expectRecords, actualRecords);
  }
}
