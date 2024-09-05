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

package org.apache.amoro.server.table.internal;

import org.apache.amoro.mixed.InternalMixedIcebergCatalog;

/** Constants defines for internal table */
public class InternalTableConstants {
  public static final String METADATA_FOLDER_NAME = "metadata";
  public static final String PROPERTIES_METADATA_LOCATION = "iceberg.metadata.location";
  public static final String PROPERTIES_PREV_METADATA_LOCATION = "iceberg.metadata.prev-location";

  public static final String CHANGE_STORE_PREFIX = "change-store.";

  public static final String MIXED_ICEBERG_BASED_REST = "mixed-iceberg.based-on-rest-catalog";

  public static final String HADOOP_FILE_IO_IMPL = "org.apache.iceberg.hadoop.HadoopFileIO";
  public static final String S3_FILE_IO_IMPL = "org.apache.iceberg.aws.s3.S3FileIO";
  public static final String S3_PROTOCOL_PREFIX = "s3://";

  public static final String CHANGE_STORE_TABLE_NAME_SUFFIX =
      InternalMixedIcebergCatalog.CHANGE_STORE_SEPARATOR
          + InternalMixedIcebergCatalog.CHANGE_STORE_NAME;
}
