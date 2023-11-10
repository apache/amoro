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

package com.netease.arctic.server.utils;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.op.ArcticHadoopTableOperations;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.iceberg.InternalTableStoreOperations;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.LocationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Util class for internal table operations. */
public class InternalTableUtil {
  private static final Logger LOG = LoggerFactory.getLogger(InternalTableUtil.class);
  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String PROPERTIES_METADATA_LOCATION = "iceberg.metadata.location";
  public static final String PROPERTIES_PREV_METADATA_LOCATION = "iceberg.metadata.prev-location";

  public static final String CHANGE_STORE_PREFIX = "change-store.";

  public static final String MIXED_ICEBERG_BASED_REST = "mixed-iceberg.based-on-rest-catalog";

  private static final String HADOOP_FILE_IO_IMPL = "org.apache.iceberg.hadoop.HadoopFileIO";
  private static final String S3_FILE_IO_IMPL = "org.apache.iceberg.aws.s3.S3FileIO";

  public static final String CHANGE_STORE_TABLE_NAME_SUFFIX =
      MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR
          + MetaTableProperties.MIXED_FORMAT_CHANGE_STORE_SUFFIX;

  private static final String S3_PROTOCOL_PREFIX = "s3://";

  /**
   * create an iceberg table operations for internal iceberg/mixed-iceberg table
   *
   * @param catalogMeta catalogMeta of table
   * @param tableMeta persistent tableMetadata
   * @param io arctic file io
   * @param changeStore is change store of mixed-iceberg
   * @return iceberg table operation.
   */
  public static TableOperations newTableOperations(
      CatalogMeta catalogMeta,
      com.netease.arctic.server.table.TableMetadata tableMeta,
      ArcticFileIO io,
      boolean changeStore) {
    if (isLegacyMixedIceberg(tableMeta)) {
      String tableLocation =
          changeStore ? tableMeta.getChangeLocation() : tableMeta.getBaseLocation();
      TableMetaStore metaStore = CatalogUtil.buildMetaStore(catalogMeta);
      return new ArcticHadoopTableOperations(
          new Path(tableLocation), io, metaStore.getConfiguration());
    }
    return new InternalTableStoreOperations(
        tableMeta.getTableIdentifier(), tableMeta, io, changeStore);
  }

  /**
   * check if the given persistent tableMetadata if a legacy mixed-iceberg table.
   * add addition table properties for REST CATALOG
   *
   * @param internalTableMetadata persistent table metadata.
   * @param metadata iceberg table metadata
   * @param changeStore is change store if mixed-berg.
   * @return Mixed-iceberg metadata adapted for the REST Catalog.
   */
  public static TableMetadata legacyTableMetadata(
      com.netease.arctic.server.table.TableMetadata internalTableMetadata,
      TableMetadata metadata,
      boolean changeStore) {
    if (!isLegacyMixedIceberg(internalTableMetadata)) {
      return metadata;
    }
    PrimaryKeySpec keySpec = PrimaryKeySpec.noPrimaryKey();
    TableMeta tableMeta = internalTableMetadata.buildTableMeta();
    if (tableMeta.isSetKeySpec()) {
      PrimaryKeySpec.Builder keyBuilder = PrimaryKeySpec.builderFor(metadata.schema());
      tableMeta.getKeySpec().getFields().forEach(keyBuilder::addColumn);
      keySpec = keyBuilder.build();
    }
    TableIdentifier changeIdentifier =
        TableIdentifier.of(
            internalTableMetadata.getTableIdentifier().getDatabase(),
            internalTableMetadata.getTableIdentifier().getTableName()
                + CHANGE_STORE_TABLE_NAME_SUFFIX);
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
    return TableMetadata.buildFrom(metadata).setProperties(properties).discardChanges().build();
  }

  public static boolean isLegacyMixedIceberg(
      com.netease.arctic.server.table.TableMetadata internalTableMetadata) {
    return TableFormat.MIXED_ICEBERG == internalTableMetadata.getFormat()
        && !Boolean.parseBoolean(
            internalTableMetadata.getProperties().get(MIXED_ICEBERG_BASED_REST));
  }

  /**
   * Check is this a change store table name
   *
   * @param tableName table name to create or load.
   * @return is this match the change store name pattern
   */
  public static boolean isMatchChangeStoreNamePattern(String tableName) {
    String separator = MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR;
    if (!tableName.contains(separator)) {
      return false;
    }
    Preconditions.checkArgument(
        tableName.indexOf(separator) == tableName.lastIndexOf(separator)
            && tableName.endsWith(CHANGE_STORE_TABLE_NAME_SUFFIX),
        "illegal table name: %s, %s is not allowed in table name.",
        tableName,
        separator);

    return true;
  }

