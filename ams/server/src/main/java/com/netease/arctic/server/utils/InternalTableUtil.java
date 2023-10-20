package com.netease.arctic.server.utils;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.server.iceberg.InternalTableStoreOperations;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.LocationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Util class for internal table operations.
 */
public class InternalTableUtil {
  private static final Logger LOG = LoggerFactory.getLogger(InternalTableUtil.class);
  public static final String DEFAULT_FILE_IO_IMPL = "org.apache.iceberg.io.ResolvingFileIO";
  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String PROPERTIES_METADATA_LOCATION = "iceberg.metadata.location";
  public static final String PROPERTIES_PREV_METADATA_LOCATION = "iceberg.metadata.prev-location";

  public static final String CHANGE_STORE_PREFIX = "change-store.";

  public static TableOperations newTableOperations(
      com.netease.arctic.server.table.TableMetadata tableMeta,
      FileIO io,
      boolean changeStore) {
    return new InternalTableStoreOperations(tableMeta.getTableIdentifier(), tableMeta, io, changeStore);
  }


  /**
   * create an iceberg file io instance
   * @param meta catalog meta
   * @return iceberg file io
   */
  public static ArcticFileIO newIcebergFileIo(CatalogMeta meta) {
    Map<String, String> catalogProperties = meta.getCatalogProperties();
    TableMetaStore store = CatalogUtil.buildMetaStore(meta);
    Configuration conf = store.getConfiguration();
    String ioImpl = catalogProperties.getOrDefault(CatalogProperties.FILE_IO_IMPL, DEFAULT_FILE_IO_IMPL);
    FileIO fileIO = org.apache.iceberg.CatalogUtil.loadFileIO(ioImpl, catalogProperties, conf);
    return ArcticFileIOs.buildAdaptIcebergFileIO(store, fileIO);
  }


  /**
   * load iceberg table metadata by given ams table metadata
   *
   * @param io        - iceberg file io
   * @param tableMeta - table metadata
   * @return iceberg table metadata object
   */
  public static TableMetadata loadIcebergTableStoreMetadata(
      FileIO io, com.netease.arctic.server.table.TableMetadata tableMeta) {
    return loadIcebergTableStoreMetadata(io, tableMeta, false);
  }

  /**
   * load iceberg table metadata by given ams table metadata
   * @param io        - iceberg file io
   * @param tableMeta - table metadata
   * @param changeStore - set true to load a change store metadata for mixed-format.
   * @return iceberg table metadata object
   */
  public static TableMetadata loadIcebergTableStoreMetadata(
      FileIO io, com.netease.arctic.server.table.TableMetadata tableMeta, boolean changeStore
  ) {
    String locationProperty = PROPERTIES_METADATA_LOCATION;
    if (changeStore) {
      Preconditions.checkState(
          TableFormat.MIXED_ICEBERG == tableMeta.getFormat(),
          "Table %s is not a mixed-iceberg table. can't load change-store",
          tableMeta.getTableIdentifier().toString()
      );
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
   * @param meta       - iceberg table metadata
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
   * @param base       - iceberg table metadata
   * @param current    - new iceberg table metadata
   * @return - file location
   */
  public static String genNewMetadataFileLocation(TableMetadata base, TableMetadata current) {
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
   * @param identifier           - table identifier
   * @param catalogMeta          - catalog meta
   * @param icebergTableMetadata - iceberg table metadata object
   * @param metadataFileLocation - iceberg table metadata file location
   * @param io                   - iceberg table file io
   * @return AMS table metadata object
   */
  public static com.netease.arctic.server.table.TableMetadata createTableInternal(
      ServerTableIdentifier identifier,
      CatalogMeta catalogMeta,
      TableMetadata icebergTableMetadata,
      String metadataFileLocation,
      FileIO io
  ) {
    OutputFile outputFile = io.newOutputFile(metadataFileLocation);
    TableMetadataParser.overwrite(icebergTableMetadata, outputFile);
    TableMeta meta = new TableMeta();
    meta.setTableIdentifier(identifier.getIdentifier());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_TABLE, icebergTableMetadata.location());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_BASE, icebergTableMetadata.location());

    meta.setFormat(TableFormat.ICEBERG.name());
    meta.putToProperties(PROPERTIES_METADATA_LOCATION, metadataFileLocation);
    return new com.netease.arctic.server.table.TableMetadata(
        identifier, meta, catalogMeta);
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
   * @param amsTableMetadata         ams table metadata
   * @param baseIcebergTableMetadata base iceberg table metadata
   * @param icebergTableMetadata     iceberg table metadata to commit
   * @param newMetadataFileLocation  new metadata file location
   * @param io                       iceberg file io
   */
  public static void commitTableInternal(
      com.netease.arctic.server.table.TableMetadata amsTableMetadata,
      TableMetadata baseIcebergTableMetadata,
      TableMetadata icebergTableMetadata,
      String newMetadataFileLocation,
      FileIO io,
      boolean changeStore
  ) {
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
