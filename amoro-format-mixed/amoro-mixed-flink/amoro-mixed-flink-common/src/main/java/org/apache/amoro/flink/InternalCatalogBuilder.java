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

package org.apache.amoro.flink;

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HIVE;
import static org.apache.iceberg.flink.FlinkCatalogFactory.HADOOP_CONF_DIR;
import static org.apache.iceberg.flink.FlinkCatalogFactory.HIVE_CONF_DIR;

import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Strings;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.ConfigurationFileUtil;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.runtime.util.HadoopUtils;
import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.util.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.flink.FlinkCatalogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Build {@link MixedFormatCatalog}. */
public class InternalCatalogBuilder implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(InternalCatalogBuilder.class);

  private String metastoreUrl;
  private Map<String, String> properties = new HashMap<>(0);
  private String catalogName;

  private MixedFormatCatalog createMixedFormatCatalog() {
    if (metastoreUrl != null) {
      return CatalogLoader.load(metastoreUrl, properties);
    } else {
      Preconditions.checkArgument(catalogName != null, "Catalog name cannot be empty");
      String metastoreType = properties.get(FlinkCatalogFactory.ICEBERG_CATALOG_TYPE);
      Preconditions.checkArgument(metastoreType != null, "Catalog type cannot be empty");
      TableMetaStore tableMetaStore =
          TableMetaStore.builder()
              .withConfiguration(clusterHadoopConf(metastoreType, properties))
              .build();
      return CatalogLoader.createCatalog(catalogName, metastoreType, properties, tableMetaStore);
    }
  }

  public static Configuration clusterHadoopConf(
      String metastoreType, Map<String, String> properties) {
    Configuration configuration =
        HadoopUtils.getHadoopConfiguration(GlobalConfiguration.loadConfiguration());
    if (ICEBERG_CATALOG_TYPE_HIVE.equals(metastoreType)) {
      String hiveConfDir = properties.get(HIVE_CONF_DIR);
      String hadoopConfDir = properties.get(HADOOP_CONF_DIR);
      configuration = mergeHiveConf(configuration, hiveConfDir, hadoopConfDir);
    }
    return configuration;
  }

  private static Configuration mergeHiveConf(
      Configuration hadoopConf, String hiveConfDir, String hadoopConfDir) {
    Configuration newConf = new Configuration(hadoopConf);
    if (!Strings.isNullOrEmpty(hiveConfDir)) {
      Preconditions.checkState(
          Files.exists(Paths.get(hiveConfDir, "hive-site.xml")),
          "There should be a hive-site.xml file under the directory %s",
          hiveConfDir);
      newConf.addResource(new Path(hiveConfDir, "hive-site.xml"));
    } else {
      // If don't provide the hive-site.xml path explicitly, it will try to load resource from
      // classpath. If still
      // couldn't load the configuration file, then it will throw exception in HiveCatalog.
      URL configFile = InternalCatalogBuilder.class.getClassLoader().getResource("hive-site.xml");
      if (configFile != null) {
        newConf.addResource(configFile);
      }
    }

    if (!Strings.isNullOrEmpty(hadoopConfDir)) {
      Preconditions.checkState(
          Files.exists(Paths.get(hadoopConfDir, "hdfs-site.xml")),
          "Failed to load Hadoop configuration: missing %s",
          Paths.get(hadoopConfDir, "hdfs-site.xml"));
      newConf.addResource(new Path(hadoopConfDir, "hdfs-site.xml"));
      Preconditions.checkState(
          Files.exists(Paths.get(hadoopConfDir, "core-site.xml")),
          "Failed to load Hadoop configuration: missing %s",
          Paths.get(hadoopConfDir, "core-site.xml"));
      newConf.addResource(new Path(hadoopConfDir, "core-site.xml"));
    }

    return newConf;
  }

  public String getMetastoreUrl() {
    return metastoreUrl;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public InternalCatalogBuilder() {}

  public static InternalCatalogBuilder builder() {
    return new InternalCatalogBuilder();
  }

  public MixedFormatCatalog build() {
    return createMixedFormatCatalog();
  }

  public InternalCatalogBuilder metastoreUrl(String metastoreUrl) {
    this.metastoreUrl = metastoreUrl;
    return this;
  }

  public InternalCatalogBuilder properties(Map<String, String> properties) {
    Map<String, String> finalProperties = new HashMap<>();
    for (Map.Entry<String, String> property : properties.entrySet()) {
      String key = property.getKey();
      String value = property.getValue();
      switch (key) {
        case CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB_PATH:
          try {
            finalProperties.put(
                CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB,
                ConfigurationFileUtil.encodeConfigurationFileWithBase64(value));
          } catch (IOException e) {
            LOG.error("encode keytab file failed", e);
            throw new CatalogException("encode keytab file failed", e);
          }
          break;
        case CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB_ENCODE:
          finalProperties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB, value);
          break;
        case CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB_PATH:
          try {
            finalProperties.put(
                CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5,
                ConfigurationFileUtil.encodeConfigurationFileWithBase64(value));
          } catch (IOException e) {
            LOG.error("encode krb5 file failed", e);
            throw new CatalogException("encode krb5 file failed", e);
          }
          break;
        case CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB_ENCODE:
          finalProperties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5, value);
          break;
        default:
          finalProperties.put(key, value);
          break;
      }
    }
    this.properties = finalProperties;
    return this;
  }

  public InternalCatalogBuilder catalogName(String catalogName) {
    this.catalogName = catalogName;
    return this;
  }
}
