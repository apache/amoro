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

package org.apache.amoro.server.table;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.CatalogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ServiceLoader;

public final class TableServiceLoader {

  private static final Logger LOG = LoggerFactory.getLogger(TableServiceLoader.class);

  private TableServiceLoader() {}

  public static TableService load(Configurations conf, CatalogManager catalogManager) {
    String impl = conf.getString(AmoroManagementConf.TABLE_SERVICE_IMPL);

    // 1) Try named providers via ServiceLoader
    ServiceLoader<TableServiceProvider> loader = ServiceLoader.load(TableServiceProvider.class);
    for (TableServiceProvider provider : loader) {
      try {
        if (provider.name().equalsIgnoreCase(impl)) {
          LOG.info("Loading TableService from provider name: {} -> {}", impl, provider.getClass());
          return provider.create(conf, catalogManager);
        }
      } catch (Throwable t) {
        LOG.warn("Failed to create TableService from provider {}", provider.getClass(), t);
      }
    }

    // 2) Try FQCN
    try {
      Class<?> clazz = Class.forName(impl);
      if (!TableService.class.isAssignableFrom(clazz)) {
        LOG.warn("Configured class {} does not implement TableService, fallback to default.", impl);
      } else {
        try {
          Constructor<?> constructor =
              clazz.getConstructor(Configurations.class, CatalogManager.class);
          LOG.info("Loading TableService from class: {}", impl);
          return (TableService) constructor.newInstance(conf, catalogManager);
        } catch (NoSuchMethodException nsme) {
          LOG.warn(
              "No (Configurations, CatalogManager) constructor for {}, fallback to default.", impl);
        }
      }
    } catch (ClassNotFoundException cnfe) {
      LOG.info("Configured TableService impl not found as class: {}. Will fallback.", impl);
    } catch (Throwable t) {
      LOG.warn("Failed to instantiate TableService impl: {}. Will fallback.", impl, t);
    }

    // 3) Fallback to 'default' provider
    for (TableServiceProvider provider : loader) {
      if ("default".equalsIgnoreCase(provider.name())) {
        LOG.info("Falling back to default TableService provider: {}", provider.getClass());
        return provider.create(conf, catalogManager);
      }
    }

    // 4) Last resort: try DefaultTableService directly (avoid circular deps by FQCN)
    LOG.info("Falling back to DefaultTableService directly.");
    return new org.apache.amoro.server.table.DefaultTableService(conf, catalogManager);
  }
}
