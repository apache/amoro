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

package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.table.TableIdentifier;
import java.util.List;

public class RuntimeDataExpireService {
  private final ArcticTransactionService transactionService;
  private final IMetaService metaService;

  Long txDataExpireInterval = 24 * 60 * 60 * 1000L;

  public RuntimeDataExpireService() {
    this.transactionService = ServiceContainer.getArcticTransactionService();
    this.metaService = ServiceContainer.getMetaService();
  }

  public void doExpire() {
    List<TableMetadata> tableMetadata = metaService.listTables();
    tableMetadata.forEach(meta -> {
      TableIdentifier identifier = meta.getTableIdentifier();
      transactionService.expire(
          identifier.buildTableIdentifier(),
          System.currentTimeMillis() - this.txDataExpireInterval);
    });
  }
}
