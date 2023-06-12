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

package com.netease.arctic.iceberg.mixed;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.iceberg.EmptyAmsClient;
import com.netease.arctic.iceberg.IcebergFormatOperations;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.TableTrashManagers;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BasicKeyedTable;
import com.netease.arctic.table.BasicUnkeyedTable;
import com.netease.arctic.table.MixedTableOperations;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.ArcticTableUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.exceptions.NoSuchTableException;

import java.util.Map;

public class MixedIcebergOperations extends IcebergFormatOperations implements MixedTableOperations {


  public MixedIcebergOperations(
      Catalog catalog, Map<String, String> catalogProperties, TableMetaStore tableMetaStore) {
    super(catalog, catalogProperties, tableMetaStore);
  }


  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public TableBuilder newTableBuilder(Schema schema, TableIdentifier identifier) {
    return new MixedIcebergTableBuilder(tableMetaStore, catalog, catalogProperties, schema, identifier);
  }

  @Override
  public ArcticTable loadTable(TableIdentifier identifier) {
    ArcticTable base = super.loadTable(identifier);
    return loadMixedTable(base);
  }

  @Override
  public boolean dropTable(TableIdentifier tableIdentifier, boolean purge) {
    ArcticTable table;
    try {
      table = loadTable(tableIdentifier);
    } catch (NoSuchTableException e) {
      return false;
    }
    ArcticTable base = table.isKeyedTable() ? table.asKeyedTable().baseTable() : table;

    // delete custom trash location
    String customTrashLocation = table.properties().get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
    ArcticFileIO io = table.io();

    boolean deleted = super.dropTable(tableIdentifier, purge);
    boolean changeDeleted = false;
    if (table.isKeyedTable()) {
      TableIdentifier changeIdentifier = ArcticTableUtil.changeStoreIdentifier(base);
      try {
        changeDeleted = super.dropTable(changeIdentifier, purge);
      } catch (Exception e) {
        // pass
      }
    }

    // delete custom trash location
    if (customTrashLocation != null) {
      String trashParentLocation = TableTrashManagers.getTrashParentLocation(tableIdentifier, customTrashLocation);
      if (io.supportFileSystemOperations() && io.exists(trashParentLocation)) {
        io.asPrefixFileIO().deletePrefix(trashParentLocation);
      }
    }
    return deleted || changeDeleted;
  }

  @Override
  public boolean isMixedTable(ArcticTable base) {
    return base.properties().containsKey(TableProperties.TABLE_FORMAT) &&
        TableProperties.TABLE_FORMAT_MIXED_ICEBERG.equalsIgnoreCase(
            base.properties().get(TableProperties.TABLE_FORMAT));
  }

  @Override
  public ArcticTable loadMixedTable(ArcticTable base) {
    if (!isMixedTable(base)) {
      throw new NoSuchTableException(base.id().toString() + " is not a " + format() + " table.");
    }
    PrimaryKeySpec keySpec = PrimaryKeySpec.noPrimaryKey();
    if (base.properties().containsKey(TableProperties.MIXED_ICEBERG_PRIMARY_KEY_FIELDS)) {
      PrimaryKeySpec.Builder keyBuilder = PrimaryKeySpec.builderFor(base.schema());
      String fieldString = base.properties().get(TableProperties.MIXED_ICEBERG_PRIMARY_KEY_FIELDS);
      String[] fields = fieldString.split(",");
      for (String field : fields) {
        keyBuilder = keyBuilder.addColumn(field);
      }
      keySpec = keyBuilder.build();
    }
    if (!keySpec.primaryKeyExisted()) {
      return new BasicUnkeyedTable(
          base.id(), base.asUnkeyedTable(), base.io(),
          new EmptyAmsClient(), null);
    }

    TableIdentifier changeIdentifier = ArcticTableUtil.changeStoreIdentifier(base);
    Table change = catalog.loadTable(MixedIcebergTableBuilder.icebergIdentifier(changeIdentifier));

    AmsClient client = new EmptyAmsClient();

    return new BasicKeyedTable(
        keySpec, client,
        new BasicKeyedTable.BaseInternalTable(base.id(), base.asUnkeyedTable(), base.io(), client, catalogProperties),
        new BasicKeyedTable.ChangeInternalTable(base.id(), change, base.io(), client, catalogProperties)
    );
  }
}
