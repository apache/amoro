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

package com.netease.arctic.server.table.internal;

import static com.netease.arctic.server.table.internal.InternalTableConstants.CHANGE_STORE_PREFIX;
import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_METADATA_LOCATION;
import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_PREV_METADATA_LOCATION;

import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;
import org.apache.iceberg.io.FileIO;

import java.util.Map;

public class MixedIcebergInternalTableStoreOperations extends IcebergInternalTableOperations {
  boolean forChangeStore;

  public MixedIcebergInternalTableStoreOperations(
      ServerTableIdentifier identifier,
      TableMetadata tableMetadata,
      FileIO io,
      boolean changeStore) {
    super(identifier, tableMetadata, io);
    this.forChangeStore = changeStore;
  }

  @Override
  protected String tableMetadataLocation(TableMetadata tableMeta) {
    if (forChangeStore) {
      return tableMeta.getProperties().get(CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION);
    }
    return super.tableMetadataLocation(tableMeta);
  }

  @Override
  protected void updateMetadataLocationProperties(
      TableMetadata serverTableMetadata,
      String oldMetadataFileLocation,
      String newMetadataFileLocation) {
    if (!forChangeStore) {
      super.updateMetadataLocationProperties(
          serverTableMetadata, oldMetadataFileLocation, newMetadataFileLocation);
    } else {
      Map<String, String> properties = serverTableMetadata.getProperties();
      properties.put(
          CHANGE_STORE_PREFIX + PROPERTIES_PREV_METADATA_LOCATION, oldMetadataFileLocation);
      properties.put(CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION, newMetadataFileLocation);
      serverTableMetadata.setProperties(properties);
    }
  }
}
