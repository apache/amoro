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

package com.netease.arctic.formats.mixed;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableSnapshot;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.iceberg.Snapshot;

import java.util.Map;

public class MixedIcebergTable implements AmoroTable<ArcticTable> {

  private final ArcticTable arcticTable;

  public MixedIcebergTable(ArcticTable arcticTable) {
    this.arcticTable = arcticTable;
  }

  @Override
  public TableIdentifier id() {
    return arcticTable.id();
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public Map<String, String> properties() {
    return arcticTable.properties();
  }

  @Override
  public ArcticTable originalTable() {
    return arcticTable;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    Snapshot changeSnapshot;
    Snapshot baseSnapshot;
    if (arcticTable.isKeyedTable()) {
      changeSnapshot = arcticTable.asKeyedTable().changeTable().currentSnapshot();
      baseSnapshot = arcticTable.asKeyedTable().baseTable().currentSnapshot();
    } else {
      changeSnapshot = null;
      baseSnapshot = arcticTable.asUnkeyedTable().currentSnapshot();
    }

    if (changeSnapshot == null && baseSnapshot == null) {
      return null;
    }

    return new MixedSnapshot(changeSnapshot, baseSnapshot);
  }
}
