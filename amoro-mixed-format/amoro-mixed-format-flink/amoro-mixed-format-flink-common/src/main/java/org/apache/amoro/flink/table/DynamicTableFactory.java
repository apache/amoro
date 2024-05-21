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

package org.apache.amoro.flink.table;

import static org.apache.amoro.table.TableProperties.ENABLE_LOG_STORE;
import static org.apache.amoro.table.TableProperties.ENABLE_LOG_STORE_DEFAULT;
import static org.apache.flink.api.common.RuntimeExecutionMode.BATCH;
import static org.apache.flink.configuration.ExecutionOptions.RUNTIME_MODE;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.PROPS_BOOTSTRAP_SERVERS;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.PROPS_GROUP_ID;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SCAN_STARTUP_MODE;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SCAN_STARTUP_TIMESTAMP_MILLIS;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SCAN_TOPIC_PARTITION_DISCOVERY;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SINK_PARTITIONER;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.TOPIC;

import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.catalog.MixedCatalog;
import org.apache.amoro.flink.catalog.factories.CatalogFactoryOptions;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.util.CompatibleFlinkPropertyUtil;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.connector.kafka.source.KafkaSourceOptions;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.factories.DynamicTableSinkFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.utils.TableSchemaUtils;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/** A factory generates {@link MixedFormatDynamicSource} and {@link MixedFormatDynamicSink} */
public class DynamicTableFactory implements DynamicTableSourceFactory, DynamicTableSinkFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DynamicTableFactory.class);
  public static final String IDENTIFIER = "mixed-format";
  private InternalCatalogBuilder internalCatalogBuilder;
  private String internalCatalogName;

  public DynamicTableFactory(MixedCatalog mixedCatalog) {
    this.internalCatalogBuilder = mixedCatalog.catalogBuilder();
    this.internalCatalogName = mixedCatalog.amsCatalogName();
  }

  public DynamicTableFactory() {}

  @Override
  public DynamicTableSource createDynamicTableSource(Context context) {
    CatalogTable catalogTable = context.getCatalogTable();
    ObjectIdentifier identifier = context.getObjectIdentifier();
    ObjectPath objectPath;

    FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
    Configuration options = (Configuration) helper.getOptions();

    InternalCatalogBuilder actualBuilder = internalCatalogBuilder;
    String actualCatalogName = internalCatalogName;

    // It denotes create table by ddl 'connector' option, not through catalog.db.tableName
    if (actualBuilder == null || actualCatalogName == null) {
      String metastoreUrl = options.get(CatalogFactoryOptions.METASTORE_URL);
      Preconditions.checkNotNull(
          metastoreUrl, String.format("%s should be set", CatalogFactoryOptions.METASTORE_URL));
      actualBuilder = InternalCatalogBuilder.builder().metastoreUrl(metastoreUrl);

      actualCatalogName = options.get(MixedFormatValidator.MIXED_FORMAT_CATALOG);
      Preconditions.checkNotNull(
          actualCatalogName,
          String.format("%s should be set", MixedFormatValidator.MIXED_FORMAT_CATALOG.key()));
    }

    if (options.containsKey(MixedFormatValidator.MIXED_FORMAT_DATABASE.key())
        && options.containsKey(MixedFormatValidator.MIXED_FORMAT_TABLE.key())) {
      objectPath =
          new ObjectPath(
              options.get(MixedFormatValidator.MIXED_FORMAT_DATABASE),
              options.get(MixedFormatValidator.MIXED_FORMAT_TABLE));
    } else {
      objectPath = new ObjectPath(identifier.getDatabaseName(), identifier.getObjectName());
    }
    MixedFormatTableLoader tableLoader =
        createTableLoader(objectPath, actualCatalogName, actualBuilder, options.toMap());
    MixedTable mixedTable = MixedFormatUtils.loadMixedTable(tableLoader);

    Configuration confWithAll = Configuration.fromMap(mixedTable.properties());

    ScanTableSource mixedFormatDynamicSource;

    String readMode =
        PropertyUtil.propertyAsString(
            mixedTable.properties(),
            MixedFormatValidator.MIXED_FORMAT_READ_MODE,
            MixedFormatValidator.MIXED_READ_MODE_DEFAULT);

    boolean dimTable =
        CompatibleFlinkPropertyUtil.propertyAsBoolean(
            mixedTable.properties(),
            MixedFormatValidator.DIM_TABLE_ENABLE.key(),
            MixedFormatValidator.DIM_TABLE_ENABLE.defaultValue());

    TableSchema tableSchema;
    if (!dimTable) {
      tableSchema =
          org.apache.amoro.flink.FlinkSchemaUtil.getPhysicalSchema(catalogTable.getSchema());
    } else {
      tableSchema =
          org.apache.amoro.flink.FlinkSchemaUtil.getPhysicalSchemaForDimTable(
              catalogTable.getSchema());
    }

    switch (readMode) {
      case MixedFormatValidator.MIXED_FORMAT_READ_FILE:
        boolean batchMode = context.getConfiguration().get(RUNTIME_MODE).equals(BATCH);
        LOG.info("Building a file reader in {} runtime mode", batchMode ? "batch" : "streaming");
        mixedFormatDynamicSource =
            new MixedFormatFileSource(tableLoader, tableSchema, mixedTable, confWithAll, batchMode);
        break;
      case MixedFormatValidator.MIXED_FORMAT_READ_LOG:
      default:
        Preconditions.checkArgument(
            CompatiblePropertyUtil.propertyAsBoolean(
                mixedTable.properties(), ENABLE_LOG_STORE, ENABLE_LOG_STORE_DEFAULT),
            String.format("Read log should enable %s at first", ENABLE_LOG_STORE));
        mixedFormatDynamicSource = createLogSource(mixedTable, context, confWithAll);
    }

    return generateDynamicTableSource(
        identifier.getObjectName(), mixedFormatDynamicSource, mixedTable, tableLoader);
  }

  protected DynamicTableSource generateDynamicTableSource(
      String tableName,
      ScanTableSource mixedFormatDynamicSource,
      MixedTable mixedTable,
      MixedFormatTableLoader tableLoader) {
    return new MixedFormatDynamicSource(
        tableName, mixedFormatDynamicSource, mixedTable, mixedTable.properties(), tableLoader);
  }

  @Override
  public MixedFormatDynamicSink createDynamicTableSink(Context context) {
    CatalogTable catalogTable = context.getCatalogTable();

    ObjectIdentifier identifier = context.getObjectIdentifier();
    Map<String, String> options = catalogTable.getOptions();

    MixedFormatTableLoader tableLoader =
        createTableLoader(
            new ObjectPath(identifier.getDatabaseName(), identifier.getObjectName()),
            internalCatalogName,
            internalCatalogBuilder,
            options);

    MixedTable table = MixedFormatUtils.loadMixedTable(tableLoader);
    return new MixedFormatDynamicSink(catalogTable, tableLoader, table.isKeyedTable());
  }

  private static MixedFormatTableLoader createTableLoader(
      ObjectPath tablePath,
      String internalCatalogName,
      InternalCatalogBuilder catalogBuilder,
      Map<String, String> flinkTableProperties) {
    TableIdentifier identifier =
        TableIdentifier.of(
            internalCatalogName, tablePath.getDatabaseName(), tablePath.getObjectName());

    return MixedFormatTableLoader.of(identifier, catalogBuilder, flinkTableProperties);
  }

  @Override
  public String factoryIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return new HashSet<>();
  }

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    final Set<ConfigOption<?>> options = new HashSet<>();
    options.add(TOPIC);
    options.add(PROPS_BOOTSTRAP_SERVERS);
    options.add(PROPS_GROUP_ID);
    options.add(SCAN_STARTUP_MODE);
    options.add(SCAN_STARTUP_TIMESTAMP_MILLIS);
    options.add(SINK_PARTITIONER);
    options.add(MixedFormatValidator.MIXED_FORMAT_CATALOG);
    options.add(MixedFormatValidator.MIXED_FORMAT_TABLE);
    options.add(MixedFormatValidator.MIXED_FORMAT_DATABASE);
    options.add(MixedFormatValidator.DIM_TABLE_ENABLE);
    options.add(CatalogFactoryOptions.METASTORE_URL);

    // lookup
    options.add(MixedFormatValidator.LOOKUP_CACHE_MAX_ROWS);
    options.add(MixedFormatValidator.LOOKUP_RELOADING_INTERVAL);
    options.add(MixedFormatValidator.LOOKUP_CACHE_TTL_AFTER_WRITE);

    options.add(MixedFormatValidator.ROCKSDB_AUTO_COMPACTIONS);
    options.add(MixedFormatValidator.ROCKSDB_WRITING_THREADS);
    options.add(MixedFormatValidator.ROCKSDB_BLOCK_CACHE_CAPACITY);
    options.add(MixedFormatValidator.ROCKSDB_BLOCK_CACHE_NUM_SHARD_BITS);
    return options;
  }

  private ScanTableSource createLogSource(
      MixedTable mixedTable, Context context, ReadableConfig tableOptions) {
    CatalogTable catalogTable = context.getCatalogTable();
    TableSchema physicalSchema = TableSchemaUtils.getPhysicalSchema(catalogTable.getSchema());
    Schema schema = FlinkSchemaUtil.convert(physicalSchema);

    final Properties properties =
        KafkaConnectorOptionsUtil.getKafkaProperties(mixedTable.properties());

    // add topic-partition discovery
    final Optional<Long> partitionDiscoveryInterval =
        tableOptions.getOptional(SCAN_TOPIC_PARTITION_DISCOVERY).map(Duration::toMillis);
    properties.setProperty(
        KafkaSourceOptions.PARTITION_DISCOVERY_INTERVAL_MS.key(),
        partitionDiscoveryInterval.orElse(-1L).toString());

    LOG.info("build log source");
    return new LogDynamicSource(properties, schema, tableOptions, mixedTable);
  }
}
