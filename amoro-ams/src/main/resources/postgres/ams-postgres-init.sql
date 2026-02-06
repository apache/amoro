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


create table if not exists table_runtime (
    table_id            bigint primary key,
    group_name          varchar(64) not null,
    status_code         int not null default 700,
    status_code_update_time timestamptz not null default now(),
    table_config        text,
    table_summary       text,
    bucket_id           varchar(4)
);

create index if not exists idx_status_and_time
    on table_runtime (status_code, status_code_update_time desc);

comment on table  table_runtime is 'Table running information of each table';
comment on column table_runtime.status_code is 'Table runtime status code.';
comment on column table_runtime.status_code_update_time is 'Table runtime status code update time';
comment on column table_runtime.table_config is 'table configuration cached from table.properties';
comment on column table_runtime.table_summary is 'table summary for ams';
comment on column table_runtime.bucket_id is 'Bucket number to which the record table belongs';

create table if not exists table_runtime_state (
    state_id      bigserial primary key,
    table_id      bigint not null,
    state_key     varchar(256) not null,
    state_value   text,
    state_version bigint not null default 0,
    create_time   timestamptz not null default now(),
    update_time   timestamptz not null default now()
);

create unique index if not exists uniq_table_state_key
    on table_runtime_state (table_id, state_key);

comment on table  table_runtime_state is 'State of Table Runtimes';
comment on column table_runtime_state.state_id is 'Primary key';
comment on column table_runtime_state.table_id is 'Table identifier id';
comment on column table_runtime_state.state_key is 'Table Runtime state key';
comment on column table_runtime_state.state_value is 'Table Runtime state value, string type';
comment on column table_runtime_state.state_version is 'Table runtime state version, auto inc when update';
comment on column table_runtime_state.create_time is 'create time';
comment on column table_runtime_state.update_time is 'update time';

CREATE TABLE table_process (
    process_id                      bigserial PRIMARY KEY,
    table_id                        bigint NOT NULL,
    external_process_identifier     varchar(256) DEFAULT NULL,
    status                          varchar(64) NOT NULL,
    process_type                    varchar(64) NOT NULL,
    process_stage                   varchar(64) NOT NULL,
    execution_engine                varchar(64) NOT NULL,
    retry_number                    int NOT NULL,
    create_time                     timestamptz NOT NULL DEFAULT now(),
    finish_time                     timestamptz,
    fail_message                    text CHECK (length(fail_message) <= 4096),
    process_parameters              text,
    summary                         text,
    CONSTRAINT table_process_unique UNIQUE (process_id)
);

CREATE INDEX table_process_table_idx ON table_process (table_id, create_time);

COMMENT ON TABLE  table_process IS 'History of optimizing after each commit';
COMMENT ON COLUMN table_process.process_id                  IS 'table process id';
COMMENT ON COLUMN table_process.table_id                    IS 'table id';
COMMENT ON COLUMN table_process.external_process_identifier IS 'Table optimizing external processidentifier';
COMMENT ON COLUMN table_process.status                      IS 'Table optimizing status';
COMMENT ON COLUMN table_process.process_type                IS 'Process action type';
COMMENT ON COLUMN table_process.process_stage               IS 'Process current stage';
COMMENT ON COLUMN table_process.execution_engine            IS 'Execution engine';
COMMENT ON COLUMN table_process.retry_number                IS 'Retry times';
COMMENT ON COLUMN table_process.create_time                 IS 'First plan time';
COMMENT ON COLUMN table_process.finish_time                 IS 'finish time or failed time';
COMMENT ON COLUMN table_process.fail_message                IS 'Error message after task failed';
COMMENT ON COLUMN table_process.process_parameters          IS 'Table process parameters';
COMMENT ON COLUMN table_process.summary                     IS 'Max change transaction id of these tasks';

CREATE TABLE optimizing_process_state (
    process_id                bigint PRIMARY KEY,
    table_id                  bigint NOT NULL,
    target_snapshot_id        bigint NOT NULL,
    target_change_snapshot_id bigint NOT NULL,
    rewrite_input             bytea,
    from_sequence             text,
    to_sequence               text
);

CREATE INDEX optimizing_process_state_table_idx ON optimizing_process_state (table_id);

COMMENT ON TABLE  optimizing_process_state IS 'History of optimizing after each commit';
COMMENT ON COLUMN optimizing_process_state.process_id                IS 'optimizing_procedure UUID';
COMMENT ON COLUMN optimizing_process_state.table_id                  IS 'table id';
COMMENT ON COLUMN optimizing_process_state.target_snapshot_id        IS 'target snapshot id';
COMMENT ON COLUMN optimizing_process_state.target_change_snapshot_id IS 'target change snapshot id';
COMMENT ON COLUMN optimizing_process_state.rewrite_input             IS 'rewrite files input';
COMMENT ON COLUMN optimizing_process_state.from_sequence             IS 'from or min sequence of each partition';
COMMENT ON COLUMN optimizing_process_state.to_sequence               IS 'to or max sequence of each partition';

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
CREATE INDEX task_runtime_index ON task_runtime (table_id, process_id);

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

