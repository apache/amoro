package com.netease.arctic.spark;

import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.base.Objects;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

import static org.apache.iceberg.types.Types.NestedField.optional;

public class TestArcticTableRead extends SparkTestBase{

  private final String database = "db_def";
  private final String table = "testA";

  private static final Schema SCHEMA = new Schema(
      optional(1, "id", Types.IntegerType.get()),
      optional(2, "data", Types.StringType.get()),
      optional(3, "doubleVal", Types.DoubleType.get())
  );

  @Before
  public void init() throws Exception {
    List<Record> rows = Lists.newArrayList(
        new Record(1, "a", 1.0),
        new Record(2, "b", 2.0),
        new Record(3, "c", Double.NaN)
    );

    Dataset<Row> df = spark.createDataFrame(rows, Record.class);
    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " data string , \n " +
        " doubleVal double , \n" +
        " ) using arctic \n", database, table);
    df.select("id", "data", "doubleVal").write()
        .format("arctic")
        .mode("append")
        .save(table);

    Dataset<Row> results = spark.read()
        .format("arctic")
        .load(table);
    results.createOrReplaceTempView("table");
  }

  @Test
  public void testSelect() {
    List<Record> expected = ImmutableList.of(
        new Record(1, "a", 1.0), new Record(2, "b", 2.0), new Record(3, "c", Double.NaN));

    Assert.assertEquals("Should return all expected rows", expected,
        sql("select * from {0}.{1}", Encoders.bean(Record.class), database, table));
  }

  @Test
  public void testExpressionPushdown() {
    List<String> expected = ImmutableList.of("b");

    Assert.assertEquals("Should return all expected rows", expected,
        sql("SELECT data FROM {0}.{1} WHERE id = 2", Encoders.STRING(), database, table));
  }


  public static class Record implements Serializable {
    private Integer id;
    private String data;
    private Double doubleVal;

    public Record() {
    }

    Record(Integer id, String data, Double doubleVal) {
      this.id = id;
      this.data = data;
      this.doubleVal = doubleVal;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public void setData(String data) {
      this.data = data;
    }

    public void setDoubleVal(Double doubleVal) {
      this.doubleVal = doubleVal;
    }

    public Integer getId() {
      return id;
    }

    public String getData() {
      return data;
    }

    public Double getDoubleVal() {
      return doubleVal;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Record record = (Record) o;
      return Objects.equal(id, record.id) && Objects.equal(data, record.data) &&
          Objects.equal(doubleVal, record.doubleVal);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id, data, doubleVal);
    }
  }

}
