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

import com.netease.arctic.spark.tests.SparkTestSupport.AMS

import scala.language.postfixOps

trait SparkCatalogTestSupport extends SparkTestSupport {

  def catalogType: String

  def arcticCatalogName: String = catalogType toUpperCase match {
    case "SESSION" => SparkTestSupport.catalogName("hive")
    case _ => SparkTestSupport.catalogName(catalogType)
  }

  def catalog: String = catalogType toUpperCase match {
    case "SESSION" => "spark_catalog"
    case _ => arcticCatalogName
  }

  def catalogUrl: String = s"${AMS.getServerUrl}/${arcticCatalogName}"

  override def sparkConf: Map[String, String] = {
    var conf = catalogType toUpperCase match {
      case "SESSION" =>
        Map(s"spark.sql.catalog.${catalog}" -> "com.netease.arctic.spark.ArcticSparkSessionCatalog")
      case _ =>
        Map(s"spark.sql.catalog.${catalog}" -> "com.netease.arctic.spark.ArcticSparkCatalog")
    }
    conf = conf + (s"spark.sql.catalog.${catalog}.url" -> s"${AMS.getServerUrl}/${arcticCatalogName}")
    conf
  }


}
