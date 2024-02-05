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

package org.apache.parquet.format.converter;

import org.apache.parquet.format.ConvertedType;
import org.apache.parquet.format.LogicalType;
import org.apache.parquet.format.SchemaElement;
import org.apache.parquet.schema.LogicalTypeAnnotation;

/** Copy from hive-apache package, because include hive-apache will cause class conflict */
public final class ParquetMetadataConverterUtil {
  private ParquetMetadataConverterUtil() {}

  public static LogicalTypeAnnotation getLogicalTypeAnnotation(
      ParquetMetadataConverter parquetMetadataConverter,
      ConvertedType convertedType,
      SchemaElement schemaElement) {
    return parquetMetadataConverter.getLogicalTypeAnnotation(convertedType, schemaElement);
  }

  public static LogicalTypeAnnotation getLogicalTypeAnnotation(
      ParquetMetadataConverter parquetMetadataConverter, LogicalType logicalType) {
    return parquetMetadataConverter.getLogicalTypeAnnotation(logicalType);
  }

  public static LogicalType convertToLogicalType(
      ParquetMetadataConverter parquetMetadataConverter,
      LogicalTypeAnnotation logicalTypeAnnotation) {
    return parquetMetadataConverter.convertToLogicalType(logicalTypeAnnotation);
  }
}
