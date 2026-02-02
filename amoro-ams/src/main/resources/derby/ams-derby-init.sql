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
    catalog_id             INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    catalog_name           VARCHAR(64) NOT NULL,
    catalog_metastore      VARCHAR(64) NOT NULL,
    storage_configs        CLOB(64m),
    auth_configs           CLOB(64m),
    catalog_properties     CLOB(64m),
    database_count         INT NOT NULL DEFAULT 0,
    table_count            INT NOT NULL DEFAULT 0,
    PRIMARY KEY (catalog_id),
    CONSTRAINT catalog_name_index UNIQUE (catalog_name)
);

CREATE TABLE database_metadata (
    catalog_name           VARCHAR(64) NOT NULL,
    db_name                VARCHAR(128) NOT NULL,
    table_count            INT NOT NULL DEFAULT 0,
    PRIMARY KEY (catalog_name, db_name)
);

CREATE TABLE optimizer (
    token                      VARCHAR(300) NOT NULL,
    resource_id                VARCHAR(100) DEFAULT NULL,
    group_name                 VARCHAR(50),
    container_name             VARCHAR(100),
    start_time                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    touch_time                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thread_count               INT,
    total_memory               INT,
    properties                 CLOB(64m),
    PRIMARY KEY (token)
);

CREATE TABLE resource (
    resource_id               VARCHAR(100),
    resource_type             SMALLINT DEFAULT 0,
    container_name            VARCHAR(100),
    group_name                VARCHAR(50),
    thread_count              INT,
    total_memory              INT,
    start_time                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    properties                CLOB(64m),
    CONSTRAINT resource_pk PRIMARY KEY (resource_id)
);

CREATE TABLE resource_group (
    group_name       VARCHAR(50) NOT NULL,
    container_name   VARCHAR(100),
    properties       CLOB,
    PRIMARY KEY (group_name)
);

CREATE TABLE table_identifier (
    table_id        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    catalog_name    VARCHAR(64) NOT NULL,
    db_name         VARCHAR(128) NOT NULL,
    table_name      VARCHAR(256) NOT NULL,
    format          VARCHAR(32)  NOT NULL,
    CONSTRAINT table_identifier_pk PRIMARY KEY (table_id),
    CONSTRAINT table_name_idx UNIQUE (catalog_name, db_name, table_name)
);

CREATE TABLE table_metadata (
    table_id         BIGINT NOT NULL,
    catalog_name     VARCHAR(256) NOT NULL,
    db_name          VARCHAR(256) NOT NULL,
    table_name       VARCHAR(256) NOT NULL,
    primary_key      VARCHAR(256),
    sort_key         VARCHAR(256),
    table_location   VARCHAR(256),
    base_location    VARCHAR(256),
    change_location  VARCHAR(256),
    properties       CLOB(64m),
    meta_store_site  CLOB(64m),
    hdfs_site        CLOB(64m),
    core_site        CLOB(64m),
    auth_method      VARCHAR(32),
    hadoop_username  VARCHAR(64),
    krb_keytab       CLOB(64m),
    krb_conf         CLOB(64m),
    krb_principal    CLOB(64m),
    current_schema_id INT NOT NULL DEFAULT 0,
    meta_version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT table_metadata_pk PRIMARY KEY (table_id)
);


CREATE TABLE table_runtime (
    table_id            BIGINT NOT NULL PRIMARY KEY,
    group_name          VARCHAR(64) NOT NULL,
    status_code         INTEGER NOT NULL DEFAULT 700,
    status_code_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    table_config        CLOB,
    table_summary       CLOB,
    bucket_id          VARCHAR(4)
);

CREATE INDEX idx_status_time_desc
    ON table_runtime (status_code, status_code_update_time DESC);

CREATE TABLE table_runtime_state (
    state_id        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    table_id        BIGINT NOT NULL,
    state_key       VARCHAR(256) NOT NULL,
    state_value     CLOB,
    state_version   BIGINT NOT NULL DEFAULT 0,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (state_id),
    CONSTRAINT uniq_table_state_key UNIQUE (table_id, state_key)
);

CREATE UNIQUE INDEX uniq_table_state_key ON table_runtime_state (table_id, state_key);

CREATE TABLE table_process (
    process_id       BIGINT NOT NULL PRIMARY KEY,
    table_id         BIGINT NOT NULL,
    external_process_identifier         VARCHAR(256) NOT NULL,
    status           VARCHAR(64) NOT NULL,
    process_type     VARCHAR(64) NOT NULL,
    process_stage    VARCHAR(64) NOT NULL,
    execution_engine VARCHAR(64) NOT NULL,
    retry_number     INT NOT NULL,
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finish_time      TIMESTAMP DEFAULT NULL,
    fail_message     CLOB,
    process_parameters          CLOB(64m),
    summary          CLOB(64m)
);
CREATE INDEX table_process_table_idx ON table_process (table_id, create_time);

