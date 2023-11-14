package com.netease.arctic.server.iceberg;

import static com.netease.arctic.server.table.internal.InternalTableConstants.CHANGE_STORE_PREFIX;
import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_METADATA_LOCATION;
import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_PREV_METADATA_LOCATION;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.utils.InternalTableUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.LocationProviders;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class InternalTableStoreOperations extends PersistentBase implements TableOperations {

  private final ServerTableIdentifier identifier;

  private TableMetadata current;
  private final FileIO io;
  private com.netease.arctic.server.table.TableMetadata tableMetadata;
  private final boolean changeStore;

  public InternalTableStoreOperations(
      ServerTableIdentifier identifier,
      com.netease.arctic.server.table.TableMetadata tableMetadata,
      FileIO io,
      boolean changeStore) {
    this.io = io;
    this.tableMetadata = tableMetadata;
    this.identifier = identifier;
    this.changeStore = changeStore;
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
      this.tableMetadata =
          getAs(
              TableMetaMapper.class, mapper -> mapper.selectTableMetaById(this.identifier.getId()));
    }
    if (this.tableMetadata == null) {
      return null;
    }
    this.current = loadTableMetadata(io, this.tableMetadata, this.changeStore);
    return this.current;
  }

  /**
   * load iceberg table metadata by given ams table metadata
   *
   * @param io - iceberg file io
   * @param tableMeta - table metadata
   * @param changeStore - set true to load a change store metadata for mixed-format.
   * @return iceberg table metadata object
   */
  private TableMetadata loadTableMetadata(
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

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    Preconditions.checkArgument(
        base != null, "Invalid table metadata for create transaction, base is null");

    Preconditions.checkArgument(
        metadata != null, "Invalid table metadata for create transaction, new metadata is null");
    if (base != current()) {
      throw new CommitFailedException("Cannot commit: stale table metadata");
    }

    String newMetadataFileLocation = InternalTableUtil.genNewMetadataFileLocation(base, metadata);

    try {
      commitTableInternal(tableMetadata, base, metadata, newMetadataFileLocation, io, changeStore);
      com.netease.arctic.server.table.TableMetadata updatedMetadata = doCommit();
      checkCommitSuccess(updatedMetadata, newMetadataFileLocation, changeStore);
    } catch (Exception e) {
      io.deleteFile(newMetadataFileLocation);
    } finally {
      this.tableMetadata = null;
    }
    refresh();
  }

  @Override
  public FileIO io() {
    return this.io;
  }

  @Override
  public String metadataFileLocation(String fileName) {
    return InternalTableUtil.genMetadataFileLocation(current(), fileName);
  }

  @Override
  public LocationProvider locationProvider() {
    return LocationProviders.locationsFor(current().location(), current().properties());
  }

  @Override
  public TableOperations temp(TableMetadata uncommittedMetadata) {
    return TableOperations.super.temp(uncommittedMetadata);
  }

  private com.netease.arctic.server.table.TableMetadata doCommit() {
    ServerTableIdentifier tableIdentifier = tableMetadata.getTableIdentifier();
    AtomicInteger effectRows = new AtomicInteger();
    AtomicReference<com.netease.arctic.server.table.TableMetadata> metadataRef =
        new AtomicReference<>();
    doAsTransaction(
        () -> {
          int effects =
              getAs(
                  TableMetaMapper.class,
                  mapper -> mapper.commitTableChange(tableIdentifier.getId(), tableMetadata));
          effectRows.set(effects);
        },
        () -> {
          com.netease.arctic.server.table.TableMetadata m =
              getAs(
                  TableMetaMapper.class,
                  mapper -> mapper.selectTableMetaById(tableIdentifier.getId()));
          metadataRef.set(m);
        });
    if (effectRows.get() == 0) {
      throw new CommitFailedException(
          "commit failed for version: " + tableMetadata.getMetaVersion() + " has been committed");
    }
    return metadataRef.get();
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
  private void commitTableInternal(
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

  public void checkCommitSuccess(
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
