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
import org.apache.amoro.server.SQLiteAMSManagerTestBase;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.TableRuntimeState;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestDefaultTableRuntimeStore extends SQLiteAMSManagerTestBase {

  StateKey<String> key1 = StateKey.stateKey("key1").stringType().defaultValue("default1");
  StateKey<String> key2 = StateKey.stateKey("key2").stringType().defaultValue("default2");
  StateKey<String> key3 = StateKey.stateKey("key3").stringType().defaultValue("default3");

  List<StateKey<?>> requiredKeys = Lists.newArrayList(key1, key2, key3);

  private Persistence persistence;
  private TableRuntimeMeta meta;
  private ServerTableIdentifier tableIdentifier;

  @Before
  public void before() {
    persistence = new Persistence();
    meta = new TableRuntimeMeta();
    meta.setGroupName("default");
    meta.setStatusCode(0);
    tableIdentifier = ServerTableIdentifier.of("catalog", "database", "table", TableFormat.ICEBERG);
    persistence.persistTableRuntimeMeta(tableIdentifier, meta);
  }

  @After
  public void after() {
    persistence.clean(tableIdentifier.getId());
  }

  @Test
  public void testInitialize() {
    persistence.insertState(tableIdentifier.getId(), "key1", "value1");
    persistence.insertState(tableIdentifier.getId(), "key2", "value2");
    persistence.insertState(tableIdentifier.getId(), "key", "value");

    TableRuntimeStore store =
        new DefaultTableRuntimeStore(
            tableIdentifier, meta, requiredKeys, persistence.allState(tableIdentifier.getId()));

    List<TableRuntimeState> states = persistence.allState(tableIdentifier.getId());
    Assert.assertEquals(3, states.size());
    Assert.assertEquals("value1", store.getState(key1));
    Assert.assertEquals("value2", store.getState(key2));
    Assert.assertEquals("default3", store.getState(key3));
    Assert.assertFalse(states.stream().anyMatch(s -> s.getStateKey().equals("key")));
    Assert.assertTrue(states.stream().anyMatch(s -> s.getStateKey().equals("key1")));
    Assert.assertTrue(states.stream().anyMatch(s -> s.getStateKey().equals("key2")));
    Assert.assertTrue(states.stream().anyMatch(s -> s.getStateKey().equals("key3")));
  }

  @Test
  public void testOperation() {
    TableRuntimeStore store =
        new DefaultTableRuntimeStore(tableIdentifier, meta, requiredKeys, Collections.emptyList());

    store
        .begin()
        .updateGroup(any -> "group1")
        .updateStatusCode(any -> 10086)
        .updateState(key1, any -> "value1")
        .updateTableConfig(any -> any.put("key", "conf"))
        .updateTableSummary(any -> any.setHealthScore(100))
        .commit();

    Assert.assertEquals("group1", store.getGroupName());
    Assert.assertEquals(10086, store.getStatusCode());
    Assert.assertEquals("value1", store.getState(key1));
    Assert.assertEquals("conf", store.getTableConfig().get("key"));
    Assert.assertEquals(100, meta.getTableSummary().getHealthScore());
  }

  @Test
  public void testOperationBreak() {
    TableRuntimeStore store =
        new DefaultTableRuntimeStore(tableIdentifier, meta, requiredKeys, Collections.emptyList());

    try {
      store
          .begin()
          .updateGroup(any -> "group1")
          .updateStatusCode(any -> 10086)
          .updateState(key1, any -> "value1")
          .updateTableConfig(any -> any.put("key", "conf"))
          .updateTableSummary(
              any -> {
                throw new RuntimeException("test");
              })
          .commit();
    } catch (Throwable t) {
      // ignore
    }
    Assert.assertEquals("default", store.getGroupName());
    Assert.assertEquals(0, store.getStatusCode());
    Assert.assertEquals("default1", store.getState(key1));
    Assert.assertFalse(store.getTableConfig().containsKey("key"));
    Assert.assertEquals(-1, meta.getTableSummary().getHealthScore());
  }

  @Test
  public void testOperationMvcc() {
    TableRuntimeStore store =
        new DefaultTableRuntimeStore(tableIdentifier, meta, requiredKeys, Collections.emptyList());
    store
        .begin()
        .updateGroup(any -> "group1")
        .updateStatusCode(any -> 10086)
        .updateState(key1, any -> "value1")
        .updateTableConfig(any -> any.put("key", "conf"))
        .updateTableSummary(any -> any.setHealthScore(100))
        .updateGroup(
            any -> {
              Assert.assertEquals("group1", any);
              return "group2";
            })
        .updateStatusCode(
            any -> {
              Assert.assertEquals(10086, any.intValue());
              return 10087;
            })
        .updateState(
            key1,
            any -> {
              Assert.assertEquals("value1", any);
              return "value2";
            })
        .updateState(
            key2,
            any -> {
              Assert.assertEquals("default2", any);
              return "value2";
            })
        .updateTableConfig(
            any -> {
              Assert.assertEquals("conf", any.get("key"));
              any.remove("key");
            })
        .updateTableSummary(
            any -> {
              Assert.assertEquals(100, any.getHealthScore());
              any.setHealthScore(1);
            })
        .commit();
    Assert.assertEquals("group2", store.getGroupName());
    Assert.assertEquals(10087, store.getStatusCode());
    Assert.assertEquals("value2", store.getState(key1));
    Assert.assertEquals("value2", store.getState(key2));
    Assert.assertFalse(store.getTableConfig().containsKey("key"));
    Assert.assertEquals(1, meta.getTableSummary().getHealthScore());
  }

  class Persistence extends PersistentBase {

    public void persistTableRuntimeMeta(ServerTableIdentifier identifier, TableRuntimeMeta meta) {
      doAs(TableMetaMapper.class, m -> m.insertTable(identifier));
      meta.setTableId(identifier.getId());
      doAs(TableRuntimeMapper.class, m -> m.insertRuntime(meta));
    }

    public void insertState(long tableId, String key, String value) {
      doAs(TableRuntimeMapper.class, m -> m.saveState(tableId, key, value));
    }

    public List<TableRuntimeState> allState(long tableId) {
      return getAs(TableRuntimeMapper.class, TableRuntimeMapper::selectAllStates).stream()
          .filter(s -> s.getTableId() == tableId)
          .collect(Collectors.toList());
    }

    public void clean(long tableId) {
      doAs(TableRuntimeMapper.class, m -> m.removeAllTableStates(tableId));
      doAs(TableMetaMapper.class, m -> m.deleteTableIdById(tableId));
      doAs(TableRuntimeMapper.class, m -> m.deleteRuntime(tableId));
    }
  }
}
