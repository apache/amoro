package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.AmoroPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


public class TestSpiPluginMananger {

  private SpiPluginManager<TestPlugin> pluginManager;
  private static final String pluginName1 = "plugin1";
  private static final String pluginName2 = "plugin2";

  @BeforeEach
  public void setup() {
    pluginManager = new SpiPluginManager<TestPlugin>() {};
  }

  @AfterEach
  public void tearDown() {
    pluginManager.close();
  }

  @Test
  public void testInstall() {
    pluginManager.install("plugin1");
    AmoroPlugin plugin = pluginManager.get("plugin1");
    Assertions.assertNotNull(plugin);
    Assertions.assertEquals("plugin1", plugin.name());
  }

  @Test
  public void testUninstall() {
    pluginManager.install("plugin1");
    pluginManager.uninstall("plugin1");
    AmoroPlugin plugin = pluginManager.get("plugin1");
    Assertions.assertNull(plugin);
  }

  @Test
  public void testList() {
    pluginManager.install("plugin1");
    pluginManager.install("plugin2");

    List<TestPlugin> plugins = pluginManager.list();
    Assertions.assertEquals(2, plugins.size());
  }

  @Test
  public void testGet() {
    pluginManager.install("plugin1");
    AmoroPlugin plugin = pluginManager.get("plugin1");
    Assertions.assertNotNull(plugin);
    Assertions.assertEquals("plugin1", plugin.name());
  }

  @Test
  public void testClose() {
    pluginManager.install("plugin1");
    pluginManager.install("plugin2");
    pluginManager.close();
    List<TestPlugin> plugins = pluginManager.list();
    Assertions.assertTrue(plugins.isEmpty());
  }

  public static class TestPluginImpl1 implements TestPlugin {
    public TestPluginImpl1() {
    }

    @Override
    public String name() {
      return pluginName1;
    }
  }

  public static class TestPluginImpl2 implements TestPlugin {

    public TestPluginImpl2() {
    }

    @Override
    public String name() {
      return pluginName2;
    }
  }
}
