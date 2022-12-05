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

import com.netease.arctic.flink.read.FlinkKafkaConsumer;
import com.netease.arctic.flink.read.LogKafkaConsumer;
import com.netease.arctic.flink.table.descriptors.ArcticValidator;
import com.netease.arctic.flink.util.CompatibleFlinkPropertyUtil;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.streaming.connectors.kafka.config.StartupMode;
import org.apache.flink.streaming.connectors.kafka.internals.KafkaDeserializationSchemaWrapper;
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.netease.arctic.flink.table.descriptors.ArcticValidator.ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.ARCTIC_LOG_CONSUMER_CHANGELOG_MODE;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.LOG_CONSUMER_CHANGELOG_MODE_ALL_KINDS;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.LOG_CONSUMER_CHANGELOG_MODE_APPEND_ONLY;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.SCAN_STARTUP_MODE_EARLIEST;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.SCAN_STARTUP_MODE_LATEST;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.SCAN_STARTUP_MODE_TIMESTAMP;
import static org.apache.flink.table.connector.ChangelogMode.insertOnly;

/**
 * This is a log source table api, create log queue consumer e.g. {@link LogKafkaConsumer}
 */
public class LogDynamicSource extends KafkaDynamicSource {

  private boolean tablePrimaryKeyExisted = false;
  private final Schema schema;
  private final ReadableConfig tableOptions;
  private final String consumerChangelogMode;
  private final boolean logRetractionEnable;

  private static final ChangelogMode ALL_KINDS = ChangelogMode.newBuilder()
      .addContainedKind(RowKind.INSERT)
      .addContainedKind(RowKind.UPDATE_BEFORE)
      .addContainedKind(RowKind.UPDATE_AFTER)
      .addContainedKind(RowKind.DELETE)
      .build();

  LogDynamicSource(
      DataType physicalDataType,
      @Nullable DecodingFormat<DeserializationSchema<RowData>> keyDecodingFormat,
      DecodingFormat<DeserializationSchema<RowData>> valueDecodingFormat,
      int[] keyProjection,
      int[] valueProjection,
      @Nullable String keyPrefix,
      @Nullable List<String> topics,
      @Nullable Pattern topicPattern,
      Properties properties,
      String startupMode,
      long startupTimestampMillis,
      boolean upsertMode,
      boolean tablePrimaryKeyExisted,
      Schema schema,
      ReadableConfig tableOptions,
      String sourceName) {
    this(
        physicalDataType,
        keyDecodingFormat,
        valueDecodingFormat,
        keyProjection,
        valueProjection,
        keyPrefix,
        topics,
        topicPattern,
        properties,
        toInternal(startupMode),
        new HashMap<>(),
        startupTimestampMillis,
        upsertMode,
        tablePrimaryKeyExisted,
        schema,
        tableOptions,
        sourceName
    );
  }

  public static StartupMode toInternal(String startupMode) {
    startupMode = startupMode.toLowerCase();
    switch (startupMode) {
      case SCAN_STARTUP_MODE_LATEST:
        return StartupMode.LATEST;
      case SCAN_STARTUP_MODE_EARLIEST:
        return StartupMode.EARLIEST;
      case SCAN_STARTUP_MODE_TIMESTAMP:
        return StartupMode.TIMESTAMP;
      default:
        throw new ValidationException(String.format(
            "%s only support '%s', '%s'. But input is '%s'", ArcticValidator.SCAN_STARTUP_MODE,
            SCAN_STARTUP_MODE_LATEST, SCAN_STARTUP_MODE_EARLIEST, startupMode));
    }
  }

