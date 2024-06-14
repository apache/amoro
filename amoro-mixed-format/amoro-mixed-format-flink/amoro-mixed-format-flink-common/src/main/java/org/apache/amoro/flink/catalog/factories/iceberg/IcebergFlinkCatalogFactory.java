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

package org.apache.amoro.flink.catalog.factories.iceberg;

import org.apache.amoro.table.TableMetaStore;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.util.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.FlinkCatalogFactory;
import org.apache.iceberg.relocated.com.google.common.base.Strings;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

/** Creating Iceberg Catalog by the hadoop configuration which stored in the AMS. */
public class IcebergFlinkCatalogFactory extends FlinkCatalogFactory {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergFlinkCatalogFactory.class);
  private final Configuration hadoopConf;
  private final TableMetaStore tableMetaStore;

  public IcebergFlinkCatalogFactory(Configuration hadoopConf, TableMetaStore tableMetaStore) {
    this.hadoopConf = hadoopConf;
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public Catalog createCatalog(String name, Map<String, String> properties) {
    String hiveSitePath = tableMetaStore.getHiveSiteLocation().get().getPath();
    properties.put(
        HIVE_CONF_DIR, hiveSitePath.substring(0, hiveSitePath.lastIndexOf(File.separator)));
    CatalogLoader catalogLoader = createCatalogLoader(name, properties, hadoopConf);
    String defaultDatabase = properties.getOrDefault(DEFAULT_DATABASE, DEFAULT_DATABASE_NAME);

    Namespace baseNamespace = Namespace.empty();
    if (properties.containsKey(BASE_NAMESPACE)) {
      baseNamespace = Namespace.of(properties.get(BASE_NAMESPACE).split("\\."));
    }

    boolean cacheEnabled =
        PropertyUtil.propertyAsBoolean(
            properties, CatalogProperties.CACHE_ENABLED, CatalogProperties.CACHE_ENABLED_DEFAULT);

    long cacheExpirationIntervalMs =
        PropertyUtil.propertyAsLong(
            properties,
            CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS,
            CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS_OFF);
    Preconditions.checkArgument(
        cacheExpirationIntervalMs != 0,
        "%s is not allowed to be 0.",
        CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS);

    return new IcebergFlinkCatalog(
        name,
        defaultDatabase,
        baseNamespace,
        catalogLoader,
        cacheEnabled,
        cacheExpirationIntervalMs,
        tableMetaStore);
  }

  static CatalogLoader createCatalogLoader(
      String name, Map<String, String> properties, Configuration hadoopConf) {
    String catalogImpl = properties.get(CatalogProperties.CATALOG_IMPL);
    if (catalogImpl != null) {
      String catalogType = properties.get(ICEBERG_CATALOG_TYPE);
      Preconditions.checkArgument(
          catalogType == null,
          "Cannot create catalog %s, both catalog-type and catalog-impl are set: catalog-type=%s, catalog-impl=%s",
          name,
          catalogType,
          catalogImpl);
      return CatalogLoader.custom(name, properties, hadoopConf, catalogImpl);
    }

    String catalogType = properties.getOrDefault(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HIVE);
    switch (catalogType.toLowerCase(Locale.ENGLISH)) {
      case ICEBERG_CATALOG_TYPE_HIVE:
        // The values of properties 'uri', 'warehouse', 'hive-conf-dir' are allowed to be null, in
        // that case it will
        // fallback to parse those values from hadoop configuration which is loaded from classpath.
        String hiveConfDir = properties.get(HIVE_CONF_DIR);
        String hadoopConfDir = properties.get(HADOOP_CONF_DIR);

        Configuration newHadoopConf = mergeHiveConf(hadoopConf, hiveConfDir, hadoopConfDir);
        return CatalogLoader.hive(name, newHadoopConf, properties);

      case ICEBERG_CATALOG_TYPE_HADOOP:
        return CatalogLoader.hadoop(name, hadoopConf, properties);

      case ICEBERG_CATALOG_TYPE_REST:
        return CatalogLoader.rest(name, hadoopConf, properties);

      default:
        throw new UnsupportedOperationException(
            "Unknown catalog-type: " + catalogType + " (Must be 'hive', 'hadoop' or 'rest')");
    }
  }

  private static Configuration mergeHiveConf(
      Configuration hadoopConf, String hiveConfDir, String hadoopConfDir) {
    Configuration newConf = new Configuration(hadoopConf);
    if (!Strings.isNullOrEmpty(hiveConfDir)) {
      Preconditions.checkState(
          Files.exists(Paths.get(hiveConfDir, "hive-site.xml")),
          "There should be a hive-site.xml file under the directory %s",
          hiveConfDir);
      LOG.info("hiveConfDir is not empty, and hive-site path is {}", hiveConfDir);
      newConf.addResource(new Path(hiveConfDir, "hive-site.xml"));
    } else {
      // If don't provide the hive-site.xml path explicitly, it will try to load resource from
      // classpath. If still
      // couldn't load the configuration file, then it will throw exception in HiveCatalog.
      URL configFile = CatalogLoader.class.getClassLoader().getResource("hive-site.xml");
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
}
