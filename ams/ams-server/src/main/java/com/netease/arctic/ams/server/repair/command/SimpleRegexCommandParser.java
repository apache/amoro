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

  private static final String ANALYZE = "ANALYZE";
  private static final String REPAIR = "REPAIR";
  private static final String USE = "USE";
  private static final String OPTIMIZE = "OPTIMIZE";
  private static final String REFRESH = "REFRESH";
  private static final String SHOW = "SHOW";
  private static final String THROUGH = "THROUGH";
  private static final String ROLLBACK = "ROLLBACK";
  private static final String FILE_CACHE = "FILE_CACHE";


  @Override
  public CallCommand parse(String line) throws IllegalCommandException {
    String[] commandSplit = line.split("\\s+");

    for (String keyword : keywords()) {
      if (StringUtils.equalsIgnoreCase(commandSplit[0], keyword)) {
        switch (keyword) {
          case ANALYZE:
            return new AnalyzeCallGenerator().generate(commandSplit[1]);
          case REPAIR:
            if (StringUtils.equalsIgnoreCase(commandSplit[2], THROUGH)) {
              if (StringUtils.equalsIgnoreCase(commandSplit[3], ROLLBACK)) {
                return new RepairCallGenerator().generate(commandSplit[1], commandSplit[3], commandSplit[4]);
              } else {
                return new RepairCallGenerator().generate(commandSplit[1], commandSplit[3], null);
              }
            } else {
              throw new IllegalCommandException("Please check if your command is correct!");
            }
          case USE:
            return new UseCallGenerator().generate(commandSplit[1]);
          case OPTIMIZE:
            return new OptimizeCallGenerator().generate(commandSplit[1], commandSplit[2]);
          case REFRESH:
            if (StringUtils.equalsIgnoreCase(commandSplit[1], FILE_CACHE)) {
              return new RefreshCallGenerator().generate(commandSplit[2]);
            } else {
              throw new IllegalCommandException("Please check if your command is correct!");
            }
          case SHOW:
            return new ShowCallGenerator().generate(commandSplit[1]);
        }
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
