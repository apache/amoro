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

package com.netease.arctic.ams.server.repair;

import java.io.IOException;

public class RepairMain {

  public static void main(String[] args) throws IOException {
    String amsUrl = amsUrl(args);
    CommandHandler commandHandler = new CallCommandHandler(amsUrl);
    SimpleShellTerminal simpleShellTerminal = new SimpleShellTerminal(commandHandler);
    simpleShellTerminal.start();
  }

  /**
   * thrift://ams-address/catalog or config path + catalog
   * @param args
   * @return
   */
  public static String amsUrl(String[] args) {
    if (args == null || args.length == 0) {
      throw new RuntimeException("Can not find any ams address or config path");
    }
    String args0 = args[0];
    if (args0.startsWith("thrift")) {
      return args0;
    }

    //todo
    return null;
  }
}
