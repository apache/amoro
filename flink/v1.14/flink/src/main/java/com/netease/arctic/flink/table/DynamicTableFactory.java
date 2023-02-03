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

package com.netease.arctic.flink.table;

import com.netease.arctic.flink.InternalCatalogBuilder;
import com.netease.arctic.flink.catalog.ArcticCatalog;
import com.netease.arctic.flink.table.descriptors.ArcticValidator;
import com.netease.arctic.flink.util.ArcticUtils;
import com.netease.arctic.flink.util.CompatibleFlinkPropertyUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
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
import org.apache.flink.table.types.DataType;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static com.netease.arctic.flink.FlinkSchemaUtil.getPhysicalSchema;
import static com.netease.arctic.flink.catalog.factories.ArcticCatalogFactoryOptions.METASTORE_URL;
import static com.netease.arctic.flink.table.KafkaConnectorOptionsUtil.createValueFormatProjection;
import static com.netease.arctic.flink.table.KafkaConnectorOptionsUtil.getKafkaProperties;
import static com.netease.arctic.flink.table.KafkaConnectorOptionsUtil.getSourceTopicPattern;
import static com.netease.arctic.flink.table.KafkaConnectorOptionsUtil.getSourceTopics;
import static com.netease.arctic.flink.table.KafkaConnectorOptionsUtil.validateSourceTopic;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.SCAN_STARTUP_MODE_TIMESTAMP;
import static com.netease.arctic.table.TableProperties.ENABLE_LOG_STORE;
import static com.netease.arctic.table.TableProperties.ENABLE_LOG_STORE_DEFAULT;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.PROPS_BOOTSTRAP_SERVERS;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.PROPS_GROUP_ID;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SCAN_STARTUP_MODE;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SCAN_STARTUP_TIMESTAMP_MILLIS;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SCAN_TOPIC_PARTITION_DISCOVERY;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.SINK_PARTITIONER;
import static org.apache.flink.streaming.connectors.kafka.table.KafkaConnectorOptions.TOPIC;

/**
 * A factory generates {@link ArcticDynamicSource} and {@link ArcticDynamicSink}
 */
