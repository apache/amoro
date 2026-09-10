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

package org.apache.amoro.io;

import org.apache.amoro.hive.AuthenticatedHiveClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Table;
import org.apache.thrift.TException;

import java.util.Map;
import java.util.function.Supplier;

public class TableOwnerResolver {

  public static String resolve(
      String metastoreType,
      TableIdentifier tableIdentifier,
      Table icebergTable,
      Map<String, String> catalogProperties,
      TableMetaStore tableMetaStore) {
    if (!AuthenticatedFileIOs.isOptimizingCommitImpersonationActive()) {
      return null;
    }
    // Refresh before evaluating the table-level setting because it overrides the catalog setting
    // and both the setting and owner may have changed on a cached Iceberg table.
    tableMetaStore.doAs(
        () -> {
          icebergTable.refresh();
          return null;
        });
    return resolve(
        metastoreType,
        tableIdentifier,
        icebergTable.properties(),
        catalogProperties,
        () -> loadHiveOwner(tableIdentifier, catalogProperties, tableMetaStore));
  }

  @VisibleForTesting
  static String resolve(
      String metastoreType,
      TableIdentifier tableIdentifier,
      Map<String, String> tableProperties,
      Map<String, String> catalogProperties,
      Supplier<String> hiveOwnerLoader) {
    if (!AuthenticatedFileIOs.isHdfsImpersonationEnabledForOptimizingCommit(
        tableProperties, catalogProperties)) {
      return null;
    }
    if (CatalogMetaProperties.CATALOG_TYPE_HIVE.equalsIgnoreCase(metastoreType)) {
      return hiveOwnerLoader.get();
    }
    return tableProperties == null ? null : tableProperties.get(TableProperties.OWNER);
  }

  private static String loadHiveOwner(
      TableIdentifier tableIdentifier,
      Map<String, String> catalogProperties,
      TableMetaStore tableMetaStore) {
    HiveConf hiveConf = new HiveConf(tableMetaStore.getConfiguration(), TableOwnerResolver.class);
    tableMetaStore.getHiveSiteLocation().ifPresent(hiveConf::addResource);
    String metastoreUri = catalogProperties.get(CatalogProperties.URI);
    if (StringUtils.isNotBlank(metastoreUri)) {
      hiveConf.setVar(HiveConf.ConfVars.METASTOREURIS, metastoreUri);
    }

    return tableMetaStore.doAs(
        () -> {
          HMSClient client = AuthenticatedHiveClientPool.createHiveMetaStoreClient(hiveConf);
          try {
            return client
                .getTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName())
                .getOwner();
          } catch (TException e) {
            throw new IllegalStateException(
                "Failed to load Hive owner for table " + tableIdentifier, e);
          } finally {
            client.close();
          }
        });
  }

  private TableOwnerResolver() {}
}