  /**
   * get the real table location.
   *
   * @param catalog - internal server catalog to create the table.
   * @param database - database of table to be created
   * @param tableName - tableName of table to be created.
   * @param location - the requested location.
   * @return - the real table location.
   */
  public static String tableLocation(
      InternalCatalog catalog,
      String database,
      String tableName,
      String location) {
    if (StringUtils.isBlank(location)) {
      String warehouse =
          catalog.getMetadata().getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);
      Preconditions.checkState(
          StringUtils.isNotBlank(warehouse), "catalog warehouse is not configured");
      warehouse = LocationUtil.stripTrailingSlash(warehouse);
      location = warehouse + "/" + database + "/" + tableName;
    } else {
      location = LocationUtil.stripTrailingSlash(location);
    }
    return location;
  }

  /**
   * get the internal table name
   *
   * @param tableName requested table name
   * @return internal table name
   */
  public static String internalTableName(String tableName) {
    if (isMatchChangeStoreNamePattern(tableName)) {
      return tableName.substring(0, tableName.length() - CHANGE_STORE_TABLE_NAME_SUFFIX.length());
    }
    return tableName;
  }

  /**
   * create an iceberg file io instance
   *
   * @param meta catalog meta
   * @return iceberg file io
   */
  public static ArcticFileIO newIcebergFileIo(CatalogMeta meta) {
    Map<String, String> catalogProperties = meta.getCatalogProperties();
    TableMetaStore store = CatalogUtil.buildMetaStore(meta);
    Configuration conf = store.getConfiguration();
    String warehouse = meta.getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);
    String ioImpl =
        catalogProperties.getOrDefault(
            CatalogProperties.FILE_IO_IMPL, defaultFileIoImpl(warehouse));
    FileIO fileIO = org.apache.iceberg.CatalogUtil.loadFileIO(ioImpl, catalogProperties, conf);
    return ArcticFileIOs.buildAdaptIcebergFileIO(store, fileIO);
  }

  private static String defaultFileIoImpl(String warehouse) {
    if (warehouse.toLowerCase().startsWith(S3_PROTOCOL_PREFIX)) {
      return S3_FILE_IO_IMPL;
    }
    return HADOOP_FILE_IO_IMPL;
  }


  /**
   * load iceberg table metadata by given ams table metadata
   *
   * @param io - iceberg file io
   * @param tableMeta - table metadata
   * @param changeStore - set true to load a change store metadata for mixed-format.
   * @return iceberg table metadata object
   */
  public static TableMetadata loadIcebergTableStoreMetadata(
      FileIO io, com.netease.arctic.server.table.TableMetadata tableMeta, boolean changeStore) {
    String locationProperty = PROPERTIES_METADATA_LOCATION;
    if (changeStore) {
      Preconditions.checkState(
          TableFormat.MIXED_ICEBERG == tableMeta.getFormat(),
          "Table %s is not a mixed-iceberg table. can't load change-store",
          tableMeta.getTableIdentifier().toString());
      locationProperty = CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION;
    }
    String metadataFileLocation = tableMeta.getProperties().get(locationProperty);
    if (StringUtils.isBlank(metadataFileLocation)) {
      return null;
    }
    return TableMetadataParser.read(io, metadataFileLocation);
  }

  /**
   * generate metadata file location with version
   *
   * @param meta - iceberg table metadata
   * @param newVersion - version of table metadata
   * @return - file location
   */
  private static String genNewMetadataFileLocation(TableMetadata meta, long newVersion) {
    String codecName =
        meta.property(
            TableProperties.METADATA_COMPRESSION, TableProperties.METADATA_COMPRESSION_DEFAULT);
    String fileExtension = TableMetadataParser.getFileExtension(codecName);
    return genMetadataFileLocation(
        meta, String.format("v%05d-%s%s", newVersion, UUID.randomUUID(), fileExtension));
  }

  /**
   * generate metadata file location with version
   *
   * @param base - iceberg table metadata
   * @param current - new iceberg table metadata
   * @return - file location
   */
  public static String genNewMetadataFileLocation(TableMetadata base, TableMetadata current) {
    if (current == null) {
      return null;
    }
    long version = 0;
    if (base != null && StringUtils.isNotBlank(base.metadataFileLocation())) {
      version = parseMetadataFileVersion(base.metadataFileLocation());
    }

    return genNewMetadataFileLocation(current, version + 1);
  }

  /**
   * generate the metadata file location with given filename
   *
   * @param metadata - the table metadata
   * @param filename - filename for table metadata
   * @return - table metadata file location
   */
  public static String genMetadataFileLocation(TableMetadata metadata, String filename) {
    String metadataLocation = metadata.properties().get(TableProperties.WRITE_METADATA_LOCATION);

    if (metadataLocation != null) {
      return String.format("%s/%s", LocationUtil.stripTrailingSlash(metadataLocation), filename);
    } else {
      return String.format("%s/%s/%s", metadata.location(), METADATA_FOLDER_NAME, filename);
    }
  }


  /**
   * create iceberg table and return an AMS table metadata object to commit.
   *
   * @param identifier - table identifier
   * @param catalogMeta - catalog meta
   * @param baseMetadata - iceberg table metadata object
   * @param baseMetadataFileLocation - iceberg table metadata file location
   * @param io - iceberg table file io
   * @return AMS table metadata object
   */
  public static com.netease.arctic.server.table.TableMetadata createTableInternal(
      TableIdentifier identifier,
      CatalogMeta catalogMeta,
      TableFormat format,
      TableMetadata baseMetadata,
      TableMetadata changeMetadata,
      PrimaryKeySpec keySpec,
      String baseMetadataFileLocation,
      String changeMetadataFileLocation,
      FileIO io) {
    TableMeta meta = new TableMeta();
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_TABLE, baseMetadata.location());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_BASE, baseMetadata.location());
    meta.setFormat(format.name());
    meta.putToProperties(PROPERTIES_METADATA_LOCATION, baseMetadataFileLocation);

    if (TableFormat.MIXED_ICEBERG == format) {
      if (keySpec.primaryKeyExisted()) {
        com.netease.arctic.ams.api.PrimaryKeySpec apiKeySpec =
            new com.netease.arctic.ams.api.PrimaryKeySpec();
        apiKeySpec.setFields(keySpec.fieldNames());
        meta.setKeySpec(apiKeySpec);
        meta.putToLocations(MetaTableProperties.LOCATION_KEY_CHANGE, changeMetadata.location());
      }
      meta.putToProperties(MIXED_ICEBERG_BASED_REST, Boolean.toString(true));
    }

    ServerTableIdentifier serverTableIdentifier =
        ServerTableIdentifier.of(
            catalogMeta.getCatalogName(),
            identifier.namespace().level(0),
            identifier.name(),
            format);
    meta.setTableIdentifier(serverTableIdentifier.getIdentifier());
    // write metadata file.
    OutputFile outputFile = io.newOutputFile(baseMetadataFileLocation);
    TableMetadataParser.overwrite(baseMetadata, outputFile);
    if (changeMetadata != null) {
      OutputFile changeStoreFile = io.newOutputFile(changeMetadataFileLocation);
      TableMetadataParser.overwrite(changeMetadata, changeStoreFile);
    }
    return new com.netease.arctic.server.table.TableMetadata(
        serverTableIdentifier, meta, catalogMeta);
  }




  private static long parseMetadataFileVersion(String metadataLocation) {
    int fileNameStart = metadataLocation.lastIndexOf('/') + 1; // if '/' isn't found, this will be 0
    String fileName = metadataLocation.substring(fileNameStart);
    if (fileName.startsWith("v")) {
      fileName = fileName.substring(1);
    }
    int versionEnd = fileName.indexOf('-');
    if (versionEnd < 0) {
      return 0;
    }
    try {
      return Long.parseLong(fileName.substring(0, versionEnd));
    } catch (NumberFormatException e) {
      LOG.warn("Unable to parse version from metadata location: {}", metadataLocation, e);
      return 0;
    }
  }

  /**
   * write iceberg table metadata and apply changes to AMS tableMetadata to commit.
   *
   * @param amsTableMetadata ams table metadata
   * @param baseIcebergTableMetadata base iceberg table metadata
   * @param icebergTableMetadata iceberg table metadata to commit
   * @param newMetadataFileLocation new metadata file location
   * @param io iceberg file io
   */
  public static void commitTableInternal(
      com.netease.arctic.server.table.TableMetadata amsTableMetadata,
      TableMetadata baseIcebergTableMetadata,
      TableMetadata icebergTableMetadata,
      String newMetadataFileLocation,
      FileIO io,
      boolean changeStore) {
    if (!Objects.equals(icebergTableMetadata.location(), baseIcebergTableMetadata.location())) {
      throw new UnsupportedOperationException("SetLocation is not supported.");
    }
    OutputFile outputFile = io.newOutputFile(newMetadataFileLocation);
    TableMetadataParser.overwrite(icebergTableMetadata, outputFile);

    Map<String, String> properties = amsTableMetadata.getProperties();
    String prevLocationKey = PROPERTIES_PREV_METADATA_LOCATION;
    String metadataLocationKey = PROPERTIES_METADATA_LOCATION;
    if (changeStore) {
      prevLocationKey = CHANGE_STORE_PREFIX + PROPERTIES_PREV_METADATA_LOCATION;
      metadataLocationKey = CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION;
    }

    properties.put(prevLocationKey, baseIcebergTableMetadata.metadataFileLocation());
    properties.put(metadataLocationKey, newMetadataFileLocation);
    amsTableMetadata.setProperties(properties);
  }

  public static void checkCommitSuccess(
      com.netease.arctic.server.table.TableMetadata updatedTableMetadata,
      String metadataFileLocation,
      boolean changeStore) {
    String metadataLocationKey = PROPERTIES_METADATA_LOCATION;
    if (changeStore) {
      metadataLocationKey = CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION;
    }
    String metaLocationInDatabase = updatedTableMetadata.getProperties().get(metadataLocationKey);
    if (!Objects.equals(metaLocationInDatabase, metadataFileLocation)) {
      throw new CommitFailedException(
          "commit conflict, some other commit happened during this commit. ");
    }
  }
}