public class DynamicTableFactory implements DynamicTableSourceFactory, DynamicTableSinkFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DynamicTableFactory.class);
  public static final String IDENTIFIER = "arctic";
  private ArcticCatalog arcticCatalog;
  private InternalCatalogBuilder internalCatalogBuilder;
  private String internalCatalogName;

  public DynamicTableFactory(
      ArcticCatalog arcticCatalog,
      InternalCatalogBuilder internalCatalogBuilder,
      String internalCatalogName) {
    this.arcticCatalog = arcticCatalog;
    this.internalCatalogBuilder = internalCatalogBuilder;
    this.internalCatalogName = internalCatalogName;
  }

  public DynamicTableFactory() {
  }

  @Override
  public DynamicTableSource createDynamicTableSource(Context context) {
    CatalogTable catalogTable = context.getCatalogTable();
    ObjectIdentifier identifier = context.getObjectIdentifier();
    ObjectPath objectPath;

    FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
    Configuration options = (Configuration) helper.getOptions();

    InternalCatalogBuilder actualBuilder = internalCatalogBuilder;
    String actualCatalogName = internalCatalogName;

    // It denotes create table by ddl 'connector' option, not through arcticCatalog.db.tableName
    if (actualBuilder == null || actualCatalogName == null) {
      String metastoreUrl = options.get(METASTORE_URL);
      Preconditions.checkNotNull(metastoreUrl, String.format("%s should be set", METASTORE_URL));
      actualBuilder = InternalCatalogBuilder.builder().metastoreUrl(metastoreUrl);

      actualCatalogName = options.get(ArcticValidator.ARCTIC_CATALOG);
      Preconditions.checkNotNull(actualCatalogName, String.format("%s should be set",
          ArcticValidator.ARCTIC_CATALOG.key()));
    }

    if (options.containsKey(ArcticValidator.ARCTIC_DATABASE.key()) &&
        options.containsKey(ArcticValidator.ARCTIC_TABLE.key())) {
      objectPath = new ObjectPath(options.get(ArcticValidator.ARCTIC_DATABASE),
          options.get(ArcticValidator.ARCTIC_TABLE));
    } else {
      objectPath = new ObjectPath(identifier.getDatabaseName(), identifier.getObjectName());
    }

    ArcticTableLoader tableLoader = createTableLoader(objectPath, actualCatalogName, actualBuilder, options.toMap());
    ArcticTable arcticTable = ArcticUtils.loadArcticTable(tableLoader);

    Configuration confWithAll = Configuration.fromMap(arcticTable.properties());

    ScanTableSource arcticDynamicSource;

    String readMode = PropertyUtil.propertyAsString(arcticTable.properties(),
        ArcticValidator.ARCTIC_READ_MODE, ArcticValidator.ARCTIC_READ_MODE_DEFAULT);

    boolean dimTable = CompatibleFlinkPropertyUtil.propertyAsBoolean(arcticTable.properties(),
        ArcticValidator.DIM_TABLE_ENABLE.key(), ArcticValidator.DIM_TABLE_ENABLE.defaultValue());
    TableSchema tableSchema = getPhysicalSchema(catalogTable.getSchema(),
        dimTable);
    switch (readMode) {
      case ArcticValidator.ARCTIC_READ_FILE:
        LOG.info("build file reader");
        arcticDynamicSource = new ArcticFileSource(tableLoader, tableSchema, arcticTable, confWithAll);
        break;
      case ArcticValidator.ARCTIC_READ_LOG:
      default:
        Preconditions.checkArgument(CompatiblePropertyUtil.propertyAsBoolean(arcticTable.properties(),
                ENABLE_LOG_STORE, ENABLE_LOG_STORE_DEFAULT),
            String.format("Read log should enable %s at first", ENABLE_LOG_STORE));
        arcticDynamicSource = createLogSource(arcticTable, context, confWithAll);
    }

    return new ArcticDynamicSource(
        identifier.getObjectName(), arcticDynamicSource, arcticTable, arcticTable.properties());
  }

  @Override
  public ArcticDynamicSink createDynamicTableSink(Context context) {
    CatalogTable catalogTable = context.getCatalogTable();

    ObjectIdentifier identifier = context.getObjectIdentifier();
    Map<String, String> options = catalogTable.getOptions();

    final String topic = options.get(TableProperties.LOG_STORE_MESSAGE_TOPIC);

    ArcticTableLoader tableLoader = createTableLoader(
        new ObjectPath(identifier.getDatabaseName(), identifier.getObjectName()),
        internalCatalogName, internalCatalogBuilder, options);

    ArcticTable table = ArcticUtils.loadArcticTable(tableLoader);
    return new ArcticDynamicSink(
        catalogTable,
        tableLoader,
        topic,
        table.isKeyedTable()
    );
  }

  private static ArcticTableLoader createTableLoader(
      ObjectPath tablePath,
      String internalCatalogName,
      InternalCatalogBuilder catalogBuilder,
      Map<String, String> flinkTableProperties) {
    TableIdentifier identifier = TableIdentifier.of(
        internalCatalogName,
        tablePath.getDatabaseName(),
        tablePath.getObjectName());

    return ArcticTableLoader.of(identifier, catalogBuilder, flinkTableProperties);
  }

  @Override
  public String factoryIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    final Set<ConfigOption<?>> options = new HashSet<>();
    return options;
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
    options.add(ArcticValidator.ARCTIC_CATALOG);
    options.add(ArcticValidator.ARCTIC_TABLE);
    options.add(ArcticValidator.ARCTIC_DATABASE);
    options.add(ArcticValidator.DIM_TABLE_ENABLE);
    options.add(METASTORE_URL);
    return options;
  }

  private LogDynamicSource createLogSource(ArcticTable arcticTable, Context context, ReadableConfig tableOptions) {
    CatalogTable catalogTable = context.getCatalogTable();
    TableSchema physicalSchema = TableSchemaUtils.getPhysicalSchema(catalogTable.getSchema());
    Schema schema = FlinkSchemaUtil.convert(physicalSchema);

    validateSourceTopic(tableOptions);

    final Properties properties = getKafkaProperties(arcticTable.properties());

    // add topic-partition discovery
    final Optional<Long> partitionDiscoveryInterval =
        tableOptions.getOptional(SCAN_TOPIC_PARTITION_DISCOVERY).map(Duration::toMillis);
    properties.setProperty(
        KafkaSourceOptions.PARTITION_DISCOVERY_INTERVAL_MS.key(),
        partitionDiscoveryInterval.orElse(-1L).toString());

    final DataType physicalDataType =
        catalogTable.getSchema().toPhysicalRowDataType();

    final int[] valueProjection = createValueFormatProjection(tableOptions, physicalDataType);

    String startupMode = tableOptions.get(ArcticValidator.SCAN_STARTUP_MODE);
    long startupTimestampMillis = 0L;
    if (Objects.equals(startupMode.toLowerCase(), SCAN_STARTUP_MODE_TIMESTAMP)) {
      startupTimestampMillis =
          Preconditions.checkNotNull(tableOptions.get(ArcticValidator.SCAN_STARTUP_TIMESTAMP_MILLIS),
              String.format("'%s' should be set in '%s' mode",
                  ArcticValidator.SCAN_STARTUP_TIMESTAMP_MILLIS.key(), SCAN_STARTUP_MODE_TIMESTAMP));
    }

    LOG.info("build log source");
    return new LogDynamicSource(valueProjection,
        getSourceTopics(tableOptions),
        getSourceTopicPattern(tableOptions),
        properties,
        startupMode,
        startupTimestampMillis,
        schema,
        tableOptions,
        arcticTable);
  }

}
