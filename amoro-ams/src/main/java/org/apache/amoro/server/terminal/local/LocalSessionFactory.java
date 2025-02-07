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

package org.apache.amoro.server.terminal.local;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.CatalogType;
import org.apache.amoro.server.dashboard.utils.DesensitizationUtil;
import org.apache.amoro.server.terminal.SparkContextUtil;
import org.apache.amoro.server.terminal.TerminalSession;
import org.apache.amoro.server.terminal.TerminalSessionFactory;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableMetaStore;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SQLConf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalSessionFactory implements TerminalSessionFactory {

  static final Set<String> STATIC_SPARK_CONF =
      Collections.unmodifiableSet(Sets.newHashSet("spark.sql.extensions"));
  static final Set<String> EXTERNAL_CONNECTORS =
      Collections.unmodifiableSet(Sets.newHashSet("iceberg", "paimon"));
  static final String SPARK_CONF_PREFIX = "spark.";

  public static ConfigOption<Integer> SPARK_CORES =
      ConfigOptions.key("cores").intType().defaultValue(1);

  SparkSession context = null;
  Configurations conf;
  List<String> sensitiveConfKeys;

  @Override
  public void initialize(Configurations properties) {
    this.conf = properties;
    this.sensitiveConfKeys =
        Arrays.stream(
                properties.getString(AmoroManagementConf.TERMINAL_SENSITIVE_CONF_KEYS).split(","))
            .map(String::trim)
            .collect(Collectors.toList());
  }

  @Override
  public TerminalSession create(TableMetaStore metaStore, Configurations configuration) {
    SparkSession context = lazyInitContext();
    SparkSession session = context.cloneSession();
    List<String> catalogs = configuration.get(SessionConfigOptions.CATALOGS);
    List<String> initializeLogs = Lists.newArrayList();
    initializeLogs.add("setup session, session factory: " + LocalSessionFactory.class.getName());

    Map<String, String> sparkConf = SparkContextUtil.getSparkConf(configuration);
    sparkConf.put(SparkContextUtil.MIXED_FORMAT_PROPERTY_REFRESH_BEFORE_USAGE, "true");

    Map<String, String> finallyConf = configuration.toMap();
    catalogs.stream()
        .filter(c -> isExternalConnector(c, configuration) || isExternalMetastore(c, configuration))
        .forEach(c -> setHadoopConfigToSparkSession(c, session, metaStore));

    for (String key : sparkConf.keySet()) {
      if (STATIC_SPARK_CONF.contains(key)) {
        continue;
      }
      updateSessionConf(session, initializeLogs, key, sparkConf.get(key));
      finallyConf.put(key, sparkConf.get(key));
    }

    return new LocalTerminalSession(catalogs, session, initializeLogs, finallyConf);
  }

  private boolean isExternalMetastore(String catalog, Configurations configurations) {
    String metastoreType =
        configurations.get(SessionConfigOptions.catalogConnector(catalog)).toLowerCase();
    return !metastoreType.equalsIgnoreCase(CatalogType.AMS.name());
  }

  private boolean isExternalConnector(String catalog, Configurations configurations) {
    String connector =
        configurations
            .get(TerminalSessionFactory.SessionConfigOptions.catalogConnector(catalog))
            .toLowerCase();
    return EXTERNAL_CONNECTORS.contains(connector);
  }

  private void setHadoopConfigToSparkSession(
      String catalog, SparkSession session, TableMetaStore metaStore) {
    org.apache.hadoop.conf.Configuration metaConf = metaStore.getConfiguration();
    for (Map.Entry<String, String> next : metaConf) {
      session
          .conf()
          .set("spark.sql.catalog." + catalog + ".hadoop." + next.getKey(), next.getValue());
    }
  }

  private void updateSessionConf(
      SparkSession session, List<String> logs, String key, String value) {
    session.conf().set(key, value);
    if (sensitiveConfKeys.contains(key)) {
      logs.add(key + "  " + DesensitizationUtil.desensitize(value));
    } else {
      logs.add(key + "  " + value);
    }
  }

  protected synchronized SparkSession lazyInitContext() {
    Preconditions.checkNotNull(this.conf);
    if (context == null) {
      SparkConf sparkconf = new SparkConf().setAppName("spark-local-context");
      sparkconf.setMaster("local[" + conf.getInteger(SPARK_CORES) + "]");
      sparkconf.set(SQLConf.PARTITION_OVERWRITE_MODE().key(), "dynamic");
      sparkconf.set("spark.executor.heartbeatInterval", "100s");
      sparkconf.set("spark.network.timeout", "200s");
      sparkconf.set(
          "spark.sql.extensions",
          SparkContextUtil.MIXED_FORMAT_EXTENSION + "," + SparkContextUtil.ICEBERG_EXTENSION);
      sparkconf.set("spark.cleaner.referenceTracking", "false");

      for (String key : this.conf.keySet()) {
        if (key.startsWith(SPARK_CONF_PREFIX)) {
          String value = this.conf.getValue(ConfigOptions.key(key).stringType().noDefaultValue());
          sparkconf.set(key, value);
        }
      }

      context = SparkSession.builder().config(sparkconf).getOrCreate();
    }

    return context;
  }
}
