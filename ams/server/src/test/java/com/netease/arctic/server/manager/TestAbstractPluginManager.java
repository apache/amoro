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

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.ams.api.ActivePlugin;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.LoadingPluginException;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestAbstractPluginManager {

  public interface TestPlugin extends ActivePlugin {
    @Override
    default void open(Map<String, String> properties) {}

    @Override
    default void close() {}
  }

  public static class TestPluginImplA implements TestPlugin {

    @Override
    public String name() {
      return "test-A";
    }
  }

  public static class TestPluginImplB implements TestPlugin {

    @Override
    public String name() {
      return "test-B";
    }
  }

  // not in spi plugin class implement
  public static class TestPluginImplC implements TestPlugin {

    @Override
    public String name() {
      return "test-C";
    }
  }

  static class TestPluginManager extends AbstractPluginManager<TestPlugin> {

    List<PluginConfiguration> configs = Lists.newArrayList();

    public TestPluginManager() {
      super("test-plugins");
    }

    public void setConfigs(List<PluginConfiguration> configs) {
      this.configs = configs;
    }

    @Override
    protected List<PluginConfiguration> loadPluginConfigurations() {
      return configs;
    }

    public int activePlugins() {
      AtomicInteger count = new AtomicInteger();
      callPlugins(t -> count.incrementAndGet());
      return count.get();
    }
  }

  static Stream<Arguments> testInstall() {
    return Stream.of(
        // enable 1 plugin
        Arguments.of(
            ImmutableList.of(new PluginConfiguration("test-A", true, Maps.newHashMap())), 1),
        // enable 2 plugin
        Arguments.of(
            ImmutableList.of(
                new PluginConfiguration("test-A", true, Maps.newHashMap()),
                new PluginConfiguration("test-B", true, Maps.newHashMap())),
            2),
        // provider 2 configs, 1 enabled
        Arguments.of(
            ImmutableList.of(
                new PluginConfiguration("test-A", false, Maps.newHashMap()),
                new PluginConfiguration("test-B", true, Maps.newHashMap())),
            1));
  }

  @ParameterizedTest
  @MethodSource
  public void testInstall(List<PluginConfiguration> configs, int expectActivePlugins)
      throws IOException {
    TestPluginManager manager = new TestPluginManager();
    manager.setConfigs(configs);
    manager.initialize();

    Assertions.assertEquals(expectActivePlugins, manager.activePlugins());
    manager.close();

    Assertions.assertEquals(0, manager.activePlugins());
  }

  @Test
  public void testPluginNotInSpiProvider() {
    List<PluginConfiguration> configs =
        ImmutableList.of(
            new PluginConfiguration("test-A", true, Maps.newHashMap()),
            new PluginConfiguration("test-B", true, Maps.newHashMap()),
            new PluginConfiguration("test-C", true, Maps.newHashMap()));
    TestPluginManager manager = new TestPluginManager();
    manager.setConfigs(configs);

    Assertions.assertThrows(LoadingPluginException.class, manager::initialize);
  }

  @Test
  public void testPluginDuplicateInstall() {
    List<PluginConfiguration> configs =
        ImmutableList.of(
            new PluginConfiguration("test-A", true, Maps.newHashMap()),
            new PluginConfiguration("test-B", true, Maps.newHashMap()),
            new PluginConfiguration("test-B", true, Maps.newHashMap()));
    TestPluginManager manager = new TestPluginManager();
    manager.setConfigs(configs);

    Assertions.assertThrows(AlreadyExistsException.class, manager::initialize);
  }
}
