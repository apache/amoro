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

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.api.client.ArcticThriftUrl;
import com.netease.arctic.ams.server.config.ConfigFileProperties;
import com.netease.arctic.ams.server.utils.YamlUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class RepairMain {

  public static void main(String[] args) throws IOException {
    RepairConfig repairConfig = getRepairConfig(args);
    bootstrap(repairConfig);
  }

  public static void bootstrap(RepairConfig repairConfig) throws IOException {
    CommandHandler commandHandler = new CallCommandHandler(repairConfig);
    SimpleShellTerminal simpleShellTerminal = new SimpleShellTerminal(commandHandler);
    simpleShellTerminal.start();
  }

  /**
   * thrift://ams-address/catalog or config path + catalog
   * @param args
   * @return
   */
  public static RepairConfig getRepairConfig(String[] args) {
    if (args == null) {
      throw new RuntimeException("Can not find any ams address or config path");
    }
    String thriftUrl = args[0];
    ArcticThriftUrl arcticThriftUrl = ArcticThriftUrl.parse(thriftUrl);
    String catalogName = arcticThriftUrl.catalogName();
    catalogName = StringUtils.isBlank(catalogName) ? null : catalogName;
    String thriftUrlWithoutCatalog =
        arcticThriftUrl.schema() + "://" + arcticThriftUrl.host() + ":" + arcticThriftUrl.port();

    Integer maxFindSnapshotNum = null;
    Integer maxRollbackSnapNum = null;
    if (args.length == 2) {
      String configPath = args[1];
      JSONObject yamlConfig = YamlUtils.load(configPath);
      JSONObject repairProperties = yamlConfig.getJSONObject(ConfigFileProperties.REPAIR_PROPERTIES);
      maxFindSnapshotNum = repairProperties.getInteger(ConfigFileProperties.REPAIR_MAX_FIND_SNAPSHOT_NUM);
      maxRollbackSnapNum = repairProperties.getInteger(ConfigFileProperties.REPAIR_MAX_ROLL_BACK_SNAPSHOT_NUM);
    }

    return new RepairConfig(thriftUrlWithoutCatalog , catalogName, maxFindSnapshotNum, maxRollbackSnapNum);
  }
}
