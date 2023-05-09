package com.netease.arctic.spark.test.suites.ut.sql.parser;

import com.netease.arctic.spark.sql.catalyst.parser.ArcticSqlExtensionsParser;
import org.apache.spark.sql.catalyst.parser.ParseException;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestSqlExtendParser {

  private ArcticSqlExtensionsParser parser;

  @BeforeEach
  public void setupParser() {
    parser = new ArcticSqlExtensionsParser(new SparkSqlParser());
  }


  public static final String[] operationNotAllowedSqlLists = {
      ""
  };

  @ParameterizedTest
  @ValueSource(strings = {
      "CREATE TABLE t1 (id int, PRIMARY KEY(id)) USING arctic ROW FORMAT SERDE 'parquet'",
      "CREATE TEMPORARY TABLE t1 PRIMARY KEY(id) USING arctic AS SELECT * from v1",
      "CREATE TABLE t1 (id int , PRIMARY KEY(id)) USING arctic AS SELECT * FROM v1",
      "CREATE TABLE t1 PRIMARY KEY(id) USING arctic PARTITIONED BY (pt string) AS SELECT * FROM v1",
      "CREATE TABLE t1 PRIMARY KEY(id) USING arctic",
      "CREATE TABLE t1 (id int, PRIMARY KEY(id)) USING arctic SKEWED BY (id) ",
      "CREATE TABLE t1 (id int, pt string, PRIMARY KEY(id)) USING arctic " +
          "CLUSTERED BY(id,pt) SORTED BY (pt DESC) INTO 8 BUCKETS",
      "CREATE TABLE t1 (id int, pt string, PRIMARY KEY(id)) USING arctic " +
          "TBLPROPERTIES('a')",
      "CREATE TEMPORARY TABLE IF NOT EXISTS t1 (id int , PRIMARY KEY(id)) USING arctic",
      "CREATE TABLE t1 (id int, PRIMARY KEY(id)) USING arctic STORED BY 'a.b.c' ",
      "CREATE TABLE t1 (id int, pt string, PRIMARY KEY(id)) USING arctic " +
          "PARTITIONED BY (days(pt), dt string)",
  })
  public void testOperationNotAllowed(String sqlText){
    ParseException e = Assertions.assertThrows(ParseException.class, () -> parser.parsePlan(sqlText));
    Assertions.assertTrue(e.getMessage().contains("Operation not allowed:"),
        "Not an 'Operation not allowed Exception'");
  }
}
