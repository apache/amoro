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

package com.netease.arctic.ams.server.terminal.kyuubi;

import com.netease.arctic.ams.server.terminal.BaseResultSet;
import com.netease.arctic.ams.server.terminal.TerminalSession;
import java.util.List;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

public class KyuubiSession implements TerminalSession {

  List<String> logs = Lists.newArrayList();
  public KyuubiSession(List<String> logs){
    this.logs.addAll(logs);
  }

  @Override
  public ResultSet executeStatement(String catalog, String statement) {
    return new BaseResultSet(Lists.newArrayList(), Lists.newArrayList());
  }

  @Override
  public synchronized List<String> logs() {
    List<String> logs = Lists.newArrayList(this.logs);
    this.logs.clear();
    return logs;
  }

  @Override
  public boolean active() {
    return true;
  }
}
