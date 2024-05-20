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

package org.apache.amoro.flink.util;

import static org.apache.amoro.table.TableProperties.ENABLE_LOG_STORE;
import static org.apache.amoro.table.TableProperties.LOG_STORE_ADDRESS;
import static org.apache.amoro.table.TableProperties.LOG_STORE_DATA_VERSION;
import static org.apache.amoro.table.TableProperties.LOG_STORE_DATA_VERSION_DEFAULT;
import static org.apache.amoro.table.TableProperties.LOG_STORE_MESSAGE_TOPIC;
import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_DEFAULT;
import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_KAFKA;
import static org.apache.amoro.table.TableProperties.LOG_STORE_TYPE;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

import org.apache.amoro.flink.metric.MetricsGenerator;
import org.apache.amoro.flink.shuffle.LogRecordV1;
import org.apache.amoro.flink.shuffle.ShuffleHelper;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.write.AutomaticLogWriter;
import org.apache.amoro.flink.write.MixedFormatLogWriter;
import org.apache.amoro.flink.write.hidden.HiddenLogWriter;
import org.apache.amoro.flink.write.hidden.kafka.HiddenKafkaFactory;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.IdGenerator;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/** An util that loads mixed-format table, build mixed-format log writer and so on. */
public class MixedFormatUtils {

  public static final Logger LOG = LoggerFactory.getLogger(MixedFormatUtils.class);

