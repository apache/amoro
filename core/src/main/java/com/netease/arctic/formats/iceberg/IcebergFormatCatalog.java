/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats.iceberg;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.FormatCatalog;
import org.apache.iceberg.catalog.Catalog;
import java.util.List;

public class IcebergFormatCatalog implements FormatCatalog {

  private final Catalog icebergCatalog;

  public IcebergFormatCatalog(Catalog icebergCatalog) {
    this.icebergCatalog = icebergCatalog;
  }

  @Override
  public List<String> listDatabases() {
    return null;
  }

  @Override
  public boolean exist(String database) {
    return false;
  }

  @Override
  public boolean exist(String database, String table) {
    return false;
  }

  @Override
  public void createDatabase(String database) {

  }

  @Override
  public void dropDatabase(String database) {

  }

  @Override
  public List<String> listTables(String database) {
    return null;
  }


  @Override
  public AmoroTable loadTable(String database, String table) {
    return null;
  }
}
