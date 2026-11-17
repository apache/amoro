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

package org.apache.amoro.server.table;

import org.apache.amoro.IcebergActions;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.server.process.iceberg.IcebergProcessFactory;
import org.apache.amoro.server.table.cleanup.TableRuntimeCleanupState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TestTableExpirationConfiguration {
  @ParameterizedTest
  @CsvSource({
    "omitted,omitted,true",
    "true,omitted,true",
    "false,omitted,false",
    "omitted,false,false",
    "true,false,false",
    "false,true,false"
  })
  void icebergExpirationKeepsExistingTableSwitchAndGcGate(
      String tableExpire, String gc, boolean expected) {
    Map<String, String> properties = new HashMap<>();
    if (!"omitted".equals(tableExpire)) {
      properties.put("table-expire.enabled", tableExpire);
    }
    if (!"omitted".equals(gc)) {
      properties.put("gc.enabled", gc);
    }
    TableRuntime runtime = Mockito.mock(TableRuntime.class);
    Mockito.when(runtime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(properties));
    Mockito.when(runtime.getState(DefaultTableRuntime.CLEANUP_STATE_KEY))
        .thenReturn(new TableRuntimeCleanupState());
    IcebergProcessFactory factory = new IcebergProcessFactory();
    factory.open(Map.of("expire-snapshots.interval", "1h"));
    factory.availableExecuteEngines(List.of(Mockito.mock(LocalExecutionEngine.class)));
    Assertions.assertEquals(
        expected, factory.trigger(runtime, IcebergActions.EXPIRE_SNAPSHOTS).isPresent());
  }
}
