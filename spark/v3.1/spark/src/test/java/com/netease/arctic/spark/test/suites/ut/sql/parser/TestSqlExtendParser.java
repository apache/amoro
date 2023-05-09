package com.netease.arctic.spark.test.suites.ut.sql.parser;

import com.netease.arctic.spark.sql.catalyst.parser.ArcticSqlExtensionsParser;
import com.netease.arctic.spark.test.helper.ScalaTestHelper;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.spark.sql.arctic.parser.ExtendAstBuilder;
import org.apache.spark.sql.catalyst.parser.AbstractSqlParser;
import org.apache.spark.sql.catalyst.parser.AstBuilder;
import org.apache.spark.sql.catalyst.parser.ParseException;
import org.apache.spark.sql.catalyst.plans.logical.CreateTableAsSelectStatement;
import org.apache.spark.sql.catalyst.plans.logical.CreateTableStatement;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import scala.collection.Seq;

import java.util.List;
import java.util.stream.Stream;

public class TestSqlExtendParser {


  private ArcticSqlExtensionsParser parser = new ArcticSqlExtensionsParser(new AbstractSqlParser(){
    @Override
    public AstBuilder astBuilder() {
      return null;
    }
  });


  private SparkSqlParser sparkSqlParser = new SparkSqlParser();





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
  public void testOperationNotAllowed(String sqlText) {
    ParseException e = Assertions.assertThrows(ParseException.class, () -> parser.parsePlan(sqlText));
    Assertions.assertTrue(e.getMessage().contains("Operation not allowed:"),
        "Not an 'Operation not allowed Exception'");
  }


  public static Arguments[] testCreateTableWithPrimaryKey() {
    return new Arguments[]{
        Arguments.arguments(
            "CREATE TABLE mydb.t1 (id int, PRIMARY KEY(id)) ",
            Lists.newArrayList("mydb", "t1"),
            new StructType().add("id", DataTypes.IntegerType, false)
        )
    };
  }


  @ParameterizedTest
  @MethodSource
  public void testCreateTableWithPrimaryKey(
      String sqlText, List<String> expectTableName, StructType expectSchema
  ) {
    LogicalPlan plan = parser.parsePlan(sqlText);
    Assertions.assertTrue(plan instanceof CreateTableStatement, "Not a CreateTableStatement");
    CreateTableStatement create = (CreateTableStatement) plan;

    Seq<String> expectNameSeq = ScalaTestHelper.seq(expectTableName);
    Assertions.assertEquals(expectNameSeq, create.tableName());
    Assertions.assertEquals(expectSchema, create.tableSchema());
  }




  public static Stream<Arguments> testCreateTableAsSelect() {
    String header = "CREATE TABLE mydb.t1 PRIMARY KEY(id) ";
    List<String> queries = Lists.newArrayList(
        "SELECT * FROM v1",
        "sELEct * FroM a",
        "select * from a union select * from b",
        "select * from a union distinct select * from b",
        "select * from a union all select * from b",
        "select * from a except select * from b",
        "select * from a except distinct select * from b",
        "select * from a except all select * from b",
        "select * from a minus select * from b",
        "select * from a minus all select * from b",
        "select * from a minus distinct select * from b",
        "select * from a intersect select * from b",
        "select * from a intersect distinct select * from b",
        "select * from a intersect all select * from b",

        "select 1",
        "select a, b",
        "select a, b from db.c",
        "select a, b from db.c where x < 1",
        "select a, b from db.c having x < 1",
        "select distinct a, b from db.c",
        "select all a, b from db.c",
        "select a from 1k.2m"


    );

    return queries.stream()
        .map(s -> Arguments.arguments(header, s));
  }


  @ParameterizedTest
  @MethodSource
  public void testCreateTableAsSelect(
      String ctasHeader, String asSelectBody
  ) {
    String sqlText = ctasHeader + " AS " + asSelectBody;
    LogicalPlan plan = parser.parsePlan(sqlText);
    Assertions.assertTrue(plan instanceof CreateTableAsSelectStatement, "Not a CreateTableAsSelectStatement");
    CreateTableAsSelectStatement create = (CreateTableAsSelectStatement) plan;
    LogicalPlan ctasQuery = create.asSelect();
    LogicalPlan expectQuery = sparkSqlParser.parsePlan(asSelectBody);

    Assertions.assertEquals(expectQuery, ctasQuery);

  }

}
