package com.netease.arctic.spark.hive;

import com.netease.arctic.spark.SparkTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestKeyedTableDml extends SparkTestBase {
  private final String database = "db_hive";
  private final String notUpsertTable = "testNotUpsert";
  private final String upsertTable = "testUpsert";
  private final String insertTable = "testInsert";

  protected String createNotUpsertTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string, primary key(id)) \n" +
      " using arctic" ;

  protected String createUpsertTableTemplate = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string, primary key(id)) \n" +
      " using arctic" +
      " tblproperties ( \n" +
      " ''write.upsert.enabled'' = ''true'' ";

  protected String createTableInsert = "create table {0}.{1}( \n" +
      " id int, \n" +
      " name string, \n" +
      " data string, primary key(id)) \n" +
      " using arctic ";

  @Before
  public void prepare() {
    sql("use " + catalogNameHive);
    sql("create database if not exists " + database);
    sql(createNotUpsertTableTemplate, database, notUpsertTable);
    sql("insert into " + database + "." + notUpsertTable +
        " values (1, 'aaa', 'abcd' ) , " +
        "(2, 'bbb', 'bbcd'), " +
        "(3, 'ccc', 'cbcd') ");
  }

  @After
  public void cleanUpTable() {
    sql("drop table if exists " + database + "." + notUpsertTable);
    sql("drop table if exists " + database + "." + upsertTable);
    sql("drop table if exists " + database + "." + insertTable);
    sql("drop database " + database);
  }

  @Test
  public void testInsertNotUpsert() {
    sql(createTableInsert, database, insertTable);
    sql("insert into " + database + "." + notUpsertTable +
        " values (1, 'aaa', 'abcd' ) , " +
        "(2, 'bbb', 'bbcd'), " +
        "(3, 'ccc', 'cbcd') ");
    rows = sql("select * from {0}.{1} ", database, notUpsertTable);
    Assert.assertEquals(6, rows.size());
    sql("insert into " + database + "." + insertTable + " select * from {0}.{1} ", database, notUpsertTable);

    rows = sql("select * from {0}.{1} ", database, notUpsertTable);
    Assert.assertEquals(6, rows.size());
  }


  @Test
  public void testInsertValuesUpsertTable() {
    sql("use " + catalogNameHive);
    sql(createUpsertTableTemplate, database, upsertTable);
    sql("insert overwrite " + database + "." + upsertTable +
        " values (1, 'aaa', 'aaaa' ) , " +
        "(4, 'bbb', 'bbcd'), " +
        "(5, 'ccc', 'cbcd') ");
    sql("insert into " + database + "." + upsertTable +
        " values (1, 'aaa', 'dddd' ) , " +
        "(2, 'bbb', 'bbbb'), " +
        "(3, 'ccc', 'cccc') ");

    rows = sql("select * from {0}.{1} ", database, upsertTable);
    Assert.assertEquals(5, rows.size());
  }

  @Test
  public void testInsertSelectUpsertTable() {
    sql("use " + catalogNameHive);
    sql(createUpsertTableTemplate, database, upsertTable);
    sql("insert overwrite " + database + "." + upsertTable +
        " values (1, 'aaa', 'aaaa' ) , " +
        "(4, 'bbb', 'bbcd'), " +
        "(5, 'ccc', 'cbcd') ");

    sql("insert into " + database + "." + upsertTable + " select * from {0}.{1} ", database, notUpsertTable);

    rows = sql("select * from {0}.{1} ", database, upsertTable);
    Assert.assertEquals(5, rows.size());
  }


  @Test
  public void testUpdate() {
    sql("insert into " + database + "." + notUpsertTable +
        " values (1, 'aaa', 'dddd' ) , " +
        "(2, 'bbb', 'bbbb'), " +
        "(3, 'ccc', 'cccc') ");
    sql("update {0}.{1} set name = \"ddd\" where id = 3", database, notUpsertTable);
    rows = sql("select id, name from {0}.{1} where id = 3", database, notUpsertTable);

    Assert.assertEquals(1, rows.size());
    Assert.assertEquals("ddd", rows.get(0)[1]);
  }

  @Test
  public void testDelete() {
    sql("delete from {0}.{1} where id = 3", database, notUpsertTable);
    rows = sql("select id, name from {0}.{1} order by id", database, notUpsertTable);

    Assert.assertEquals(2, rows.size());
    Assert.assertEquals(1, rows.get(0)[0]);
    Assert.assertEquals(2, rows.get(1)[0]);
  }
}
