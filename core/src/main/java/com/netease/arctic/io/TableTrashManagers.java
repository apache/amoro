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
   * Get TableTrashManager from ArcticTable.
   *
   * @param table - ArcticTable
   * @return TableTrashManager
   */
  public static TableTrashManager of(ArcticTable table) {
    Map<String, String> properties = table.properties();
    String customTrashRootLocation = properties.get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
    String trashLocation = getTrashLocation(table.id(), table.location(), customTrashRootLocation);
    return new BasicTableTrashManager(table.id(), table.io(), table.location(), trashLocation);
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
