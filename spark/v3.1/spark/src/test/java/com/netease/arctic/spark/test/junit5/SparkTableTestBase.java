package com.netease.arctic.spark.test.junit5;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;



public class SparkTableTestBase extends SparkTestBase {

  private String database = "spark_test_database";
  private String table = "spark_test_table";


  public String database() {
    return this.database;
  }

  public String table() {
    return this.table;
  }

  public String databaseAndTable() {
    return database + "." + table;
  }

  public ArcticTable loadTable() {
    return catalog().loadTable(TableIdentifier.of(catalog().name(), database(), table()));
  }

  public Table loadHiveTable() {
    return context.loadHiveTable(database(), table());
  }

  public String provider(TableFormat format) {
    switch (format) {
      case MIXED_HIVE:
      case MIXED_ICEBERG:
        return "arctic";
      case ICEBERG:
        return "iceberg";
      default:
        throw new IllegalArgumentException("un-supported type of format");
    }
  }

  @BeforeEach
  public void before() {
    try {
      LOG.debug("prepare database for table test: " + database);
      catalog().createDatabase(database);
    } catch (AlreadyExistsException e){
      // pass
    }
  }

  @AfterEach
  public void after() {
    LOG.debug("clean up table after test: " + catalog().name() + "." + database + "." + table);
    catalog().dropTable(TableIdentifier.of(catalog().name(), database, table), true);
  }




}
