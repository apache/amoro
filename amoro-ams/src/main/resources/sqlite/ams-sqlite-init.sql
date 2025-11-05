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

CREATE TABLE catalog_metadata (
    catalog_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    catalog_name           VARCHAR(64) NOT NULL,
    catalog_metastore      VARCHAR(64) NOT NULL,
    storage_configs        TEXT,
    auth_configs           TEXT,
    catalog_properties     TEXT,
    database_count         INTEGER NOT NULL DEFAULT 0,
    table_count            INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT catalog_name_index UNIQUE (catalog_name)
);

CREATE TABLE database_metadata (
    catalog_name           VARCHAR(64) NOT NULL,
    db_name                VARCHAR(128) NOT NULL,
    table_count            INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (catalog_name, db_name)
);

CREATE TABLE optimizer (
    token                      VARCHAR(300) NOT NULL,
    resource_id                VARCHAR(100) DEFAULT NULL,
    group_name                 VARCHAR(50),
    container_name             VARCHAR(100),
    start_time                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    touch_time                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thread_count               INTEGER,
    total_memory               INTEGER,
    properties                 TEXT,
    PRIMARY KEY (token)
);

CREATE TABLE resource (
    resource_id               VARCHAR(100),
    resource_type             INTEGER DEFAULT 0,
    container_name            VARCHAR(100),
    group_name                VARCHAR(50),
    thread_count              INTEGER,
    total_memory              INTEGER,
    start_time                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    properties                TEXT,
    CONSTRAINT resource_pk PRIMARY KEY (resource_id)
);

CREATE TABLE resource_group (
    group_name       VARCHAR(50) NOT NULL,
    container_name   VARCHAR(100),
    properties       TEXT,
    PRIMARY KEY (group_name)
);

CREATE TABLE table_identifier (
    table_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    catalog_name    VARCHAR(64) NOT NULL,
    db_name         VARCHAR(128) NOT NULL,
    table_name      VARCHAR(256) NOT NULL,
    format          VARCHAR(32)  NOT NULL,
    CONSTRAINT table_name_idx UNIQUE (catalog_name, db_name, table_name)
);

CREATE TABLE table_metadata (
    table_id         INTEGER NOT NULL,
    catalog_name     VARCHAR(256) NOT NULL,
    db_name          VARCHAR(256) NOT NULL,
    table_name       VARCHAR(256) NOT NULL,
    primary_key      VARCHAR(256),
    sort_key         VARCHAR(256),
    table_location   VARCHAR(256),
    base_location    VARCHAR(256),
    change_location  VARCHAR(256),
    properties       TEXT,
    meta_store_site  TEXT,
    hdfs_site        TEXT,
    core_site        TEXT,
    auth_method      VARCHAR(32),
    hadoop_username  VARCHAR(64),
    krb_keytab       TEXT,
    krb_conf         TEXT,
    krb_principal    TEXT,
    current_schema_id INTEGER NOT NULL DEFAULT 0,
    meta_version     INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT table_metadata_pk PRIMARY KEY (table_id)
);

CREATE TABLE table_runtime (
    table_id            INTEGER NOT NULL PRIMARY KEY,
    group_name          VARCHAR(64) NOT NULL,
    status_code         INTEGER NOT NULL DEFAULT 700,
    status_code_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    table_config        TEXT,
    table_summary       TEXT,
    bucket_id          VARCHAR(4)
);

CREATE INDEX idx_status_time_desc
    ON table_runtime (status_code, status_code_update_time DESC);

CREATE TABLE table_runtime_state (
    state_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    table_id        INTEGER NOT NULL,
    state_key       VARCHAR(256) NOT NULL,
    state_value     TEXT,
    state_version   INTEGER NOT NULL DEFAULT 0,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uniq_table_state_key UNIQUE (table_id, state_key)
);

CREATE UNIQUE INDEX uniq_table_state_key ON table_runtime_state (table_id, state_key);

CREATE TABLE table_process (
    process_id       INTEGER NOT NULL PRIMARY KEY,
    table_id         INTEGER NOT NULL,
    status           VARCHAR(64) NOT NULL,
    process_type     VARCHAR(64) NOT NULL,
    process_stage    VARCHAR(64) NOT NULL,
    execution_engine VARCHAR(64) NOT NULL,
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finish_time      TIMESTAMP DEFAULT NULL,
    fail_message     TEXT,
    summary          TEXT
);
CREATE INDEX table_process_table_idx ON table_process (table_id, create_time);

