package com.netease.arctic.utils;

import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.expressions.Literal;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class ArcticDataFiles {
    public static final OffsetDateTime EPOCH = Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC);
    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    private static final String HIVE_NULL = "__HIVE_DEFAULT_PARTITION__";

    public static String readMonthData(String dateStr) {
        try {
            Date parse = sdf1.parse(dateStr);
            String format1 = sdf2.format(parse);
            LocalDate collectTimeDate = LocalDate.parse(format1, dtf);
            return String.valueOf(Math.abs(ChronoUnit.MONTHS.between(collectTimeDate, EPOCH)));
        } catch (ParseException e) {
            throw new UnsupportedOperationException("Failed to parse month data ", e);
        }
    }

    public static Object fromPartitionString(Type type, String asString) {
        if (asString == null || HIVE_NULL.equals(asString)) {
            return null;
        }

        switch (type.typeId()) {
            case BOOLEAN:
                return Boolean.valueOf(asString);
            case INTEGER:
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
                return Arrays.copyOf(
                        asString.getBytes(StandardCharsets.UTF_8), fixed.length());
            case BINARY:
                return asString.getBytes(StandardCharsets.UTF_8);
            case DECIMAL:
                return new BigDecimal(asString);
            case DATE:
                return Literal.of(asString).to(Types.DateType.get()).value();
            default:
                throw new UnsupportedOperationException(
                        "Unsupported type for fromPartitionString: " + type);
        }
    }

    public static GenericRecord data(PartitionSpec spec, String partitionPath) {
        GenericRecord data = GenericRecord.create(spec.schema());
        String[] partitions = partitionPath.split("/", -1);
        Preconditions.checkArgument(partitions.length <= spec.fields().size(),
                "Invalid partition data, too many fields (expecting %s): %s",
                spec.fields().size(), partitionPath);
        Preconditions.checkArgument(partitions.length >= spec.fields().size(),
                "Invalid partition data, not enough fields (expecting %s): %s",
                spec.fields().size(), partitionPath);

        for (int i = 0; i < partitions.length; i += 1) {
            PartitionField field = spec.fields().get(i);
            String[] parts = partitions[i].split("=", 2);
            Preconditions.checkArgument(
                    parts.length == 2 &&
                            parts[0] != null &&
                            field.name().equals(parts[0]),
                    "Invalid partition: %s",
                    partitions[i]);
            String partitionValue = parts[1];
            if (parts[0].endsWith("month")) {
                partitionValue = ArcticDataFiles.readMonthData(parts[1]);
            }
            data.set(i, ArcticDataFiles.fromPartitionString(spec.partitionType().fieldType(parts[0]), partitionValue));
        }

        return data;
    }
}
