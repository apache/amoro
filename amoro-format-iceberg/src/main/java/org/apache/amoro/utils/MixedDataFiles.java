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

package org.apache.amoro.utils;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.expressions.Literal;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MixedDataFiles {
  public static final LocalDateTime EPOCH = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
  private static final DateTimeFormatter FORMAT_HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
  private static final int EPOCH_YEAR = EPOCH.getYear();
  private static final String HIVE_NULL = "__HIVE_DEFAULT_PARTITION__";
  private static final String MONTH_TYPE = "month";
  private static final String HOUR_TYPE = "hour";

  /** return the number of months away from the epoch, reverse {@link TransformUtil#humanMonth} */
  public static Integer readMonthData(String dateStr) {
    String[] dateParts = dateStr.split("-", -1);
    int year = Integer.parseInt(dateParts[0]);
    int month = Integer.parseInt(dateParts[1]);
    return Math.multiplyExact((year - EPOCH_YEAR), 12) + month - 1;
  }

  /** return the number of hours away from the epoch, reverse {@link TransformUtil#humanHour} */
  private static Integer readHoursData(String asString) {
    LocalDateTime dateTime = LocalDateTime.parse(asString, FORMAT_HOUR);
    return Math.toIntExact(ChronoUnit.HOURS.between(EPOCH, dateTime));
  }

  public static Object fromPartitionString(PartitionField field, Type type, String asString) {
    if (asString == null || HIVE_NULL.equals(asString)) {
      return null;
    }

    switch (type.typeId()) {
      case BOOLEAN:
        return Boolean.valueOf(asString);
      case INTEGER:
        if (MONTH_TYPE.equals(field.transform().toString())) {
          return readMonthData(asString);
        } else if (HOUR_TYPE.equals(field.transform().toString())) {
          return readHoursData(asString);
        }
        return Integer.valueOf(asString);
      case STRING:
        return asString;
      case LONG:
        return Long.valueOf(asString);
      case FLOAT:
        return Float.valueOf(asString);
      case DOUBLE:
        return Double.valueOf(asString);
      case UUID:
        return UUID.fromString(asString);
      case FIXED:
        Types.FixedType fixed = (Types.FixedType) type;
        return Arrays.copyOf(asString.getBytes(StandardCharsets.UTF_8), fixed.length());
      case BINARY:
        return asString.getBytes(StandardCharsets.UTF_8);
      case DECIMAL:
        return new BigDecimal(asString);
      case DATE:
        return Literal.of(asString).to(Types.DateType.get()).value();
      case TIMESTAMP:
        if (((Types.TimestampType) type).shouldAdjustToUTC()) {
          return Literal.of(asString).to(Types.TimestampType.withZone()).value();
        } else {
          return Literal.of(asString).to(Types.TimestampType.withoutZone()).value();
        }
      default:
        throw new UnsupportedOperationException(
            "Unsupported type for fromPartitionString: " + type);
    }
  }

  public static GenericRecord data(PartitionSpec spec, String partitionPath) {
    GenericRecord data = genericRecord(spec);
    String[] partitions = partitionPath.split("/", -1);
    Preconditions.checkArgument(
        partitions.length <= spec.fields().size(),
        "Invalid partition data, too many fields (expecting %s): %s",
        spec.fields().size(),
        partitionPath);
    Preconditions.checkArgument(
        partitions.length >= spec.fields().size(),
        "Invalid partition data, not enough fields (expecting %s): %s",
        spec.fields().size(),
        partitionPath);

    for (int i = 0; i < partitions.length; i += 1) {
      PartitionField field = spec.fields().get(i);
      String[] parts = partitions[i].split("=", 2);
      Preconditions.checkArgument(
          parts.length == 2 && parts[0] != null && field.name().equals(parts[0]),
          "Invalid partition: %s",
          partitions[i]);

      String value;
      if ("null".equals(parts[1])) {
        value = null;
      } else {
        try {
          /*
          Decode URL with UTF-8, because in org.apache.iceberg.PartitionSpec#escape,
          the value is encoded to URL with UTF-8
           */
          value = URLDecoder.decode(parts[1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(
              String.format("failed to decode %s of %s", parts[1], partitionPath), e);
        }
      }

      data.set(
          i,
          MixedDataFiles.fromPartitionString(
              field, spec.partitionType().fieldType(parts[0]), value));
    }

    return data;
  }

  private static GenericRecord genericRecord(PartitionSpec spec) {
    List<String> collect =
        spec.fields().stream()
            .map(s -> spec.schema().findColumnName(s.sourceId()))
            .collect(Collectors.toList());
    return GenericRecord.create(spec.schema().select(collect));
  }
}
