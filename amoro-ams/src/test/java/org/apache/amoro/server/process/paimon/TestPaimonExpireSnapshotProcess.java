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

package org.apache.amoro.server.process.paimon;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.HttpRemoteSparkStandAloneSubmit;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestPaimonExpireSnapshotProcess {

  @Test
  public void testGetProcessParametersUseExecuteUser() {
    HttpRemoteSparkStandAloneSubmit engine = new HttpRemoteSparkStandAloneSubmit();
    Map<String, String> engineProps = new HashMap<>();
    engineProps.put("execute.user", "sl_real_time_merger");
    engine.open(engineProps);

    TableRuntime runtime = Mockito.mock(TableRuntime.class);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "db", "tbl", TableFormat.PAIMON));
    Mockito.when(runtime.getTableConfig()).thenReturn(Collections.emptyMap());
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);

    PaimonExpireSnapshotProcess process = new PaimonExpireSnapshotProcess(runtime, engine, 354);

    Map<String, String> params = process.getProcessParameters();
    Assert.assertEquals("sl_real_time_merger", params.get("curUser"));
    Assert.assertEquals("sl_real_time_merger", params.get("logUser"));
    Assert.assertEquals("AMORO", params.get("sourceTag"));
    Assert.assertEquals(
        "{\"sparkVersion\":\"354\",\"paimon.version\":\"1.3\"}", params.get("conf"));
  }

  @Test
  public void testBuildExpireSnapshotsSqlContainsMaxDeletes() {
    TableRuntime runtime = Mockito.mock(TableRuntime.class);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "default", "orders", TableFormat.PAIMON));

    Map<String, String> tableConfig = new HashMap<>();
    tableConfig.put("snapshot.time-retained", "2 h");
    tableConfig.put("snapshot.num-retained.max", "12");
    Mockito.when(runtime.getTableConfig()).thenReturn(tableConfig);

    HttpRemoteSparkStandAloneSubmit engine = new HttpRemoteSparkStandAloneSubmit();
    engine.open(Collections.singletonMap("execute.user", "amoro"));

    PaimonExpireSnapshotProcess process = new PaimonExpireSnapshotProcess(runtime, engine, 354);
    String sql = process.buildExpireSnapshotsSql();

    Assert.assertTrue(sql.contains("CALL sys.expire_snapshots"));
    Assert.assertTrue(sql.contains("table => 'default.orders'"));
    Assert.assertTrue(sql.contains("retain_max => 12"));
    Assert.assertTrue(sql.contains("max_deletes => 550"));
  }
}
