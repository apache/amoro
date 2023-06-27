package com.netease.arctic.server.iceberg;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.LocationProviders;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.LocationUtil;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class InternalIcebergTableOperations extends PersistentBase implements TableOperations {
  private static final String METADATA_FOLDER_NAME = "metadata";
  public static final String PROPERTIES_METADATA_LOCATION = "iceberg.metadata.location";
  public static final String PROPERTIES_PREV_METADATA_LOCATION = "iceberg.metadata.prev-location";
  public static final String PROPERTIES_METADATA_VERSION = "iceberg.metadata.version";

  private final CatalogMeta catalogMeta;

  private final ServerTableIdentifier identifier;

  private TableMetadata current;
  private long currentVersion = 0;
  private final FileIO io;
  private com.netease.arctic.server.table.TableMetadata tableMetadata;


  public static InternalIcebergTableOperations buildForCreate(
      CatalogMeta catalogMeta, ServerTableIdentifier identifier, FileIO io) {
    return new InternalIcebergTableOperations(catalogMeta, identifier, io);
  }

  public static InternalIcebergTableOperations buildForLoad(
      CatalogMeta catalogMeta, com.netease.arctic.server.table.TableMetadata tableMetadata, FileIO io
  ) {
    return new InternalIcebergTableOperations(catalogMeta, tableMetadata.getTableIdentifier(), tableMetadata, io);
  }

  public InternalIcebergTableOperations(
      CatalogMeta catalogMeta,
      ServerTableIdentifier identifier,
      FileIO io) {
    this.catalogMeta = catalogMeta;
    this.identifier = identifier;
    this.io = io;
  }

  public InternalIcebergTableOperations(
      CatalogMeta catalogMeta,
      ServerTableIdentifier identifier,
      com.netease.arctic.server.table.TableMetadata tableMetadata,
      FileIO io
  ) {
    this.catalogMeta = catalogMeta;
    this.io = io;
    this.tableMetadata = tableMetadata;
    this.identifier = identifier;
  }


  @Override
  public TableMetadata current() {
    if (this.current == null) {
      this.refresh();
    }
    return this.current;
  }

  @Override
  public TableMetadata refresh() {
    if (this.tableMetadata == null) {
      this.tableMetadata = getAs(TableMetaMapper.class, mapper -> mapper.selectTableMetaById(this.identifier.getId()));
    }
    if (this.tableMetadata == null) {
      return null;
    }
    String metadataFileLocation = this.tableMetadata.getProperties().get(PROPERTIES_METADATA_LOCATION);
    if (StringUtils.isBlank(metadataFileLocation)) {
      return null;
    }
    this.current = TableMetadataParser.read(io, metadataFileLocation);
    this.currentVersion = Integer.parseInt(this.tableMetadata.getProperties().get(PROPERTIES_METADATA_VERSION));
    return this.current;
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    if (base == null) {
      Preconditions.checkArgument(this.tableMetadata == null,
          "Invalid table metadata for create transaction, expected null");
    }
    long newVersion = currentVersion() + 1;
    String newMetadataFileLocation = newMetadataFileLocation(metadata, newVersion);
    OutputFile outputFile = io().newOutputFile(newMetadataFileLocation);
    try {
      TableMetadataParser.overwrite(metadata, outputFile);
      if (base == null) {
        // for create.
        createTable(metadata, newMetadataFileLocation);
      } else {
        commitTable(base, metadata, newMetadataFileLocation, newVersion);
      }
    } catch (Exception e) {
      io.deleteFile(outputFile);
    }
    refresh();
  }

  @Override
  public FileIO io() {
    return this.io;
  }

  @Override
  public String metadataFileLocation(String fileName) {
    return metadataFileLocation(current(), fileName);
  }

  private String metadataFileLocation(TableMetadata metadata, String filename) {
    String metadataLocation = metadata.properties().get(TableProperties.WRITE_METADATA_LOCATION);

    if (metadataLocation != null) {
      return String.format("%s/%s", LocationUtil.stripTrailingSlash(metadataLocation), filename);
    } else {
      return String.format("%s/%s/%s", metadata.location(), METADATA_FOLDER_NAME, filename);
    }
  }

  @Override
  public LocationProvider locationProvider() {
    return LocationProviders.locationsFor(current().location(), current().properties());
  }

  @Override
  public TableOperations temp(TableMetadata uncommittedMetadata) {
    return TableOperations.super.temp(uncommittedMetadata);
  }

  private String newMetadataFileLocation(TableMetadata meta, long newVersion) {
    String codecName =
        meta.property(
            TableProperties.METADATA_COMPRESSION, TableProperties.METADATA_COMPRESSION_DEFAULT);
    String fileExtension = TableMetadataParser.getFileExtension(codecName);
    return metadataFileLocation(
        meta, String.format("v%05d-%s%s", newVersion, UUID.randomUUID(), fileExtension));
  }


  private long currentVersion() {
    return currentVersion;
  }

  private void createTable(TableMetadata metadata, String newMetadataFileLocation) {
    TableMeta meta = new TableMeta();
    meta.setTableIdentifier(identifier.getIdentifier());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_TABLE, metadata.location());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_BASE, metadata.location());

    meta.putToProperties(MetaTableProperties.TABLE_FORMAT, TableFormat.ICEBERG.name());
    meta.putToProperties(PROPERTIES_METADATA_LOCATION, newMetadataFileLocation);
    meta.putToProperties(PROPERTIES_METADATA_VERSION, "1");
    com.netease.arctic.server.table.TableMetadata tableMetadata = new com.netease.arctic.server.table.TableMetadata(
        identifier, meta, catalogMeta);
    doAsTransaction(
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier)),
        () -> doAs(TableMetaMapper.class, mapper -> mapper.insertTableMeta(tableMetadata)),
        () -> doAsExisted(
            CatalogMetaMapper.class,
            mapper -> mapper.incTableCount(1, catalogMeta.getCatalogName()),
            () -> new ObjectNotExistsException(catalogMeta.getCatalogName())),
        () -> doAsExisted(
            TableMetaMapper.class,
            mapper -> mapper.incTableCount(1, identifier.getDatabase()),
            () -> new ObjectNotExistsException(catalogMeta.getCatalogName() + "." + identifier.getDatabase()))
    );
    this.tableMetadata = tableMetadata;
  }

  private void commitTable(
      TableMetadata base, TableMetadata newMetadata, String newMetadataFileLocation, long newVersion) {
    if (!Objects.equals(newMetadata.location(), base.location())) {
      throw new UnsupportedOperationException("SetLocation is not supported.");
    }
    Map<String, String> properties = this.tableMetadata.getProperties();
    properties.put(PROPERTIES_METADATA_VERSION, String.valueOf(newVersion));
    properties.put(PROPERTIES_PREV_METADATA_LOCATION, properties.get(PROPERTIES_METADATA_VERSION));
    properties.put(PROPERTIES_METADATA_LOCATION, newMetadataFileLocation);

    long id = this.tableMetadata.getTableIdentifier().getId();
    doAs(TableMetaMapper.class,
        mapper -> mapper.commitTablePropertiesChange(id, properties, this.tableMetadata.getMetaVersion()));
    com.netease.arctic.server.table.TableMetadata updatedMetadata = getAs(
        TableMetaMapper.class,
        mapper -> mapper.selectTableMetaById(id));
    String metaLocationInDatabase = updatedMetadata.getProperties().get(PROPERTIES_METADATA_LOCATION);
    if (!Objects.equals(metaLocationInDatabase, newMetadataFileLocation)) {
      throw new CommitFailedException(
          "commit conflict, some other commit happened during this commit. ");
    }

    this.tableMetadata = null;
  }
}