  LogDynamicSource(
      DataType physicalDataType,
      @Nullable DecodingFormat<DeserializationSchema<RowData>> keyDecodingFormat,
      DecodingFormat<DeserializationSchema<RowData>> valueDecodingFormat,
      int[] keyProjection,
      int[] valueProjection,
      @Nullable String keyPrefix,
      @Nullable List<String> topics,
      @Nullable Pattern topicPattern,
      Properties properties,
      StartupMode startupMode,
      Map<KafkaTopicPartition, Long> specificStartupOffsets,
      long startupTimestampMillis,
      boolean upsertMode,
      boolean tablePrimaryKeyExisted,
      Schema schema,
      ReadableConfig tableOptions,
      String sourceName) {
    super(
        physicalDataType,
        keyDecodingFormat,
        valueDecodingFormat,
        keyProjection,
        valueProjection,
        keyPrefix,
        topics,
        topicPattern,
        properties,
        startupMode,
        specificStartupOffsets,
        startupTimestampMillis,
        upsertMode,
        sourceName
    );
    this.tablePrimaryKeyExisted = tablePrimaryKeyExisted;
    this.schema = schema;
    this.tableOptions = tableOptions;
    this.consumerChangelogMode = tableOptions.get(ARCTIC_LOG_CONSUMER_CHANGELOG_MODE);
    this.logRetractionEnable = CompatibleFlinkPropertyUtil.propertyAsBoolean(tableOptions,
        ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE);
  }

  @Override
  protected FlinkKafkaConsumer<RowData> createKafkaConsumer(
      DeserializationSchema<RowData> keyDeserialization,
      DeserializationSchema<RowData> valueDeserialization,
      TypeInformation<RowData> producedTypeInfo) {

    final FlinkKafkaConsumer<RowData> kafkaConsumer;

    final KafkaDeserializationSchemaWrapper<RowData> deserializationSchemaWrapper =
        new KafkaDeserializationSchemaWrapper<>(valueDeserialization);
    Schema projectedSchema = schema;
    if (projectedFields != null) {
      final List<Types.NestedField> columns = schema.columns();
      projectedSchema = new Schema(Arrays.stream(projectedFields).mapToObj(columns::get).collect(Collectors.toList()));
    }
    if (topics != null) {
      kafkaConsumer =
          new LogKafkaConsumer(
              topics,
              deserializationSchemaWrapper,
              properties,
              projectedSchema,
              tableOptions);
    } else {
      kafkaConsumer =
          new LogKafkaConsumer(
              topicPattern,
              deserializationSchemaWrapper,
              properties,
              projectedSchema,
              tableOptions);
    }

    switch (startupMode) {
      case EARLIEST:
        kafkaConsumer.setStartFromEarliest();
        break;
      case LATEST:
        kafkaConsumer.setStartFromLatest();
        break;
      case GROUP_OFFSETS:
        kafkaConsumer.setStartFromGroupOffsets();
        break;
      case SPECIFIC_OFFSETS:
        kafkaConsumer.setStartFromSpecificOffsets(specificStartupOffsets);
        break;
      case TIMESTAMP:
        kafkaConsumer.setStartFromTimestamp(startupTimestampMillis);
        break;
    }

    kafkaConsumer.setCommitOffsetsOnCheckpoints(properties.getProperty("group.id") != null);

    if (watermarkStrategy != null) {
      kafkaConsumer.assignTimestampsAndWatermarks(watermarkStrategy);
    }
    return kafkaConsumer;
  }

  @Override
  public ChangelogMode getChangelogMode() {
    switch (consumerChangelogMode) {
      case LOG_CONSUMER_CHANGELOG_MODE_APPEND_ONLY:
        if (logRetractionEnable) {
          throw new IllegalArgumentException(
              String.format(
                  "Only %s is false when %s is %s",
                  ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE.key(),
                  ARCTIC_LOG_CONSUMER_CHANGELOG_MODE.key(),
                  LOG_CONSUMER_CHANGELOG_MODE_APPEND_ONLY));
        }
        return insertOnly();
      case LOG_CONSUMER_CHANGELOG_MODE_ALL_KINDS:
        return ALL_KINDS;
      default:
        throw new UnsupportedOperationException(
            String.format(
                "As of now, %s can't support this option %s.",
                ARCTIC_LOG_CONSUMER_CHANGELOG_MODE.key(),
                consumerChangelogMode
            ));
    }
  }

  @Override
  public DynamicTableSource copy() {
    return new LogDynamicSource(
        this.physicalDataType,
        this.keyDecodingFormat,
        this.valueDecodingFormat,
        this.keyProjection,
        this.valueProjection,
        this.keyPrefix,
        this.topics,
        this.topicPattern,
        this.properties,
        this.startupMode,
        this.specificStartupOffsets,
        this.startupTimestampMillis,
        this.upsertMode,
        this.tablePrimaryKeyExisted,
        this.schema,
        tableOptions,
        sourceName);
  }

  @Override
  public String asSummaryString() {
    return "arctic";
  }
}
