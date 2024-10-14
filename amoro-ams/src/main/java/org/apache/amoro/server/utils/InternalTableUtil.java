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

package org.apache.amoro.server.utils;

import static org.apache.amoro.server.table.internal.InternalTableConstants.HADOOP_FILE_IO_IMPL;
import static org.apache.amoro.server.table.internal.InternalTableConstants.METADATA_FOLDER_NAME;
import static org.apache.amoro.server.table.internal.InternalTableConstants.MIXED_ICEBERG_BASED_REST;
import static org.apache.amoro.server.table.internal.InternalTableConstants.S3_FILE_IO_IMPL;
import static org.apache.amoro.server.table.internal.InternalTableConstants.S3_PROTOCOL_PREFIX;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.util.LocationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/** Util class for internal table operations. */
public class InternalTableUtil {
  private static final Logger LOG = LoggerFactory.getLogger(InternalTableUtil.class);

  /**
   * Check if this table is created before version 0.7.0
   *
   * @param internalTableMetadata table metadata.
   * @return true if this table is created before version 0.7.0
   */
  public static boolean isLegacyMixedIceberg(
      org.apache.amoro.server.table.TableMetadata internalTableMetadata) {
    return TableFormat.MIXED_ICEBERG.equals(internalTableMetadata.getFormat())
        && !Boolean.parseBoolean(
            internalTableMetadata.getProperties().get(MIXED_ICEBERG_BASED_REST));
  }

  /**
   * check the table is a keyed mixed-format table
   *
   * @param serverTableMetadata server table metadata
   * @return true if the table is a keyed mixed format
   */
  public static boolean isKeyedMixedTable(
      org.apache.amoro.server.table.TableMetadata serverTableMetadata) {
    return StringUtils.isNotBlank(serverTableMetadata.getChangeLocation())
        && StringUtils.isNotBlank(serverTableMetadata.getPrimaryKey());
  }

  /**
   * create an iceberg file io instance
   *
   * @param meta catalog meta
   * @return iceberg file io
   */
  public static AuthenticatedFileIO newIcebergFileIo(CatalogMeta meta) {
    Map<String, String> catalogProperties = meta.getCatalogProperties();
    TableMetaStore store = CatalogUtil.buildMetaStore(meta);
    Configuration conf = store.getConfiguration();
    String warehouse = meta.getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);
    String defaultImpl = HADOOP_FILE_IO_IMPL;
    if (warehouse.toLowerCase().startsWith(S3_PROTOCOL_PREFIX)) {
      defaultImpl = S3_FILE_IO_IMPL;
    }
    String ioImpl = catalogProperties.getOrDefault(CatalogProperties.FILE_IO_IMPL, defaultImpl);
    FileIO fileIO = org.apache.iceberg.CatalogUtil.loadFileIO(ioImpl, catalogProperties, conf);
    return AuthenticatedFileIOs.buildAdaptIcebergFileIO(store, fileIO);
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
}
