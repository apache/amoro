-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE TABLE `catalog_metadata`
(
    `catalog_id`             int(11) NOT NULL AUTO_INCREMENT,
    `catalog_name`           varchar(64) NOT NULL COMMENT 'catalog name',
    `catalog_metastore`      varchar(64) NOT NULL COMMENT 'catalog type like hms/ams/hadoop/custom',
    `storage_configs`        mediumtext COMMENT 'base64 code of storage configs',
    `auth_configs`           mediumtext COMMENT 'base64 code of auth configs',
    `catalog_properties`     mediumtext COMMENT 'catalog properties',
    `database_count`         int(11) NOT NULL default 0,
    `table_count`            int(11) NOT NULL default 0,
    PRIMARY KEY (`catalog_id`),
    UNIQUE KEY `catalog_name_index` (`catalog_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'catalog metadata';

CREATE TABLE `database_metadata`
(
    `catalog_name`           varchar(64) NOT NULL COMMENT 'catalog name',
    `db_name`                varchar(128) NOT NULL COMMENT 'database name',
    `table_count`            int(11) NOT NULL default 0,
    PRIMARY KEY (`catalog_name`, `db_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'database metadata';


CREATE TABLE `optimizer`
(
    `token`                      varchar(50) NOT NULL,
    `resource_id`                varchar(100) DEFAULT NULL  COMMENT 'optimizer instance id',
    `group_name`                 varchar(50) DEFAULT NULL COMMENT 'group/queue name',
    `container_name`             varchar(100) DEFAULT NULL  COMMENT 'container name',
    `start_time`                 timestamp not null default CURRENT_TIMESTAMP COMMENT 'optimizer start time',
    `touch_time`                 timestamp not null default CURRENT_TIMESTAMP COMMENT 'update time',
    `thread_count`               int(11) DEFAULT NULL COMMENT 'total number of all CPU resources',
    `total_memory`               bigint(30) DEFAULT NULL COMMENT 'optimizer use memory size',
    `properties`                 mediumtext COMMENT 'optimizer state info, contains like yarn application id and flink job id',
    PRIMARY KEY (`token`),
    KEY  `resource_group` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'resource table';

CREATE TABLE `resource`
(
    `resource_id`               varchar(100) NOT NULL  COMMENT 'optimizer instance id',
    `resource_type`             tinyint(4) DEFAULT 0 COMMENT 'resource type like optimizer/ingestor',
    `container_name`            varchar(100) DEFAULT NULL  COMMENT 'container name',
    `group_name`                varchar(50) DEFAULT NULL COMMENT 'queue name',
    `thread_count`              int(11) DEFAULT NULL COMMENT 'total number of all CPU resources',
    `total_memory`              bigint(30) DEFAULT NULL COMMENT 'optimizer use memory size',
    `start_time`                timestamp not null default CURRENT_TIMESTAMP COMMENT 'optimizer start time',
    `properties`                mediumtext COMMENT 'optimizer instance properties',
    PRIMARY KEY (`resource_id`),
    KEY  `resource_group` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimizer instance info';

CREATE TABLE `resource_group`
(
    `group_name`       varchar(50) NOT NULL  COMMENT 'Optimize group name',
    `container_name`   varchar(100) DEFAULT NULL  COMMENT 'Container name',
    `properties`       mediumtext  COMMENT 'Properties',
    PRIMARY KEY (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Group to divide optimize resources';

CREATE TABLE `table_identifier`
(
    `table_id`        bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto increment id',
    `catalog_name`    varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`         varchar(128) NOT NULL COMMENT 'Database name',
    `table_name`      varchar(256) NOT NULL COMMENT 'Table name',
    `format`          VARCHAR(32)  NOT NULL COMMENT 'Table Format',
    PRIMARY KEY (`table_id`),
    UNIQUE KEY `table_name_index` (`catalog_name`,`db_name`,`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Table identifier for AMS' ROW_FORMAT=DYNAMIC;

CREATE TABLE `table_metadata`
(
    `table_id`        bigint(20) NOT NULL COMMENT 'table id',
    `catalog_name`    varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`         varchar(128) NOT NULL COMMENT 'Database name',
    `table_name`      varchar(256) NOT NULL COMMENT 'Table name',
    `primary_key`     varchar(256) DEFAULT NULL COMMENT 'Primary key',
    `sort_key`        varchar(256) DEFAULT NULL COMMENT 'Sort key',
    `table_location`  varchar(256) DEFAULT NULL COMMENT 'Table location',
    `base_location`   varchar(256) DEFAULT NULL COMMENT 'Base table location',
    `change_location` varchar(256) DEFAULT NULL COMMENT 'change table location',
    `properties`      mediumtext COMMENT 'Table properties',
    `meta_store_site` mediumtext COMMENT 'base64 code of meta store site',
    `hdfs_site`       mediumtext COMMENT 'base64 code of hdfs site',
    `core_site`       mediumtext COMMENT 'base64 code of core site',
    `auth_method`     varchar(32)  DEFAULT NULL COMMENT 'auth method like KERBEROS/SIMPLE',
    `hadoop_username` varchar(64)  DEFAULT NULL COMMENT 'hadoop username when auth method is SIMPLE',
    `krb_keytab`      text COMMENT 'kerberos keytab when auth method is KERBEROS',
    `krb_conf`        text COMMENT 'kerberos conf when auth method is KERBEROS',
    `krb_principal`   text COMMENT 'kerberos principal when auth method is KERBEROS',
    `current_schema_id`   int(11) NOT NULL DEFAULT 0 COMMENT 'current schema id',
    `meta_version`    bigint(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Table metadata';

CREATE TABLE `table_runtime`
(
    `table_id`                      bigint(20) NOT NULL,
    `group_name`                    varchar(64) NOT NULL,
    `status_code`                   int DEFAULT 700 NOT NULL COMMENT 'Table runtime status code.',
    `status_code_update_time`       timestamp(3) default CURRENT_TIMESTAMP(3) COMMENT 'Table runtime status code update time',
    `table_config`                  mediumtext COMMENT 'table configuration cached from table.properties',
    `table_summary`                 mediumtext COMMENT 'table summary for ams',
    `bucket_id`          VARCHAR(4)  DEFAULT NULL COMMENT 'Bucket id to which the record table belongs',
    PRIMARY KEY (`table_id`),
    INDEX idx_status_and_time (status_code, status_code_update_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Table running information of each table' ROW_FORMAT=DYNAMIC;


CREATE TABLE `table_runtime_state` (
  `state_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `table_id` bigint unsigned NOT NULL COMMENT 'Table identifier id',
  `state_key` varchar(256) NOT NULL COMMENT 'Table Runtime state key',
  `state_value` mediumtext COMMENT 'Table Runtime state value, string type',
  `state_version` bigint NOT NULL DEFAULT '0' COMMENT 'Table runtime state version, auto inc when update',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`state_id`),
  UNIQUE KEY `uniq_table_state_key` (`table_id`,`state_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='State of Table Runtimes';

CREATE TABLE `table_process`
(
    `process_id`                    bigint(20) NOT NULL COMMENT 'table process id',
    `table_id`                      bigint(20) NOT NULL COMMENT 'table id',
    `external_process_identifier`   varchar(256) DEFAULT NULL COMMENT 'Table optimizing external processidentifier',
    `status`                        varchar(64) NOT NULL COMMENT 'Table optimizing status',
    `process_type`                  varchar(64) NOT NULL COMMENT 'Process action type',
    `process_stage`                 varchar(64) NOT NULL COMMENT 'Process current stage',
    `execution_engine`              varchar(64) NOT NULL COMMENT 'Execution engine',
    `retry_number`                  int(11) NOT NULL DEFAULT 0 COMMENT 'Retry times',
    `create_time`                   timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'First plan time',
    `finish_time`                   timestamp NULL DEFAULT NULL COMMENT 'finish time or failed time',
    `fail_message`                  mediumtext DEFAULT NULL COMMENT 'Error message after task failed',
    `process_parameters`            mediumtext COMMENT 'Table process parameters',
    `summary`                       mediumtext COMMENT 'Max change transaction id of these tasks',
    PRIMARY KEY (`process_id`),
    KEY  `table_index` (`table_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'History of optimizing after each commit';

CREATE TABLE `optimizing_process_state`
(
    `process_id`                    bigint(20) NOT NULL COMMENT 'optimizing_procedure UUID',
    `table_id`                      bigint(20) NOT NULL,
    `target_snapshot_id`            bigint(20) NOT NULL,
    `target_change_snapshot_id`     bigint(20) NOT NULL,
    `rewrite_input`                 longblob DEFAULT NULL COMMENT 'rewrite files input',
    `from_sequence`                 mediumtext COMMENT 'from or min sequence of each partition',
    `to_sequence`                   mediumtext COMMENT 'to or max sequence of each partition',
    PRIMARY KEY (`process_id`),
    KEY  `table_index` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'History of optimizing after each commit';

CREATE TABLE `task_runtime`
(
    `process_id`                bigint(20) NOT NULL,
    `task_id`                   int(11) NOT NULL,
    `retry_num`                 int(11) DEFAULT NULL COMMENT 'Retry times',
    `table_id`                  bigint(20) NOT NULL,
    `partition_data`            varchar(128)  DEFAULT NULL COMMENT 'Partition data',
    `create_time`               timestamp NULL DEFAULT NULL COMMENT 'Task create time',
    `start_time`                timestamp NULL DEFAULT NULL COMMENT 'Time when task start waiting to execute',
    `end_time`                  timestamp NULL DEFAULT NULL COMMENT 'Time when task finished',
    `cost_time`                 bigint(20) DEFAULT NULL,
    `status`                    varchar(16) DEFAULT NULL  COMMENT 'Optimize Status: PLANNED, SCHEDULED, ACKED, FAILED, SUCCESS, CANCELED',
    `fail_reason`               varchar(4096) DEFAULT NULL COMMENT 'Error message after task failed',
    `optimizer_token`           varchar(50) DEFAULT NULL COMMENT 'Job type',
    `thread_id`                 int(11) DEFAULT NULL COMMENT 'Job id',
    `rewrite_output`            longblob DEFAULT NULL COMMENT 'rewrite files output',
    `metrics_summary`           text COMMENT 'metrics summary',
    `properties`                mediumtext COMMENT 'task properties',
    PRIMARY KEY (`process_id`, `task_id`),
    KEY  `table_index` (`table_id`, `process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimize task basic information';

CREATE TABLE `table_process_state`
(
    `process_id`                    bigint(20) NOT NULL COMMENT 'optimizing_procedure UUID',
    `action`                        varchar(16) NOT NULL COMMENT 'process action',
    `table_id`                      bigint(20) NOT NULL,
    `retry_num`                     int(11) DEFAULT NULL COMMENT 'Retry times',
    `status`                        varchar(10) NOT NULL COMMENT 'Direct to TableOptimizingStatus',
    `start_time`                    timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'First plan time',
    `end_time`                      timestamp NULL DEFAULT NULL COMMENT 'finish time or failed time',
    `fail_reason`                   varchar(4096) DEFAULT NULL COMMENT 'Error message after task failed',
    `summary`                       mediumtext COMMENT 'state summary, usually a map',
    PRIMARY KEY (`process_id`),
    KEY  `table_index` (`table_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'History of optimizing after each commit';

CREATE TABLE `optimizing_task_quota`
(
    `process_id`                bigint(20) NOT NULL COMMENT 'Optimize type: Major, Minor, FullMajor',
    `task_id`                   int(11) NOT NULL COMMENT 'Optimize task unique id',
    `retry_num`                 int(11) DEFAULT 0 COMMENT 'Retry times',
    `table_id`                  bigint(20) NOT NULL,
    `start_time`                timestamp default CURRENT_TIMESTAMP COMMENT 'Time when task start waiting to execute',
    `end_time`                  timestamp default CURRENT_TIMESTAMP COMMENT 'Time when task finished',
    `fail_reason`               varchar(4096) DEFAULT NULL COMMENT 'Error message after task failed',
    PRIMARY KEY (`process_id`, `task_id`, `retry_num`),
    KEY  `table_index` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimize task basic information';


CREATE TABLE `api_tokens`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `apikey`     varchar(256) NOT NULL COMMENT 'openapi client public key',
    `secret`     varchar(256) NOT NULL COMMENT 'The key used by the client to generate the request signature',
    `apply_time` timestamp NULL DEFAULT NULL COMMENT 'apply time',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `account_unique` (`apikey`) USING BTREE COMMENT 'account unique'
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='Openapi  secret';

CREATE TABLE `platform_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'file id',
  `file_name` varchar(100) NOT NULL COMMENT 'file name',
  `file_content_b64` mediumtext NOT NULL COMMENT 'file content encoded with base64',
  `file_path` varchar(100) DEFAULT NULL COMMENT 'may be hdfs path , not be used now',
  `add_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'add timestamp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='store files info saved in the platform';

CREATE TABLE `table_blocker` (
  `blocker_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Blocker unique id',
  `catalog_name` varchar(64) NOT NULL COMMENT 'Catalog name',
  `db_name` varchar(128) NOT NULL COMMENT 'Database name',
  `table_name` varchar(256) NOT NULL COMMENT 'Table name',
  `operations` varchar(128) NOT NULL COMMENT 'Blocked operations',
  `create_time` timestamp NULL DEFAULT NULL COMMENT 'Blocker create time',
  `expiration_time` timestamp NULL DEFAULT NULL COMMENT 'Blocker expiration time',
  `properties` mediumtext COMMENT 'Blocker properties',
  `prev_blocker_id` bigint(20) NOT NULL DEFAULT -1 COMMENT 'prev blocker id when created',
  PRIMARY KEY (`blocker_id`),
  UNIQUE KEY `uq_prev` (`catalog_name`,`db_name`,`table_name`, `prev_blocker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table blockers' ROW_FORMAT=DYNAMIC;

CREATE TABLE `http_session` (
    `session_id`    varchar(120) NOT NULL COMMENT 'HTTP Session ID',
    `context_path`  varchar(60) COMMENT 'Jetty Context path',
    `virtual_host`  varchar(60) COMMENT 'Jetty virtual host',
    `last_node`     varchar(60) COMMENT 'Last node',
    `access_time`   bigint(20) COMMENT 'Access time',
    `last_access_time`  bigint(20) COMMENT 'Last access time',
    `create_time`   bigint(20)  COMMENT 'Create time',
    `cookie_time`   bigint(20)  COMMENT 'Cookie time',
    `last_save_time` bigint(20) COMMENT 'Last save time',
    `expiry_time`   bigint(20)  COMMENT 'Expiry time',
    `max_interval`  bigint(20)  COMMENT 'Max internal',
    `data_store`    blob        COMMENT 'Session data store',
    PRIMARY KEY(`session_id`, `context_path`, `virtual_host`),
    KEY `idx_session_expiry` (`expiry_time`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Http session store' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS ha_lease (
  cluster_name       VARCHAR(64)   NOT NULL COMMENT 'AMS cluster name',
  service_name       VARCHAR(64)   NOT NULL COMMENT 'Service name (AMS/TABLE_SERVICE/OPTIMIZING_SERVICE)',
  node_id            VARCHAR(256)  NULL COMMENT 'Unique node identifier (host:port:uuid)',
  node_ip            VARCHAR(64)   NULL COMMENT 'Node IP address',
  server_info_json   TEXT          NULL COMMENT 'JSON encoded server info (AmsServerInfo)',
  lease_expire_ts    BIGINT        NULL COMMENT 'Lease expiration timestamp (ms since epoch)',
  version            INT           NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version of the lease row',
  updated_at         BIGINT        NOT NULL COMMENT 'Last update timestamp (ms since epoch)',
  PRIMARY KEY (cluster_name, service_name),
  KEY `idx_ha_lease_expire` (lease_expire_ts) COMMENT 'Index for querying expired leases',
  KEY `idx_ha_lease_node` (node_id) COMMENT 'Index for querying leases by node ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HA lease table for leader election and heartbeat renewal';

CREATE TABLE `dynamic_conf`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `conf_key`    varchar(256) NOT NULL,
    `conf_value`  varchar(256) NOT NULL,
    `conf_group`  varchar(256) NOT NULL COMMENT 'Group: AMS for service, PLUGIN_{category} for plugins',
    `plugin_name` varchar(256) DEFAULT NULL COMMENT 'Plugin identifier; valid only when conf_group = PLUGIN_*',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conf` (`conf_group`, `plugin_name`, `conf_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dynamic configuration overrides for AMS and plugins';
