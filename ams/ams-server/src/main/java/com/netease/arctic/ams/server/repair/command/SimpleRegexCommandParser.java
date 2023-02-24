/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.repair.command;

import org.apache.commons.lang3.StringUtils;

public class SimpleRegexCommandParser implements CommandParser {

  private AnalyzeCallGenerator analyzeCallGenerator;

  private RepairCallGenerator repairCallGenerator;

  private OptimizeCallGenerator optimizeCallGenerator;

  private RefreshCallGenerator refreshCallGenerator;

  private ShowCallGenerator showCallGenerator;

  private UseCallGenerator useCallGenerator;

  private static final String ANALYZE = "ANALYZE";
  private static final String REPAIR = "REPAIR";
  private static final String THROUGH = "THROUGH";
  private static final String USE = "USE";
  private static final String OPTIMIZE = "OPTIMIZE";
  private static final String REFRESH = "REFRESH";
  private static final String FILE_CACHE = "FILE_CACHE";
  private static final String SHOW = "SHOW";


  @Override
  public CallCommand parse(String line) throws IllegalCommandException {
    String[] commandSplit = line.split("\\s+");
    if (commandSplit.length < 2) {
      throw new IllegalCommandException("Please check if your command is correct!");
    }

    switch (commandSplit[0].toUpperCase()) {
      case ANALYZE:
        return analyzeCallGenerator.generate(commandSplit[1]);
      case REPAIR:
        if (commandSplit.length < 4 || !StringUtils.equalsIgnoreCase(commandSplit[2], THROUGH)) {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
        if (StringUtils.equalsIgnoreCase(commandSplit[3], String.valueOf(RepairCall.way.ROLLBACK))) {
          if (commandSplit.length < 5) {
            throw new IllegalCommandException("Please check if you enter your SnapshotID!");
          } else {
            return repairCallGenerator.generate(commandSplit[1], RepairCall.way.ROLLBACK, commandSplit[4]);
          }
        } else if (StringUtils.equalsIgnoreCase(commandSplit[3], String.valueOf(RepairCall.way.FIND_BACK)) ||
            StringUtils.equalsIgnoreCase(commandSplit[3], String.valueOf(RepairCall.way.SYNC_METADATA))) {
          return repairCallGenerator.generate(commandSplit[1], RepairCall.way.valueOf(commandSplit[3]), null);
        }
      case USE:
        return useCallGenerator.generate(commandSplit[1]);
      case OPTIMIZE:
        if (commandSplit.length < 3) {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
        if (StringUtils.equalsIgnoreCase(commandSplit[1], String.valueOf(OptimizeCall.action.Start)) ||
            StringUtils.equalsIgnoreCase(commandSplit[1], String.valueOf(OptimizeCall.action.Stop))) {
          return optimizeCallGenerator.generate(OptimizeCall.action.valueOf(commandSplit[1]), commandSplit[2]);
        }
      case REFRESH:
        if (!(commandSplit.length < 3) && StringUtils.equalsIgnoreCase(commandSplit[1], FILE_CACHE)) {
          return refreshCallGenerator.generate(commandSplit[2]);
        } else {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
      case SHOW:
        if (StringUtils.equalsIgnoreCase(commandSplit[1], String.valueOf(ShowCall.namespaces.DATABASES)) ||
            StringUtils.equalsIgnoreCase(commandSplit[1], String.valueOf(ShowCall.namespaces.TABLES))) {
          return showCallGenerator.generate(ShowCall.namespaces.valueOf(commandSplit[1]));
        } else {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
    }
    throw new IllegalCommandException("Please check if your command is correct!");
  }

  @Override
  public String[] keywords() {
    return new String[]{
        ANALYZE,
        REPAIR,
        USE,
        OPTIMIZE,
        REFRESH,
        SHOW
    };
  }
}
