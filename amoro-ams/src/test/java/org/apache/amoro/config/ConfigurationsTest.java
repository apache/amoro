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

package org.apache.amoro.config;

import org.apache.amoro.config.shade.utils.ConfigShadeUtils;
import org.apache.amoro.server.AmoroManagementConf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * End-to-end test cases for configuration documentation.
 *
 * <p>The golden result file is "docs/configuration/".
 *
 * <p>To run the test suite:
 *
 * <pre>
 * ./mvnw test -pl amoro-ams -am -Dtest=ConfigurationsTest
 * </pre>
 *
 * <p>To re-generate golden file, run:
 *
 * <pre>
 * UPDATE=1 ./mvnw test -pl amoro-ams -am -Dtest=ConfigurationsTest
 * </pre>
 */
public class ConfigurationsTest {
  public static String CONFIGURATION_DOCS_PATH = "docs/configuration";
  public static String UPDATE_CMD =
      "UPDATE=1 ./mvnw test -pl amoro-ams -am -Dtest=ConfigurationsTest";

  @Test
  public void testAmoroManagementConfDocumentation() throws Exception {
    List<AmoroConfInfo> confInfoList = new ArrayList<>();
    confInfoList.add(
        new AmoroConfInfo(
            AmoroManagementConf.class,
            "Amoro Management Service Configuration",
            "The configuration options for Amoro Management Service (AMS)."));
    confInfoList.add(
        new AmoroConfInfo(
            ConfigShadeUtils.class,
            "Shade Utils Configuration",
            "The configuration options for Amoro Configuration Shade Utils."));
    generateConfigurationMarkdown("ams-config.md", "AMS Configuration", 100, confInfoList);
  }

  @Test
  public void testGetDurationInMillis() throws Exception {
    Properties properties = new Properties();
    properties.put(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT.key(), "1h");
    Configurations configuration = ConfigHelpers.createConfiguration(properties);
    long durationInMillis =
        configuration.getDurationInMillis(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT);
    Assertions.assertEquals(3600000, durationInMillis);

    // default value test
    properties = new Properties();
    configuration = ConfigHelpers.createConfiguration(properties);
    durationInMillis =
        configuration.getDurationInMillis(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT);
    Assertions.assertEquals(Integer.MAX_VALUE * 1000L, durationInMillis);

    properties.put(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT.key(), Long.MAX_VALUE + "m");
    final Configurations conf1 = ConfigHelpers.createConfiguration(properties);
    Assertions.assertThrows(
        ConfigurationException.class,
        () -> conf1.getDurationInMillis(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT));

    properties.put(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT.key(), "-1m");
    final Configurations conf2 = ConfigHelpers.createConfiguration(properties);
    Assertions.assertThrows(
        ConfigurationException.class,
        () -> conf2.getDurationInMillis(AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT));
  }

  /**
   * Generate configuration documentation for multiple configuration classes.
   *
   * @param markdown The markdown file name to generate
   * @param title The title for the markdown document
   * @param weight The weight for ordering in the menu
   * @param confInfoList List of configuration class information
   * @throws Exception if generation fails
   */
  protected void generateConfigurationMarkdown(
      String markdown, String title, int weight, List<AmoroConfInfo> confInfoList)
      throws Exception {
    Path markdownFile = getMarkdownFilePath(CONFIGURATION_DOCS_PATH + "/" + markdown);
    List<String> output = new ArrayList<>();

    appendFrontHeader(output, title, markdown, weight);
    appendLicenseHeader(output);
    output.add("<!-- This file is auto-generated. To update, run: " + UPDATE_CMD + " -->");
    output.add("");
    output.add("# " + title);
    output.add("");

    for (AmoroConfInfo confInfo : confInfoList) {
      output.add("## " + confInfo.title);
      output.add("");
      output.add(confInfo.description);
      output.add("");
      appendTableStyle(output);
      appendConfigurationTableHeader(output);

      List<ConfigOptionInfo> configOptions = extractConfigOptions(confInfo.confClass);
      configOptions.sort(Comparator.comparing(c -> c.key));

      for (ConfigOptionInfo info : configOptions) {
        String row =
            String.format(
                "| %s | %s | %s |",
                escape(info.key), escape(info.defaultValue), escape(info.description));
        output.add(row);
      }

      // Add some space between different configuration sections
      output.add("");
      output.add("");
    }

    verifyOutput(markdownFile, output);
  }

  private Path getMarkdownFilePath(String relativePath) {
    // Get the project root directory
    String projectRoot = System.getProperty("user.dir");
    if (projectRoot.endsWith("amoro-ams")) {
      projectRoot = Paths.get(projectRoot).getParent().toString();
    }
    return Paths.get(projectRoot, relativePath);
  }

