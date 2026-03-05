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

package org.apache.amoro.server.process;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.table.TableService;

/**
 * Simple runtime context holder for AMS process-related plugins.
 *
 * <p>Process factories and execute engines are loaded via {@link java.util.ServiceLoader} and
 * currently do not participate in dependency injection.
 */
public final class AmsProcessContext {

  private static volatile Configurations serviceConfig;
  private static volatile TableService tableService;

  private AmsProcessContext() {}

  public static void initServiceConfig(Configurations config) {
    serviceConfig = config;
  }

  public static Configurations serviceConfig() {
    return serviceConfig;
  }

  public static void initTableService(TableService service) {
    tableService = service;
  }

  public static TableService tableService() {
    return tableService;
  }
}
