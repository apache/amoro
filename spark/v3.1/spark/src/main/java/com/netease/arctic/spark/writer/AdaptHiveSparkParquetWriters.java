package com.netease.arctic.spark.writer;

import org.apache.iceberg.data.parquet.TimestampInt96Writer;
import org.apache.iceberg.parquet.ParquetValueReaders;
import org.apache.iceberg.parquet.ParquetValueWriter;
import org.apache.iceberg.parquet.ParquetValueWriters;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.data.ParquetWithSparkSchemaVisitor;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.DecimalUtil;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.DecimalMetadata;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.util.ArrayData;
import org.apache.spark.sql.catalyst.util.MapData;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.ByteType;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.Decimal;
import org.apache.spark.sql.types.MapType;
import org.apache.spark.sql.types.ShortType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.unsafe.types.UTF8String;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class AdaptHiveSparkParquetWriters {

  private AdaptHiveSparkParquetWriters() {
  }

  @SuppressWarnings("unchecked")
  public static <T> ParquetValueWriter<T> buildWriter(StructType dfSchema, MessageType type) {
    return (ParquetValueWriter<T>) ParquetWithSparkSchemaVisitor.visit(dfSchema, type,
        new AdaptHiveSparkParquetWriters.WriteBuilder(type));
  }

  private static class WriteBuilder extends ParquetWithSparkSchemaVisitor<ParquetValueWriter<?>> {
    private final MessageType type;

    WriteBuilder(MessageType type) {
      this.type = type;
    }

    @Override
    public ParquetValueWriter<?> message(StructType struct, MessageType message,
        List<ParquetValueWriter<?>> fieldWriters) {
      return struct(struct, message.asGroupType(), fieldWriters);
    }

    @Override
    public ParquetValueWriter<?> struct(StructType structType, GroupType struct,
        List<ParquetValueWriter<?>> fieldWriters) {
      List<Type> fields = struct.getFields();
      StructField[] sparkFields = structType.fields();
      List<ParquetValueWriter<?>> writers = Lists.newArrayListWithExpectedSize(fieldWriters.size());
      List<DataType> sparkTypes = Lists.newArrayList();
      for (int i = 0; i < fields.size(); i += 1) {
        writers.add(newOption(struct.getType(i), fieldWriters.get(i)));
        sparkTypes.add(sparkFields[i].dataType());
      }

      return new AdaptHiveSparkParquetWriters.InternalRowWriter(writers, sparkTypes);
    }

    @Override
    public ParquetValueWriter<?> list(ArrayType arrayType, GroupType array, ParquetValueWriter<?> elementWriter) {
      GroupType repeated = array.getFields().get(0).asGroupType();
      String[] repeatedPath = currentPath();

      int repeatedD = type.getMaxDefinitionLevel(repeatedPath);
      int repeatedR = type.getMaxRepetitionLevel(repeatedPath);

      return new AdaptHiveSparkParquetWriters.ArrayDataWriter<>(repeatedD, repeatedR,
          newOption(repeated.getType(0), elementWriter),
          arrayType.elementType());
    }

    @Override
    public ParquetValueWriter<?> map(
        MapType mapType, GroupType map,
        ParquetValueWriter<?> keyWriter, ParquetValueWriter<?> valueWriter) {
      GroupType repeatedKeyValue = map.getFields().get(0).asGroupType();
      String[] repeatedPath = currentPath();

      int repeatedD = type.getMaxDefinitionLevel(repeatedPath);
      int repeatedR = type.getMaxRepetitionLevel(repeatedPath);

      return new AdaptHiveSparkParquetWriters.MapDataWriter<>(repeatedD, repeatedR,
          newOption(repeatedKeyValue.getType(0), keyWriter),
          newOption(repeatedKeyValue.getType(1), valueWriter),
          mapType.keyType(), mapType.valueType());
    }

    private ParquetValueWriter<?> newOption(org.apache.parquet.schema.Type fieldType, ParquetValueWriter<?> writer) {
      int maxD = type.getMaxDefinitionLevel(path(fieldType.getName()));
      return ParquetValueWriters.option(fieldType, maxD, writer);
    }

    @Override
    public ParquetValueWriter<?> primitive(DataType dataType, PrimitiveType primitive) {
      ColumnDescriptor desc = type.getColumnDescription(currentPath());

      if (primitive.getOriginalType() != null) {
        switch (primitive.getOriginalType()) {
          case ENUM:
          case JSON:
          case UTF8:
            return utf8Strings(desc);
          case DATE:
          case INT_8:
          case INT_16:
          case INT_32:
            return ints(dataType, desc);
          case INT_64:
          case TIME_MICROS:
          case TIMESTAMP_MICROS:
            return ParquetValueWriters.longs(desc);
          case DECIMAL:
            DecimalMetadata decimal = primitive.getDecimalMetadata();
            switch (primitive.getPrimitiveTypeName()) {
              case INT32:
                return decimalAsInteger(desc, decimal.getPrecision(), decimal.getScale());
              case INT64:
                return decimalAsLong(desc, decimal.getPrecision(), decimal.getScale());
              case BINARY:
              case FIXED_LEN_BYTE_ARRAY:
                return decimalAsFixed(desc, decimal.getPrecision(), decimal.getScale());
              default:
                throw new UnsupportedOperationException(
                    "Unsupported base type for decimal: " + primitive.getPrimitiveTypeName());
            }
          case BSON:
            return byteArrays(desc);
          default:
            throw new UnsupportedOperationException(
                "Unsupported logical type: " + primitive.getOriginalType());
        }
      }

      switch (primitive.getPrimitiveTypeName()) {
        case FIXED_LEN_BYTE_ARRAY:
        case BINARY:
          return byteArrays(desc);
        case BOOLEAN:
          return ParquetValueWriters.booleans(desc);
        case INT32:
          return ints(dataType, desc);
        case INT64:
          return ParquetValueWriters.longs(desc);
        case INT96:
          return new TimestampInt96Writer<>(desc);
        case FLOAT:
          return ParquetValueWriters.floats(desc);
        case DOUBLE:
          return ParquetValueWriters.doubles(desc);
        default:
          throw new UnsupportedOperationException("Unsupported type: " + primitive);
      }
    }
  }

  private static ParquetValueWriters.PrimitiveWriter<?> ints(DataType type, ColumnDescriptor desc) {
    if (type instanceof ByteType) {
      return ParquetValueWriters.tinyints(desc);
    } else if (type instanceof ShortType) {
      return ParquetValueWriters.shorts(desc);
    }
    return ParquetValueWriters.ints(desc);
  }

  private static ParquetValueWriters.PrimitiveWriter<UTF8String> utf8Strings(ColumnDescriptor desc) {
    return new AdaptHiveSparkParquetWriters.UTF8StringWriter(desc);
  }

  private static ParquetValueWriters.PrimitiveWriter<Decimal> decimalAsInteger(ColumnDescriptor desc,
      int precision, int scale) {
    return new AdaptHiveSparkParquetWriters.IntegerDecimalWriter(desc, precision, scale);
  }

  private static ParquetValueWriters.PrimitiveWriter<Decimal> decimalAsLong(ColumnDescriptor desc,
      int precision, int scale) {
    return new AdaptHiveSparkParquetWriters.LongDecimalWriter(desc, precision, scale);
  }

  private static ParquetValueWriters.PrimitiveWriter<Decimal> decimalAsFixed(ColumnDescriptor desc,
      int precision, int scale) {
    return new AdaptHiveSparkParquetWriters.FixedDecimalWriter(desc, precision, scale);
  }

  private static ParquetValueWriters.PrimitiveWriter<byte[]> byteArrays(ColumnDescriptor desc) {
    return new AdaptHiveSparkParquetWriters.ByteArrayWriter(desc);
  }

  private static class UTF8StringWriter extends ParquetValueWriters.PrimitiveWriter<UTF8String> {
    private UTF8StringWriter(ColumnDescriptor desc) {
      super(desc);
    }

    @Override
    public void write(int repetitionLevel, UTF8String value) {
      column.writeBinary(repetitionLevel, Binary.fromReusedByteArray(value.getBytes()));
    }
  }

  private static class IntegerDecimalWriter extends ParquetValueWriters.PrimitiveWriter<Decimal> {
    private final int precision;
    private final int scale;

    private IntegerDecimalWriter(ColumnDescriptor desc, int precision, int scale) {
      super(desc);
      this.precision = precision;
      this.scale = scale;
    }

    @Override
    public void write(int repetitionLevel, Decimal decimal) {
      Preconditions.checkArgument(decimal.scale() == scale,
          "Cannot write value as decimal(%s,%s), wrong scale: %s", precision, scale, decimal);
      Preconditions.checkArgument(decimal.precision() <= precision,
          "Cannot write value as decimal(%s,%s), too large: %s", precision, scale, decimal);

      column.writeInteger(repetitionLevel, (int) decimal.toUnscaledLong());
    }
  }

  private static class LongDecimalWriter extends ParquetValueWriters.PrimitiveWriter<Decimal> {
    private final int precision;
    private final int scale;

    private LongDecimalWriter(ColumnDescriptor desc, int precision, int scale) {
      super(desc);
      this.precision = precision;
      this.scale = scale;
    }

    @Override
    public void write(int repetitionLevel, Decimal decimal) {
      Preconditions.checkArgument(decimal.scale() == scale,
          "Cannot write value as decimal(%s,%s), wrong scale: %s", precision, scale, decimal);
      Preconditions.checkArgument(decimal.precision() <= precision,
          "Cannot write value as decimal(%s,%s), too large: %s", precision, scale, decimal);

      column.writeLong(repetitionLevel, decimal.toUnscaledLong());
    }
  }

  private static class FixedDecimalWriter extends ParquetValueWriters.PrimitiveWriter<Decimal> {
    private final int precision;
    private final int scale;
    private final ThreadLocal<byte[]> bytes;

    private FixedDecimalWriter(ColumnDescriptor desc, int precision, int scale) {
      super(desc);
      this.precision = precision;
      this.scale = scale;
      this.bytes = ThreadLocal.withInitial(() -> new byte[TypeUtil.decimalRequiredBytes(precision)]);
    }

    @Override
    public void write(int repetitionLevel, Decimal decimal) {
      byte[] binary = DecimalUtil.toReusedFixLengthBytes(precision, scale, decimal.toJavaBigDecimal(), bytes.get());
      column.writeBinary(repetitionLevel, Binary.fromReusedByteArray(binary));
    }
  }

  private static class ByteArrayWriter extends ParquetValueWriters.PrimitiveWriter<byte[]> {
    private ByteArrayWriter(ColumnDescriptor desc) {
      super(desc);
    }

    @Override
    public void write(int repetitionLevel, byte[] bytes) {
      column.writeBinary(repetitionLevel, Binary.fromReusedByteArray(bytes));
    }
  }

  private static class ArrayDataWriter<E> extends ParquetValueWriters.RepeatedWriter<ArrayData, E> {
    private final DataType elementType;

    private ArrayDataWriter(int definitionLevel, int repetitionLevel,
        ParquetValueWriter<E> writer, DataType elementType) {
      super(definitionLevel, repetitionLevel, writer);
      this.elementType = elementType;
    }

    @Override
    protected Iterator<E> elements(ArrayData list) {
      return new ElementIterator<>(list);
    }

    private class ElementIterator<E> implements Iterator<E> {
      private final int size;
      private final ArrayData list;
      private int index;

      private ElementIterator(ArrayData list) {
        this.list = list;
        size = list.numElements();
        index = 0;
      }

      @Override
      public boolean hasNext() {
        return index != size;
      }

      @Override
      @SuppressWarnings("unchecked")
      public E next() {
        if (index >= size) {
          throw new NoSuchElementException();
        }

        E element;
        if (list.isNullAt(index)) {
          element = null;
        } else {
          element = (E) list.get(index, elementType);
        }

        index += 1;

        return element;
      }
    }
  }

  private static class MapDataWriter<K, V> extends ParquetValueWriters.RepeatedKeyValueWriter<MapData, K, V> {
    private final DataType keyType;
    private final DataType valueType;

    private MapDataWriter(int definitionLevel, int repetitionLevel,
        ParquetValueWriter<K> keyWriter, ParquetValueWriter<V> valueWriter,
        DataType keyType, DataType valueType) {
      super(definitionLevel, repetitionLevel, keyWriter, valueWriter);
      this.keyType = keyType;
      this.valueType = valueType;
    }

    @Override
    protected Iterator<Map.Entry<K, V>> pairs(MapData map) {
      return new EntryIterator<>(map);
    }

    private class EntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
      private final int size;
      private final ArrayData keys;
      private final ArrayData values;
      private final ParquetValueReaders.ReusableEntry<K, V> entry;
      private int index;

      private EntryIterator(MapData map) {
        size = map.numElements();
        keys = map.keyArray();
        values = map.valueArray();
        entry = new ParquetValueReaders.ReusableEntry<>();
        index = 0;
      }

      @Override
      public boolean hasNext() {
        return index != size;
      }

      @Override
      @SuppressWarnings("unchecked")
      public Map.Entry<K, V> next() {
        if (index >= size) {
          throw new NoSuchElementException();
        }

        if (values.isNullAt(index)) {
          entry.set((K) keys.get(index, keyType), null);
        } else {
          entry.set((K) keys.get(index, keyType), (V) values.get(index, valueType));
        }

        index += 1;

        return entry;
      }
    }
  }

  private static class InternalRowWriter extends ParquetValueWriters.StructWriter<InternalRow> {
    private final DataType[] types;

    private InternalRowWriter(List<ParquetValueWriter<?>> writers, List<DataType> types) {
      super(writers);
      this.types = types.toArray(new DataType[types.size()]);
    }

    @Override
    protected Object get(InternalRow struct, int index) {
      return struct.get(index, types[index]);
    }
  }
}
