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

package com.netease.arctic.iceberg.mixed;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ArcticTables;
import com.netease.arctic.table.MixedTables;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.Schema;
import org.apache.iceberg.exceptions.NoSuchTableException;

public class MixedIcebergTables implements MixedTables {


  ArcticTables icebergTables;

  public MixedIcebergTables(ArcticTables icebergTables) {
    this.icebergTables = icebergTables;
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public TableBuilder newTableBuilder(Schema schema, TableIdentifier identifier) {
    return new MixedIcebergTableBuilder(icebergTables, schema, identifier);
  }

  @Override
  public ArcticTable loadTable(TableIdentifier identifier) {
    ArcticTable base = icebergTables.loadTable(identifier);
    return loadMixedTable(base);
  }

  @Override
  public boolean dropTable(TableIdentifier tableIdentifier, boolean purge) {
    return false;
  }

  @Override
  public boolean isMixedTable(ArcticTable base) {
    return false;
  }

  @Override
  public ArcticTable loadMixedTable(ArcticTable base) {
    if (!isMixedTable(base)) {
      throw new NoSuchTableException(base.id().toString() + " is not a " + format() + " table.");
    }
    return null;
  }
}
