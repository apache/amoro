CREATE TABLE `catalog_metadata`
(
    `catalog_id`         int(11) NOT NULL AUTO_INCREMENT,
    `catalog_name`       varchar(64) NOT NULL COMMENT 'catalog name',
    `display_name`       varchar(64) DEFAULT NULL COMMENT 'display name of catalog',
    `catalog_type`       varchar(64) NOT NULL COMMENT 'catalog type like hive/hadoop',
    `storage_configs`    mediumtext COMMENT 'base64 code of storage configs',
    `auth_configs`       mediumtext COMMENT 'base64 code of auth configs',
    `catalog_properties` mediumtext COMMENT 'catalog properties',
    PRIMARY KEY (`catalog_id`),
    UNIQUE KEY `catalog_metadata_catalog_name_uindex` (`catalog_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'catalog metadata';

CREATE TABLE `container_metadata`
(
    `name`       varchar(64) NOT NULL COMMENT 'container name',
    `type`       varchar(64) NOT NULL COMMENT 'container type like flink/local',
    `properties` mediumtext COMMENT 'container properties',
    PRIMARY KEY (`name`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'container metadata';

CREATE TABLE `database_metadata`
(
    `db_id`        int(11) NOT NULL AUTO_INCREMENT,
    `catalog_name` varchar(64) NOT NULL COMMENT 'catalog name',
    `db_name`      varchar(64) NOT NULL COMMENT 'database name',
    PRIMARY KEY (`db_id`),
    UNIQUE KEY `database_name_uindex` (`catalog_name`,`db_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'database metadata';

CREATE TABLE `file_info_cache`
(
    `primary_key_md5`   varchar(64) NOT NULL COMMENT 'generate md5 with table_identifier+inner_table+file_path+partition_name as primary_key',
    `table_identifier`   varchar(384) NOT NULL COMMENT 'table full name with catalog.db.table',
    `add_snapshot_id`    bigint(20) NOT NULL COMMENT 'the snapshot id who add this file',
    `parent_snapshot_id` bigint(20) NOT NULL COMMENT 'parent snapshot of add_snapshot_id',
    `delete_snapshot_id` bigint(20) DEFAULT NULL COMMENT 'the snapshot id who delete this file',
    `inner_table`        varchar(64)          DEFAULT NULL COMMENT 'table type like change/base',
    `file_path`          varchar(400)         NOT NULL COMMENT 'table type like change/base',
    `file_type`          varchar(64)          DEFAULT NULL COMMENT 'absolute file path',
    `file_size`          bigint(20) DEFAULT NULL COMMENT 'file size',
    `file_mask`          bigint(20) DEFAULT NULL COMMENT 'file mask',
    `file_index`         bigint(20) DEFAULT NULL COMMENT 'file index',
    `spec_id`            bigint(20) DEFAULT NULL COMMENT 'file spec id',
    `record_count`       bigint(20) DEFAULT NULL COMMENT 'file record count',
    `partition_name`     varchar(256)         DEFAULT NULL COMMENT 'the partition name which file belongs to',
    `action`             varchar(64)          DEFAULT NULL COMMENT 'snapshot type',
    `commit_time`        timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'file commit time',
    `watermark`          timestamp  NULL DEFAULT NULL COMMENT 'file max event time',
    `producer`           varchar(64) NOT NULL DEFAULT 'INGESTION' COMMENT 'who produce this snapshot',
    PRIMARY KEY (`primary_key_md5`),
    KEY  `table_snap_index` (`table_identifier`,`add_snapshot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'cache files info of table';

CREATE TABLE `optimize_file`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto increment id',
    `optimize_type` varchar(10) NOT NULL COMMENT 'Optimize type: Major, Minor, FullMajor',
    `trace_id`      varchar(40) NOT NULL COMMENT 'Optimize task unique id',
    `file_type`     varchar(16) NOT NULL COMMENt 'File type: BASE_FILE, INSERT_FILE, EQ_DELETE_FILE, POS_DELETE_FILE',
    `is_target`     tinyint(4) DEFAULT '0' COMMENT 'Is file newly generated by optimizing',
    `file_content`  varbinary(60000) DEFAULT NULL COMMENT 'File bytes after serialization',
    PRIMARY KEY (`id`),
    KEY             `compact_task_id` (`optimize_type`,`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimize files for Optimize task';

CREATE TABLE `optimize_history`
(
    `history_id`                     bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'History auto increment id',
    `catalog_name`                  varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`                       varchar(64) NOT NULL COMMENT 'Database name',
    `table_name`                    varchar(64) NOT NULL COMMENT 'Table name',
    `optimize_range`                varchar(10) NOT NULL COMMENT 'Optimize Range: Table, Partition, Node',
    `visible_time`                  datetime(3) DEFAULT NULL COMMENT 'Latest visible time',
    `commit_time`                   datetime(3) DEFAULT NULL COMMENT 'Commit time',
    `plan_time`                     datetime(3) DEFAULT NULL COMMENT 'First plan time',
    `duration`                      bigint(20) DEFAULT NULL COMMENT 'Execute cost time',
    `total_file_cnt_before`         int(11) NOT NULL COMMENT 'Total file cnt before optimizing',
    `total_file_size_before`        bigint(20) NOT NULL COMMENT 'Total file size in bytes before optimizing',
    `insert_file_cnt_before`        int(11) NOT NULL COMMENT 'Insert file cnt before optimizing',
    `insert_file_size_before`       bigint(20) NOT NULL COMMENT 'Insert file size in bytes before optimizing',
    `delete_file_cnt_before`        int(11) NOT NULL COMMENT 'Delete file cnt before optimizing',
    `delete_file_size_before`       bigint(20) NOT NULL COMMENT 'Delete file size in bytes before optimizing',
    `base_file_cnt_before`          int(11) NOT NULL COMMENT 'Base file cnt before optimizing',
    `base_file_size_before`         bigint(20) NOT NULL COMMENT 'Base file size in bytes before optimizing',
    `pos_delete_file_cnt_before`    int(11) NOT NULL COMMENT 'Pos-Delete file cnt before optimizing',
    `pos_delete_file_size_before`   bigint(20) NOT NULL COMMENT 'Pos-Delete file size in bytes before optimizing',
    `total_file_cnt_after`          int(11) NOT NULL COMMENT 'Total file cnt after optimizing',
    `total_file_size_after`         bigint(20) NOT NULL COMMENT 'Total file cnt after optimizing',
    `snapshot_id`                   bigint(20) DEFAULT NULL COMMENT 'Snapshot id after commit',
    `total_size`                    bigint(20) DEFAULT NULL COMMENT 'Total size of the snapshot',
    `added_files`                   int(11) DEFAULT NULL COMMENT 'Added files cnt of the snapshot',
    `removed_files`                 int(11) DEFAULT NULL COMMENT 'Removed files cnt of the snapshot',
    `added_records`                 bigint(20) DEFAULT NULL COMMENT 'Added records of the snapshot',
    `removed_records`               bigint(20) DEFAULT NULL COMMENT 'Removed records of the snapshot',
    `added_files_size`              bigint(20) DEFAULT NULL COMMENT 'Added files size of the snapshot',
    `removed_files_size`            bigint(20) DEFAULT NULL COMMENT 'Removed files size of the snapshot',
    `total_files`                   bigint(20) DEFAULT NULL COMMENT 'Total file size of the snapshot',
    `total_records`                 bigint(20) DEFAULT NULL COMMENT 'Total records of the snapshot',
    `partition_cnt`                 int(11) NOT NULL COMMENT 'Partition cnt for this optimizing',
    `partitions`                    text COMMENT 'Partitions',
    `max_change_transaction_id` mediumtext COMMENT 'Max change transaction id of these tasks',
    `optimize_type`                 varchar(10) NOT NULL COMMENT 'Optimize type: Major, Minor',
    PRIMARY KEY (`history_id`),
    KEY                             `table_name_record` (`catalog_name`,`db_name`,`table_name`,`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'History of optimizing after each commit';

CREATE TABLE `optimizer`
(
    `optimizer_id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `optimizer_name`             varchar(1024) DEFAULT NULL COMMENT 'optimizer name',
    `queue_id`             int(11) DEFAULT NULL COMMENT 'queue id',
    `queue_name`           varchar(1024) DEFAULT NULL COMMENT 'queue name',
    `optimizer_start_time`       varchar(1024) DEFAULT NULL COMMENT 'optimizer start time',
    `optimizer_fail_time`        varchar(1024) DEFAULT NULL COMMENT 'optimizer fail time',
    `optimizer_status`           varchar(16)   DEFAULT NULL COMMENT 'optimizer status',
    `core_number`          int(11) DEFAULT NULL COMMENT 'total number of all CPU resources',
    `memory`               bigint(30) DEFAULT NULL COMMENT 'optimizer use memory size',
    `parallelism`          int(11) DEFAULT NULL COMMENT 'optimizer parallelism',
    `jobmanager_url`       varchar(1024) DEFAULT NULL COMMENT 'jobmanager url',
    `optimizer_instance`   blob COMMENT 'optimizer instance bytes, use to deserialize optimizer instance',
    `optimizer_state_info` mediumtext COMMENT 'optimizer state info, contains like yarn application id and flink job id',
    `container`            varchar(50)   DEFAULT '' COMMENT 'name of container which this optimizer belongs to',
    `update_time` timestamp not null default CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`optimizer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimizer info';

CREATE TABLE `optimize_group`
(
    `group_id`   int(11) NOT NULL AUTO_INCREMENT  COMMENT 'Optimize group unique id',
    `name`       varchar(50) NOT NULL  COMMENT 'Optimize group name',
    `properties` mediumtext  COMMENT 'Properties',
    `container`  varchar(100) DEFAULT NULL  COMMENT 'Container: local, flink',
    PRIMARY KEY (`group_id`),
    UNIQUE KEY `uniqueName` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Group to divide optimize resources';

CREATE TABLE `optimize_task`
(
    `trace_id`                  varchar(40) NOT NULL COMMENT 'Optimize task uuid',
    `optimize_type`             varchar(10) NOT NULL COMMENT 'Optimize type: Major, Minor',
    `catalog_name`              varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`                   varchar(64) NOT NULL COMMENT 'Database name',
    `table_name`                varchar(64) NOT NULL COMMENT 'Table name',
    `partition`                 varchar(128)  DEFAULT NULL COMMENT 'Partition',
    `task_commit_group`         varchar(40)   DEFAULT NULL COMMENT 'UUID. Commit group of task, task of one commit group should commit together',
    `max_change_transaction_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT 'Max change transaction id',
    `create_time`               datetime(3) DEFAULT NULL COMMENT 'Task create time',
    `properties`                text COMMENT 'Task properties',
    `queue_id`                  int(11) NOT NULL COMMENT 'Task group id',
    `insert_files`              int(11) DEFAULT NULL COMMENT 'Insert file cnt',
    `delete_files`              int(11) DEFAULT NULL COMMENT 'Delete file cnt',
    `base_files`                int(11) DEFAULT NULL COMMENT 'Base file cnt',
    `pos_delete_files`          int(11) DEFAULT NULL COMMENT 'Pos-Delete file cnt',
    `insert_file_size`          bigint(20) DEFAULT NULL COMMENT 'Insert file size in bytes',
    `delete_file_size`          bigint(20) DEFAULT NULL COMMENT 'Delete file size in bytes',
    `base_file_size`            bigint(20) DEFAULT NULL COMMENT 'Base file size in bytes',
    `pos_delete_file_size`      bigint(20) DEFAULT NULL COMMENT 'Pos-Delete file size in bytes',
    `source_nodes`              varchar(2048) DEFAULT NULL COMMENT 'Source nodes of task',
    `task_plan_group`           varchar(40)   DEFAULT NULL COMMENT 'UUID. Plan group of task, task of one plan group are planned together',
    `status`        varchar(16)   DEFAULT NULL  COMMENT 'Optimize Status: Init, Pending, Executing, Failed, Prepared, Committed',
    `pending_time`  datetime(3) DEFAULT NULL COMMENT 'Time when task start waiting to execute',
    `execute_time`  datetime(3) DEFAULT NULL COMMENT 'Time when task start executing',
    `prepared_time` datetime(3) DEFAULT NULL COMMENT 'Time when task finish executing',
    `report_time`   datetime(3) DEFAULT NULL COMMENT 'Time when task report result',
    `commit_time`   datetime(3) DEFAULT NULL COMMENT 'Time when task committed',
    `job_type`      varchar(16)   DEFAULT NULL COMMENT 'Job type',
    `job_id`        varchar(32)   DEFAULT NULL COMMENT 'Job id',
    `attempt_id`    varchar(40)   DEFAULT NULL COMMENT 'Attempt id',
    `retry`         int(11) DEFAULT NULL COMMENT 'Retry times',
    `fail_reason`   varchar(4096) DEFAULT NULL COMMENT 'Error message after task failed',
    `fail_time`     datetime(3) DEFAULT NULL COMMENT 'Fail time',
    `new_file_size` bigint(20) DEFAULT NULL COMMENT 'File size generated by task executing',
    `new_file_cnt`  int(11) DEFAULT NULL COMMENT 'File cnt generated by task executing',
    `cost_time`     bigint(20) DEFAULT NULL COMMENT 'Task Execute cost time',
    PRIMARY KEY (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimize task basic information';

CREATE TABLE `snapshot_info_cache`
(
    `table_identifier`   varchar(384) NOT NULL COMMENT 'table full name with catalog.db.table',
    `snapshot_id`        bigint(20) NOT NULL COMMENT 'snapshot id',
    `parent_snapshot_id` bigint(20) NOT NULL COMMENT 'parent snapshot id',
    `action`             varchar(64)          DEFAULT NULL COMMENT 'snapshot type',
    `inner_table`        varchar(64)          NOT NULL COMMENT 'table type like change/base',
    `producer`           varchar(64)          NOT NULL DEFAULT 'INGESTION' COMMENT 'who produce this snapshot',
    `file_size`          bigint(20)           NOT NULL DEFAULT 0 COMMENT 'file size',
    `file_count`         int(11)              NOT NULL DEFAULT 0 COMMENT 'file count',
    `commit_time`        timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'snapshot commit time',
    PRIMARY KEY (`table_identifier`,`inner_table`,`snapshot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'cache snapshot info of table';

CREATE TABLE `table_metadata`
(
    `catalog_name`    varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`         varchar(64) NOT NULL COMMENT 'Database name',
    `table_name`      varchar(64) NOT NULL COMMENT 'Table name',
    `primary_key`     varchar(256) DEFAULT NULL COMMENT 'Primary key',
    `sort_key`        varchar(256) DEFAULT NULL COMMENT 'Sort key',
    `table_location`  varchar(256) DEFAULT NULL COMMENT 'Table location',
    `base_location`   varchar(256) DEFAULT NULL COMMENT 'Base table location',
    `delta_location`  varchar(256) DEFAULT NULL COMMENT 'change table location',
    `properties`      text COMMENT 'Table properties',
    `meta_store_site` mediumtext COMMENT 'base64 code of meta store site',
    `hdfs_site`       mediumtext COMMENT 'base64 code of hdfs site',
    `core_site`       mediumtext COMMENT 'base64 code of core site',
    `hbase_site`      mediumtext COMMENT 'base64 code of hbase site',
    `auth_method`     varchar(32)  DEFAULT NULL COMMENT 'auth method like KERBEROS/SIMPLE',
    `hadoop_username` varchar(64)  DEFAULT NULL COMMENT 'hadpp username when auth method is SIMPLE',
    `krb_keytab`      text COMMENT 'kerberos keytab when auth method is KERBEROS',
    `krb_conf`        text COMMENT 'kerberos conf when auth method is KERBEROS',
    `krb_principal`   text COMMENT 'kerberos principal when auth method is KERBEROS',
    `current_tx_id`   bigint(20) DEFAULT NULL COMMENT 'current transaction id',
    `cur_schema_id`   int(11) NOT NULL DEFAULT 0 COMMENT 'current schema id',
    PRIMARY KEY `table_name_index` (`catalog_name`,`db_name`,`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Table metadata';

CREATE TABLE `optimize_table_runtime`
(
    `catalog_name`               varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`                    varchar(64) NOT NULL COMMENT 'Database name',
    `table_name`                 varchar(64) NOT NULL COMMENT 'Table name',
    `current_snapshot_id`        bigint(20) NOT NULL DEFAULT '-1' COMMENT 'Base table current snapshot id',
    `latest_major_optimize_time` mediumtext COMMENT 'Latest Major Optimize time for all partitions',
    `latest_minor_optimize_time` mediumtext COMMENT 'Latest Minor Optimize time for all partitions',
    `latest_task_plan_group`     varchar(40) DEFAULT NULL COMMENT 'Latest task plan group',
    `optimize_status`            varchar(20) DEFAULT 'Idle' COMMENT 'Table optimize status: MajorOptimizing, MinorOptimizing, Pending, Idle',
    `optimize_status_start_time` datetime(3) DEFAULT NULL COMMENT 'Table optimize status start time',
    `current_change_snapshotId`  bigint(20) DEFAULT NULL COMMENT 'Change table current snapshot id',
    `latest_full_optimize_time`  MEDIUMTEXT NULL COMMENT 'Latest Full Optimize time for all partitions',
    PRIMARY KEY `table_name_index` (`catalog_name`,`db_name`,`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Optimize running information of each table';

CREATE TABLE `optimize_task_history`
(
    `task_trace_id`     varchar(50) NOT NULL COMMENT 'Optimize task uuid',
    `retry`             int(11) NOT NULL COMMENT 'Retry times for the same task_trace_id',
    `task_plan_group`   varchar(40) NOT NULL COMMENT 'Plan group of task, task of one plan group are planned together',
    `catalog_name`      varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`           varchar(64) NOT NULL COMMENT 'Database name',
    `table_name`        varchar(64) NOT NULL COMMENT 'Table name',
    `start_time`        datetime(3) DEFAULT NULL COMMENT 'Task start time',
    `end_time`          datetime(3) DEFAULT NULL COMMENT 'Task end time',
    `cost_time`         bigint(20) DEFAULT NULL COMMENT 'Task cost time',
    `queue_id`          int(11) DEFAULT NULL COMMENT 'Queue id which execute task',
    PRIMARY KEY (`task_trace_id`, `retry`),
    KEY `table_end_time_plan_group_index` (`catalog_name`, `db_name`, `table_name`, `end_time`, `task_plan_group`),
    KEY `table_plan_group_index` (`catalog_name`, `db_name`, `table_name`, `task_plan_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'History of each optimize task execute';

CREATE TABLE `table_transaction_meta`
(
    `table_identifier` varchar(384) NOT NULL COMMENT 'table full name with catalog.db.table',
    `transaction_id`   bigint(20) NOT NULL COMMENT 'allocated transaction id',
    `signature`        varchar(256) NOT NULL COMMENT 'transaction request signature',
    `commit_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'transaction allocate time',
    PRIMARY KEY (`table_identifier`,`transaction_id`),
    UNIQUE KEY `signature_unique` (`table_identifier`,`signature`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'transaction meta info of table';

CREATE TABLE `api_tokens`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `apikey`     varchar(256) NOT NULL COMMENT 'openapi client public key',
    `secret`     varchar(256) NOT NULL COMMENT 'The key used by the client to generate the request signature',
    `apply_time` datetime DEFAULT NULL COMMENT 'apply time',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `account_unique` (`apikey`) USING BTREE COMMENT 'account unique'
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='Openapi  secret';

CREATE TABLE `ddl_record`
(
    `table_identifier` varchar(384) NOT NULL COMMENT 'table full name with catalog.db.table',
    `ddl`              mediumtext COMMENT 'ddl',
    `ddl_type`         varchar(256) NOT NULL COMMENT 'ddl type',
    `commit_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'ddl commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'ddl record of table';

CREATE TABLE `table_metric_statistics`
(
    `table_identifier` varchar(384) NOT NULL COMMENT 'table full name with catalog.db.table',
    `inner_table`      varchar(64)  NOT NULL COMMENT 'table type like change/base',
    `metric_name`      varchar(256) NOT NULL COMMENT 'metric name',
    `metric_value`     varchar(256) NOT NULL COMMENT 'metric value',
    `commit_time`      timestamp    NOT NULL ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'metric commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'metric statistics of table';

CREATE TABLE `metric_statistics_summary`
(
    `metric_name`      varchar(256) NOT NULL COMMENT 'metric name',
    `metric_value`     varchar(256) NOT NULL COMMENT 'metric value',
    `commit_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'metric commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'metric statistics summary';

CREATE TABLE `optimizer_metric_statistics`
(
    `optimizer_id`     bigint NOT NULL COMMENT 'optimizer id',
    `subtask_id`       varchar(256)  NOT NULL COMMENT 'optimizer subtask id',
    `metric_name`      varchar(256) NOT NULL COMMENT 'metric name',
    `metric_value`     varchar(256) NOT NULL COMMENT 'metric value',
    `commit_time`      timestamp    NOT NULL ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'metric commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'metric statistics of optimizer';