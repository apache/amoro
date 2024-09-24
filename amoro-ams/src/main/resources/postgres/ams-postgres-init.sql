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

CREATE TABLE catalog_metadata
(
    catalog_id serial PRIMARY KEY,
    catalog_name varchar(64) NOT NULL,
    catalog_metastore varchar(64) NOT NULL,
    storage_configs text,
    auth_configs text,
    catalog_properties text,
    database_count integer NOT NULL DEFAULT 0,
    table_count integer NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX catalog_name_index ON catalog_metadata (catalog_name);

COMMENT ON TABLE catalog_metadata IS 'Catalog metadata';
COMMENT ON COLUMN catalog_metadata.catalog_id IS 'Catalog ID';
COMMENT ON COLUMN catalog_metadata.catalog_name IS 'Catalog name';
COMMENT ON COLUMN catalog_metadata.catalog_metastore IS 'Catalog type like hms/ams/hadoop/custom';
COMMENT ON COLUMN catalog_metadata.storage_configs IS 'Base64 code of storage configurations';
COMMENT ON COLUMN catalog_metadata.auth_configs IS 'Base64 code of authentication configurations';
COMMENT ON COLUMN catalog_metadata.catalog_properties IS 'Catalog properties';

CREATE TABLE database_metadata
(
    catalog_name varchar(64) NOT NULL,
    db_name varchar(128) NOT NULL,
    table_count integer NOT NULL DEFAULT 0,
    PRIMARY KEY (catalog_name, db_name)
);
COMMENT ON TABLE database_metadata IS 'Database metadata';
COMMENT ON COLUMN database_metadata.catalog_name IS 'Catalog name';
COMMENT ON COLUMN database_metadata.db_name IS 'Database name';
COMMENT ON COLUMN database_metadata.table_count IS 'Number of tables in the database';

CREATE TABLE optimizer
(
    token varchar(50) NOT NULL,
    resource_id varchar(100) DEFAULT NULL,
    group_name varchar(50) DEFAULT NULL,
    container_name varchar(100) DEFAULT NULL,
    start_time timestamp not null default CURRENT_TIMESTAMP,
    touch_time timestamp not null default CURRENT_TIMESTAMP,
    thread_count integer DEFAULT NULL,
    total_memory bigint DEFAULT NULL,
    properties text,
    PRIMARY KEY (token)
);
CREATE INDEX optimizer_resource_group_index ON optimizer (group_name);

COMMENT ON TABLE optimizer IS 'Optimizer metadata';
COMMENT ON COLUMN optimizer.resource_id IS 'Optimizer instance ID';
COMMENT ON COLUMN optimizer.group_name IS 'Group/queue name';
COMMENT ON COLUMN optimizer.container_name IS 'Container name';
COMMENT ON COLUMN optimizer.start_time IS 'Optimizer start time';
COMMENT ON COLUMN optimizer.touch_time IS 'Update time';
COMMENT ON COLUMN optimizer.thread_count IS 'Total number of all CPU resources';
COMMENT ON COLUMN optimizer.total_memory IS 'Optimizer memory usage';
COMMENT ON COLUMN optimizer.properties IS 'optimizer state info, contains like yarn application id and flink job id';

CREATE TABLE resource
(
    resource_id VARCHAR(100) PRIMARY KEY,
    resource_type SMALLINT DEFAULT 0,
    container_name VARCHAR(100),
    group_name VARCHAR(50),
    thread_count INT,
    total_memory BIGINT,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    properties TEXT
);
CREATE INDEX resource_resource_group_index ON resource (group_name);

COMMENT ON TABLE resource IS 'Optimizer instance info';
COMMENT ON COLUMN resource.resource_id IS 'Optimizer instance id';
COMMENT ON COLUMN resource.resource_type IS 'Resource type like optimizer/ingestor';
COMMENT ON COLUMN resource.container_name IS 'Container name';
COMMENT ON COLUMN resource.group_name IS 'Queue name';
COMMENT ON COLUMN resource.thread_count IS 'Total number of all CPU resources';
COMMENT ON COLUMN resource.total_memory IS 'Optimizer use memory size';
COMMENT ON COLUMN resource.start_time IS 'Optimizer start time';
COMMENT ON COLUMN resource.properties IS 'Optimizer instance properties';

CREATE TABLE resource_group
(
    group_name varchar(50) NOT NULL,
    container_name varchar(100) DEFAULT NULL,
    properties TEXT,
    PRIMARY KEY (group_name)
);
COMMENT ON TABLE resource_group IS 'Group to divide optimize resources';
COMMENT ON COLUMN resource_group.group_name IS 'Optimize group name';
COMMENT ON COLUMN resource_group.container_name IS 'Container name';
COMMENT ON COLUMN resource_group.properties IS 'Properties';


CREATE TABLE table_identifier
(
    table_id BIGSERIAL PRIMARY KEY,
    catalog_name VARCHAR(64) NOT NULL,
    db_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(256) NOT NULL,
    format     VARCHAR(32)  NOT NULL
);
CREATE UNIQUE INDEX table_name_index ON table_identifier (catalog_name, db_name, table_name);

COMMENT ON TABLE table_identifier IS 'Table identifier';
COMMENT ON COLUMN table_identifier.table_id IS 'Auto-increment ID';
COMMENT ON COLUMN table_identifier.catalog_name IS 'Catalog name';
COMMENT ON COLUMN table_identifier.db_name IS 'Database name';
COMMENT ON COLUMN table_identifier.table_name IS 'Table name';
COMMENT ON COLUMN table_identifier.format IS 'Table format';

CREATE TABLE table_metadata
(
    table_id BIGINT NOT NULL,
    catalog_name VARCHAR(64) NOT NULL,
    db_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(256) NOT NULL,
    primary_key VARCHAR(256),
    sort_key VARCHAR(256),
    table_location VARCHAR(256),
    base_location VARCHAR(256),
    change_location VARCHAR(256),
    properties TEXT,
    meta_store_site TEXT,
    hdfs_site TEXT,
    core_site TEXT,
    auth_method VARCHAR(32),
    hadoop_username VARCHAR(64),
    krb_keytab TEXT,
    krb_conf TEXT,
    krb_principal TEXT,
    current_schema_id INT NOT NULL DEFAULT 0,
    meta_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (table_id)
);
COMMENT ON TABLE table_metadata IS 'Table metadata';
COMMENT ON COLUMN table_metadata.table_id IS 'Table ID';
COMMENT ON COLUMN table_metadata.catalog_name IS 'Catalog name';
COMMENT ON COLUMN table_metadata.db_name IS 'Database name';
COMMENT ON COLUMN table_metadata.table_name IS 'Table name';
COMMENT ON COLUMN table_metadata.primary_key IS 'Primary key';
COMMENT ON COLUMN table_metadata.sort_key IS 'Sort key';
COMMENT ON COLUMN table_metadata.table_location IS 'Table location';
COMMENT ON COLUMN table_metadata.base_location IS 'Base table location';
COMMENT ON COLUMN table_metadata.change_location IS 'Change table location';
COMMENT ON COLUMN table_metadata.properties IS 'Table properties';
COMMENT ON COLUMN table_metadata.meta_store_site IS 'Base64 code of meta store site';
COMMENT ON COLUMN table_metadata.hdfs_site IS 'Base64 code of HDFS site';
COMMENT ON COLUMN table_metadata.core_site IS 'Base64 code of core site';
COMMENT ON COLUMN table_metadata.auth_method IS 'Authentication method like KERBEROS/SIMPLE';
COMMENT ON COLUMN table_metadata.hadoop_username IS 'Hadoop username when auth method is SIMPLE';
COMMENT ON COLUMN table_metadata.krb_keytab IS 'Kerberos keytab when auth method is KERBEROS';
COMMENT ON COLUMN table_metadata.krb_conf IS 'Kerberos conf when auth method is KERBEROS';
COMMENT ON COLUMN table_metadata.krb_principal IS 'Kerberos principal when auth method is KERBEROS';
COMMENT ON COLUMN table_metadata.current_schema_id IS 'Current schema ID';

CREATE TABLE table_runtime
(
    table_id BIGINT NOT NULL,
    catalog_name VARCHAR(64) NOT NULL,
    db_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(256) NOT NULL,
    current_snapshot_id BIGINT NOT NULL DEFAULT -1,
    current_change_snapshotId BIGINT,
    last_optimized_snapshotId BIGINT NOT NULL DEFAULT -1,
    last_optimized_change_snapshotId BIGINT NOT NULL DEFAULT -1,
    last_major_optimizing_time TIMESTAMP,
    last_minor_optimizing_time TIMESTAMP,
    last_full_optimizing_time TIMESTAMP,
    optimizing_status_code INT DEFAULT 700,
    optimizing_status_start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    optimizing_process_id BIGINT NOT NULL,
    optimizer_group VARCHAR(64) NOT NULL,
    table_config TEXT,
    optimizing_config TEXT,
    pending_input TEXT,
    table_summary TEXT,
    PRIMARY KEY (table_id),
    UNIQUE (catalog_name, db_name, table_name)
);
COMMENT ON TABLE table_runtime IS 'Optimize running information of each table';
COMMENT ON COLUMN table_runtime.table_id IS 'Table ID';
COMMENT ON COLUMN table_runtime.catalog_name IS 'Catalog name';
COMMENT ON COLUMN table_runtime.db_name IS 'Database name';
COMMENT ON COLUMN table_runtime.table_name IS 'Table name';
COMMENT ON COLUMN table_runtime.current_snapshot_id IS 'Base table current snapshot ID';
COMMENT ON COLUMN table_runtime.current_change_snapshotId IS 'Change table current snapshot ID';
COMMENT ON COLUMN table_runtime.last_optimized_snapshotId IS 'Last optimized snapshot ID';
COMMENT ON COLUMN table_runtime.last_optimized_change_snapshotId IS 'Last optimized change snapshot ID';
COMMENT ON COLUMN table_runtime.last_major_optimizing_time IS 'Latest Major Optimize time for all partitions';
COMMENT ON COLUMN table_runtime.last_minor_optimizing_time IS 'Latest Minor Optimize time for all partitions';
COMMENT ON COLUMN table_runtime.last_full_optimizing_time IS 'Latest Full Optimize time for all partitions';
COMMENT ON COLUMN table_runtime.optimizing_status_code IS 'Table optimize status code: 100(FULL_OPTIMIZING),' ||
        ' 200(MAJOR_OPTIMIZING), 300(MINOR_OPTIMIZING), 400(COMMITTING), 500(PLANING), 600(PENDING), 700(IDLE)';
COMMENT ON COLUMN table_runtime.optimizing_status_start_time IS 'Table optimize status start time';
COMMENT ON COLUMN table_runtime.optimizing_process_id IS 'Optimizing procedure UUID';
COMMENT ON COLUMN table_runtime.optimizer_group IS 'Optimizer group';
COMMENT ON COLUMN table_runtime.table_config IS 'Table-specific configuration';
COMMENT ON COLUMN table_runtime.optimizing_config IS 'Optimizing configuration';
COMMENT ON COLUMN table_runtime.pending_input IS 'Pending input data';
COMMENT ON COLUMN table_runtime.table_summary IS 'Table summary data';
CREATE INDEX idx_optimizer_status_and_time ON table_runtime(optimizing_status_code, optimizing_status_start_time DESC);

CREATE TABLE table_optimizing_process
(
    process_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    catalog_name VARCHAR(64) NOT NULL,
    db_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(256) NOT NULL,
    target_snapshot_id BIGINT NOT NULL,
    target_change_snapshot_id BIGINT NOT NULL,
    status VARCHAR(10) NOT NULL,
    optimizing_type VARCHAR(10) NOT NULL,
    plan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    fail_reason VARCHAR(4096),
    rewrite_input BYTEA,
    summary TEXT,
    from_sequence TEXT,
    to_sequence TEXT,
    PRIMARY KEY (process_id)
);
CREATE INDEX process_index ON table_optimizing_process (table_id, plan_time);

COMMENT ON TABLE table_optimizing_process IS 'History of optimizing after each commit';
COMMENT ON COLUMN table_optimizing_process.process_id IS 'Optimizing procedure UUID';
COMMENT ON COLUMN table_optimizing_process.table_id IS 'Table ID';
COMMENT ON COLUMN table_optimizing_process.catalog_name IS 'Catalog name';
COMMENT ON COLUMN table_optimizing_process.db_name IS 'Database name';
COMMENT ON COLUMN table_optimizing_process.table_name IS 'Table name';
COMMENT ON COLUMN table_optimizing_process.target_snapshot_id IS 'Target snapshot ID';
COMMENT ON COLUMN table_optimizing_process.target_change_snapshot_id IS 'Target change snapshot ID';
COMMENT ON COLUMN table_optimizing_process.status IS 'Optimizing status';
COMMENT ON COLUMN table_optimizing_process.optimizing_type IS 'Optimizing type: Major, Minor';
COMMENT ON COLUMN table_optimizing_process.plan_time IS 'Plan time';
COMMENT ON COLUMN table_optimizing_process.end_time IS 'Finish time or failed time';
COMMENT ON COLUMN table_optimizing_process.fail_reason IS 'Error message after task failure';
COMMENT ON COLUMN table_optimizing_process.rewrite_input IS 'Rewrite input files';
COMMENT ON COLUMN table_optimizing_process.summary IS 'Summary of optimizing tasks';
COMMENT ON COLUMN table_optimizing_process.from_sequence IS 'From or min sequence of each partition';
COMMENT ON COLUMN table_optimizing_process.to_sequence IS 'To or max sequence of each partition';

CREATE TABLE task_runtime
(
    process_id BIGINT NOT NULL,
    task_id INT NOT NULL,
    retry_num INT,
    table_id BIGINT NOT NULL,
    partition_data VARCHAR(128),
    create_time TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    cost_time BIGINT,
    status VARCHAR(16),
    fail_reason VARCHAR(4096),
    optimizer_token VARCHAR(50),
    thread_id INT,
    rewrite_output BYTEA,
    metrics_summary TEXT,
    properties TEXT,
    PRIMARY KEY (process_id, task_id)
);
CREATE INDEX task_runtime_index ON table_optimizing_process (table_id, process_id);

COMMENT ON TABLE task_runtime IS 'Optimize task basic information';
COMMENT ON COLUMN task_runtime.process_id IS 'Process ID';
COMMENT ON COLUMN task_runtime.task_id IS 'Task ID';
COMMENT ON COLUMN task_runtime.retry_num IS 'Retry times';
COMMENT ON COLUMN task_runtime.table_id IS 'Table ID';
COMMENT ON COLUMN task_runtime.partition_data IS 'Partition data';
COMMENT ON COLUMN task_runtime.create_time IS 'Task create time';
COMMENT ON COLUMN task_runtime.start_time IS 'Time when task starts waiting to execute';
COMMENT ON COLUMN task_runtime.end_time IS 'Time when task is finished';
COMMENT ON COLUMN task_runtime.cost_time IS 'Task execution time';
COMMENT ON COLUMN task_runtime.status IS 'Optimize Status: PLANNED, SCHEDULED, ACKED, FAILED, SUCCESS, CANCELED';
COMMENT ON COLUMN task_runtime.fail_reason IS 'Error message after task failure';
COMMENT ON COLUMN task_runtime.optimizer_token IS 'Job type';
COMMENT ON COLUMN task_runtime.thread_id IS 'Job ID';
COMMENT ON COLUMN task_runtime.rewrite_output IS 'Rewrite files output';
COMMENT ON COLUMN task_runtime.metrics_summary IS 'Metrics summary';
COMMENT ON COLUMN task_runtime.properties IS 'Task properties';

CREATE TABLE optimizing_task_quota
(
    process_id BIGINT NOT NULL,
    task_id INT NOT NULL,
    retry_num INT DEFAULT 0,
    table_id BIGINT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fail_reason VARCHAR(4096),
    PRIMARY KEY (process_id, task_id, retry_num)
);
CREATE INDEX quota_index ON table_optimizing_process (table_id);

COMMENT ON TABLE optimizing_task_quota IS 'Optimize task basic information';
COMMENT ON COLUMN optimizing_task_quota.process_id IS 'Optimizing procedure UUID';
COMMENT ON COLUMN optimizing_task_quota.task_id IS 'Task ID';
COMMENT ON COLUMN optimizing_task_quota.retry_num IS 'Retry times';
COMMENT ON COLUMN optimizing_task_quota.table_id IS 'Table ID';
COMMENT ON COLUMN optimizing_task_quota.start_time IS 'Time when task starts waiting to execute';
COMMENT ON COLUMN optimizing_task_quota.end_time IS 'Time when task is finished';
COMMENT ON COLUMN optimizing_task_quota.fail_reason IS 'Error message after task failure';

CREATE TABLE api_tokens
(
    id SERIAL PRIMARY KEY,
    apikey VARCHAR(256) NOT NULL,
    secret VARCHAR(256) NOT NULL,
    apply_time TIMESTAMP,
    UNIQUE (apikey)
);
COMMENT ON TABLE api_tokens IS 'OpenAPI client tokens';
COMMENT ON COLUMN api_tokens.id IS 'Token ID';
COMMENT ON COLUMN api_tokens.apikey IS 'API Key';
COMMENT ON COLUMN api_tokens.secret IS 'API Secret';
COMMENT ON COLUMN api_tokens.apply_time IS 'Token application time';

CREATE TABLE platform_file
(
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(100) NOT NULL,
    file_content_b64 TEXT NOT NULL,
    file_path VARCHAR(100),
    add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE platform_file IS 'Files stored in the platform';
COMMENT ON COLUMN platform_file.id IS 'File ID';
COMMENT ON COLUMN platform_file.file_name IS 'File Name';
COMMENT ON COLUMN platform_file.file_content_b64 IS 'File content encoded with base64';
COMMENT ON COLUMN platform_file.file_path IS 'File path (may be an HDFS path, not used currently)';
COMMENT ON COLUMN platform_file.add_time IS 'File add timestamp';

CREATE TABLE table_blocker
(
    blocker_id BIGSERIAL PRIMARY KEY,
    catalog_name VARCHAR(64) NOT NULL,
    db_name VARCHAR(128) NOT NULL,
    table_name VARCHAR(256) NOT NULL,
    operations VARCHAR(128) NOT NULL,
    create_time TIMESTAMP,
    expiration_time TIMESTAMP,
    properties TEXT,
    prev_blocker_id BIGSERIAL NOT NULL
);
CREATE UNIQUE INDEX uq_prev ON table_blocker (catalog_name, db_name, table_name, prev_blocker_id);

COMMENT ON TABLE table_blocker IS 'Table blockers';
COMMENT ON COLUMN table_blocker.blocker_id IS 'Blocker unique ID';
COMMENT ON COLUMN table_blocker.catalog_name IS 'Catalog name';
COMMENT ON COLUMN table_blocker.db_name IS 'Database name';
COMMENT ON COLUMN table_blocker.table_name IS 'Table name';
COMMENT ON COLUMN table_blocker.operations IS 'Blocked operations';
COMMENT ON COLUMN table_blocker.create_time IS 'Blocker create time';
COMMENT ON COLUMN table_blocker.expiration_time IS 'Blocker expiration time';
COMMENT ON COLUMN table_blocker.properties IS 'Blocker properties';
COMMENT ON COLUMN table_blocker.prev_blocker_id is 'prev blocker id when created';
