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

import com.google.common.collect.Lists;
import com.netease.arctic.ams.server.config.Configuration;
import com.netease.arctic.ams.server.terminal.TerminalSession;
import com.netease.arctic.ams.server.terminal.TerminalSessionFactory;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkExtensions;
import com.netease.arctic.table.TableMetaStore;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SQLConf;

import java.util.List;

public class LocalSessionFactory implements TerminalSessionFactory {

  SparkSession context = null;

  @Override
  public void initialize(Configuration properties) {

  }

  @Override
  public TerminalSession create(TableMetaStore metaStore, Configuration configuration) {
    SparkSession context = lazyInitContext();
    SparkSession session = context.cloneSession();
    List<String> catalogs = configuration.get(SessionConfigOptions.CATALOGS);
    List<String> initializeLogs = Lists.newArrayList();
    initializeLogs.add("initialize session, session factory: " + LocalSessionFactory.class.getName());

    for (String catalog : catalogs) {
      String type = configuration.getString(SessionConfigOptions.catalogType(catalog));
      String url = configuration.getString(SessionConfigOptions.catalogUrl(catalog));
      initializeLogs.add("add catalog config to spark session:");

      updateSessionConf(session, initializeLogs, "spark.sql.catalog." + catalog, ArcticSparkCatalog.class.getName());
      updateSessionConf(session, initializeLogs, "spark.sql.catalog." + catalog + ".url", url);
    }

    return new LocalTerminalSession(catalogs, session, initializeLogs);
  }

  private void updateSessionConf(SparkSession session, List<String> logs, String key, String value) {
    session.conf().set(key, value);
    logs.add(key + "  " + value);
  }

  protected synchronized SparkSession lazyInitContext() {
    if (context == null) {
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
