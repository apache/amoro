/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.hive;


import com.netease.arctic.spark.SparkTestContext;

import java.io.File;

/**
 * context from all spark test with com.netease.spark.hive dependency
 */
public class SparkHiveTestContext extends SparkTestContext {

  static final File hmsDir = new File(testBaseDir, "hive");
  static final HMSMockServer hms = new HMSMockServer(hmsDir);

  public static void setUpHMS(){
    System.out.println("======================== start com.netease.spark.hive metastore ========================= ");
    hms.start();
    additionSparkConfigs.put("com.netease.spark.hive.metastore.uris", "thrift://127.0.0.1:" + hms.getMetastorePort()) ;
    additionSparkConfigs.put("spark.sql.catalogImplementation", "hive");
    additionSparkConfigs.put("spark.sql.com.netease.spark.hive.metastore.version", "2.3.7");
    additionSparkConfigs.put( "arctic.sql.delegate-hive-table", "true");
    //com.netease.spark.hive.metastore.client.capability.check
    additionSparkConfigs.put("com.netease.spark.hive.metastore.client.capability.check", "false");
  }

  public static void cleanUpHive() {
    System.out.println("======================== stop com.netease.spark.hive metastore ========================= ");
    hms.stop();
  }
}
