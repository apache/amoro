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

package com.netease.arctic.ams.server.repair;

import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.client.OptimizeManagerClient;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.ams.server.repair.command.CallFactory;
import com.netease.arctic.ams.server.repair.command.DefaultCallFactory;
import com.netease.arctic.catalog.CatalogManager;
import com.netease.arctic.catalog.TableTestBase;

public class CallCommandTestBase extends TableTestBase {

  private static final Integer maxFindSnapshotNum = 100;
  private static final Integer maxRollbackSnapNum = 100;

  public static CallFactory callFactory = new DefaultCallFactory(
      new RepairConfig(TEST_AMS.getServerUrl(), TEST_CATALOG_NAME, maxFindSnapshotNum, maxRollbackSnapNum),
      new CatalogManager(TEST_AMS.getServerUrl()),
      new OptimizeManagerClient(TEST_AMS.getServerUrl()),
      new PooledAmsClient(TEST_AMS.getServerUrl())
  );

  public CallCommandTestBase() {
    super(TableFormat.MIXED_ICEBERG, true, true);
  }

}