CREATE TABLE optimizing_process_state (
    process_id                BIGINT NOT NULL PRIMARY KEY,
    table_id                  BIGINT NOT NULL,
    target_snapshot_id        BIGINT NOT NULL,
    target_change_snapshot_id BIGINT NOT NULL,
    rewrite_input             BLOB(64m),
    from_sequence             CLOB(64m),
    to_sequence               CLOB(64m)
);
CREATE INDEX optimizing_process_state_table_idx
    ON optimizing_process_state (table_id);


CREATE TABLE task_runtime (
    process_id      BIGINT NOT NULL,
    task_id         INT NOT NULL,
    retry_num       INT,
    table_id        BIGINT NOT NULL,
    partition_data  VARCHAR(128),
    create_time     TIMESTAMP DEFAULT NULL,
    start_time      TIMESTAMP DEFAULT NULL,
    end_time        TIMESTAMP DEFAULT NULL,
    cost_time       BIGINT,
    status          VARCHAR(16),
    fail_reason     VARCHAR(4096),
    optimizer_token VARCHAR(50),
    thread_id       INT,
    rewrite_output  BLOB,
    metrics_summary CLOB,
    properties      CLOB,
    CONSTRAINT task_runtime_pk PRIMARY KEY (process_id, task_id)
);

CREATE TABLE table_process_state
(
    process_id                    BIGINT NOT NULL,
    action                        VARCHAR(16) NOT NULL,
    table_id                      BIGINT NOT NULL,
    retry_num                     INT DEFAULT NULL,
    status                        VARCHAR(10) NOT NULL,
    start_time                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time                      TIMESTAMP DEFAULT NULL,
    fail_reason                   VARCHAR(4096) DEFAULT NULL,
    summary                       CLOB,
    CONSTRAINT table_process_state_pk PRIMARY KEY (process_id)
);
CREATE INDEX table_process_state_table_idx ON table_process_state (table_id, start_time);

CREATE TABLE optimizing_task_quota (
    process_id      BIGINT NOT NULL,
    task_id         INT NOT NULL,
    retry_num       INT DEFAULT 0,
    table_id        BIGINT NOT NULL,
    start_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fail_reason     VARCHAR(4096),
    CONSTRAINT optimizing_task_quota_pk PRIMARY KEY (process_id, task_id, retry_num)
);

CREATE TABLE api_tokens (
    id          INT GENERATED ALWAYS AS IDENTITY,
    apikey      VARCHAR(256) NOT NULL,
    secret      VARCHAR(256) NOT NULL,
    apply_time  TIMESTAMP,
    CONSTRAINT api_tokens_pk PRIMARY KEY (id),
    CONSTRAINT api_tokens_un UNIQUE (apikey)
);

CREATE TABLE platform_file (
    id                 INT GENERATED ALWAYS AS IDENTITY,
    file_name          VARCHAR(100) NOT NULL,
    file_content_b64   CLOB NOT NULL,
    file_path          VARCHAR(100),
    add_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT platform_file_pk PRIMARY KEY (id)
);

CREATE TABLE table_blocker (
  blocker_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  catalog_name varchar(64) NOT NULL,
  db_name varchar(128) NOT NULL,
  table_name varchar(256) NOT NULL,
  operations varchar(128) NOT NULL,
  create_time timestamp DEFAULT NULL,
  expiration_time timestamp DEFAULT NULL,
  properties clob(64m),
  prev_blocker_id bigint NOT NULL DEFAULT -1,
  PRIMARY KEY (blocker_id),
  CONSTRAINT prev_uq UNIQUE (catalog_name, db_name, table_name, prev_blocker_id)
);

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
    data_store    BLOB,
    PRIMARY KEY(session_id, context_path, virtual_host)
);

CREATE TABLE ha_lease (
  cluster_name       VARCHAR(64)    NOT NULL,
  service_name       VARCHAR(64)    NOT NULL,
  node_id            VARCHAR(256),
  node_ip            VARCHAR(64),
  server_info_json   VARCHAR(32672),
  lease_expire_ts    BIGINT,
  version            INT            NOT NULL DEFAULT 0,
  updated_at         BIGINT         NOT NULL,
  PRIMARY KEY (cluster_name, service_name)
);

CREATE INDEX idx_ha_lease_expire ON ha_lease (lease_expire_ts);
CREATE INDEX idx_ha_lease_node   ON ha_lease (node_id);
CREATE TABLE dynamic_conf (
    id          BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    conf_key    VARCHAR(256) NOT NULL,
    conf_value  VARCHAR(256) NOT NULL,
    conf_group  VARCHAR(256) NOT NULL,
    plugin_name VARCHAR(256),
    PRIMARY KEY (id),
    CONSTRAINT uk_conf UNIQUE (conf_group, plugin_name, conf_key)
);
