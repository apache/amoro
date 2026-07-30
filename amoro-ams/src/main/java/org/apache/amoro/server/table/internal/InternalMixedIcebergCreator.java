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
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.utils.InternalTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.rest.requests.CreateTableRequest;

import java.util.Map;

/** Table creator for mixed-iceberg format */
public class InternalMixedIcebergCreator extends InternalIcebergCreator {

  private String changMetadataFileLocation;

  public InternalMixedIcebergCreator(
      CatalogMeta catalog, String database, String tableName, CreateTableRequest request) {
    super(catalog, database, tableName, request);
  }

  public InternalMixedIcebergCreator(
      CatalogMeta catalog,
      String database,
      String tableName,
      CreateTableRequest request,
      String namespaceLocation) {
    super(catalog, database, tableName, request, namespaceLocation);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public org.apache.iceberg.TableMetadata stage() {
    validate(icebergMetadata);
    return super.stage();
  }

  @Override
  public TableMetadata create() {
    return create(icebergMetadata);
  }

  @Override
  public TableMetadata create(org.apache.iceberg.TableMetadata baseMetadata) {
    validate(baseMetadata);

    TableMetadata metadata = super.create(baseMetadata);
    metadata
        .getProperties()
        .put(InternalTableConstants.MIXED_ICEBERG_BASED_REST, Boolean.toString(true));

    PrimaryKeySpec keySpec =
        TablePropertyUtil.parsePrimaryKeySpec(baseMetadata.schema(), baseMetadata.properties());
    if (!keySpec.primaryKeyExisted()) {
      return metadata;
    }

    Map<String, String> changeProperties = Maps.newHashMap(baseMetadata.properties());
    changeProperties.putAll(
        TablePropertyUtil.changeStoreProperties(keySpec, TableFormat.MIXED_ICEBERG));
    String changeTableLocation = metadata.getTableLocation() + "/change";
    org.apache.iceberg.TableMetadata changeMetadata =
        org.apache.iceberg.TableMetadata.newTableMetadata(
            baseMetadata.schema(),
            baseMetadata.spec(),
            baseMetadata.sortOrder(),
            changeTableLocation,
            changeProperties);
    String changeMetadataFileLocation =
        InternalTableUtil.genNewMetadataFileLocation(null, changeMetadata);
    metadata
        .getProperties()
        .put(
            InternalTableConstants.CHANGE_STORE_PREFIX
                + InternalTableConstants.PROPERTIES_METADATA_LOCATION,
            changeMetadataFileLocation);
    metadata.setChangeLocation(changeTableLocation);
    metadata.setPrimaryKey(keySpec.description());

    OutputFile changeStoreFile = io.newOutputFile(changeMetadataFileLocation);
    this.changMetadataFileLocation = changeMetadataFileLocation;
    TableMetadataParser.overwrite(changeMetadata, changeStoreFile);
    return metadata;
  }

  private void validate(org.apache.iceberg.TableMetadata metadata) {
    Map<String, String> properties = metadata.properties();
    Preconditions.checkArgument(
        TablePropertyUtil.isBaseStore(properties, TableFormat.MIXED_ICEBERG),
        "The table creation request must be base store of mixed-iceberg");

    PrimaryKeySpec keySpec =
        TablePropertyUtil.parsePrimaryKeySpec(metadata.schema(), metadata.properties());

    if (keySpec.primaryKeyExisted()) {
      TableIdentifier identifier = TableIdentifier.of(database, tableName);
      TableIdentifier changeIdentifier = TablePropertyUtil.parseChangeIdentifier(properties);
      String expectChangeStoreName =
          identifier.name() + InternalTableConstants.CHANGE_STORE_TABLE_NAME_SUFFIX;
      TableIdentifier expectChangeIdentifier =
          TableIdentifier.of(identifier.namespace(), expectChangeStoreName);
      Preconditions.checkArgument(
          expectChangeIdentifier.equals(changeIdentifier),
          "the change store identifier is not expected. expected: %s, but found %s",
          expectChangeIdentifier.toString(),
          changeIdentifier.toString());
    }
  }

  @Override
  public void rollback() {
    super.rollback();
    if (StringUtils.isNotEmpty(changMetadataFileLocation)) {
      io.deleteFile(changMetadataFileLocation);
    }
  }
}
