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

package org.apache.amoro.config.shade.utils;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.shade.ConfigShade;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

/** Config shade utilities. */
public final class ConfigShadeUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigShadeUtils.class);

  private static final Map<String, ConfigShade> CONFIG_SHADES = new HashMap<>();

  private static final ConfigShade DEFAULT_SHADE = new DefaultConfigShade();

  public static final ConfigOption<String> SHADE_IDENTIFIER_OPTION =
      ConfigOptions.key("shade.identifier")
          .stringType()
          .defaultValue("default")
          .withDescription(
              "The identifier of the encryption method for decryption. Defaults to \"default\", indicating no encryption");

  public static final ConfigOption<List<String>> SHADE_SENSITIVE_KEYWORDS =
      ConfigOptions.key("shade.sensitive-keywords")
          .stringType()
          .asList()
          .defaultValues("admin-password", "database.password")
          .withDescription(
              "A semicolon-separated list of keywords for the configuration items to be decrypted.");

  static {
    ServiceLoader<ConfigShade> serviceLoader = ServiceLoader.load(ConfigShade.class);
    Iterator<ConfigShade> it = serviceLoader.iterator();
    it.forEachRemaining(
        configShade -> {
          CONFIG_SHADES.put(configShade.getIdentifier(), configShade);
          LOG.info(
              "Load config shade spi [{}] from {}",
              configShade.getIdentifier(),
              configShade.getClass());
        });
  }

  @VisibleForTesting
  public static String decryptOption(String identifier, String content) {
    ConfigShade configShade = CONFIG_SHADES.getOrDefault(identifier, DEFAULT_SHADE);
    return configShade.decrypt(content);
  }

  public static Map<String, Object> decryptConfig(Map<String, Object> configMap) throws Exception {
    Configurations serviceConfig = Configurations.fromObjectMap(configMap);
    String identifier = serviceConfig.get(SHADE_IDENTIFIER_OPTION);
    List<String> sensitiveOptions = serviceConfig.get(SHADE_SENSITIVE_KEYWORDS);
    return decryptConfig(identifier, configMap, sensitiveOptions, serviceConfig);
  }

  public static Map<String, Object> decryptConfig(
      String identifier,
      Map<String, Object> configMap,
      List<String> sensitiveOptions,
      Configurations serviceConfig)
      throws Exception {
    ConfigShade configShade = CONFIG_SHADES.get(identifier);
    if (configShade == null) {
      LOG.error("Can not find config shade: {}", identifier);
      throw new IllegalStateException("Can not find config shade: " + identifier);
    }
    configShade.initialize(serviceConfig);

    if (DEFAULT_SHADE.getIdentifier().equals(configShade.getIdentifier())) {
      return configMap;
    }

    LOG.info("Use config shade: {}", identifier);
    BiFunction<String, Object, String> processFunction =
        (key, value) -> configShade.decrypt(value.toString());

    for (String sensitiveOption : sensitiveOptions) {
      try {
        configMap.computeIfPresent(sensitiveOption, processFunction);
      } catch (Exception e) {
        LOG.error("Failed to decrypt sensitive option {}:", sensitiveOption, e);
        throw e;
      }
    }
    LOG.info(
        "Sensitive option{} {} {} been decrypted and refreshed",
        sensitiveOptions.size() > 1 ? "s" : "",
        sensitiveOptions,
        sensitiveOptions.size() > 1 ? "have" : "has");

    return configMap;
  }

  /** Default ConfigShade. */
  public static class DefaultConfigShade implements ConfigShade {
    private static final String IDENTIFIER = "default";

    @Override
    public String getIdentifier() {
      return IDENTIFIER;
    }

    @Override
    public String decrypt(String content) {
      return content;
    }
  }
}
