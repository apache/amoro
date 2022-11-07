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

package com.netease.arctic.ams.server.terminal.local;

import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.model.SqlRunningInfo;
import com.netease.arctic.ams.server.terminal.TerminalSession;
import com.netease.arctic.ams.server.terminal.TerminalSessionFactory;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkExtensions;
import java.util.Date;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SQLConf;

public class LocalSessionFactory implements TerminalSessionFactory {

  SparkSession context = null;

  @Override
  public void initialize(Map<String, String> properties) {

  }

  @Override
  public TerminalSession create(Map<String, String> properties) {
    SparkSession context = lazyInitContext();
    SparkSession session = context.cloneSession();
    return new LocalTerminalSession(session);
  }



  protected synchronized SparkSession lazyInitContext(){
    if (context == null){
      SparkConf sparkconf = new SparkConf()
          .setAppName("spark-local-context")
          .setMaster("local[*]");
      sparkconf.set(SQLConf.PARTITION_OVERWRITE_MODE().key(), "dynamic");
      sparkconf.set("spark.executor.heartbeatInterval", "100s");
      sparkconf.set("spark.network.timeout", "200s");
      sparkconf.set("spark.sql.extensions", ArcticSparkExtensions.class.getName());
      context = SparkSession
          .builder()
          .config(sparkconf)
          .getOrCreate();
      context.sparkContext().setLogLevel("WARN");
    }

    return context;
  }
}
