/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.tests

import com.netease.arctic.hive.TestHMS
import com.netease.arctic.spark.hive.HiveCatalogMetaTestUtil
import com.netease.arctic.spark.tests.SparkTestSupport.{HMS, warehouse}
import com.netease.arctic.spark.{ArcticSparkExtensions, SparkSQLProperties}
import com.netease.arctic.{CatalogMetaTestUtil, TestAms}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.junit.rules.TemporaryFolder
import org.junit.{AfterClass, Assert, BeforeClass, ClassRule}

import scala.language.postfixOps


object SparkTestSupport {
  private lazy val _temp = new TemporaryFolder()
  private lazy val _AMS = new TestAms()
  private lazy val _HMS = new TestHMS()

  /**
   * base dir for all tests.
   */
  @ClassRule def warehouse: TemporaryFolder = _temp

  /**
   * Mocked AMS instance for tests
   */
  @ClassRule def AMS: TestAms = _AMS

  /**
   * embedded HMS instance for tests
   */
  @ClassRule def HMS: TestHMS = _HMS

  def catalogName(catalogType: String): String = catalogType toUpperCase match {
    case "HIVE" => "hive_catalog"
    case "ARCTIC" => "arctic_catalog"
  }

  @BeforeClass def setupCatalogs(): Unit = {
    println("setup catalogs")
    val arcticCatalogMeta = CatalogMetaTestUtil.createArcticCatalog(_temp.getRoot)
    arcticCatalogMeta.setCatalogName(catalogName("ARCTIC"))
    AMS.getAmsHandler.createCatalog(arcticCatalogMeta)

    val hiveConf = HMS.getHiveConf
    val hiveCatalogMeta = HiveCatalogMetaTestUtil.createArcticCatalog(_temp.getRoot, hiveConf)
    hiveCatalogMeta.setCatalogName(catalogName("HIVE"))
    AMS.getAmsHandler.createCatalog(hiveCatalogMeta)
  }

  @AfterClass def cleanCatalogs(): Unit = _AMS.getAmsHandler.cleanUp()

}

abstract class SparkTestSupport {


  lazy val spark: SparkSession = lazySparkSession

  def sparkConf: Map[String, String]

  protected def lazySparkSession: SparkSession = {
    val conf = new SparkConf()
      .setAppName("spark-unit-test")
      .setMaster("local[*]")

    val hiveVersion = classOf[SparkTestSupport].getClassLoader
      .loadClass("org.apache.hadoop.hive.metastore.HiveMetaStoreClient")
      .getPackage
      .getImplementationVersion


    var sparkConfMap = sparkConf
    sparkConfMap = sparkConfMap + (
      "hive.metastore.uris" -> s"thrift://127.0.0.1:${HMS.getMetastorePort}",
      "spark.sql.catalogImplementation" -> "hive",
      "spark.sql.hive.metastore.version" -> hiveVersion,
      "spark.sql.hive.metastore.jars" -> "maven",
      "hive.metastore.client.capability.check" -> "false",
      "spark.executor.heartbeatInterval" -> "500s",
      "spark.cores.max" -> "6",
      "spark.executor.cores" -> "2",
      "spark.default.parallelism" -> "12",
      "spark.network.timeout" -> "600s",
      "spark.sql.warehouse.dir" -> warehouse.getRoot.toString,
      "spark.sql.extensions" -> classOf[ArcticSparkExtensions].getName,
      "spark.testing.memory" -> "943718400"
    )

    sparkConfMap.foreach(kv => conf.set(kv._1, kv._2))

    val _spark = SparkSession.builder
      .config(conf)
      .getOrCreate()
    _spark.sparkContext.setLogLevel("WARN")
    _spark
  }


  protected def sql(sqlTest: String): Dataset[Row] = {
    println(f"Execute SQL: $sqlTest")
    val ds = spark.sql(sqlTest)
    if (ds.count() > 1) {
      ds.show()
    } else {
      println("+----------------+")
      println("|  Empty Result  |")
      println("+----------------+")
    }
    ds
  }

  protected def assertDataset(expect: Array[Seq[AnyRef]], dataset: Dataset[Row], message: String = ""): Unit = {
    val errorMessage = if (message.nonEmpty) s"$message:" else ""
    val rows = dataset.collect()

    Assert.assertEquals(
      s"$errorMessage number of result should match",
      expect.length, rows.length)

    var i = 0
    while (i < rows.length) {
      assertRow(expect.apply(i), rows.apply(i), message = message, index = i)
      i = i + 1
    }
  }

  protected def assertRow(expect: Seq[AnyRef], actual: Row, message: String = "", index: Int = 0): Unit = {
    val errorMessage = if (message.nonEmpty) s"$message:" else ""
    Assert.assertEquals(
      s"$errorMessage row size miss match at line: $index",
      expect.length, actual.length
    )
    var i = 0
    while (i < expect.length) {
      val expectValue = expect.apply(i)
      val actualValue = actual.apply(i)
      if (expectValue != actualValue) {
        val message = s"$errorMessage row miss match at line: $index, pos: $i " +
          s"line expected: <$expect> but was: <$actual>"
        throw new AssertionError(message)
      }
      i = i + 1
    }
  }

  def useTimestampWithoutZoneInNewTable: Boolean = sparkConf.getOrElse(
    SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES,
    SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT).toBoolean

}
