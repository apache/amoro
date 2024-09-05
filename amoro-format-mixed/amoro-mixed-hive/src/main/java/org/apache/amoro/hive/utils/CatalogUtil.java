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

package org.apache.amoro.hive.utils;

import org.apache.amoro.hive.catalog.MixedHiveCatalog;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogUtil {
  private static final Logger LOG = LoggerFactory.getLogger(CatalogUtil.class);

  /**
   * check if the catalog is mixed-hive catalog
   *
   * @param mixedCatalog target mixed catalog
   * @return Whether mixed-hive catalog.
   */
  public static boolean isMixedHiveCatalog(MixedFormatCatalog mixedCatalog) {
    return mixedCatalog instanceof MixedHiveCatalog;
  }
}