  private List<ConfigOptionInfo> extractConfigOptions(Class<?> confClass) {
    List<ConfigOptionInfo> options = new ArrayList<>();

    for (Field field : confClass.getDeclaredFields()) {
      // Only process static ConfigOption fields
      if (!Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      if (!ConfigOption.class.isAssignableFrom(field.getType())) {
        continue;
      }

      try {
        field.setAccessible(true);
        ConfigOption<?> configOption = (ConfigOption<?>) field.get(null);

        ConfigOptionInfo info = new ConfigOptionInfo();
        info.key = configOption.key();
        info.defaultValue =
            Optional.ofNullable(configOption.defaultValue())
                .map(ConfigHelpers::convertToString)
                .orElse("<undefined>");
        info.description = configOption.description();

        options.add(info);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Failed to access field: " + field.getName(), e);
      }
    }

    return options;
  }

  private String escape(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("|", "\\|").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
  }

  private void appendFrontHeader(List<String> output, String title, String markdown, int weight) {
    String baseName = markdown.replace(".md", "");
    output.add("---");
    output.add("title: " + title);
    output.add("url: " + baseName);
    output.add("aliases:");
    output.add("  - \"configuration/" + baseName + "\"");
    output.add("menu:");
    output.add("  main:");
    output.add("    parent: Configuration");
    output.add("    weight: " + weight);
    output.add("---");
  }

  private void appendLicenseHeader(List<String> output) {
    output.add("<!--");
    output.add("  ~ Licensed to the Apache Software Foundation (ASF) under one");
    output.add("  ~ or more contributor license agreements.  See the NOTICE file");
    output.add("  ~ distributed with this work for additional information");
    output.add("  ~ regarding copyright ownership.  The ASF licenses this file");
    output.add("  ~ to you under the Apache License, Version 2.0 (the");
    output.add("  ~ \"License\"); you may not use this file except in compliance");
    output.add("  ~ with the License.  You may obtain a copy of the License at");
    output.add("  ~");
    output.add("  ~     http://www.apache.org/licenses/LICENSE-2.0");
    output.add("  ~");
    output.add("  ~ Unless required by applicable law or agreed to in writing, software");
    output.add("  ~ distributed under the License is distributed on an \"AS IS\" BASIS,");
    output.add("  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
    output.add("  ~ See the License for the specific language governing permissions and");
    output.add("  ~ limitations under the License.");
    output.add("  -->");
  }

  private void appendTableStyle(List<String> output) {
    output.add("<style>");
    output.add("table { width: 100%; table-layout: fixed; }");
    output.add("table th, table td { vertical-align: top; }");
    output.add(
        "table td:first-child, table th:first-child { word-break: normal; width: 40%; }"); // Key
    output.add(
        "table td:nth-child(2), table th:nth-child(2) { width: 20%; word-break: break-all; }"); // Default
    output.add(
        "table td:last-child, table th:last-child { width: 40%; word-break: break-all; }"); // Description
    output.add("</style>");
  }

  private void appendConfigurationTableHeader(List<String> output) {
    output.add("| Key  | Default | Description |");
    output.add("| ---  | ------- | ----------- |");
  }

  private void verifyOutput(Path goldenFile, List<String> output) throws IOException {
    if ("1".equals(System.getenv("UPDATE"))) {
      // Update mode: write the golden file
      Files.createDirectories(goldenFile.getParent());
      Files.write(
          goldenFile,
          output,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
      System.out.println("Updated golden file: " + goldenFile);
    } else {
      // Verify mode: compare with existing golden file
      if (!Files.exists(goldenFile)) {
        Assertions.fail(
            String.format("%s does not exist. Generate it with:\n%s", goldenFile, UPDATE_CMD));
      }

      List<String> expected = Files.readAllLines(goldenFile, StandardCharsets.UTF_8);
      String hint =
          String.format(
              "\n%s is out of date, please update the golden file with:\n\n%s\n",
              goldenFile, UPDATE_CMD);

      Assertions.assertEquals(expected.size(), output.size(), hint + "Number of lines mismatch");

      for (int i = 0; i < expected.size(); i++) {
        Assertions.assertEquals(
            expected.get(i), output.get(i), hint + "Line " + (i + 1) + " mismatch");
      }
    }
  }

  private static class ConfigOptionInfo {
    String key;
    String defaultValue;
    String description;
  }

  /** Class to hold configuration class information for documentation generation. */
  public static class AmoroConfInfo {
    Class<?> confClass;
    String title;
    String description;

    public AmoroConfInfo(Class<?> confClass, String title, String description) {
      this.confClass = confClass;
      this.title = title;
      this.description = description;
    }
  }
}
