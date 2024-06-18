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

package org.apache.amoro.server.terminal.kyuubi;

import org.apache.amoro.api.config.ConfigOption;
import org.apache.amoro.api.config.ConfigOptions;
import org.apache.amoro.api.config.Configurations;
import org.apache.amoro.server.terminal.SparkContextUtil;
import org.apache.amoro.server.terminal.TerminalSession;
import org.apache.amoro.server.terminal.TerminalSessionFactory;
import org.apache.amoro.shade.guava32.com.google.common.base.Joiner;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.TableMetaStore;
import org.apache.kyuubi.jdbc.KyuubiHiveDriver;
import org.apache.kyuubi.jdbc.hive.JdbcConnectionParams;
import org.apache.kyuubi.jdbc.hive.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class KyuubiTerminalSessionFactory implements TerminalSessionFactory {

  private static final Logger LOG = LoggerFactory.getLogger(KyuubiTerminalSessionFactory.class);

  public static ConfigOption<Boolean> KERBEROS_ENABLE =
      ConfigOptions.key("kerberos.enabled").booleanType().defaultValue(false);

  public static ConfigOption<Boolean> KERBEROS_PROXY_ENABLE =
      ConfigOptions.key("kerberos.proxy.enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription("proxy principal to kyuubi server instead of auth by client");

  public static ConfigOption<String> KERBEROS_DEFAULT_PRINCIPAL =
      ConfigOptions.key("kerberos.default.principal")
          .stringType()
          .noDefaultValue()
          .withDescription("principal to use when connection kerberos info is lack");

  public static ConfigOption<String> KERBEROS_DEFAULT_KEYTAB =
      ConfigOptions.key("kerberos.default.keytab")
          .stringType()
          .noDefaultValue()
          .withDescription(
              "keytab file location to use when connection kerberos info is " + "lack");

  public static ConfigOption<String> KYUUBI_URL =
      ConfigOptions.key("jdbc.url").stringType().noDefaultValue();

  public static ConfigOption<String> KYUUBI_USERNAME =
      ConfigOptions.key("jdbc.username").stringType().noDefaultValue();

  public static ConfigOption<String> KYUUBI_PASSWORD =
      ConfigOptions.key("jdbc.password").stringType().noDefaultValue();

  private String jdbcUrl;
  private boolean kyuubiKerberosEnable;
  private boolean proxyKerberosEnable;
  private String username;
  private String password;

  private JdbcConnectionParams params;
  final KyuubiHiveDriver driver = new KyuubiHiveDriver();

  @Override
  public void initialize(Configurations properties) {
    this.jdbcUrl =
        properties
            .getOptional(KYUUBI_URL)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "lack require properties: jdbc.url. when kyuubi as terminal backend, this is require"));
    this.kyuubiKerberosEnable = properties.get(KERBEROS_ENABLE);
    this.proxyKerberosEnable = properties.getBoolean(KERBEROS_PROXY_ENABLE);
    this.username = properties.get(KYUUBI_USERNAME);
    this.password = properties.get(KYUUBI_PASSWORD);
    try {
      this.params = Utils.extractURLComponents(jdbcUrl, new Properties());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TerminalSession create(TableMetaStore metaStore, Configurations configuration) {
    List<String> logs = Lists.newArrayList();
    JdbcConnectionParams connectionParams = new JdbcConnectionParams(this.params);
    if (metaStore.isKerberosAuthMethod()) {
      checkAndFillKerberosInfo(connectionParams, metaStore);
    }

    Map<String, String> sparkConf = SparkContextUtil.getSparkConf(configuration);
    sparkConf.forEach((k, v) -> connectionParams.getHiveVars().put(k, v));

    String kyuubiJdbcUrl = getConnectionUrl(connectionParams);
    logMessage(logs, "try to create a kyuubi connection via url: " + kyuubiJdbcUrl);
    logMessage(logs, "");

    Map<String, String> sessionConf = configuration.toMap();
    sessionConf.put("jdbc.url", kyuubiJdbcUrl);
    Properties properties = new Properties();

    if (!metaStore.isKerberosAuthMethod() && Objects.nonNull(metaStore.getHadoopUsername())) {
      properties.put(JdbcConnectionParams.AUTH_USER, metaStore.getHadoopUsername());
      sessionConf.put(JdbcConnectionParams.AUTH_USER, metaStore.getHadoopUsername());
    }

    Connection connection = metaStore.doAs(() -> driver.connect(kyuubiJdbcUrl, properties));
    return new KyuubiSession(connection, logs, sessionConf);
  }

  private String getConnectionUrl(JdbcConnectionParams params) {
    StringBuilder kyuubiConnectionUrl =
        new StringBuilder("jdbc:hive2://" + params.getSuppliedURLAuthority() + "/;");

    if (!params.getSessionVars().isEmpty()) {
      kyuubiConnectionUrl.append(mapAsParams(params.getSessionVars()));
    }

    if (!params.getHiveConfs().isEmpty()) {
      kyuubiConnectionUrl.append("#").append(mapAsParams(params.getHiveConfs()));
    }
    if (!params.getHiveVars().isEmpty()) {
      kyuubiConnectionUrl.append("?").append(mapAsParams(params.getHiveVars()));
    }
    return kyuubiConnectionUrl.toString();
  }

  private String mapAsParams(Map<String, String> vars) {
    List<String> kvList =
        vars.entrySet().stream()
            .map(kv -> kv.getKey() + "=" + kv.getValue())
            .collect(Collectors.toList());
    return Joiner.on(";").join(kvList);
  }

  private void logMessage(List<String> logs, String message) {
    logs.add(message);
    LOG.info(message);
  }

  private void checkAndFillKerberosInfo(
      JdbcConnectionParams connectionParams, TableMetaStore metaStore) {
    if (connectionParams.getSessionVars().containsKey(JdbcConnectionParams.AUTH_PRINCIPAL)) {
      throw new RuntimeException(
          "jdbc url should not contain principal when kyuubi kerberos enable");
    }
    connectionParams
        .getSessionVars()
        .put(
            JdbcConnectionParams.AUTH_KERBEROS_AUTH_TYPE,
            JdbcConnectionParams.AUTH_KERBEROS_AUTH_TYPE_FROM_SUBJECT);
    connectionParams
        .getSessionVars()
        .put(JdbcConnectionParams.AUTH_PRINCIPAL, metaStore.getKrbPrincipal());
  }
}
