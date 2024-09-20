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

package org.apache.iceberg.parquet;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.FieldMetrics;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.MetricsModes;
import org.apache.iceberg.MetricsUtil;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Literal;
import org.apache.iceberg.mapping.NameMapping;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.BinaryUtil;
import org.apache.iceberg.util.UnicodeUtil;
import org.apache.parquet.column.statistics.Statistics;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.ColumnPath;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Copy from {@link ParquetUtil} to resolve int96 in metric. */
public class AdaptHiveParquetUtil {
  // not meant to be instantiated
  private AdaptHiveParquetUtil() {}

  public static Metrics footerMetrics(
      ParquetMetadata metadata,
      Stream<FieldMetrics<?>> fieldMetrics,
      MetricsConfig metricsConfig,
      Schema schema) {
    return footerMetrics(metadata, fieldMetrics, metricsConfig, null, schema);
  }

  @SuppressWarnings("checkstyle:CyclomaticComplexity")
  public static Metrics footerMetrics(
      ParquetMetadata metadata,
      Stream<FieldMetrics<?>> fieldMetrics,
      MetricsConfig metricsConfig,
      NameMapping nameMapping,
      Schema schema) {
    Preconditions.checkNotNull(fieldMetrics, "fieldMetrics should not be null");

    long rowCount = 0;
    Map<Integer, Long> columnSizes = Maps.newHashMap();
    Map<Integer, Long> valueCounts = Maps.newHashMap();
    Map<Integer, Long> nullValueCounts = Maps.newHashMap();
    Map<Integer, Literal<?>> lowerBounds = Maps.newHashMap();
    Map<Integer, Literal<?>> upperBounds = Maps.newHashMap();
    Set<Integer> missingStats = Sets.newHashSet();

    // ignore metrics for fields we failed to determine reliable IDs
    MessageType parquetTypeWithIds = getParquetTypeWithIds(metadata, nameMapping);
    Schema fileSchema = ParquetSchemaUtil.convertAndPrune(parquetTypeWithIds);
    fileSchema = refineTimestampType(fileSchema, schema);

    Map<Integer, FieldMetrics<?>> fieldMetricsMap =
        fieldMetrics.collect(Collectors.toMap(FieldMetrics::id, Function.identity()));

    List<BlockMetaData> blocks = metadata.getBlocks();
    for (BlockMetaData block : blocks) {
      rowCount += block.getRowCount();
      for (ColumnChunkMetaData column : block.getColumns()) {

        Integer fieldId = fileSchema.aliasToId(column.getPath().toDotString());
        if (fieldId == null) {
          // fileSchema may contain a subset of columns present in the file
          // as we prune columns we could not assign ids
          continue;
        }

        increment(columnSizes, fieldId, column.getTotalSize());

        MetricsModes.MetricsMode metricsMode =
            MetricsUtil.metricsMode(fileSchema, metricsConfig, fieldId);
        if (metricsMode == MetricsModes.None.get()) {
          continue;
        }
        increment(valueCounts, fieldId, column.getValueCount());

        Statistics stats = column.getStatistics();
        if (stats == null) {
          missingStats.add(fieldId);
        } else if (!stats.isEmpty()) {
          increment(nullValueCounts, fieldId, stats.getNumNulls());

          // when there are metrics gathered by Iceberg for a column, we should use those instead
          // of the ones from Parquet
          if (metricsMode != MetricsModes.Counts.get() && !fieldMetricsMap.containsKey(fieldId)) {
            Types.NestedField field = fileSchema.findField(fieldId);
            if (field != null && stats.hasNonNullValue() && shouldStoreBounds(column, fileSchema)) {
              // Change for mixed-hive table ⬇
              // Add metrics for int96 type
              Literal<?> min =
                  AdaptHiveParquetConversions.fromParquetPrimitive(
                      field.type(), column.getPrimitiveType(), stats.genericGetMin());
              updateMin(lowerBounds, fieldId, field.type(), min, metricsMode);
              Literal<?> max =
                  AdaptHiveParquetConversions.fromParquetPrimitive(
                      field.type(), column.getPrimitiveType(), stats.genericGetMax());
              updateMax(upperBounds, fieldId, field.type(), max, metricsMode);
              // Change for mixed-hive table ⬆
            }
          }
        }
      }
    }

    // discard accumulated values if any stats were missing
    for (Integer fieldId : missingStats) {
      nullValueCounts.remove(fieldId);
      lowerBounds.remove(fieldId);
      upperBounds.remove(fieldId);
    }

    updateFromFieldMetrics(fieldMetricsMap, metricsConfig, fileSchema, lowerBounds, upperBounds);

    return new Metrics(
        rowCount,
        columnSizes,
        valueCounts,
        nullValueCounts,
        MetricsUtil.createNanValueCounts(
            fieldMetricsMap.values().stream(), metricsConfig, fileSchema),
        toBufferMap(fileSchema, lowerBounds),
        toBufferMap(fileSchema, upperBounds));
  }

  private static Schema refineTimestampType(Schema schema, Schema original) {
    List<Types.NestedField> columns = schema.columns();
    List<Types.NestedField> result = new ArrayList<>();
    for (Types.NestedField nestedField : columns) {
      if (nestedField.type().typeId() == Type.TypeID.TIMESTAMP) {
        result.add(
            Types.NestedField.of(
                nestedField.fieldId(),
                nestedField.isOptional(),
                nestedField.name(),
                original.findType(nestedField.fieldId()),
                nestedField.doc()));
      } else {
        result.add(nestedField);
      }
    }
    return new Schema(result, schema.getAliases(), schema.identifierFieldIds());
  }

