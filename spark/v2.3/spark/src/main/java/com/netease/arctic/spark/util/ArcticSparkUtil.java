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

package com.netease.arctic.spark.util;

import com.netease.arctic.spark.reader.SparkParquetV2Readers;
import org.apache.avro.generic.GenericData;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.util.ByteBuffers;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.RuntimeConfig;
import org.apache.spark.sql.types.BinaryType;
import org.apache.spark.sql.types.Decimal;
import org.apache.spark.unsafe.types.UTF8String;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArcticSparkUtil {
  public static final String CATALOG_URL = "arctic.catalog.url";
  public static final String SQL_DELEGATE_HIVE_TABLE = "spark.arctic.sql.delegate.enable";

  public static String catalogUrl(RuntimeConfig conf) {
    String catalogUrl = conf.get(CATALOG_URL, "");
    if (StringUtils.isBlank(catalogUrl)) {
      throw new IllegalArgumentException("arctic.catalog.url is blank");
    }
    return catalogUrl;
  }

  public static boolean delegateHiveTable(RuntimeConfig config) {
    String val = config.get(SQL_DELEGATE_HIVE_TABLE, "true");
    return Boolean.parseBoolean(val);
  }

  public static Object convertConstant(Type type, Object value) {
    if (value == null) {
      return null;
    }

    switch (type.typeId()) {
      case DECIMAL:
        return Decimal.apply((BigDecimal) value);
        /*case STRING:
        if (value instanceof Utf8) {
          Utf8 utf8 = (Utf8) value;
          return UTF8String.fromBytes(utf8.getBytes(), 0, utf8.getByteLength());
        }
        return UTF8String.fromString(value.toString());*/
      case FIXED:
        if (value instanceof byte[]) {
          return value;
        } else if (value instanceof GenericData.Fixed) {
          return ((GenericData.Fixed) value).bytes();
        }
        return ByteBuffers.toByteArray((ByteBuffer) value);
      case BINARY:
        return ByteBuffers.toByteArray((ByteBuffer) value);
      default:
    }
    return value;
  }

  public static Object[] convertRowObject(Object[] objects, Schema schema) {
    for (int i = 0; i < objects.length; i++) {
      Object object = objects[i];
      Type type = schema.columns().get(i).type();
      /*if (object instanceof UTF8String) {
        objects[i] = object.toString();
      } else*/
      if (object instanceof Double) {
        objects[i] = new BigDecimal((Double) object).doubleValue();
      } else if (object instanceof Float) {
        objects[i] = new BigDecimal((Float) object).floatValue();
      } else if (object instanceof Decimal) {
        objects[i] = ((Decimal) object).toJavaBigDecimal();
      } else if (object instanceof BinaryType) {
        objects[i] = ByteBuffer.wrap((byte[]) object);
      } else if (object instanceof SparkParquetV2Readers.ReusableMapData) {
        Object[] keyArray = ((SparkParquetV2Readers.ReusableMapData) object).keyArray().array();
        Object[] valueArray = ((SparkParquetV2Readers.ReusableMapData) object).valueArray().array();
        Map map = new HashMap();
        for (int j = 0; j < keyArray.length; j++) {
          if (keyArray[j] instanceof UTF8String) {
            keyArray[j] = keyArray[j].toString();
          }
          if (valueArray[j] instanceof UTF8String) {
            valueArray[j] = valueArray[j].toString();
          }
          map.put(keyArray[j], valueArray[j]);
        }
        scala.collection.Map scalaMap = (scala.collection.Map) JavaConverters.mapAsScalaMapConverter(map).asScala();
        objects[i] = scalaMap;
      } else if (object instanceof SparkParquetV2Readers.ReusableArrayData) {
        Object[] array = ((SparkParquetV2Readers.ReusableArrayData) object).array();
        for (int j = 0; j < array.length; j++) {
          if (array[j] instanceof UTF8String) {
            array[j] = array[j].toString();
          }
        }
        Seq seq = JavaConverters.asScalaIteratorConverter(Arrays.asList(array).iterator()).asScala().toSeq();
        objects[i] = seq;
      }
    }
    return objects;
  }

  public static Row convertInterRowToRow(Row row, Schema schema) {
    Seq<Object> objectSeq = row.toSeq();
    Object[] objects = JavaConverters.seqAsJavaListConverter(objectSeq).asJava().toArray();
    return RowFactory.create(convertRowObject(objects, schema));
  }

}
