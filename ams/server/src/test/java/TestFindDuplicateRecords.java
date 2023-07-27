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

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFindDuplicateRecords {
  private static final Logger LOG = LoggerFactory.getLogger(TestFindDuplicateRecords.class);
  private final String thriftUrl;

  public static void main(String[] args) {
    TestFindDuplicateRecords scan = new TestFindDuplicateRecords("thrift://localhost:1260");
    scan.test(TableIdentifier.of("xxx", "yyy", "zzz"));
  }

  public TestFindDuplicateRecords(String thriftUrl) {
    this.thriftUrl = thriftUrl;
  }

  public void test(TableIdentifier tableIdentifier) {
    ArcticTable arcticTable = loadTable(tableIdentifier);
    // TODO
    arcticTable.asUnkeyedTable();
  }

  private ArcticTable loadTable(TableIdentifier tableIdentifier) {
    String catalogUrl = thriftUrl + "/" + tableIdentifier.getCatalog();

    // 1.scan files
    ArcticCatalog load = CatalogLoader.load(catalogUrl);
    return load.loadTable(tableIdentifier);
  }
}
