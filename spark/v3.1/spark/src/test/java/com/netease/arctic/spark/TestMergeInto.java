package com.netease.arctic.spark;

import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
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
  }

  @After
  public void cleanUp() {
    sql("drop table {0}.{1}", database, tgTableA);
    sql("drop table {0}.{1}", database, srcTableA);
  }

  @Test
  public void testMergeWithAllCauses() {
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''a''), (4, ''d''), (5, ''e''), (6, ''c'')", database, tgTableA) ;
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA) ;
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
    ImmutableList<Object[]> expectedRows = ImmutableList.of(
        row(2, "e"), // new
        row(4, "g"), // new
        row(5, "e"), // new
        row(6, "f")  // new
    );
    assertEquals("Should have expected rows", expectedRows,
        sql("SELECT * FROM {0}.{1} ORDER BY id", database, tgTableA));
  }

  @Test
  public void testMergeIntoEmptyTargetInsertAllNonMatchingRows() {
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA) ;
    sql("MERGE INTO {0}.{1} AS t USING {0}.{2} AS s " +
        "ON t.id == s.id " +
        "WHEN NOT MATCHED THEN " +
        "  INSERT *", database, tgTableA, srcTableA);
    ImmutableList<Object[]> expectedRows = ImmutableList.of(
        row(1, "d"), // new
        row(2, "e"), // new
        row(4, "g"), // new
        row(6, "f")  // new
    );
    assertEquals("Should have expected rows", expectedRows,
        sql("SELECT * FROM {0}.{1} ORDER BY id", database, tgTableA));
  }

  @Test
  public void testMergeIntoEmptyTargetInsertOnlyMatchingRows() {
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA) ;
    sql("MERGE INTO {0}.{1} AS t USING {0}.{2} AS s " +
        "ON t.id == s.id " +
        "WHEN NOT MATCHED AND (s.id >=2) THEN " +
        "  INSERT *", database, tgTableA, srcTableA);

    ImmutableList<Object[]> expectedRows = ImmutableList.of(
        row(2, "e"), // new
        row(4, "g"), // new
        row(6, "f")  // new
    );
    assertEquals("Should have expected rows", expectedRows,
        sql("SELECT * FROM {0}.{1} ORDER BY id", database, tgTableA));
  }

  @Test
  public void testMergeWithOnlyUpdateClause() {
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''a''), (4, ''d''), (5, ''e''), (6, ''c'')", database, tgTableA);
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA);
    sql("MERGE INTO {0}.{1} AS t USING {0}.{2} AS s " +
        "ON t.id == s.id " +
        "WHEN MATCHED AND t.id = 1 THEN " +
        "  UPDATE SET *", database, tgTableA, srcTableA);
    ImmutableList<Object[]> expectedRows = ImmutableList.of(
        row(1, "d"), // new
        row(4, "d"), // new
        row(5, "e"), // new
        row(6, "c")  // new
    );
    assertEquals("Should have expected rows", expectedRows,
        sql("SELECT * FROM {0}.{1} ORDER BY id", database, tgTableA));
  }

  @Test
  public void testMergeWithOnlyDeleteClause() {
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''a''), (4, ''d''), (5, ''e''), (6, ''c'')", database, tgTableA);
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA);
    sql("MERGE INTO {0}.{1} AS t USING {0}.{2} AS s " +
        "ON t.id == s.id " +
        "WHEN MATCHED AND t.id = 6 THEN" +
        "  DELETE", database, tgTableA, srcTableA);
    ImmutableList<Object[]> expectedRows = ImmutableList.of(
        row(1, "a"), // new
        row(4, "d"), // new
        row(5, "e")  // new
    );
    assertEquals("Should have expected rows", expectedRows,
        sql("SELECT * FROM {0}.{1} ORDER BY id", database, tgTableA));
  }

  @Test
  public void testMergeWithAllCausesWithExplicitColumnSpecification() {
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''a''), (4, ''d''), (5, ''e''), (6, ''c'')", database, tgTableA) ;
    sql("INSERT OVERWRITE TABLE {0}.{1} VALUES (1, ''d''), (4, ''g''), (2, ''e''), (6, ''f'')", database, srcTableA) ;
    sql("MERGE INTO {0}.{1} AS t USING {0}.{2} AS s " +
        "ON t.id == s.id " +
        "WHEN MATCHED AND t.id = 1 THEN " +
        "  DELETE " +
        "WHEN MATCHED AND t.id = 6 THEN " +
        "  UPDATE SET t.id = s.id, t.data = s.data  " +
        "WHEN MATCHED AND t.id = 4 THEN " +
        "  UPDATE SET t.id = s.id, t.data = s.data  " +
        "WHEN NOT MATCHED AND s.id != 1 THEN " +
        "  INSERT (t.id, t.data) VALUES (s.id, s.data)", database, tgTableA, srcTableA);
    ImmutableList<Object[]> expectedRows = ImmutableList.of(
        row(2, "e"), // new
        row(4, "g"), // new
        row(5, "e"), // new
        row(6, "f")  // new
    );
    assertEquals("Should have expected rows", expectedRows,
        sql("SELECT * FROM {0}.{1} ORDER BY id", database, tgTableA));
  }

}
