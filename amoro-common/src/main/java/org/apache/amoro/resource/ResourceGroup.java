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

package org.apache.amoro.resource;

import org.apache.amoro.Constants;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResourceGroup {
  private String name;
  private String container;
  private Map<String, String> properties;

  public static String OPTIMIZE_GROUP_RULE_MATCH_KEY = "match.rules";

  public static final String RULE_SEPARATOR = ",";
  public static final String SPACE_SEPARATOR_REGEXP = "\\\\.";
  public static final String SPACE_SEPARATOR = "\\.";

  public static String getSpaceSeparator(String rule) {
    if (rule.contains("\\.")) {
      return SPACE_SEPARATOR_REGEXP;
    } else {
      return SPACE_SEPARATOR;
    }
  }

  public static boolean validateRule(String rule) {
    return rule.split(SPACE_SEPARATOR_REGEXP).length == 3
        || rule.split(SPACE_SEPARATOR).length == 3;
  }

  protected ResourceGroup() {}

  private ResourceGroup(String name, String container) {
    this.name = name;
    this.container = container;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  protected void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getContainer() {
    return container;
  }

  public String getOptimizeGroupRule() {
    return this.properties.getOrDefault(OPTIMIZE_GROUP_RULE_MATCH_KEY, null);
  }

  public boolean match(TableIdentifier tableIdentifier) {
    return match(this.properties, tableIdentifier);
  }

  /**
   * if only numbers, letters, and underscores are contained, then the rule is not regexp and the
   * rule will overwrite the regexp rule
   */
  private boolean noRegExp(String rule) {
    String fmtString = rule.replaceAll(getSpaceSeparator(rule), "");
    if (fmtString.replaceAll("[a-zA-Z0-9_]", "").length() == 0) {
      return true;
    }
    return false;
  }

  public List<String> getFullMatchNameInRules() {
    return Arrays.stream(properties.getOrDefault(OPTIMIZE_GROUP_RULE_MATCH_KEY, "").split(","))
        .filter(item -> noRegExp(item))
        .collect(Collectors.toList());
  }

  /**
   * Full match means we manually assign the table to this group.
   *
   * @param tableIdentifier
   * @return
   */
  public boolean fullMatch(TableIdentifier tableIdentifier) {
    final String tableName =
        String.format(
            "%s.%s.%s",
            tableIdentifier.getCatalog(),
            tableIdentifier.getDatabase(),
            tableIdentifier.getTableName());
    return Arrays.stream(properties.getOrDefault(OPTIMIZE_GROUP_RULE_MATCH_KEY, "").split(","))
        .filter(item -> item.contains(tableName))
        .findAny()
        .isPresent();
  }

  private boolean match(Map<String, String> properties, TableIdentifier tableIdentifier) {
    String rulePropertyKey = OPTIMIZE_GROUP_RULE_MATCH_KEY;
    if (properties != null && properties.containsKey(rulePropertyKey)) {
      List<String> matchRules =
          Arrays.stream(properties.getOrDefault(rulePropertyKey, "").split(","))
              .collect(Collectors.toList());

      final String tableName =
          String.format(
              "%s.%s.%s",
              tableIdentifier.getCatalog(),
              tableIdentifier.getDatabase(),
              tableIdentifier.getTableName());

      return matchRules.stream()
          .filter(
              item -> {
                item = item.trim();
                // Compile the regex into a pattern
                Pattern pattern = Pattern.compile(item);
                // Create a matcher for the input
                Matcher matcher = pattern.matcher(tableName);
                return matcher.find();
              })
          .findFirst()
          .isPresent();
    }
    return false;
  }

  // generate inner builder class, use addProperties instead of set
  public static class Builder {
    private final String name;
    private final String container;
    private final Map<String, String> properties = new HashMap<>();

    public Builder(String name, String container) {
      Preconditions.checkArgument(
          name != null && container != null,
          "Resource group name and container name can not be null");
      this.name = name;
      this.container = container;
    }

    public Builder(String name) {
      Preconditions.checkArgument(name != null, "Resource group name can not be null");
      this.name = name;
      this.container = Constants.EXTERNAL_RESOURCE_CONTAINER;
    }

    public String getName() {
      return name;
    }

    public String getContainer() {
      return container;
    }

    public Builder addProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
    }

    public Builder addProperties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    public ResourceGroup build() {
      ResourceGroup resourceGroup = new ResourceGroup(name, container);
      resourceGroup.setProperties(properties);
      return resourceGroup;
    }
  }
}
