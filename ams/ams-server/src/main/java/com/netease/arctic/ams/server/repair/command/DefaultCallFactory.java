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

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.client.OptimizeManagerClient;
import com.netease.arctic.ams.server.repair.RepairConfig;
import com.netease.arctic.ams.server.repair.RepairWay;
import com.netease.arctic.catalog.CatalogManager;

public class DefaultCallFactory implements CallFactory{

  private RepairConfig config;

  private CatalogManager catalogManager;

  private OptimizeManagerClient optimizeManagerClient;

  private AmsClient amsClient;

  public DefaultCallFactory(
      RepairConfig config,
      CatalogManager catalogManager,
      OptimizeManagerClient optimizeManagerClient, AmsClient amsClient) {
    this.config = config;
    this.catalogManager = catalogManager;
    this.optimizeManagerClient = optimizeManagerClient;
    this.amsClient = amsClient;
  }

  @Override
  public AnalyzeCall generateAnalyzeCall(String tablePath) {
    return new AnalyzeCall(tablePath, catalogManager, config.getMaxFindSnapshotNum(), config.getMaxRollbackSnapNum());
  }

  @Override
  public HelpCall generateHelpCall() {
    return new HelpCall();
  }

  @Override
  public OptimizeCall generateOptimizeCall(OptimizeCall.Action action, String tablePath) {
    return new OptimizeCall(optimizeManagerClient, action, tablePath);
  }

  @Override
  public RefreshCall generateRefreshCall(String tablePath) {
    return new RefreshCall(amsClient, tablePath);
  }

  @Override
  public RepairCall generateRepairCall(
      String tablePath, RepairWay way, Long option) {
    return new RepairCall(tablePath, way, option, catalogManager);
  }

  @Override
  public ShowCall generateShowCall(ShowCall.Namespaces namespaces) {
    return new ShowCall(namespaces, catalogManager);
  }

  @Override
  public UseCall generateUseCall(String namespace) {
    return new UseCall(namespace, catalogManager);
  }
}
