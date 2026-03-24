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

package org.apache.amoro.server.authorization;

import io.javalin.http.Context;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DashboardPrivilegeMapper {
  private static final String MAPPING_RESOURCE = "authorization/privilege_mapping.yaml";

  private final List<DashboardPrivilegeMappingRule> mappingRules;

  public DashboardPrivilegeMapper() {
    this.mappingRules = loadRules();
  }

  public Optional<AuthorizationRequest> resolve(Context ctx) {
    String method = ctx.method().toUpperCase();
    String path = ctx.path();
    return mappingRules.stream()
        .filter(rule -> rule.matches(method, path))
        .map(DashboardPrivilegeMappingRule::getAuthorizationRequest)
        .findFirst();
  }

  private static List<DashboardPrivilegeMappingRule> loadRules() {
    Map<String, Object> root = loadYaml(MAPPING_RESOURCE);
    Object mappingsObject = root.get("mappings");
    if (!(mappingsObject instanceof List)) {
      throw new IllegalArgumentException(
          "Invalid dashboard privilege mapping resource: missing mappings list");
    }

    List<?> mappings = (List<?>) mappingsObject;
    return mappings.stream()
        .map(item -> toRule(castMap(item, "mapping entry")))
        .collect(Collectors.toList());
  }

  private static DashboardPrivilegeMappingRule toRule(Map<String, Object> ruleMap) {
    Set<String> methods = toMethodSet(ruleMap.get("methods"));
    Set<String> paths = toStringSet(ruleMap.get("paths"));
    Set<String> prefixes = toStringSet(ruleMap.get("prefixes"));
    String resourceTypeValue = requireString(ruleMap, "resource-type");
    String privilegeValue = requireString(ruleMap, "privilege");
    String resourceId =
        Optional.ofNullable(ruleMap.get("resource-id"))
            .map(String::valueOf)
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .orElse(AuthorizationRequest.GLOBAL_RESOURCE_ID);
    Preconditions.checkArgument(
        !paths.isEmpty() || !prefixes.isEmpty(),
        "Dashboard privilege mapping requires at least one path or prefix");

    AuthorizationRequest request =
        AuthorizationRequest.of(
            ResourceType.valueOf(resourceTypeValue.trim().toUpperCase()),
            resourceId,
            Privilege.valueOf(privilegeValue.trim().toUpperCase()));
    return new DashboardPrivilegeMappingRule(methods, paths, prefixes, request);
  }

  private static Set<String> toStringSet(Object value) {
    if (value == null) {
      return Collections.emptySet();
    }
    if (!(value instanceof List)) {
      throw new IllegalArgumentException(
          "Invalid dashboard privilege mapping resource: expected list but found " + value);
    }
    return ((List<?>) value)
        .stream()
            .map(String::valueOf)
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static Set<String> toMethodSet(Object value) {
    return toStringSet(value).stream()
        .map(String::toUpperCase)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static String requireString(Map<String, Object> source, String key) {
    Object value = source.get(key);
    Preconditions.checkArgument(value != null, "Missing required mapping field: %s", key);
    String text = String.valueOf(value).trim();
    Preconditions.checkArgument(!text.isEmpty(), "Missing required mapping field: %s", key);
    return text;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Object value, String label) {
    if (!(value instanceof Map)) {
      throw new IllegalArgumentException(
          "Invalid dashboard privilege mapping resource: " + label + " must be a map");
    }
    return (Map<String, Object>) value;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> loadYaml(String resource) {
    Yaml yaml = new Yaml();
    String content = readResource(resource);
    Object loaded = yaml.load(content);
    if (!(loaded instanceof Map)) {
      throw new IllegalArgumentException(
          "Invalid dashboard privilege mapping resource: root must be a map");
    }
    return (Map<String, Object>) loaded;
  }

  private static String readResource(String resource) {
    try (InputStream inputStream =
            DashboardPrivilegeMapper.class.getClassLoader().getResourceAsStream(resource);
        InputStreamReader inputStreamReader =
            new InputStreamReader(
                Objects.requireNonNull(inputStream, "Missing resource: " + resource),
                StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load authorization resource: " + resource, e);
    }
  }
}
