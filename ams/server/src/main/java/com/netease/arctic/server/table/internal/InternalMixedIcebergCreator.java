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
import static com.netease.arctic.server.table.internal.InternalTableConstants.CHANGE_STORE_TABLE_NAME_SUFFIX;
import static com.netease.arctic.server.table.internal.InternalTableConstants.MIXED_ICEBERG_BASED_REST;
import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_METADATA_LOCATION;
import static com.netease.arctic.server.utils.InternalTableUtil.genNewMetadataFileLocation;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.requests.CreateTableRequest;

import java.util.Map;

public class InternalMixedIcebergCreator extends InternalIcebergCreator {

  private String changMetadataFileLocation;

  public InternalMixedIcebergCreator(
      CatalogMeta catalog, String database, String tableName, CreateTableRequest request) {
    super(catalog, database, tableName, request);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public TableMetadata create() {
    Map<String, String> properties = request.properties();
    Preconditions.checkArgument(
        TablePropertyUtil.isBaseStore(properties, TableFormat.MIXED_ICEBERG),
        "The table creation request must be base store of mixed-iceberg");

    PrimaryKeySpec keySpec =
        TablePropertyUtil.parsePrimaryKeySpec(request.schema(), request.properties());

    if (keySpec.primaryKeyExisted()) {
      TableIdentifier identifier = TableIdentifier.of(database, tableName);
      TableIdentifier changeIdentifier = TablePropertyUtil.parseChangeIdentifier(properties);
      String expectChangeStoreName = identifier.name() + CHANGE_STORE_TABLE_NAME_SUFFIX;
      TableIdentifier expectChangeIdentifier =
          TableIdentifier.of(identifier.namespace(), expectChangeStoreName);
      Preconditions.checkArgument(
          expectChangeIdentifier.equals(changeIdentifier),
          "the change store identifier is not expected. expected: %s, but found %s",
          expectChangeIdentifier.toString(),
          changeIdentifier.toString());
    }

    TableMetadata metadata = super.create();
    metadata.getProperties().put(MIXED_ICEBERG_BASED_REST, Boolean.toString(true));
    if (!keySpec.primaryKeyExisted()) {
      return metadata;
    }

    Map<String, String> changeProperties = Maps.newHashMap(request.properties());
    changeProperties.putAll(
        TablePropertyUtil.changeStoreProperties(keySpec, TableFormat.MIXED_ICEBERG));
    String changeTableLocation = metadata.getTableLocation() + "/change";
    org.apache.iceberg.TableMetadata changeMetadata =
        org.apache.iceberg.TableMetadata.newTableMetadata(
            icebergMetadata.schema(),
            icebergMetadata.spec(),
            icebergMetadata.sortOrder(),
            changeTableLocation,
            changeProperties);
    String changeMetadataFileLocation = genNewMetadataFileLocation(null, changeMetadata);
    metadata
        .getProperties()
        .put(CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION, changeMetadataFileLocation);

    OutputFile changeStoreFile = io.newOutputFile(changeMetadataFileLocation);
    this.changMetadataFileLocation = changeMetadataFileLocation;
    TableMetadataParser.overwrite(changeMetadata, changeStoreFile);
    return metadata;
  }

  @Override
  public void rollback() {
    super.rollback();
    if (StringUtils.isNotEmpty(changMetadataFileLocation)) {
      io.deleteFile(changMetadataFileLocation);
    }
  }
}