  private static void updateFromFieldMetrics(
      Map<Integer, FieldMetrics<?>> idToFieldMetricsMap,
      MetricsConfig metricsConfig,
      Schema schema,
      Map<Integer, Literal<?>> lowerBounds,
      Map<Integer, Literal<?>> upperBounds) {
    idToFieldMetricsMap
        .entrySet()
        .forEach(
            entry -> {
              int fieldId = entry.getKey();
              FieldMetrics<?> metrics = entry.getValue();
              MetricsModes.MetricsMode metricsMode =
                  MetricsUtil.metricsMode(schema, metricsConfig, fieldId);

              // only check for MetricsModes.None, since we don't truncate float/double values.
              if (metricsMode != MetricsModes.None.get()) {
                if (!metrics.hasBounds()) {
                  lowerBounds.remove(fieldId);
                  upperBounds.remove(fieldId);
                } else if (metrics.upperBound() instanceof Float) {
                  lowerBounds.put(fieldId, Literal.of((Float) metrics.lowerBound()));
                  upperBounds.put(fieldId, Literal.of((Float) metrics.upperBound()));
                } else if (metrics.upperBound() instanceof Double) {
                  lowerBounds.put(fieldId, Literal.of((Double) metrics.lowerBound()));
                  upperBounds.put(fieldId, Literal.of((Double) metrics.upperBound()));
                } else {
                  throw new UnsupportedOperationException(
                      "Expected only float or double column metrics");
                }
              }
            });
  }

  private static MessageType getParquetTypeWithIds(
      ParquetMetadata metadata, NameMapping nameMapping) {
    MessageType type = metadata.getFileMetaData().getSchema();

    if (ParquetSchemaUtil.hasIds(type)) {
      return type;
    }

    if (nameMapping != null) {
      return ParquetSchemaUtil.applyNameMapping(type, nameMapping);
    }

    return ParquetSchemaUtil.addFallbackIds(type);
  }

  // we allow struct nesting, but not maps or arrays
  private static boolean shouldStoreBounds(ColumnChunkMetaData column, Schema schema) {
    // Add metrics for int96 type
    ColumnPath columnPath = column.getPath();
    Iterator<String> pathIterator = columnPath.iterator();
    Type currentType = schema.asStruct();

    while (pathIterator.hasNext()) {
      if (currentType == null || !currentType.isStructType()) {
        return false;
      }
      String fieldName = pathIterator.next();
      currentType = currentType.asStructType().fieldType(fieldName);
    }

    return currentType != null && currentType.isPrimitiveType();
  }

  private static void increment(Map<Integer, Long> columns, int fieldId, long amount) {
    if (columns != null) {
      if (columns.containsKey(fieldId)) {
        columns.put(fieldId, columns.get(fieldId) + amount);
      } else {
        columns.put(fieldId, amount);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> void updateMin(
      Map<Integer, Literal<?>> lowerBounds,
      int id,
      Type type,
      Literal<T> min,
      MetricsModes.MetricsMode metricsMode) {
    Literal<T> currentMin = (Literal<T>) lowerBounds.get(id);
    if (currentMin == null || min.comparator().compare(min.value(), currentMin.value()) < 0) {
      if (metricsMode == MetricsModes.Full.get()) {
        lowerBounds.put(id, min);
      } else {
        MetricsModes.Truncate truncateMode = (MetricsModes.Truncate) metricsMode;
        int truncateLength = truncateMode.length();
        switch (type.typeId()) {
          case STRING:
            lowerBounds.put(
                id, UnicodeUtil.truncateStringMin((Literal<CharSequence>) min, truncateLength));
            break;
          case FIXED:
          case BINARY:
            lowerBounds.put(
                id, BinaryUtil.truncateBinaryMin((Literal<ByteBuffer>) min, truncateLength));
            break;
          default:
            lowerBounds.put(id, min);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> void updateMax(
      Map<Integer, Literal<?>> upperBounds,
      int id,
      Type type,
      Literal<T> max,
      MetricsModes.MetricsMode metricsMode) {
    Literal<T> currentMax = (Literal<T>) upperBounds.get(id);
    if (currentMax == null || max.comparator().compare(max.value(), currentMax.value()) > 0) {
      if (metricsMode == MetricsModes.Full.get()) {
        upperBounds.put(id, max);
      } else {
        MetricsModes.Truncate truncateMode = (MetricsModes.Truncate) metricsMode;
        int truncateLength = truncateMode.length();
        switch (type.typeId()) {
          case STRING:
            Literal<CharSequence> truncatedMaxString =
                UnicodeUtil.truncateStringMax((Literal<CharSequence>) max, truncateLength);
            if (truncatedMaxString != null) {
              upperBounds.put(id, truncatedMaxString);
            }
            break;
          case FIXED:
          case BINARY:
            Literal<ByteBuffer> truncatedMaxBinary =
                BinaryUtil.truncateBinaryMax((Literal<ByteBuffer>) max, truncateLength);
            if (truncatedMaxBinary != null) {
              upperBounds.put(id, truncatedMaxBinary);
            }
            break;
          default:
            upperBounds.put(id, max);
        }
      }
    }
  }

  private static Map<Integer, ByteBuffer> toBufferMap(Schema schema, Map<Integer, Literal<?>> map) {
    Map<Integer, ByteBuffer> bufferMap = Maps.newHashMap();
    for (Map.Entry<Integer, Literal<?>> entry : map.entrySet()) {
      bufferMap.put(
          entry.getKey(),
          Conversions.toByteBuffer(schema.findType(entry.getKey()), entry.getValue().value()));
    }
    return bufferMap;
  }
}
