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

package org.apache.amoro.io;

import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.MixedCatalogUtil;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

public class ArcticFileIOs {

  public static final boolean CLOSE_TRASH = true;

  public static MixedHadoopFileIO buildRecoverableHadoopFileIO(
      TableIdentifier tableIdentifier,
      String tableLocation,
      Map<String, String> tableProperties,
      TableMetaStore tableMetaStore,
      Map<String, String> catalogProperties) {
    tableProperties =
        MixedCatalogUtil.mergeCatalogPropertiesToTable(tableProperties, catalogProperties);
    if (!CLOSE_TRASH
        && PropertyUtil.propertyAsBoolean(
            tableProperties,
            TableProperties.ENABLE_TABLE_TRASH,
            TableProperties.ENABLE_TABLE_TRASH_DEFAULT)) {
      MixedHadoopFileIO fileIO = new MixedHadoopFileIO(tableMetaStore);
      TableTrashManager trashManager =
          TableTrashManagers.build(tableIdentifier, tableLocation, tableProperties, fileIO);
      String trashFilePattern =
          PropertyUtil.propertyAsString(
              tableProperties,
              TableProperties.TABLE_TRASH_FILE_PATTERN,
              TableProperties.TABLE_TRASH_FILE_PATTERN_DEFAULT);

      return new RecoverableHadoopFileIO(tableMetaStore, trashManager, trashFilePattern);
    } else {
      return new MixedHadoopFileIO(tableMetaStore);
    }
  }

  public static MixedHadoopFileIO buildHadoopFileIO(TableMetaStore tableMetaStore) {
    return new MixedHadoopFileIO(tableMetaStore);
  }

  public static MixedFileIO buildAdaptIcebergFileIO(TableMetaStore tableMetaStore, FileIO io) {
    if (io instanceof HadoopFileIO) {
      return buildHadoopFileIO(tableMetaStore);
    } else {
      return new MixedFileIOAdapter(io);
    }
  }
}
