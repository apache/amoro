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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.server.manager.PluginConfiguration;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.utils.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Pins the naming contract between the two Paimon {@link ProcessFactory} SPI implementations:
 *
 * <ul>
 *   <li>{@code amoro-format-paimon}'s {@code PaimonProcessFactory} (optimizing) — name {@code
 *       "paimon"}.
 *   <li>{@code amoro-ams}'s {@code PaimonMaintainProcessFactory} (sync-meta / maintenance) — name
 *       {@code "paimon-maintain"}.
 * </ul>
 *
 * <p>Both jars land on AMS's runtime classpath (the AMS pom depends on {@code amoro-format-paimon}
 * in compile scope), so a single {@link ServiceLoader#load(Class)} call will discover <em>both</em>
 * implementations. {@code AbstractPluginManager.addToFoundedPlugin(...)} then rejects two plugins
 * reporting the same {@code name()} but different classes with an {@link IllegalStateException}. If
 * anyone reverts {@link PaimonMaintainProcessFactory#PLUGIN_NAME} back to {@code "paimon"} the
 * {@code distinctPluginNames()} assertion below will fail fast at build time — well before AMS
 * startup would blow up.
 */
public class TestPaimonPluginLoading {

  @Test
  public void yamlInstallsPaimonOptimizerFactoryDisabledByDefault() throws Exception {
    List<PluginConfiguration> configs = loadProcessFactoryConfigurationsForTest();
    PluginConfiguration paimon =
        configs.stream()
            .filter(c -> "paimon".equals(c.getName()))
            .findFirst()
            .orElseThrow(
                () -> new AssertionError("process-factories.yaml must contain name: paimon"));

    assertTrue(
        paimon.isEnabled(), "paimon plugin should be installed so the flag can control routing");
    assertEquals(
        "false",
        paimon.getProperties().get("paimon-optimizer.enabled"),
        "default config must not enable Paimon optimizing");
  }

  @Test
  public void distinctPluginNames() {
    List<ProcessFactory> filtered = loadPaimonFactories();
    Set<String> nameSet = filtered.stream().map(ProcessFactory::name).collect(Collectors.toSet());

    assertEquals(
        filtered.size(),
        nameSet.size(),
        "Paimon ProcessFactory implementations must have distinct plugin names to survive "
            + "AbstractPluginManager.addToFoundedPlugin(); found duplicates: "
            + findDuplicateNames(filtered));
  }

  @Test
  public void paimonFactoriesRegisteredUnderExpectedNames() {
    Set<String> names =
        loadPaimonFactories().stream().map(ProcessFactory::name).collect(Collectors.toSet());

    assertTrue(
        names.contains("paimon"),
        "Expected optimizing factory PaimonProcessFactory to register under 'paimon', "
            + "but discovered Paimon factory names are: "
            + names);
    assertTrue(
        names.contains("paimon-maintain"),
        "Expected maintain factory PaimonMaintainProcessFactory to register under 'paimon-maintain', "
            + "but discovered Paimon factory names are: "
            + names);
  }

  private static List<ProcessFactory> loadPaimonFactories() {
    ServiceLoader<ProcessFactory> loader = ServiceLoader.load(ProcessFactory.class);
    return StreamSupport.stream(loader.spliterator(), false)
        .filter(factory -> factory.getClass().getSimpleName().contains("Paimon"))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private static List<PluginConfiguration> loadProcessFactoryConfigurationsForTest()
      throws Exception {
    Path yamlPath =
        findProjectRoot().resolve("dist/src/main/amoro-bin/conf/plugins/process-factories.yaml");
    Map<String, Object> yamlObj;
    try (InputStream input = Files.newInputStream(yamlPath)) {
      yamlObj = new Yaml().loadAs(input, Map.class);
    }
    JsonNode pluginList = JacksonUtil.fromObjects(yamlObj).get("process-factories");

    List<PluginConfiguration> configs = new ArrayList<>();
    for (int i = 0; i < pluginList.size(); i++) {
      configs.add(PluginConfiguration.fromJSONObject(pluginList.get(i)));
    }
    return configs;
  }

  private static Path findProjectRoot() {
    Path current = Paths.get("").toAbsolutePath();
    while (current != null && !Files.exists(current.resolve("dist/src/main/amoro-bin"))) {
      current = current.getParent();
    }
    if (current == null) {
      throw new IllegalStateException("Cannot locate Amoro project root");
    }
    return current;
  }

  private static Map<String, List<String>> findDuplicateNames(List<ProcessFactory> factories) {
    Map<String, List<String>> byName = new HashMap<>();
    for (ProcessFactory factory : factories) {
      byName
          .computeIfAbsent(factory.name(), key -> new ArrayList<>())
          .add(factory.getClass().getName());
    }
    Map<String, List<String>> duplicates = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : byName.entrySet()) {
      if (new HashSet<>(entry.getValue()).size() > 1) {
        duplicates.put(entry.getKey(), entry.getValue());
      }
    }
    return duplicates;
  }
}
