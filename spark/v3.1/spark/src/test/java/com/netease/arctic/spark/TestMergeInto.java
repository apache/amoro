package com.netease.arctic.spark;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMergeInto extends SparkTestBase{
  private final String database = "db_test";

  private final String tgTableA = "tgTableA";
  private final String srcTableA = "srcTableA";

  @Before
  public void before() {
    sql("use " + catalogNameArctic);
    sql("create database if not exists {0}", database);
    sql("CREATE TABLE {0}.{1} (" +
        "id int, data string, primary key(id)) " +
        "USING arctic", database, tgTableA) ;
    sql("CREATE TABLE {0}.{1} (" +
        "id int, data string, primary key(id)) " +
        "USING arctic", database, srcTableA) ;
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''a''), (4, ''d''), (5, ''e''), (6, ''c'')", database, tgTableA) ;
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA) ;
  }

  @After
  public void cleanUp() {
    sql("drop table {0}.{1}", database, tgTableA);
    sql("drop table {0}.{1}", database, srcTableA);
  }

  @Test
  public void testMergeInto() {
    sql("MERGE INTO {0}.{1} AS t USING {0}.{2} AS s " +
        "ON t.id == s.id " +
        "WHEN MATCHED AND t.id = 1 THEN " +
        "  DELETE " +
        "WHEN MATCHED AND t.id = 6 THEN " +
        "  UPDATE SET * " +
        "WHEN MATCHED AND t.id = 4 THEN " +
        "  UPDATE SET * " +
        "WHEN NOT MATCHED AND s.id != 1 THEN " +
        "  INSERT *", database, tgTableA, srcTableA);
    rows = sql("select * from {0}.{1} order by id", database, tgTableA);
    Assert.assertEquals(4, rows.size());
    Assert.assertEquals(2, rows.get(0)[0]);
    Assert.assertEquals("g", rows.get(1)[1]);
    Assert.assertEquals("e", rows.get(2)[1]);
    Assert.assertEquals("f", rows.get(3)[1]);
  }


}
