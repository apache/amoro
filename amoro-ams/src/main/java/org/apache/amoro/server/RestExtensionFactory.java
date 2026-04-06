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

package org.apache.amoro.server;

import org.apache.amoro.ActivePlugin;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.RequestForwarder;
import org.apache.amoro.server.table.TableManager;

public interface RestExtensionFactory extends ActivePlugin {
  RestExtensionFactory withServiceConfig(Configurations serviceConfig);

  RestExtensionFactory withCatalogManager(CatalogManager catalogManager);

  RestExtensionFactory withTableManager(TableManager tableManager);

  default RestExtensionFactory withRequestForwarder(RequestForwarder requestForwarder) {
    return this;
  }

  RestExtension build();
}
