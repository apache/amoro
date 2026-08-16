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

package org.apache.amoro.server.optimizing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.table.TableIdentifier;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSchedulingPolicy {

  @Test
  public void testTableRuntimesSnapshotIsIndependentFromLiveMap() {
    SchedulingPolicy policy =
        new SchedulingPolicy(new ResourceGroup.Builder("test", "local").build());
    DefaultTableRuntime runtime = mock(DefaultTableRuntime.class);
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of(
            TableIdentifier.of("catalog", "db", "table"), TableFormat.ICEBERG);
    when(runtime.getTableIdentifier()).thenReturn(identifier);
    policy.addTable(runtime);

    List<DefaultTableRuntime> snapshot = policy.snapshotTableRuntimes();
    policy.removeTable(runtime);

    Assert.assertEquals(1, snapshot.size());
    Assert.assertTrue(policy.snapshotTableRuntimes().isEmpty());
  }
}
