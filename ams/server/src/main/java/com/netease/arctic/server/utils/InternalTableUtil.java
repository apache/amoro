package com.netease.arctic.server.utils;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.iceberg.InternalTableStoreOperations;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.NotFoundException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.LocationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.netease.arctic.table.PrimaryKeySpec.PRIMARY_KEY_COLUMN_JOIN_DELIMITER;

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

  public static final String MIXED_ICEBERG_BASED_REST = "mixed-iceberg.based-on-rest-catalog";

  public static TableOperations newTableOperations(
      com.netease.arctic.server.table.TableMetadata tableMeta,
      FileIO io,
      boolean changeStore) {
    return new InternalTableStoreOperations(tableMeta.getTableIdentifier(), tableMeta, io, changeStore);
  }

  /**
   * Check is this a change store table name
   *
   * @param tableName table name to create or load.
   * @return is this match the change store name pattern
   */
  public static boolean isMatchChangeStoreNamePattern(String tableName) {
    if (!tableName.contains(MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR)) {
      return false;
    }
    String separator = MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR;
    String changeStoreSuffix = separator + "change";
    Preconditions.checkArgument(
        tableName.indexOf(separator) == tableName.lastIndexOf(separator) && tableName.endsWith(changeStoreSuffix),
        "illegal table name: %s, %s is not allowed in table name.",
        tableName, MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR);

    return true;
  }

  /**
   * get the real table location.
   *
   * @param catalog     - internal server catalog to create the table.
   * @param database    - database of table to be created
   * @param tableName   - tableName of table to be created.
   * @param location    - the requested location.
   * @param changeStore - is this table is a change store
   * @return - the real table location.
   */
  public static String tableLocation(
      InternalCatalog catalog, String database, String tableName, String location, boolean changeStore) {
    if (StringUtils.isBlank(location)) {
      String warehouse = catalog.getMetadata().getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);
      Preconditions.checkState(
          StringUtils.isNotBlank(warehouse),
          "catalog warehouse is not configured");
      warehouse = LocationUtil.stripTrailingSlash(warehouse);
      if (!changeStore) {
        location = warehouse + "/" + database + "/" + tableName;
      } else {
        String realTableName = internalTableName(tableName);
        location = warehouse + "/" + database + "/" + realTableName + "/change";
      }
    } else {
      location = LocationUtil.stripTrailingSlash(location);
    }
    return location;
  }

  public static void validateTableNameForCreating(
      InternalCatalog catalog, String database, String tableName, boolean changeStore
  ) {
    if (!changeStore) {
      if (catalog.exist(database, tableName)) {
        throw new AlreadyExistsException("Table: " + tableName + " already exists.");
      }
    } else {
      String internalTableName = internalTableName(tableName);
      if (!catalog.exist(database, internalTableName)) {
        throw new NotFoundException("You are creating the change store of table: " + internalTableName +
            ", but the base store of table " + internalTableName + " is not exists.");
      }
      com.netease.arctic.server.table.TableMetadata internalMetadata =
          catalog.loadTableMetadata(database, internalTableName);
      Preconditions.checkState(
          StringUtils.isEmpty(internalMetadata.getChangeLocation()),
          "The change store of table: " + internalTableName + " has already been created.");
    }
  }

  /**
   * get the internal table name
   *
   * @param tableName requested table name
   * @return internal table name
   */
  public static String internalTableName(String tableName) {
    if (isMatchChangeStoreNamePattern(tableName)) {
      String suffix = MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR
          + MetaTableProperties.MIXED_FORMAT_CHANGE_STORE_SUFFIX;
      return tableName.substring(0, tableName.length() - suffix.length());
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
   *
   * @param io          - iceberg file io
   * @param tableMeta   - table metadata
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
   * @param base    - iceberg table metadata
   * @param current - new iceberg table metadata
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
      TableIdentifier identifier,
      CatalogMeta catalogMeta,
      TableMetadata icebergTableMetadata,
      String metadataFileLocation,
      FileIO io
  ) {
    TableMeta meta = new TableMeta();
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_TABLE, icebergTableMetadata.location());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_BASE, icebergTableMetadata.location());

    Map<String, String> properties = icebergTableMetadata.properties();
    TableFormat format = TableFormat.ICEBERG;
    if (TablePropertyUtil.isBaseStore(properties, TableFormat.MIXED_ICEBERG)) {
      format = TableFormat.MIXED_ICEBERG;
      PrimaryKeySpec keySpec = PrimaryKeySpec.parse(icebergTableMetadata.schema(), properties);
      if (keySpec.primaryKeyExisted()) {
        TableIdentifier changeIdentifier = TablePropertyUtil.parseChangeIdentifier(properties);
        String expectChangeStoreName = identifier.name() + MetaTableProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR +
            MetaTableProperties.MIXED_FORMAT_CHANGE_STORE_SUFFIX;
        TableIdentifier expectChangeIdentifier = TableIdentifier.of(identifier.namespace(), expectChangeStoreName);
        Preconditions.checkArgument(
            expectChangeIdentifier.equals(changeIdentifier),
            "the change store identifier is not expected. expected: %s, but found %s",
            expectChangeIdentifier.toString(), changeIdentifier.toString()
        );
        com.netease.arctic.ams.api.PrimaryKeySpec apiKeySpec =
            new com.netease.arctic.ams.api.PrimaryKeySpec();
        apiKeySpec.setFields(keySpec.fieldNames());
        meta.setKeySpec(apiKeySpec);
      }
      meta.putToProperties(MIXED_ICEBERG_BASED_REST, Boolean.toString(true));
    }
    meta.setFormat(format.name());
    meta.putToProperties(PROPERTIES_METADATA_LOCATION, metadataFileLocation);
    ServerTableIdentifier serverTableIdentifier = ServerTableIdentifier.of(
        catalogMeta.getCatalogName(), identifier.namespace().level(0), identifier.name(), format);
    meta.setTableIdentifier(serverTableIdentifier.getIdentifier());
    // write metadata file.
    OutputFile outputFile = io.newOutputFile(metadataFileLocation);
    TableMetadataParser.overwrite(icebergTableMetadata, outputFile);
    return new com.netease.arctic.server.table.TableMetadata(serverTableIdentifier, meta, catalogMeta);
  }


  public static com.netease.arctic.server.table.TableMetadata upgradeToMixedTableInternal(
      TableIdentifier identifier, CatalogMeta catalogMeta,
      com.netease.arctic.server.table.TableMetadata internalTableMetadata,
      TableMetadata changeTableStoreMetadata, String changeMetadataFileLocation, FileIO io
  ) {
    internalTableMetadata.setChangeLocation(changeTableStoreMetadata.location());
    Map<String, String> properties = internalTableMetadata.getProperties();
    String metadataLocationKey = CHANGE_STORE_PREFIX + PROPERTIES_METADATA_LOCATION;
    properties.put(metadataLocationKey, changeMetadataFileLocation);

    OutputFile outputFile = io.newOutputFile(changeMetadataFileLocation);
    TableMetadataParser.overwrite(changeTableStoreMetadata, outputFile);
    return internalTableMetadata;
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
