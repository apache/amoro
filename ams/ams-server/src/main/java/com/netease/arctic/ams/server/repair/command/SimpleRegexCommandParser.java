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
  private static final String FIND_BACK = "FIND_BACK";
  private static final String SYNC_METADATA = "SYNC_METADATA";
  private static final String ROLLBACK = "ROLLBACK";
  private static final String USE = "USE";
  private static final String OPTIMIZE = "OPTIMIZE";
  private static final String START = "START";
  private static final String STOP = "STOP";
  private static final String REFRESH = "REFRESH";
  private static final String FILE_CACHE = "FILE_CACHE";
  private static final String SHOW = "SHOW";
  private static final String DATABASES = "DATABASES";
  private static final String TABLES = "TABLES";


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
        if (StringUtils.equalsIgnoreCase(commandSplit[2], THROUGH) && !(commandSplit.length < 4)) {
          if (StringUtils.equalsIgnoreCase(commandSplit[3], ROLLBACK)) {
            if (commandSplit.length < 5) {
              throw new IllegalCommandException("Please check if you enter your SnapshotID!");
            } else {
              return repairCallGenerator.generate(commandSplit[1], commandSplit[3], commandSplit[4]);
            }
          } else if (StringUtils.equalsIgnoreCase(commandSplit[3], FIND_BACK) ||
              StringUtils.equalsIgnoreCase(commandSplit[3], SYNC_METADATA)) {
            return repairCallGenerator.generate(commandSplit[1], commandSplit[3], null);
          }
        } else {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
      case USE:
        return useCallGenerator.generate(commandSplit[1]);
      case OPTIMIZE:
        if (!(commandSplit.length < 3)) {
          if (StringUtils.equalsIgnoreCase(commandSplit[1], START) ||
              StringUtils.equalsIgnoreCase(commandSplit[1], STOP)) {
            return optimizeCallGenerator.generate(commandSplit[1], commandSplit[2]);
          }
        } else {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
      case REFRESH:
        if (StringUtils.equalsIgnoreCase(commandSplit[1], FILE_CACHE) && !(commandSplit.length < 3)) {
          return refreshCallGenerator.generate(commandSplit[2]);
        } else {
          throw new IllegalCommandException("Please check if your command is correct!");
        }
      case SHOW:
        if (StringUtils.equalsIgnoreCase(commandSplit[1], DATABASES) ||
            StringUtils.equalsIgnoreCase(commandSplit[1], TABLES)) {
          return showCallGenerator.generate(commandSplit[1]);
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
