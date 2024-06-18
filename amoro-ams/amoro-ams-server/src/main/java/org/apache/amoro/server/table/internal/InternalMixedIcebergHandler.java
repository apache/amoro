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

package org.apache.amoro.server.table.internal;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.op.MixedHadoopTableOperations;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.utils.InternalTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.MixedCatalogUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;

import java.util.Map;

/** Table handler for mixed-iceberg format */
public class InternalMixedIcebergHandler extends InternalIcebergHandler {
  private final boolean changeStore;
  private final CatalogMeta catalogMeta;

  public InternalMixedIcebergHandler(
      CatalogMeta catalogMeta, TableMetadata metadata, boolean changeStore) {
    super(catalogMeta, metadata);
    this.changeStore = changeStore;
    this.catalogMeta = catalogMeta;
  }

  @Override
  public TableOperations newTableOperator() {
    return newTableOperations(this.changeStore);
  }

  @Override
  public void dropTable(boolean purge) {
    checkClosed();
    if (purge && InternalTableUtil.isKeyedMixedTable(tableMetadata())) {
      purgeIceberg(newTableOperations(true));
    }
    super.dropTable(purge);
  }

  private TableOperations newTableOperations(boolean changeStore) {
    if (InternalTableUtil.isLegacyMixedIceberg(tableMetadata())) {
      String tableLocation =
          changeStore ? tableMetadata().getChangeLocation() : tableMetadata().getBaseLocation();
      TableMetaStore metaStore = MixedCatalogUtil.buildMetaStore(catalogMeta);

      MixedHadoopTableOperations ops =
          new MixedHadoopTableOperations(new Path(tableLocation), io, metaStore.getConfiguration());
      org.apache.iceberg.TableMetadata current = ops.current();
      if (current == null) {
        return ops;
      }
      org.apache.iceberg.TableMetadata legacyCurrent = legacyTableMetadata(current, changeStore);
      if (!current.equals(legacyCurrent)) {
        // add rest based mixed-format table properties
        ops.commit(current, legacyCurrent);
      }
      return ops;
    }
    return new MixedIcebergInternalTableStoreOperations(
        tableMetadata().getTableIdentifier(), tableMetadata(), io, changeStore);
  }

  private org.apache.iceberg.TableMetadata legacyTableMetadata(
      org.apache.iceberg.TableMetadata metadata, boolean changeStore) {
    PrimaryKeySpec keySpec = PrimaryKeySpec.noPrimaryKey();
    TableMeta tableMeta = tableMetadata().buildTableMeta();
    if (tableMeta.isSetKeySpec()) {
      PrimaryKeySpec.Builder keyBuilder = PrimaryKeySpec.builderFor(metadata.schema());
      tableMeta.getKeySpec().getFields().forEach(keyBuilder::addColumn);
      keySpec = keyBuilder.build();
    }
    TableIdentifier changeIdentifier =
        TableIdentifier.of(
            tableMetadata().getTableIdentifier().getDatabase(),
            tableMetadata().getTableIdentifier().getTableName()
                + InternalTableConstants.CHANGE_STORE_TABLE_NAME_SUFFIX);
    Map<String, String> properties = Maps.newHashMap(metadata.properties());
    if (!changeStore) {
      properties.putAll(
          TablePropertyUtil.baseStoreProperties(
              keySpec, changeIdentifier, TableFormat.MIXED_ICEBERG));
    } else {
      properties.putAll(
          TablePropertyUtil.changeStoreProperties(keySpec, TableFormat.MIXED_ICEBERG));
    }
    if (Maps.difference(properties, metadata.properties()).areEqual()) {
      return metadata;
    }
    return org.apache.iceberg.TableMetadata.buildFrom(metadata)
        .setProperties(properties)
        .discardChanges()
        .build();
  }
}
