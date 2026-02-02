---
title: AMS Configuration
url: ams-config
aliases:
  - "configuration/ams-config"
menu:
  main:
    parent: Configuration
    weight: 100
---
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<!-- This file is auto-generated. To update, run: UPDATE=1 ./mvnw test -pl amoro-ams -am -Dtest=ConfigurationsTest -->

# AMS Configuration

## Amoro Management Service Configuration

The configuration options for Amoro Management Service (AMS).

<style>
table { width: 100%; table-layout: fixed; }
table th, table td { vertical-align: top; }
table td:first-child, table th:first-child { word-break: normal; width: 40%; }
table td:nth-child(2), table th:nth-child(2) { width: 20%; word-break: break-all; }
table td:last-child, table th:last-child { width: 40%; word-break: break-all; }
</style>
| Key  | Default | Description |
| ---  | ------- | ----------- |
| admin-password | admin | The administrator password |
| admin-username | admin | The administrator account name. |
| auto-create-tags.enabled | true | Enable creating tags. |
| auto-create-tags.interval | 1 min | Interval for creating tags. |
| auto-create-tags.thread-count | 3 | The number of threads used for creating tags. |
| blocker.timeout | 1 min | Session timeout. Default unit is milliseconds if not specified. |
| catalog-meta-cache.expiration-interval | 1 min | TTL for catalog metadata. |
| clean-dangling-delete-files.enabled | true | Enable dangling delete files cleaning. |
| clean-dangling-delete-files.thread-count | 10 | The number of threads used for dangling delete files cleaning. |
| clean-orphan-files.enabled | true | Enable orphan files cleaning. |
| clean-orphan-files.interval | 1 d | Interval for cleaning orphan files. |
| clean-orphan-files.thread-count | 10 | The number of threads used for orphan files cleaning. |
| data-expiration.enabled | true | Enable data expiration |
| data-expiration.interval | 1 d | Execute interval for data expiration |
| data-expiration.thread-count | 10 | The number of threads used for data expiring |
| database.auto-create-tables | true | Auto init table schema when started |
| database.connection-pool-max-idle | 16 | Max idle connect count of database connect pool. |
| database.connection-pool-max-total | 20 | Max connect count of database connect pool. |
| database.connection-pool-max-wait-millis | 30000 | Max wait time before getting a connection timeout. |
| database.jdbc-driver-class | org.apache.derby.jdbc.EmbeddedDriver | The JDBC driver class name for connecting to the database. |
| database.password |  | The password for connecting to the database. |
| database.type | derby | Database type. |
| database.url | jdbc:derby:/tmp/amoro/derby;create=true | Database connection address |
| database.username | root | The username for connecting to the database. |
| dynamic-config.enabled | false | Whether to enable dynamic configuration backed by database table `dynamic_conf`. |
| dynamic-config.namespace | AMS | Logical namespace used when loading dynamic configuration overrides for AMS. |
| dynamic-config.refresh-interval | 30 s | Refresh interval for reloading dynamic configuration overrides from database. |
| expire-snapshots.enabled | true | Enable snapshots expiring. |
| expire-snapshots.thread-count | 10 | The number of threads used for snapshots expiring. |
| ha.cluster-name | default | Amoro management service cluster name. |
| ha.connection-timeout | 5 min | The Zookeeper connection timeout in milliseconds. |
| ha.enabled | false | Whether to enable high availability mode. |
| ha.heartbeat-interval | 10 s | HA heartbeat interval. |
| ha.lease-ttl | 30 s | TTL of HA lease. |
| ha.session-timeout | 30 s | The Zookeeper session timeout in milliseconds. |
| ha.type | zk | High availability implementation type: zk or database. |
| ha.zookeeper-address |  | The Zookeeper address used for high availability. |
| ha.zookeeper-auth-keytab |  | The Zookeeper authentication keytab file path when auth type is KERBEROS. |
| ha.zookeeper-auth-principal |  | The Zookeeper authentication principal when auth type is KERBEROS. |
| ha.zookeeper-auth-type | NONE | The Zookeeper authentication type, NONE or KERBEROS. |
| http-server.auth-basic-provider | org.apache.amoro.server.authentication.DefaultPasswdAuthenticationProvider | User-defined password authentication implementation of org.apache.amoro.authentication.PasswdAuthenticationProvider |
| http-server.auth-jwt-provider | &lt;undefined&gt; | User-defined JWT (JSON Web Token) authentication implementation of org.apache.amoro.authentication.TokenAuthenticationProvider |
| http-server.bind-port | 19090 | Port that the Http server is bound to. |
| http-server.login-auth-ldap-url | &lt;undefined&gt; | LDAP connection URL(s), value could be a SPACE separated list of URLs to multiple LDAP servers for resiliency. URLs are tried in the order specified until the connection is successful |
| http-server.login-auth-ldap-user-pattern | &lt;undefined&gt; | LDAP user pattern for authentication. The pattern defines how to construct the user's distinguished name (DN) in the LDAP directory. Use {0} as a placeholder for the username. For example, 'cn={0},ou=people,dc=example,dc=com' will search for users in the specified organizational unit. |
| http-server.login-auth-provider | org.apache.amoro.server.authentication.DefaultPasswdAuthenticationProvider | User-defined login authentication implementation of org.apache.amoro.authentication.PasswdAuthenticationProvider |
| http-server.proxy-client-ip-header | X-Real-IP | The HTTP header to record the real client IP address. If your server is behind a load balancer or other proxy, the server will see this load balancer or proxy IP address as the client IP address, to get around this common issue, most load balancers or proxies offer the ability to record the real remote IP address in an HTTP header that will be added to the request for other devices to use. |
| http-server.rest-auth-type | token | The authentication used by REST APIs, token (default), basic or jwt. |
| http-server.session-timeout | 7 d | Timeout for http session. |
| optimizer.heart-beat-timeout | 1 min | Timeout duration for Optimizer heartbeat. |
| optimizer.max-planning-parallelism | 1 | Max planning parallelism in one optimizer group. |
| optimizer.polling-timeout | 3 s | Optimizer polling task timeout. |
| optimizer.task-ack-timeout | 30 s | Timeout duration for task acknowledgment. |
| optimizer.task-execute-timeout | 2147483647 s | Timeout duration for task execution, default to Integer.MAX_VALUE seconds(about 24,855 days). |
| overview-cache.max-size | 3360 | Max size of overview cache. |
| overview-cache.refresh-interval | 3 min | Interval for refreshing overview cache. |
| plugin.property.category-key | plugin.category | The property key used to store plugin category identifier in plugin properties. |
| plugin.property.name-key | plugin.name | The property key used to store plugin name identifier in plugin properties. |
| refresh-external-catalogs.interval | 3 min | Interval to refresh the external catalog. |
| refresh-external-catalogs.queue-size | 1000000 | The queue size of the executors of the external catalog explorer. |
| refresh-external-catalogs.thread-count | 10 | The number of threads used for discovering tables in external catalogs. |
| refresh-tables.interval | 1 min | Interval for refreshing table metadata. |
| refresh-tables.max-pending-partition-count | 100 | Filters will not be used beyond that number of partitions. |
| refresh-tables.thread-count | 10 | The number of threads used for refreshing tables. |
| self-optimizing.break-quota-limit-enabled | true | Allow the table to break the quota limit when the resource is sufficient. |
| self-optimizing.commit-manifest-io-thread-count | 10 | Sets the size of the worker pool. The worker pool limits the number of tasks concurrently processing manifests in the base table implementation across all concurrent commit operations. |
| self-optimizing.commit-thread-count | 10 | The number of threads that self-optimizing uses to submit results. |
| self-optimizing.plan-manifest-io-thread-count | 10 | Sets the size of the worker pool. The worker pool limits the number of tasks concurrently processing manifests in the base table implementation across all concurrent planning operations. |
| self-optimizing.refresh-group-interval | 30 s | Optimizer group refresh interval. |
| self-optimizing.runtime-data-expire-interval-hours | 1 | The number of hours that self-optimizing runtime data expire interval. |
| self-optimizing.runtime-data-keep-days | 30 | The number of days that self-optimizing runtime data keeps the runtime. |
| server-bind-host | 0.0.0.0 | The host bound to the server. |
| server-expose-host |  | The exposed host of the server. |
| sync-hive-tables.enabled | false | Enable synchronizing Hive tables. |
| sync-hive-tables.thread-count | 10 | The number of threads used for synchronizing Hive tables. |
| table-manifest-io.thread-count | 20 | Sets the size of the worker pool. The worker pool limits the number of tasks concurrently processing manifests in the base table implementation across all concurrent planning or commit operations. |
| terminal.backend | local | Terminal backend implementation. local, kyuubi and custom are valid values. |
| terminal.factory | &lt;undefined&gt; | Session factory implement of terminal, `terminal.backend` must be `custom` if this is set. |
| terminal.result.limit | 1000 | Row limit of result-set |
| terminal.sensitive-conf-keys |  | Comma-separated list of sensitive conf keys used to desensitize related value. |
| terminal.session.timeout | 30 min | Session timeout. Default unit is milliseconds if not specified (** Note: default units are minutes when version &lt; 0.8). |
| terminal.stop-on-error | false | When a statement fails to execute, stop execution or continue executing the remaining statements. |
| thrift-server.max-message-size | 100 mb | Maximum message size that the Thrift server can accept. Default unit is bytes if not specified. |
| thrift-server.optimizing-service.bind-port | 1261 | Port that the optimizing service thrift server is bound to. |
| thrift-server.selector-queue-size | 4 | The number of queue size per selector thread for the Thrift server |
| thrift-server.selector-thread-count | 2 | The number of selector threads for the Thrift server. |
| thrift-server.table-service.bind-port | 1260 | Port that the table service thrift server is bound to. |
| thrift-server.table-service.worker-thread-count | 20 | The number of worker threads for the Thrift server. |
| use-master-slave-mode | false | This setting controls whether to enable the AMS horizontal scaling feature, which is currently under development and testing. |


## Shade Utils Configuration

The configuration options for Amoro Configuration Shade Utils.

<style>
table { width: 100%; table-layout: fixed; }
table th, table td { vertical-align: top; }
table td:first-child, table th:first-child { word-break: normal; width: 40%; }
table td:nth-child(2), table th:nth-child(2) { width: 20%; word-break: break-all; }
table td:last-child, table th:last-child { width: 40%; word-break: break-all; }
</style>
| Key  | Default | Description |
| ---  | ------- | ----------- |
| shade.identifier | default | The identifier of the encryption method for decryption. Defaults to "default", indicating no encryption |
| shade.sensitive-keywords | admin-password;database.password | A semicolon-separated list of keywords for the configuration items to be decrypted. |


