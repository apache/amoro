package com.netease.arctic.spark.hive;

import com.netease.arctic.spark.SparkTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestUnkeyedTableDml extends SparkTestBase {
  private final String database = "db_hive";
  private final String table = "test";

  protected String createTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string) \n" +
      " using arctic partitioned by (data) ;";

  @Before
  public void prepare() {
    sql("use " + catalogNameHive);
    sql("create database if not exists " + database);
    sql(createTableTemplate, database, table);
  }

  @After
  public void cleanUpTable() {
    sql("drop table " + database + "." + table);
    sql("drop database " + database);
  }

  @Test
  public void testUpdate() {
    sql("insert into " + database + "." + table +
        " values (1, 'aaa', '1' ) , " +
        "(2, 'bbb', '2'), " +
        "(3, 'ccc', '1') ");

    sql("update {0}.{1} set name = \"ddd\" where id = 3", database, table);
    rows = sql("select id, name from {0}.{1} where id = 3", database, table);

    Assert.assertEquals(1, rows.size());
    Assert.assertEquals("ddd", rows.get(0)[1]);
    sql("select * from {0}.{1} ", database, table);
    Assert.assertEquals(3, rows.size());

  }

  @Test
  public void testDelete() {
    sql("insert into " + database + "." + table +
        " values (1, 'aaa', '1' ) , " +
        "(2, 'bbb', '2' ), " +
        "(3, 'ccc', '1' ) ");

    sql("delete from {0}.{1} where id = 3", database, table);
    rows = sql("select id, name from {0}.{1} order by id", database, table);

    Assert.assertEquals(2, rows.size());
    Assert.assertEquals(1, rows.get(0)[0]);
    Assert.assertEquals(2, rows.get(1)[0]);
  }

  @Test
  public void testInsert() {
    sql("insert into " + database + "." + table +
        " values (1, 'aaa', 'abcd' ) , " +
        "(2, 'bbb', 'bbcd'), " +
        "(3, 'ccc', 'cbcd') ");

    rows = sql("select * from {0}.{1} ", database, table);

    Assert.assertEquals(3, rows.size());
  }
}
