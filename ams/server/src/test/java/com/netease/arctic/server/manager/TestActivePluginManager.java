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

package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.ActivePlugin;
import com.netease.arctic.ams.api.PluginManager;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestActivePluginManager {

  private static final String PLUGIN_NAME_1 = "plugin1";
  private static final String PLUGIN_NAME_2 = "plugin2";
  private static final String PLUGIN_CLASS_1 =
      "com.netease.arctic.server.manager.TestDefaultPluginManager$TestPluginImpl1";
  private static final String PLUGIN_CLASS_2 =
      "com.netease.arctic.server.manager.TestDefaultPluginManager$TestPluginImpl2";

  private static final Map<String, String> PLUGIN_CLASS_MAP = new HashMap<String, String>() {
    {
      put(PLUGIN_NAME_1, PLUGIN_CLASS_1);
      put(PLUGIN_NAME_2, PLUGIN_CLASS_2);
    }
  };

  private PluginManager<TestPlugin> pluginManager;

  @BeforeEach
  void setUp() {
    pluginManager = new ActivePluginManager<TestPlugin>() {
      @Override
      protected Map<String, String> loadProperties(String pluginName) {
        return new HashMap<String, String>() {
          {
            put(PUGIN_IMPLEMENTION_CLASS, PLUGIN_CLASS_MAP.get(pluginName));
          }
        };
      }
    };
  }

  @AfterEach
  void tearDown() {
    pluginManager.close();
  }

  @Test
  void testInstall() {
    assertEquals(0, pluginManager.list().size());

    pluginManager.install(PLUGIN_NAME_1);
    assertEquals(1, pluginManager.list().size());

    TestPlugin installedPlugin = pluginManager.get(PLUGIN_NAME_1);
    assertTrue(installedPlugin instanceof TestPluginImpl1);
    assertEquals(PLUGIN_NAME_1, installedPlugin.name());
    assertTrue(installedPlugin.isOpen());

    pluginManager.install(PLUGIN_NAME_2);
    assertEquals(2, pluginManager.list().size());
    installedPlugin = pluginManager.get(PLUGIN_NAME_2);
    assertTrue(installedPlugin instanceof TestPluginImpl2);
    assertEquals(PLUGIN_NAME_2, installedPlugin.name());
    assertTrue(installedPlugin.isOpen());
  }

  @Test
  void testUninstall()  {
    assertEquals(0, pluginManager.list().size());

    pluginManager.install(PLUGIN_NAME_1);
    TestPlugin installedPlugin = pluginManager.get(PLUGIN_NAME_1);

    pluginManager.uninstall(PLUGIN_NAME_1);
    assertEquals(0, pluginManager.list().size());
    assertFalse(installedPlugin.isOpen());
  }

  @Test
  void testList() {
    assertEquals(0, pluginManager.list().size());

    pluginManager.install(PLUGIN_NAME_1);
    assertEquals(1, pluginManager.list().size());

    pluginManager.install(PLUGIN_NAME_2);
    assertEquals(2, pluginManager.list().size());

    pluginManager.uninstall(PLUGIN_NAME_1);
    assertEquals(1, pluginManager.list().size());
  }

  @Test
  void testGet() {
    assertEquals(0, pluginManager.list().size());
    pluginManager.install(PLUGIN_NAME_2);

    TestPlugin plugin = pluginManager.get(PLUGIN_NAME_2);

    assertNotNull(plugin);
    assertTrue(plugin instanceof TestPluginImpl2);
    assertTrue(plugin.isOpen());
  }

  @Test
  void testClose() {
    assertEquals(0, pluginManager.list().size());
    pluginManager.install(PLUGIN_NAME_1);
    pluginManager.install(PLUGIN_NAME_2);
    List<TestPlugin> installedPlugins = pluginManager.list();

    pluginManager.close();
    assertEquals(0, pluginManager.list().size());

    installedPlugins.forEach(plugin -> assertFalse(plugin.isOpen()));
  }

  @Test
  public void testDuplcateInstall() {
    pluginManager.install(PLUGIN_NAME_1);
    Assertions.assertThrows(AlreadyExistsException.class, () ->
        pluginManager.install(PLUGIN_NAME_1));
  }

  @Test
  public void testDuplcateUninstall() {
    pluginManager.install(PLUGIN_NAME_1);
    pluginManager.uninstall(PLUGIN_NAME_1);
    Assertions.assertThrows(ObjectNotExistsException.class, () ->
        pluginManager.uninstall(PLUGIN_NAME_1));
  }

  private abstract static class TestPlugin implements ActivePlugin {

    boolean isOpen = false;

    @Override
    public void open(Map<String, String> properties) {
      isOpen = true;
    }

    @Override
    public void close() {
      isOpen = false;
    }

    public boolean isOpen() {
      return isOpen;
    }
  }

  private static class TestPluginImpl1 extends TestPlugin {
    public TestPluginImpl1() {
    }

    @Override
    public String name() {
      return PLUGIN_NAME_1;
    }
  }

  private static class TestPluginImpl2 extends TestPlugin {

    public TestPluginImpl2() {
    }

    @Override
    public String name() {
      return PLUGIN_NAME_2;
    }
  }
}