CREATE TABLE table_process_state
(
     process_id   BIGINT NOT NULL,
     action       VARCHAR(16) NOT NULL,
     table_id     BIGINT NOT NULL,
     retry_num    INT DEFAULT NULL,
     status       VARCHAR(10) NOT NULL,
     start_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     end_time     TIMESTAMP DEFAULT NULL,
     fail_reason  VARCHAR(4096) DEFAULT NULL,
     summary      TEXT,
     PRIMARY KEY (process_id)
);
CREATE INDEX table_process_state_index ON table_process_state (table_id, start_time);

COMMENT ON TABLE table_process_state IS 'History of optimizing after each commit';

COMMENT ON COLUMN table_process_state.process_id IS 'optimizing_procedure UUID';
COMMENT ON COLUMN table_process_state.action IS 'process action';
COMMENT ON COLUMN table_process_state.retry_num IS 'Retry times';
COMMENT ON COLUMN table_process_state.status IS 'Direct to TableOptimizingStatus';
COMMENT ON COLUMN table_process_state.start_time IS 'First plan time';
COMMENT ON COLUMN table_process_state.end_time IS 'finish time or failed time';
COMMENT ON COLUMN table_process_state.fail_reason IS 'Error message after task failed';
COMMENT ON COLUMN table_process_state.summary IS 'state summary, usually a map';

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
CREATE INDEX quota_index ON optimizing_task_quota (table_id);

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


CREATE TABLE http_session (
    session_id    VARCHAR(120) NOT NULL,
    context_path  VARCHAR(60),
    virtual_host  VARCHAR(60),
    last_node     VARCHAR(60),
    access_time   BIGINT,
    last_access_time  BIGINT,
    create_time   BIGINT,
    cookie_time   BIGINT,
    last_save_time BIGINT,
    expiry_time   BIGINT,
    max_interval  BIGINT,
    data_store    BYTEA,
    PRIMARY KEY (session_id, context_path, virtual_host)
);
CREATE INDEX idx_session_expiry ON http_session (expiry_time);

COMMENT ON COLUMN http_session.session_id IS 'Http session id';
COMMENT ON COLUMN http_session.context_path IS 'Jetty context path';
COMMENT ON COLUMN http_session.virtual_host IS 'Jetty virtual host';
COMMENT ON COLUMN http_session.last_node IS 'Last node';
COMMENT ON COLUMN http_session.access_time IS 'Access time';
COMMENT ON COLUMN http_session.last_access_time IS 'Last access time';
COMMENT ON COLUMN http_session.create_time IS 'Create time';
COMMENT ON COLUMN http_session.cookie_time IS 'Cookie time';
COMMENT ON COLUMN http_session.last_save_time IS 'Last save time';
COMMENT ON COLUMN http_session.expiry_time IS 'Expiry time';
COMMENT ON COLUMN http_session.max_interval IS 'Max internal';
COMMENT ON COLUMN http_session.data_store IS 'Session data store';
COMMENT ON TABLE http_session IS 'Http session store';

CREATE TABLE IF NOT EXISTS ha_lease (
  cluster_name       VARCHAR(64)  NOT NULL,
  service_name       VARCHAR(64)  NOT NULL,
  node_id            VARCHAR(256) NULL,
  node_ip            VARCHAR(64)  NULL,
  server_info_json   TEXT         NULL,
  lease_expire_ts    BIGINT       NULL,
  version            INT          NOT NULL DEFAULT 0,
  updated_at         BIGINT       NOT NULL,
  PRIMARY KEY (cluster_name, service_name)
);

CREATE INDEX IF NOT EXISTS idx_ha_lease_expire ON ha_lease (lease_expire_ts);
CREATE INDEX IF NOT EXISTS idx_ha_lease_node   ON ha_lease (node_id);

COMMENT ON COLUMN service_name IS 'Service name (AMS/TABLE_SERVICE/OPTIMIZING_SERVICE)';
COMMENT ON COLUMN node_id IS 'Unique node identifier (host:port:uuid)';
COMMENT ON COLUMN node_ip IS 'Node IP address';
COMMENT ON COLUMN server_info_json IS 'JSON encoded server info (AmsServerInfo)';
COMMENT ON COLUMN lease_expire_ts IS 'Lease expiration timestamp (ms since epoch)';
COMMENT ON COLUMN version IS 'Optimistic lock version of the lease row';
COMMENT ON COLUMN updated_at IS 'Last update timestamp (ms since epoch)';
CREATE TABLE dynamic_conf
(
    id          BIGSERIAL PRIMARY KEY,
    conf_key    varchar(256) NOT NULL,
    conf_value  varchar(256) NOT NULL,
    conf_group  varchar(256) NOT NULL,
    plugin_name varchar(256),
    CONSTRAINT uk_conf UNIQUE (conf_group, plugin_name, conf_key)
);

COMMENT ON TABLE dynamic_conf IS 'Dynamic configuration overrides for AMS and plugins';
COMMENT ON COLUMN dynamic_conf.conf_group IS 'Group: AMS for service, PLUGIN_{category} for plugins';
COMMENT ON COLUMN dynamic_conf.plugin_name IS 'Plugin identifier; valid only when conf_group = PLUGIN_*';
