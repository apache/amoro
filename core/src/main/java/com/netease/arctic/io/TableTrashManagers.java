/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.io;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.relocated.com.google.common.base.Strings;

import java.util.Map;

public class TableTrashManagers {
  public static String DEFAULT_TRASH_DIR = ".trash";

  /**
   * Build {@link TableTrashManager} from {@link ArcticTable}.
   *
   * @param table - {@link ArcticTable}
   * @return TableTrashManager
   */
  public static TableTrashManager build(ArcticTable table) {
    return build(table.id(), table.location(), table.properties(), table.io());
  }

  /**
   * Build {@link TableTrashManager}.
   *
   * @param tableIdentifier - table identifier
   * @param tableLocation   - table root location
   * @param tableProperties - table properties
   * @param fileIO          - table file io
   * @return - built table trash manager
   */
  public static TableTrashManager build(TableIdentifier tableIdentifier, String tableLocation,
                                        Map<String, String> tableProperties, ArcticFileIO fileIO) {
    String customTrashRootLocation = tableProperties.get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
    String trashLocation = getTrashLocation(tableIdentifier, tableLocation, customTrashRootLocation);
    return new BasicTableTrashManager(tableIdentifier, fileIO, tableLocation, trashLocation);
  }

  /**
   * Get trash location.
   *
   * @param tableIdentifier         - table identifier
   * @param tableLocation           - table root location
   * @param customTrashRootLocation - from the table property table-trash.custom-root-location
   * @return trash location
   */
  public static String getTrashLocation(TableIdentifier tableIdentifier, String tableLocation,
                                        String customTrashRootLocation) {
    String trashLocation;
    if (Strings.isNullOrEmpty(customTrashRootLocation)) {
      trashLocation = tableLocation + "/" + DEFAULT_TRASH_DIR;
    } else {
      if (!customTrashRootLocation.endsWith("/")) {
        customTrashRootLocation = customTrashRootLocation + "/";
      }
      trashLocation = customTrashRootLocation + tableIdentifier.getDatabase() +
          "/" + tableIdentifier.getTableName() +
          "/" + DEFAULT_TRASH_DIR;
    }
    return trashLocation;
  }
}
