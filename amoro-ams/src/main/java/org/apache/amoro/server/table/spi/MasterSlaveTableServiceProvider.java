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

package org.apache.amoro.server.table.spi;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.table.MasterSlaveTableService;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.TableServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** It can be used by configuring the item "table-service.impl=master-slave" */
public class MasterSlaveTableServiceProvider implements TableServiceProvider {

  private static final Logger LOG = LoggerFactory.getLogger(MasterSlaveTableServiceProvider.class);

  @Override
  public String name() {
    return "master-slave";
  }

  @Override
  public TableService create(Configurations configuration, CatalogManager catalogManager) {
    LOG.info("Creating master-slave TableService implementation");
    return new MasterSlaveTableService(configuration, catalogManager);
  }
}
