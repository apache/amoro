package com.netease.arctic.spark.hive;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.netease.arctic.spark.SparkTestContext;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHive extends SparkTestContext {


  @BeforeClass
  public static void setup() throws IOException {
    setUpTestDirAndArctic();
    setUpHMS();
    setUpSparkSession();
  }


  @AfterClass
  public static void tearDown() {
    cleanUpAdditionSparkConfigs();
    cleanUpSparkSession();
    cleanUpHive();
    cleanUpAms();
  }


  @Test
  public void testHive() throws TException {
    sql("create database if not exists test");
    sql("create table test.xxx ( id int , data string) using parquet partitioned by ( dt string, ht string)");

    sql("insert overwrite table test.xxx partition ( dt = '1' , ht ) " +
        "values " +
        "(1, 'aaa', '1' ), " +
        "(2, 'bbb', '2' ) ");

    sql("insert overwrite table test.xxx partition ( dt = '2' , ht ) " +
        "values " +
        "(3, 'aaa', '1' ), " +
        "(4, 'bbb', '2' ) ");
    printPartitions();
  }


  private void printPartitions() throws TException {
    List<Partition> partitions = hms.getClient().listPartitions("test", "xxx", (short) -1);
    Table tbl = hms.getClient().getTable("test", "xxx");

    System.out.println("partition size: " + partitions.size());
    for (Partition p: partitions){
      System.out.println("============= partition ======================");
      Map<String, String> parameters = p.getParameters();
      String location = p.getSd().getLocation();
      List<String> values = p.getValues();
      System.out.println(p.getSd());
      System.out.println("partition: " + location);
      System.out.println("db:" + p.getDbName());
      System.out.println("table:" + p.getTableName());
      System.out.println("privileges: " + p.getPrivileges());
      System.out.println("last_access_time: " + p.getLastAccessTime());
      System.out.println("create_time: " + p.getCreateTime());

      System.out.println("parameters:");
      for (String key: parameters.keySet()){
        String val = parameters.get(key);
        System.out.println("  " + key + " => " + val);
      }
      System.out.println("values: [" + Joiner.on(",").join(values) + "]");
    }

    System.out.println("table sd:");
    System.out.println(tbl.getSd());
  }

}
