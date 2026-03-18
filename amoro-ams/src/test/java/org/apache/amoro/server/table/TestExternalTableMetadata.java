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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class TestExternalTableMetadata {

  @Test
  public void testBuildFromExternalTable() {
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of("catalog", "database", "table", TableFormat.PAIMON);
    identifier.setId(1L);

    TableMetadata metadata =
        TableMetadata.fromExternalTable(
            identifier,
            "file:///warehouse/table",
            "file:///warehouse/table/data",
            null,
            "id,dt",
            Collections.singletonMap("k", "v"),
            "[{\"field\":\"id\"}]",
            "HASH_DYNAMIC",
            8,
            "101",
            "table-comment");

    Assert.assertEquals(identifier, metadata.getTableIdentifier());
    Assert.assertEquals("file:///warehouse/table", metadata.getTableLocation());
    Assert.assertEquals("file:///warehouse/table/data", metadata.getBaseLocation());
    Assert.assertNull(metadata.getChangeLocation());
    Assert.assertEquals("id,dt", metadata.getPrimaryKey());
    Assert.assertEquals("[{\"field\":\"id\"}]", metadata.getSchema());
    Assert.assertEquals("HASH_DYNAMIC", metadata.getBucketMode());
    Assert.assertEquals(Integer.valueOf(8), metadata.getNumBuckets());
    Assert.assertEquals("101", metadata.getSnapshotId());
    Assert.assertEquals("table-comment", metadata.getTableComment());
    Assert.assertEquals("v", metadata.getProperties().get("k"));
  }
}
