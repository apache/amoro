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

package com.netease.arctic.utils;

import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

/**
 * copy from {@link org.apache.iceberg.types.TypeUtil} and remove this class after upgrading Iceberg
 * version to 1.5+.
 */
public class AmoroTypeUtil {

  private static final int HEADER_SIZE = 12;

  /**
   * Estimates the number of bytes a value for a given field may occupy in memory.
   *
   * <p>This method approximates the memory size based on heuristics and the internal Java
   * representation defined by {@link Type.TypeID}. It is important to note that the actual size
   * might differ from this estimation. The method is designed to handle a variety of data types,
   * including primitive types, strings, and nested types such as structs, maps, and lists.
   *
   * @param field a field for which to estimate the size
   * @return the estimated size in bytes of the field's value in memory
   */
  public static int estimateSize(Types.NestedField field) {
    return estimateSize(field.type());
  }

  private static int estimateSize(Type type) {
    switch (type.typeId()) {
      case BOOLEAN:
        // the size of a boolean variable is virtual machine dependent
        // it is common to believe booleans occupy 1 byte in most JVMs
        return 1;
      case INTEGER:
      case FLOAT:
      case DATE:
        // ints and floats occupy 4 bytes
        // dates are internally represented as ints
        return 4;
      case LONG:
      case DOUBLE:
      case TIME:
      case TIMESTAMP:
        // longs and doubles occupy 8 bytes
        // times and timestamps are internally represented as longs
        return 8;
      case STRING:
        // 12 (header) + 6 (fields) + 16 (array overhead) + 20 (10 chars, 2 bytes each) = 54 bytes
        return 54;
      case UUID:
        // 12 (header) + 16 (two long variables) = 28 bytes
        return 28;
      case FIXED:
        return ((Types.FixedType) type).length();
      case BINARY:
        return 80;
      case DECIMAL:
        // 12 (header) + (12 + 12 + 4) (BigInteger) + 4 (scale) = 44 bytes
        return 44;
      case STRUCT:
        Types.StructType struct = (Types.StructType) type;
        return HEADER_SIZE + struct.fields().stream().mapToInt(AmoroTypeUtil::estimateSize).sum();
      case LIST:
        Types.ListType list = (Types.ListType) type;
        return HEADER_SIZE + 5 * estimateSize(list.elementType());
      case MAP:
        Types.MapType map = (Types.MapType) type;
        int entrySize = HEADER_SIZE + estimateSize(map.keyType()) + estimateSize(map.valueType());
        return HEADER_SIZE + 5 * entrySize;
      default:
        return 16;
    }
  }
}
