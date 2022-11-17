package com.netease.arctic.ams.server.optimize;

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.ArrayUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HADOOP;

public class TestIcebergBase {
  @ClassRule
  public static final TemporaryFolder tempFolder = new TemporaryFolder();
  protected static final String ICEBERG_HADOOP_CATALOG_NAME = "iceberg_hadoop";

  static ArcticCatalog icebergCatalog;
  ArcticTable icebergTable;

  static final String DATABASE = "native_test_db";
  static final String TABLE_NAME = "native_test_tb";
  TableIdentifier tableIdentifier = TableIdentifier.of(DATABASE, TABLE_NAME);

  @BeforeClass
  public static void createIcebergCatalog() throws IOException {
    Map<String, String> storageConfig = new HashMap<>();
    storageConfig.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HDFS);
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, MockArcticMetastoreServer.getHadoopSite());
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, MockArcticMetastoreServer.getHadoopSite());

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
        System.getProperty("user.name"));

    tempFolder.create();
    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, tempFolder.newFolder().getPath());
    catalogProperties.put(CatalogMetaProperties.TABLE_FORMATS, "iceberg");

    CatalogMeta catalogMeta = new CatalogMeta(ICEBERG_HADOOP_CATALOG_NAME, CATALOG_TYPE_HADOOP,
        storageConfig, authConfig, catalogProperties);
    MockArcticMetastoreServer.getInstance().handler().createCatalog(catalogMeta);

    MockArcticMetastoreServer.getInstance().createCatalogIfAbsent(catalogMeta);
    icebergCatalog =
        CatalogLoader.load(MockArcticMetastoreServer.getInstance().getUrl(ICEBERG_HADOOP_CATALOG_NAME));
    icebergCatalog.createDatabase(DATABASE);
  }

  @AfterClass
  public static void clearCatalog() {
    icebergCatalog.dropDatabase(DATABASE);
    tempFolder.delete();
  }

  @Before
  public void initTable() throws Exception {
    CatalogMeta catalogMeta = MockArcticMetastoreServer.getInstance().handler().getCatalog(ICEBERG_HADOOP_CATALOG_NAME);
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HADOOP);
    Catalog nativeIcebergCatalog = org.apache.iceberg.CatalogUtil.buildIcebergCatalog(ICEBERG_HADOOP_CATALOG_NAME,
        catalogProperties, new Configuration());
    Map<String, String> tableProperty = new HashMap<>();
    tableProperty.put(TableProperties.FORMAT_VERSION, "2");
    nativeIcebergCatalog.createTable(tableIdentifier, TableTestBase.TABLE_SCHEMA, PartitionSpec.unpartitioned(), tableProperty);
    icebergTable = icebergCatalog.loadTable(
        com.netease.arctic.table.TableIdentifier.of(ICEBERG_HADOOP_CATALOG_NAME, DATABASE, TABLE_NAME));
  }

  @After
  public void clearTable() throws Exception {
    CatalogMeta catalogMeta = MockArcticMetastoreServer.getInstance().handler().getCatalog(ICEBERG_HADOOP_CATALOG_NAME);
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, catalogMeta.getCatalogType());
    Catalog nativeIcebergCatalog = org.apache.iceberg.CatalogUtil.buildIcebergCatalog(ICEBERG_HADOOP_CATALOG_NAME,
        catalogProperties, new Configuration());
    nativeIcebergCatalog.dropTable(tableIdentifier);
  }

  protected List<DataFile> insertDataFiles(UnkeyedTable arcticTable, int length) throws IOException {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile();

    long smallSizeByBytes = PropertyUtil.propertyAsLong(arcticTable.properties(),
        com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
        com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT);
    List<DataFile> result = new ArrayList<>();
    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.PARQUET, null);

    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length, arcticTable.schema())) {
        if (writer.length() > smallSizeByBytes || result.size() > 0) {
          writer.close();
          result.add(writer.toDataFile());
          EncryptedOutputFile newOutputFile = outputFileFactory.newOutputFile();
          writer = appenderFactory
              .newDataWriter(newOutputFile, FileFormat.PARQUET, null);
        }
        writer.write(record);
      }
    }
    writer.close();
    result.add(writer.toDataFile());

    AppendFiles baseAppend = arcticTable.newAppend();
    result.forEach(baseAppend::appendFile);
    baseAppend.commit();

    return result;
  }

  protected void insertEqDeleteFiles(UnkeyedTable arcticTable, int length) throws IOException {
    Record tempRecord = baseRecords(0, 1, arcticTable.schema()).get(0);
    PartitionKey partitionKey = new PartitionKey(arcticTable.spec(), arcticTable.schema());
    partitionKey.partition(tempRecord);
    List<Integer> equalityFieldIds = Lists.newArrayList(arcticTable.schema().findField("id").fieldId());
    Schema eqDeleteRowSchema = arcticTable.schema().select("id");
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec(),
            ArrayUtil.toIntArray(equalityFieldIds), eqDeleteRowSchema, null);
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(arcticTable.spec(), partitionKey);

    List<DeleteFile> result = new ArrayList<>();
    EqualityDeleteWriter<Record> writer = appenderFactory
        .newEqDeleteWriter(outputFile, FileFormat.PARQUET, partitionKey);

    for (int i = 1; i < length * 10; i = i + length) {
      List<Record> records = baseRecords(i, length, arcticTable.schema());
      for (int j = 0; j < records.size(); j++) {
        if (j % 2 == 0) {
          writer.write(records.get(j));
        }
      }
    }
    writer.close();
    result.add(writer.toDeleteFile());

    RowDelta rowDelta = arcticTable.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  protected void insertPosDeleteFiles(UnkeyedTable arcticTable, List<DataFile> dataFiles) throws IOException {
    Record tempRecord = baseRecords(0, 1, arcticTable.schema()).get(0);
    PartitionKey partitionKey = new PartitionKey(arcticTable.spec(), arcticTable.schema());
    partitionKey.partition(tempRecord);
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(arcticTable.spec(), partitionKey);

    List<DeleteFile> result = new ArrayList<>();
    PositionDeleteWriter<Record> writer = appenderFactory
        .newPosDeleteWriter(outputFile, FileFormat.PARQUET, partitionKey);
    for (int i = 0; i < dataFiles.size(); i++) {
      DataFile dataFile = dataFiles.get(i);
      if (i % 2 == 0) {
        PositionDelete<Record> positionDelete = PositionDelete.create();
        positionDelete.set(dataFile.path().toString(), 0L, null);
        writer.write(positionDelete);
      }
    }
    writer.close();
    result.add(writer.toDeleteFile());

    RowDelta rowDelta = arcticTable.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  protected List<DataFile> insertOptimizeTargetDataFiles(UnkeyedTable arcticTable, int length) throws IOException {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile();
    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.PARQUET, null);

    List<DataFile> result = new ArrayList<>();
    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length, arcticTable.schema())) {
        writer.write(record);
      }
    }
    writer.close();
    result.add(writer.toDataFile());
    return result;
  }

  public List<Record> baseRecords(int start, int length, Schema tableSchema) {
    GenericRecord record = GenericRecord.create(tableSchema);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = start; i < start + length; i++) {
      builder.add(record.copy(ImmutableMap.of("id", i, "name", "name",
          "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0))));
    }

    return builder.build();
  }
}
