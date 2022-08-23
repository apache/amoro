package com.netease.arctic.hive.utils;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.util.ArrayList;
import java.util.List;

public class HiveTypeUtils {
  private HiveTypeUtils() {
  }

  /**
   * Convert a Hive ObjectInspector to a Iceberg data type.
   *
   * @param inspector a Hive inspector
   * @return the corresponding Iceberg data type
   */
  public static Type toIcebergType(ObjectInspector inspector) {
    return toIcebergType(TypeInfoUtils.getTypeInfoFromTypeString(inspector.getTypeName()));
  }

  /**
   * Convert Hive data type to a Iceberg data type.
   *
   * @param hiveType a Hive data type
   * @return the corresponding Iceberg data type
   */
  public static Type toIcebergType(TypeInfo hiveType) {
    switch (hiveType.getCategory()) {
      case PRIMITIVE:
        return toIcebergPrimitiveType((PrimitiveTypeInfo) hiveType);
      case LIST:
        ListTypeInfo listTypeInfo = (ListTypeInfo) hiveType;
        return Types.ListType.ofOptional(1, toIcebergType(listTypeInfo.getListElementTypeInfo()));
      case MAP:
        MapTypeInfo mapTypeInfo = (MapTypeInfo) hiveType;
        return Types.MapType.ofOptional(1, 2, toIcebergType(mapTypeInfo.getMapKeyTypeInfo()),
            toIcebergType(mapTypeInfo.getMapValueTypeInfo()));
      case STRUCT:
        StructTypeInfo structTypeInfo = (StructTypeInfo) hiveType;

        List<String> names = structTypeInfo.getAllStructFieldNames();
        List<TypeInfo> typeInfos = structTypeInfo.getAllStructFieldTypeInfos();

        List<Types.NestedField> fields = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
          fields.add(Types.NestedField.required(fields.size() + 1, names.get(i), toIcebergType(typeInfos.get(i))));
        }
        return Types.StructType.of(fields);
      default:
        throw new UnsupportedOperationException(
            String.format("Cannot convert Hive data type %s to arctic data type.", hiveType));
    }
  }

  private static Type toIcebergPrimitiveType(PrimitiveTypeInfo hiveType) {
    switch (hiveType.getPrimitiveCategory()) {
      case STRING:
      case CHAR:
      case VARCHAR:
        return Types.StringType.get();
      case BOOLEAN:
        return Types.BooleanType.get();
      case SHORT:
      case INT:
        return Types.IntegerType.get();
      case LONG:
        return Types.LongType.get();
      case FLOAT:
        return Types.FloatType.get();
      case DOUBLE:
        return Types.DoubleType.get();
      case DATE:
        return Types.DateType.get();
      case TIMESTAMP:
        return Types.TimestampType.withoutZone();
      case BYTE:
      case BINARY:
        return Types.BinaryType.get();
      case DECIMAL:
        DecimalTypeInfo decimalTypeInfo = (DecimalTypeInfo) hiveType;
        return Types.DecimalType.of(decimalTypeInfo.getPrecision(), decimalTypeInfo.getScale());
      default:
        throw new UnsupportedOperationException(
            String.format("Cannot convert Hive data type %s to arctic data type.", hiveType));
    }
  }

  /**
   * INTERVAL are not available in older versions. So better to have our own enum for primitive categories.
   */
  public enum HivePrimitiveCategory {
    VOID, BOOLEAN, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING,
    DATE, TIMESTAMP, BINARY, DECIMAL, VARCHAR, CHAR, INTERVAL_YEAR_MONTH, INTERVAL_DAY_TIME,
    UNKNOWN
  }
}
