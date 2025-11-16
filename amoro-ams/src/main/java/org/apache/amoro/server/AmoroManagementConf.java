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

package org.apache.amoro.server;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.server.authentication.DefaultPasswdAuthenticationProvider;
import org.apache.amoro.utils.MemorySize;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class AmoroManagementConf {

  public static final ConfigOption<String> SERVER_BIND_HOST =
      ConfigOptions.key("server-bind-host")
          .stringType()
          .defaultValue("0.0.0.0")
          .withDescription("The host bound to the server.");

  public static final ConfigOption<String> SERVER_EXPOSE_HOST =
      ConfigOptions.key("server-expose-host")
          .stringType()
          .defaultValue("")
          .withDescription("The exposed host of the server.");

  public static final ConfigOption<String> SERVER_KEYTAB =
      ConfigOptions.key("server-keytab")
          .stringType()
          .defaultValue("")
          .withDescription("Location of Amoro server's keytab");

  public static final ConfigOption<String> SERVER_PRINCIPAL =
      ConfigOptions.key("server-principal")
          .stringType()
          .defaultValue("")
          .withDescription("Name of the Amoro server's keytab kerberos principal.");

  public static final ConfigOption<Duration> SERVER_KINIT_INTERVAL =
      ConfigOptions.key("server-kinit-interval")
          .durationType()
          .defaultValue(Duration.ofHours(1))
          .withDescription(
              "How often will the Amoro server run `kinit -kt [keytab] [principal]` to renew "
                  + " the local Kerberos credentials cache.");

  public static final ConfigOption<Integer> SERVER_KINIT_MAX_ATTEMPTS =
      ConfigOptions.key("server-kinit-max-attempts")
          .intType()
          .defaultValue(10)
          .withDescription("How many times will `kinit` process retry.");

  public static final ConfigOption<String> ADMIN_USERNAME =
      ConfigOptions.key("admin-username")
          .stringType()
          .defaultValue("admin")
          .withDescription("The administrator account name.");

  public static final ConfigOption<String> ADMIN_PASSWORD =
      ConfigOptions.key("admin-password")
          .stringType()
          .defaultValue("admin")
          .withDescription("The administrator password");

  public static final ConfigOption<Duration> CATALOG_META_CACHE_EXPIRATION_INTERVAL =
      ConfigOptions.key("catalog-meta-cache.expiration-interval")
          .durationType()
          .defaultValue(Duration.ofSeconds(60))
          .withDescription("TTL for catalog metadata.");

  public static final ConfigOption<Integer> TABLE_MANIFEST_IO_THREAD_COUNT =
      ConfigOptions.key("table-manifest-io.thread-count")
          .intType()
          .defaultValue(20)
          .withDescription(
              "Sets the size of the worker pool. The worker pool limits the number of tasks concurrently processing "
                  + "manifests in the base table implementation across all concurrent planning or commit operations.");

  public static final ConfigOption<Integer> TABLE_MANIFEST_IO_PLANNING_THREAD_COUNT =
      ConfigOptions.key("self-optimizing.plan-manifest-io-thread-count")
          .intType()
          .defaultValue(10)
          .withDescription(
              "Sets the size of the worker pool. The worker pool limits the number of tasks concurrently processing "
                  + "manifests in the base table implementation across all concurrent planning operations.");

  public static final ConfigOption<Integer> TABLE_MANIFEST_IO_COMMIT_THREAD_COUNT =
      ConfigOptions.key("self-optimizing.commit-manifest-io-thread-count")
          .intType()
          .defaultValue(10)
          .withDescription(
              "Sets the size of the worker pool. The worker pool limits the number of tasks concurrently processing "
                  + "manifests in the base table implementation across all concurrent commit operations.");

  public static final ConfigOption<Duration> REFRESH_EXTERNAL_CATALOGS_INTERVAL =
      ConfigOptions.key("refresh-external-catalogs.interval")
          .durationType()
          .defaultValue(Duration.ofMinutes(3))
          .withDescription("Interval to refresh the external catalog.");

  public static final ConfigOption<Integer> REFRESH_EXTERNAL_CATALOGS_THREAD_COUNT =
      ConfigOptions.key("refresh-external-catalogs.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription(
              "The number of threads used for discovering tables in external catalogs.");

  public static final ConfigOption<Integer> REFRESH_EXTERNAL_CATALOGS_QUEUE_SIZE =
      ConfigOptions.key("refresh-external-catalogs.queue-size")
          .intType()
          .defaultValue(1000000)
          .withDescription("The queue size of the executors of the external catalog explorer.");

  public static final ConfigOption<Boolean> EXPIRE_SNAPSHOTS_ENABLED =
      ConfigOptions.key("expire-snapshots.enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription("Enable snapshots expiring.");

  public static final ConfigOption<Integer> EXPIRE_SNAPSHOTS_THREAD_COUNT =
      ConfigOptions.key("expire-snapshots.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads used for snapshots expiring.");

  public static final ConfigOption<Boolean> CLEAN_ORPHAN_FILES_ENABLED =
      ConfigOptions.key("clean-orphan-files.enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription("Enable orphan files cleaning.");

  public static final ConfigOption<Integer> CLEAN_ORPHAN_FILES_THREAD_COUNT =
      ConfigOptions.key("clean-orphan-files.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads used for orphan files cleaning.");

  public static final ConfigOption<Duration> CLEAN_ORPHAN_FILES_INTERVAL =
      ConfigOptions.key("clean-orphan-files.interval")
          .durationType()
          .defaultValue(Duration.ofDays(1))
          .withDescription("Interval for cleaning orphan files.");

  public static final ConfigOption<Boolean> CLEAN_DANGLING_DELETE_FILES_ENABLED =
      ConfigOptions.key("clean-dangling-delete-files.enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription("Enable dangling delete files cleaning.");

  public static final ConfigOption<Integer> CLEAN_DANGLING_DELETE_FILES_THREAD_COUNT =
      ConfigOptions.key("clean-dangling-delete-files.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads used for dangling delete files cleaning.");

  public static final ConfigOption<Boolean> SYNC_HIVE_TABLES_ENABLED =
      ConfigOptions.key("sync-hive-tables.enabled")
          .booleanType()
          .defaultValue(false)
          .withDescription("Enable synchronizing Hive tables.");

  public static final ConfigOption<Integer> SYNC_HIVE_TABLES_THREAD_COUNT =
      ConfigOptions.key("sync-hive-tables.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads used for synchronizing Hive tables.");

  public static final ConfigOption<Integer> REFRESH_TABLES_THREAD_COUNT =
      ConfigOptions.key("refresh-tables.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads used for refreshing tables.");

  public static final ConfigOption<Boolean> AUTO_CREATE_TAGS_ENABLED =
      ConfigOptions.key("auto-create-tags.enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription("Enable creating tags.");

  public static final ConfigOption<Integer> AUTO_CREATE_TAGS_THREAD_COUNT =
      ConfigOptions.key("auto-create-tags.thread-count")
          .intType()
          .defaultValue(3)
          .withDescription("The number of threads used for creating tags.");

  public static final ConfigOption<Duration> AUTO_CREATE_TAGS_INTERVAL =
      ConfigOptions.key("auto-create-tags.interval")
          .durationType()
          .defaultValue(Duration.ofMinutes(1))
          .withDescription("Interval for creating tags.");

  public static final ConfigOption<Duration> REFRESH_TABLES_INTERVAL =
      ConfigOptions.key("refresh-tables.interval")
          .durationType()
          .defaultValue(Duration.ofMinutes(1))
          .withDescription("Interval for refreshing table metadata.");

  public static final ConfigOption<Integer> REFRESH_MAX_PENDING_PARTITIONS =
      ConfigOptions.key("refresh-tables.max-pending-partition-count")
          .intType()
          .defaultValue(100)
          .withDescription("Filters will not be used beyond that number of partitions.");

  public static final ConfigOption<Duration> BLOCKER_TIMEOUT =
      ConfigOptions.key("blocker.timeout")
          .durationType()
          .defaultValue(Duration.ofMinutes(1))
          .withDescription("Session timeout. Default unit is milliseconds if not specified.");

  public static final ConfigOption<Boolean> HA_ENABLE =
      ConfigOptions.key("ha.enabled")
          .booleanType()
          .defaultValue(false)
          .withDescription("Whether to enable high availability mode.");

  public static final ConfigOption<String> HA_CLUSTER_NAME =
      ConfigOptions.key("ha.cluster-name")
          .stringType()
          .defaultValue("default")
          .withDescription("Amoro management service cluster name.");

  public static final ConfigOption<String> HA_ZOOKEEPER_ADDRESS =
      ConfigOptions.key("ha.zookeeper-address")
          .stringType()
          .defaultValue("")
          .withDescription("The Zookeeper address used for high availability.");

  public static final ConfigOption<Duration> HA_ZOOKEEPER_SESSION_TIMEOUT =
      ConfigOptions.key("ha.session-timeout")
          .durationType()
          .defaultValue(Duration.ofSeconds(30))
          .withDescription("The Zookeeper session timeout in milliseconds.");

  public static final ConfigOption<Duration> HA_ZOOKEEPER_CONNECTION_TIMEOUT =
      ConfigOptions.key("ha.connection-timeout")
          .durationType()
          .defaultValue(Duration.ofSeconds(300))
          .withDescription("The Zookeeper connection timeout in milliseconds.");

  public static final ConfigOption<Integer> TABLE_SERVICE_THRIFT_BIND_PORT =
      ConfigOptions.key("thrift-server.table-service.bind-port")
          .intType()
          .defaultValue(1260)
          .withDescription("Port that the table service thrift server is bound to.");

  public static final ConfigOption<Integer> OPTIMIZING_SERVICE_THRIFT_BIND_PORT =
      ConfigOptions.key("thrift-server.optimizing-service.bind-port")
          .intType()
          .defaultValue(1261)
          .withDescription("Port that the optimizing service thrift server is bound to.");

  public static final ConfigOption<MemorySize> THRIFT_MAX_MESSAGE_SIZE =
      ConfigOptions.key("thrift-server.max-message-size")
          .memorySizeType()
          .defaultValue(MemorySize.ofMebiBytes(100))
          .withDescription(
              "Maximum message size that the Thrift server can accept. Default unit is bytes if not specified.");

  public static final ConfigOption<Integer> THRIFT_WORKER_THREADS =
      ConfigOptions.key("thrift-server.table-service.worker-thread-count")
          .intType()
          .defaultValue(20)
          .withDescription("The number of worker threads for the Thrift server.");

  public static final ConfigOption<Integer> THRIFT_SELECTOR_THREADS =
      ConfigOptions.key("thrift-server.selector-thread-count")
          .intType()
          .defaultValue(2)
          .withDescription("The number of selector threads for the Thrift server.");

  public static final ConfigOption<Integer> THRIFT_QUEUE_SIZE_PER_THREAD =
      ConfigOptions.key("thrift-server.selector-queue-size")
          .intType()
          .defaultValue(4)
          .withDescription("The number of queue size per selector thread for the Thrift server");

  public static final ConfigOption<Integer> HTTP_SERVER_PORT =
      ConfigOptions.key("http-server.bind-port")
          .intType()
          .defaultValue(19090)
          .withDescription("Port that the Http server is bound to.");

  public static final ConfigOption<String> HTTP_SERVER_REST_AUTH_TYPE =
      ConfigOptions.key("http-server.rest-auth-type")
          .stringType()
          .defaultValue("token")
          .withDescription("The authentication used by REST APIs, token (default) or basic.");

  public static final ConfigOption<Duration> HTTP_SERVER_SESSION_TIMEOUT =
      ConfigOptions.key("http-server.session-timeout")
          .durationType()
          .defaultValue(Duration.ofDays(7))
          .withDescription("Timeout for http session.");

  public static final ConfigOption<String> HTTP_SERVER_AUTH_BASIC_PROVIDER =
      ConfigOptions.key("http-server.auth-basic-provider")
          .stringType()
          .defaultValue(DefaultPasswdAuthenticationProvider.class.getName())
          .withDescription(
              "User-defined password authentication implementation of"
                  + " org.apache.amoro.spi.authentication.PasswdAuthenticationProvider");

  public static final ConfigOption<Integer> OPTIMIZING_COMMIT_THREAD_COUNT =
      ConfigOptions.key("self-optimizing.commit-thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads that self-optimizing uses to submit results.");

  public static final ConfigOption<Integer> OPTIMIZING_RUNTIME_DATA_KEEP_DAYS =
      ConfigOptions.key("self-optimizing.runtime-data-keep-days")
          .intType()
          .defaultValue(30)
          .withDescription(
              "The number of days that self-optimizing runtime data keeps the runtime.");

  public static final ConfigOption<Integer> OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL_HOURS =
      ConfigOptions.key("self-optimizing.runtime-data-expire-interval-hours")
          .intType()
          .defaultValue(1)
          .withDescription(
              "The number of hours that self-optimizing runtime data expire interval.");

  public static final ConfigOption<Boolean> OPTIMIZING_BREAK_QUOTA_LIMIT_ENABLED =
      ConfigOptions.key("self-optimizing.break-quota-limit-enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription(
              "Allow the table to break the quota limit when the resource is sufficient.");

  public static final ConfigOption<Duration> OVERVIEW_CACHE_REFRESH_INTERVAL =
      ConfigOptions.key("overview-cache.refresh-interval")
          .durationType()
          .defaultValue(Duration.ofSeconds(180))
          .withDescription("Interval for refreshing overview cache.");

  public static final ConfigOption<Integer> OVERVIEW_CACHE_MAX_SIZE =
      ConfigOptions.key("overview-cache.max-size")
          .intType()
          .defaultValue(3360)
          .withDescription("Max size of overview cache.");

  public static final ConfigOption<String> DB_TYPE =
      ConfigOptions.key("database.type")
          .stringType()
          .defaultValue("derby")
          .withDescription("Database type.");

  public static final ConfigOption<String> DB_CONNECTION_URL =
      ConfigOptions.key("database.url")
          .stringType()
          .defaultValue("jdbc:derby:/tmp/amoro/derby;create=true")
          .withDescription("Database connection address");

  public static final ConfigOption<Boolean> DB_AUTO_CREATE_TABLES =
      ConfigOptions.key("database.auto-create-tables")
          .booleanType()
          .defaultValue(true)
          .withDescription("Auto init table schema when started");

  public static final ConfigOption<String> DB_DRIVER_CLASS_NAME =
      ConfigOptions.key("database.jdbc-driver-class")
          .stringType()
          .defaultValue("org.apache.derby.jdbc.EmbeddedDriver")
          .withDescription("The JDBC driver class name for connecting to the database.");

  public static final ConfigOption<String> DB_USER_NAME =
      ConfigOptions.key("database.username")
          .stringType()
          .defaultValue("root")
          .withDescription("The username for connecting to the database.");

  public static final ConfigOption<String> DB_PASSWORD =
      ConfigOptions.key("database.password")
          .stringType()
          .defaultValue("")
          .withDescription("The password for connecting to the database.");

  public static final ConfigOption<Integer> DB_CONNECT_MAX_TOTAL =
      ConfigOptions.key("database.connection-pool-max-total")
          .intType()
          .defaultValue(20)
          .withDescription("Max connect count of database connect pool.");

  public static final ConfigOption<Integer> DB_CONNECT_MAX_IDLE =
      ConfigOptions.key("database.connection-pool-max-idle")
          .intType()
          .defaultValue(16)
          .withDescription("Max idle connect count of database connect pool.");

  public static final ConfigOption<Long> DB_CONNECT_MAX_WAIT_MILLIS =
      ConfigOptions.key("database.connection-pool-max-wait-millis")
          .longType()
          .defaultValue(30000L)
          .withDescription("Max wait time before getting a connection timeout.");

  public static final ConfigOption<Duration> OPTIMIZER_HB_TIMEOUT =
      ConfigOptions.key("optimizer.heart-beat-timeout")
          .durationType()
          .defaultValue(Duration.ofMinutes(1))
          .withDescription("Timeout duration for Optimizer heartbeat.");

  public static final ConfigOption<Duration> OPTIMIZER_TASK_ACK_TIMEOUT =
      ConfigOptions.key("optimizer.task-ack-timeout")
          .durationType()
          .defaultValue(Duration.ofSeconds(30))
          .withDescription("Timeout duration for task acknowledgment.");

  public static final ConfigOption<Duration> OPTIMIZER_TASK_EXECUTE_TIMEOUT =
      ConfigOptions.key("optimizer.task-execute-timeout")
          .durationType()
          .defaultValue(Duration.ofHours(1))
          .withDescription("Timeout duration for task execution, default to 1 hour.");

  public static final ConfigOption<Integer> OPTIMIZER_MAX_PLANNING_PARALLELISM =
      ConfigOptions.key("optimizer.max-planning-parallelism")
          .intType()
          .defaultValue(1)
          .withDescription("Max planning parallelism in one optimizer group.");

  public static final ConfigOption<Duration> OPTIMIZER_POLLING_TIMEOUT =
      ConfigOptions.key("optimizer.polling-timeout")
          .durationType()
          .defaultValue(Duration.ofSeconds(3))
          .withDescription("Optimizer polling task timeout.");

  public static final ConfigOption<Duration> OPTIMIZING_REFRESH_GROUP_INTERVAL =
      ConfigOptions.key("self-optimizing.refresh-group-interval")
          .durationType()
          .defaultValue(Duration.ofSeconds(30))
          .withDescription("Optimizer group refresh interval.");

  /** config key prefix of terminal */
  public static final String TERMINAL_PREFIX = "terminal.";

  public static final ConfigOption<String> TERMINAL_BACKEND =
      ConfigOptions.key("terminal.backend")
          .stringType()
          .defaultValue("local")
          .withDescription(
              "Terminal backend implementation. local, kyuubi and custom are valid values.");

  public static final ConfigOption<String> TERMINAL_SESSION_FACTORY =
      ConfigOptions.key("terminal.factory")
          .stringType()
          .noDefaultValue()
          .withDescription(
              "Session factory implement of terminal, `terminal.backend` must be `custom` if this is set.");

  public static final ConfigOption<Integer> TERMINAL_RESULT_LIMIT =
      ConfigOptions.key("terminal.result.limit")
          .intType()
          .defaultValue(1000)
          .withDescription("Row limit of result-set");

  public static final ConfigOption<Boolean> TERMINAL_STOP_ON_ERROR =
      ConfigOptions.key("terminal.stop-on-error")
          .booleanType()
          .defaultValue(false)
          .withDescription(
              "When a statement fails to execute, stop execution or continue executing the remaining statements.");

  public static final ConfigOption<Duration> TERMINAL_SESSION_TIMEOUT =
      ConfigOptions.key("terminal.session.timeout")
          .durationType()
          .defaultValue(Duration.ofMinutes(30))
          .withDescription(
              "Session timeout. Default unit is milliseconds if not specified (** Note: default units are minutes when version < 0.8).");

  public static final ConfigOption<String> TERMINAL_SENSITIVE_CONF_KEYS =
      ConfigOptions.key("terminal.sensitive-conf-keys")
          .stringType()
          .defaultValue("")
          .withDescription(
              "Comma-separated list of sensitive conf keys used to desensitize related value.");

  /** configs of data expiration */
  public static final ConfigOption<Boolean> DATA_EXPIRATION_ENABLED =
      ConfigOptions.key("data-expiration.enabled")
          .booleanType()
          .defaultValue(true)
          .withDescription("Enable data expiration");

  public static final ConfigOption<Integer> DATA_EXPIRATION_THREAD_COUNT =
      ConfigOptions.key("data-expiration.thread-count")
          .intType()
          .defaultValue(10)
          .withDescription("The number of threads used for data expiring");
  public static final ConfigOption<Duration> DATA_EXPIRATION_INTERVAL =
      ConfigOptions.key("data-expiration.interval")
          .durationType()
          .defaultValue(Duration.ofDays(1))
          .withDescription("Execute interval for data expiration");

  public static final String SYSTEM_CONFIG = "ams";

  public static final String CATALOG_CORE_SITE = "core-site";
  public static final String CATALOG_HDFS_SITE = "hdfs-site";
  public static final String CATALOG_HIVE_SITE = "hive-site";

  // container config
  public static final String CONTAINER_LIST = "containers";
  public static final String CONTAINER_NAME = "name";
  public static final String CONTAINER_IMPL = "container-impl";
  public static final String CONTAINER_PROPERTIES = "properties";

  public static final String DB_TYPE_DERBY = "derby";
  public static final String DB_TYPE_MYSQL = "mysql";
  public static final String DB_TYPE_POSTGRES = "postgres";

  // terminal config
  public static final List<String> TERMINAL_BACKEND_VALUES =
      Arrays.asList("local", "kyuubi", "custom");

  // plugin config
  public static final String METRIC_REPORTERS = "metric-reports";

  public static final String EVENT_LISTENERS = "event-listeners";
}
