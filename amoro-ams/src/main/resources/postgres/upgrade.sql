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

-- If you have any changes to the AMS database, please record them in this file.
-- We will confirm the corresponding version of these upgrade scripts when releasing.

-- Update the precision from s level to ms.
ALTER TABLE table_runtime
    ALTER COLUMN optimizing_status_start_time TYPE TIMESTAMP(3),
    ALTER COLUMN optimizing_status_start_time SET DEFAULT CURRENT_TIMESTAMP(3);

-- Update processId to SnowflakeId
UPDATE table_optimizing_process SET process_id = process_id /10 << 13;
UPDATE task_runtime SET process_id = process_id /10 << 13;
UPDATE optimizing_task_quota SET process_id = process_id /10 << 13;
UPDATE table_runtime SET optimizing_process_id = optimizing_process_id /10 << 13;

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

CREATE TABLE table_process (
    process_id      bigserial PRIMARY KEY,
    table_id        bigint NOT NULL,
    status          varchar(64) NOT NULL,
    process_type    varchar(64) NOT NULL,
    process_stage   varchar(64) NOT NULL,
    execution_engine varchar(64) NOT NULL,
    create_time     timestamptz NOT NULL DEFAULT now(),
    finish_time     timestamptz,
    fail_message    text CHECK (length(fail_message) <= 4096),
    summary         text,
    CONSTRAINT table_process_unique UNIQUE (process_id)
);

CREATE INDEX table_process_table_idx ON table_process (table_id, create_time);

COMMENT ON TABLE  table_process IS 'History of optimizing after each commit';
COMMENT ON COLUMN table_process.process_id      IS 'table process id';
COMMENT ON COLUMN table_process.table_id        IS 'table id';
COMMENT ON COLUMN table_process.status          IS 'Table optimizing status';
COMMENT ON COLUMN table_process.process_type    IS 'Process action type';
COMMENT ON COLUMN table_process.process_stage   IS 'Process current stage';
COMMENT ON COLUMN table_process.execution_engine IS 'Execution engine';
COMMENT ON COLUMN table_process.create_time     IS 'First plan time';
COMMENT ON COLUMN table_process.finish_time     IS 'finish time or failed time';
COMMENT ON COLUMN table_process.fail_message    IS 'Error message after task failed';
COMMENT ON COLUMN table_process.summary         IS 'Max change transaction id of these tasks';

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

INSERT INTO table_process
(process_id, table_id, status, process_type,
 process_stage, execution_engine, create_time, finish_time, fail_message, summary)
SELECT
    p.process_id,
    p.table_id,
    p.status,
    p.optimizing_type,
    CASE t.optimizing_status_code
        WHEN 700 THEN 'IDLE'
        WHEN 600 THEN 'PENDING'
        WHEN 500 THEN 'PLANNING'
        WHEN 400 THEN 'COMMITTING'
        WHEN 300 THEN 'MINOR_OPTIMIZING'
        WHEN 200 THEN 'MAJOR_OPTIMIZING'
        WHEN 100 THEN 'FULL_OPTIMIZING'
    END,
    'AMORO',
    p.plan_time,
    p.end_time,
    p.fail_reason,
    p.summary
FROM table_optimizing_process AS p
JOIN table_runtime AS t
  ON p.table_id = t.table_id;

INSERT INTO optimizing_process_state
(process_id, table_id,
 target_snapshot_id, target_change_snapshot_id,
 rewrite_input, from_sequence, to_sequence)
SELECT
    process_id,
    table_id,
    target_snapshot_id,
    target_change_snapshot_id,
    rewrite_input,
    from_sequence,
    to_sequence
FROM table_optimizing_process;

DROP TABLE IF EXISTS table_optimizing_process;

ALTER TABLE table_runtime RENAME TO table_runtime_old;

create table if not exists table_runtime (
    table_id            bigint primary key,
    group_name          varchar(64) not null,
    status_code         int not null default 700,
    status_code_update_time timestamptz not null default now(),
    table_config        text,
    table_summary       text
);

create index if not exists idx_status_and_time
    on table_runtime (status_code, status_code_update_time desc);

comment on table  table_runtime is 'Table running information of each table';
comment on column table_runtime.status_code is 'Table runtime status code.';
comment on column table_runtime.status_code_update_time is 'Table runtime status code update time';
comment on column table_runtime.table_config is 'table configuration cached from table.properties';
comment on column table_runtime.table_summary is 'table summary for ams';


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

INSERT INTO table_runtime
(table_id,group_name,status_code,status_code_update_time,table_config,table_summary)
SELECT
table_id,optimizer_group,optimizing_status_code,optimizing_status_start_time,table_config,table_summary
FROM table_runtime_old;

INSERT INTO table_runtime_state (table_id,state_key,state_value)
SELECT table_id,'pending_input',pending_input FROM table_runtime_old;

INSERT INTO table_runtime_state (table_id,state_key,state_value)
SELECT table_id,'process_id',optimizing_process_id FROM table_runtime_old;

INSERT INTO table_runtime_state (table_id,state_key,state_value)
SELECT table_id,'optimizing_state',
       jsonb_build_object(
        'currentSnapshotId',current_snapshot_id,
        'currentChangeSnapshotId',current_change_snapshotId,
        'lastOptimizedSnapshotId',last_optimized_snapshotId,
        'lastOptimizedChangeSnapshotId',last_optimized_change_snapshotId,
        'lastMajorOptimizingTime',last_major_optimizing_time,
        'lastFullOptimizingTime',last_full_optimizing_time,
        'lastMinorOptimizingTime',last_minor_optimizing_time)::text
FROM table_runtime_old;

DROP TABLE IF EXISTS table_runtime_old;

-- ADD bucket_id to table_runtime
ALTER TABLE table_runtime ADD COLUMN bucket_id varchar(4);

-- Assign bucket_id to existing tables using round-robin strategy
-- Bucket IDs range from 1 to 100 (default bucket-id.total-count)
-- This is mainly for upgrade scenarios where existing tables may not have bucketId assigned
UPDATE table_runtime
SET bucket_id = CAST((ROW_NUMBER() OVER (ORDER BY table_id) - 1) % 100 + 1 AS VARCHAR)
WHERE bucket_id IS NULL;