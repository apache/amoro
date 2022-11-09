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

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.ams.server.terminal.BaseResultSet;
import com.netease.arctic.ams.server.terminal.TerminalSession;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;

public class LocalTerminalSession implements TerminalSession {

  List<String> logs = Lists.newArrayList();

  List<String> catalogs;
  SparkSession session;
  String currentCatalog;

  LocalTerminalSession(List<String> supportedCatalogs, SparkSession session, List<String> initLogs) {
    this.session = session;
    this.catalogs = supportedCatalogs;
    this.logs.addAll(initLogs);
  }

  @Override
  public ResultSet executeStatement(String catalog, String statement) {
    if (currentCatalog == null || !currentCatalog.equalsIgnoreCase(catalog)){
      session.sql("use " + catalog);
      currentCatalog = catalog;
      logs.add("switch to new catalog via: use " + catalog);
    }

    Dataset<Row> ds = session.sql(statement);
    List<Object[]> rows = ds.collectAsList()
        .stream()
        .map(r -> JavaConverters.seqAsJavaList(r.toSeq()).toArray(new Object[0]))
        .collect(Collectors.toList());

    return new BaseResultSet(Arrays.asList(ds.columns()), rows);
  }

  @Override
  public List<String> logs() {
    List<String> logs = Lists.newArrayList(this.logs);
    this.logs.clear();
    return logs;
  }

  @Override
  public boolean active() {
    try {
      return this.session.sql("select 1").collect() != null;
    } catch (Throwable t) {
      return false;
    }
  }


}