  public static MixedTable loadMixedTable(MixedFormatTableLoader tableLoader) {
    tableLoader.open();
    MixedTable table = tableLoader.loadMixedFormatTable();
    try {
      tableLoader.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return table;
  }

  public static List<String> getPrimaryKeys(MixedTable table) {
    if (table.isUnkeyedTable()) {
      return Collections.emptyList();
    }
    return table.asKeyedTable().primaryKeySpec().fields().stream()
        .map(PrimaryKeySpec.PrimaryKeyField::fieldName)
        .collect(Collectors.toList());
  }

  public static MetricsGenerator getMetricsGenerator(
      boolean metricsEventLatency,
      boolean metricsEnable,
      MixedTable mixedTable,
      RowType flinkSchemaRowType,
      Schema writeSchema) {
    MetricsGenerator metricsGenerator;
    if (metricsEventLatency) {
      String modifyTimeColumn = mixedTable.properties().get(TableProperties.TABLE_EVENT_TIME_FIELD);
      metricsGenerator =
          MetricsGenerator.newGenerator(
              mixedTable.schema(), flinkSchemaRowType, modifyTimeColumn, metricsEnable);
    } else {
      metricsGenerator = MetricsGenerator.empty(metricsEnable);
    }
    return metricsGenerator;
  }

  public static boolean mixedFormatWALWriterEnable(
      Map<String, String> properties, String emitMode) {
    boolean streamEnable =
        CompatiblePropertyUtil.propertyAsBoolean(
            properties, ENABLE_LOG_STORE, TableProperties.ENABLE_LOG_STORE_DEFAULT);

    if (emitMode.contains(MixedFormatValidator.MIXED_FORMAT_EMIT_LOG)) {
      if (!streamEnable) {
        throw new ValidationException(
            "emit to kafka was set, but no kafka config be found, please set kafka config first");
      }
      return true;
    } else if (emitMode.equals(MixedFormatValidator.MIXED_FORMAT_EMIT_AUTO)) {
      LOG.info(
          "mixed-format emit mode is auto, and the mixed-format table {} is {}",
          ENABLE_LOG_STORE,
          streamEnable);
      return streamEnable;
    }

    return false;
  }

  /**
   * only when {@link MixedFormatValidator#MIXED_FORMAT_EMIT_MODE} contains {@link
   * MixedFormatValidator#MIXED_FORMAT_EMIT_FILE} and enable {@link
   * TableProperties#ENABLE_LOG_STORE} create logWriter according to {@link
   * TableProperties#LOG_STORE_DATA_VERSION}
   *
   * @param properties mixed-format table properties
   * @param producerConfig
   * @param topic
   * @param tableSchema
   * @param tableLoader mixed-format table loader
   * @param watermarkWriteGap watermark gap that triggers automatic writing to log storage
   * @return mixed-formatLogWriter
   */
  public static MixedFormatLogWriter buildLogWriter(
      Map<String, String> properties,
      @Nullable Properties producerConfig,
      @Nullable String topic,
      TableSchema tableSchema,
      String emitMode,
      ShuffleHelper helper,
      MixedFormatTableLoader tableLoader,
      Duration watermarkWriteGap) {
    if (!mixedFormatWALWriterEnable(properties, emitMode)) {
      return null;
    }

    if (topic == null) {
      topic =
          CompatibleFlinkPropertyUtil.propertyAsString(properties, LOG_STORE_MESSAGE_TOPIC, null);
    }
    Preconditions.checkNotNull(
        topic,
        String.format("Topic should be specified. It can be set by '%s'", LOG_STORE_MESSAGE_TOPIC));

    producerConfig = combineTableAndUnderlyingLogstoreProperties(properties, producerConfig);

    String version =
        properties.getOrDefault(LOG_STORE_DATA_VERSION, LOG_STORE_DATA_VERSION_DEFAULT);
    if (LOG_STORE_DATA_VERSION_DEFAULT.equals(version)) {
      if (emitMode.equals(MixedFormatValidator.MIXED_FORMAT_EMIT_AUTO)) {
        LOG.info(
            "mixed-format emit mode is auto, and we will build automatic log writer: AutomaticLogWriter(v1)");
        return new AutomaticLogWriter(
            FlinkSchemaUtil.convert(tableSchema),
            producerConfig,
            topic,
            new HiddenKafkaFactory<>(),
            LogRecordV1.FIELD_GETTER_FACTORY,
            IdGenerator.generateUpstreamId(),
            helper,
            tableLoader,
            watermarkWriteGap);
      }

      LOG.info("build log writer: HiddenLogWriter(v1)");
      return new HiddenLogWriter(
          FlinkSchemaUtil.convert(tableSchema),
          producerConfig,
          topic,
          new HiddenKafkaFactory<>(),
          LogRecordV1.FIELD_GETTER_FACTORY,
          IdGenerator.generateUpstreamId(),
          helper);
    }
    throw new UnsupportedOperationException(
        "don't support log version '" + version + "'. only support 'v1' or empty");
  }

  /**
   * Extract and combine the properties for underlying log store queue.
   *
   * @param tableProperties mixed-format table properties
   * @param producerConfig can be set by java API
   * @return properties with tableProperties and producerConfig which has higher priority.
   */
  private static Properties combineTableAndUnderlyingLogstoreProperties(
      Map<String, String> tableProperties, Properties producerConfig) {
    Properties finalProp;
    Properties underlyingLogStoreProps =
        CompatibleFlinkPropertyUtil.fetchLogstorePrefixProperties(tableProperties);
    if (producerConfig == null) {
      finalProp = underlyingLogStoreProps;
    } else {
      underlyingLogStoreProps
          .stringPropertyNames()
          .forEach(k -> producerConfig.putIfAbsent(k, underlyingLogStoreProps.get(k)));
      finalProp = producerConfig;
    }

    String logStoreAddress =
        CompatibleFlinkPropertyUtil.propertyAsString(tableProperties, LOG_STORE_ADDRESS, null);

    String logType =
        CompatibleFlinkPropertyUtil.propertyAsString(
            tableProperties, LOG_STORE_TYPE, LOG_STORE_STORAGE_TYPE_DEFAULT);
    if (logType.equals(LOG_STORE_STORAGE_TYPE_KAFKA)) {
      finalProp.putIfAbsent(
          "key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
      finalProp.putIfAbsent(
          "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
      finalProp.putIfAbsent(
          "key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
      finalProp.putIfAbsent(
          "value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

      if (logStoreAddress != null) {
        finalProp.putIfAbsent(BOOTSTRAP_SERVERS_CONFIG, logStoreAddress);
      }

      Preconditions.checkArgument(
          finalProp.containsKey(BOOTSTRAP_SERVERS_CONFIG),
          String.format("%s should be set", LOG_STORE_ADDRESS));
    }

    return finalProp;
  }

  public static boolean fileWriterEnable(String emitMode) {
    return emitMode.contains(MixedFormatValidator.MIXED_FORMAT_EMIT_FILE)
        || emitMode.equals(MixedFormatValidator.MIXED_FORMAT_EMIT_AUTO);
  }

  public static boolean isToBase(boolean overwrite) {
    boolean toBase = overwrite;
    LOG.info("is write to base:{}", toBase);
    return toBase;
  }

  public static RowData removeMixedFormatMetaColumn(RowData rowData, int columnSize) {
    GenericRowData newRowData = new GenericRowData(rowData.getRowKind(), columnSize);
    if (rowData instanceof GenericRowData) {
      GenericRowData before = (GenericRowData) rowData;
      for (int i = 0; i < newRowData.getArity(); i++) {
        newRowData.setField(i, before.getField(i));
      }
      return newRowData;
    }
    throw new UnsupportedOperationException(
        String.format(
            "Can't remove mixed-format meta column from this RowData %s",
            rowData.getClass().getSimpleName()));
  }
}