CREATE TABLE optimizing_process_state (
    process_id                INTEGER NOT NULL PRIMARY KEY,
    table_id                  INTEGER NOT NULL,
    target_snapshot_id        INTEGER NOT NULL,
    target_change_snapshot_id INTEGER NOT NULL,
    rewrite_input             BLOB,
    from_sequence             TEXT,
    to_sequence               TEXT
);
CREATE INDEX optimizing_process_state_table_idx
    ON optimizing_process_state (table_id);

CREATE TABLE task_runtime (
    process_id      INTEGER NOT NULL,
    task_id         INTEGER NOT NULL,
    retry_num       INTEGER,
    table_id        INTEGER NOT NULL,
    partition_data  VARCHAR(128),
    create_time     TIMESTAMP DEFAULT NULL,
    start_time      TIMESTAMP DEFAULT NULL,
    end_time        TIMESTAMP DEFAULT NULL,
    cost_time       INTEGER,
    status          VARCHAR(16),
    fail_reason     VARCHAR(4096),
    optimizer_token VARCHAR(50),
    thread_id       INTEGER,
    rewrite_output  BLOB,
    metrics_summary TEXT,
    properties      TEXT,
    CONSTRAINT task_runtime_pk PRIMARY KEY (process_id, task_id)
);

CREATE TABLE table_process_state
(
    process_id                    INTEGER NOT NULL,
    action                        VARCHAR(16) NOT NULL,
    table_id                      INTEGER NOT NULL,
    retry_num                     INTEGER DEFAULT NULL,
    status                        VARCHAR(10) NOT NULL,
    start_time                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time                      TIMESTAMP DEFAULT NULL,
    fail_reason                   VARCHAR(4096) DEFAULT NULL,
    summary                       TEXT,
    CONSTRAINT table_process_state_pk PRIMARY KEY (process_id)
);
CREATE INDEX table_process_state_table_idx ON table_process_state (table_id, start_time);

CREATE TABLE optimizing_task_quota (
    process_id      INTEGER NOT NULL,
    task_id         INTEGER NOT NULL,
    retry_num       INTEGER DEFAULT 0,
    table_id        INTEGER NOT NULL,
    start_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fail_reason     VARCHAR(4096),
    CONSTRAINT optimizing_task_quota_pk PRIMARY KEY (process_id, task_id, retry_num)
);

CREATE TABLE api_tokens (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    apikey      VARCHAR(256) NOT NULL,
    secret      VARCHAR(256) NOT NULL,
    apply_time  TIMESTAMP,
    CONSTRAINT api_tokens_un UNIQUE (apikey)
);

CREATE TABLE platform_file (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name          VARCHAR(100) NOT NULL,
    file_content_b64   TEXT NOT NULL,
    file_path          VARCHAR(100),
    add_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE table_blocker (
  blocker_id INTEGER PRIMARY KEY AUTOINCREMENT,
  catalog_name VARCHAR(64) NOT NULL,
  db_name VARCHAR(128) NOT NULL,
  table_name VARCHAR(256) NOT NULL,
  operations VARCHAR(128) NOT NULL,
  create_time TIMESTAMP DEFAULT NULL,
  expiration_time TIMESTAMP DEFAULT NULL,
  properties TEXT,
  prev_blocker_id INTEGER NOT NULL DEFAULT -1,
  CONSTRAINT prev_uq UNIQUE (catalog_name, db_name, table_name, prev_blocker_id)
);

CREATE TABLE http_session (
    session_id    VARCHAR(120) NOT NULL,
    context_path  VARCHAR(60),
    virtual_host  VARCHAR(60),
    last_node     VARCHAR(60),
    access_time   INTEGER,
    last_access_time  INTEGER,
    create_time   INTEGER,
    cookie_time   INTEGER,
    last_save_time INTEGER,
    expiry_time   INTEGER,
    max_interval  INTEGER,
    data_store    BLOB,
    PRIMARY KEY(session_id, context_path, virtual_host)
);