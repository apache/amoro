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

-- NEW SCHEMA CHANGE FOR CAS BASE BLOCKER
TRUNCATE TABLE `table_blocker`;
ALTER TABLE `table_blocker` DROP INDEX `table_index`;
ALTER TABLE `table_blocker` ADD COLUMN `prev_blocker_id` bigint(20) NOT NULL DEFAULT -1 COMMENT 'prev blocker id when created';
ALTER TABLE `table_blocker` ADD UNIQUE KEY `uq_prev` (`catalog_name`,`db_name`,`table_name`, `prev_blocker_id`);

-- ADD COLUMN table_summary FOR TABLE_RUNTIME
ALTER TABLE `table_runtime` ADD COLUMN `table_summary` mediumtext AFTER `pending_input`;

RENAME TABLE table_runtime TO table_runtime_backup;
CREATE TABLE table_runtime LIKE table_runtime_backup;

ALTER TABLE table_runtime CHANGE COLUMN optimizing_status optimizing_status_code INT DEFAULT 700;
CREATE INDEX idx_optimizer_status_and_time ON table_runtime(optimizing_status_code, optimizing_status_start_time DESC);

INSERT INTO table_runtime(
    `table_id`,`catalog_name`, `db_name`, `table_name`, `current_snapshot_id`,`current_change_snapshotId`, `last_optimized_snapshotId`,
    `last_optimized_change_snapshotId`, `last_major_optimizing_time`, `last_minor_optimizing_time`, `last_full_optimizing_time`,
    `optimizing_status_code`, `optimizing_status_start_time`, `optimizing_process_id`, `optimizer_group`, `table_config`,
    `optimizing_config`, `pending_input`, `table_summary`)
SELECT  `table_id`,`catalog_name`, `db_name`, `table_name`, `current_snapshot_id`,`current_change_snapshotId`, `last_optimized_snapshotId`,
        `last_optimized_change_snapshotId`, `last_major_optimizing_time`, `last_minor_optimizing_time`, `last_full_optimizing_time`,
        CASE
            WHEN `optimizing_status` = 'IDLE' THEN 700
            WHEN `optimizing_status` = 'PENDING' THEN 600
            WHEN `optimizing_status` = 'PLANNING' THEN 500
            WHEN `optimizing_status` = 'COMMITTING' THEN 400
            WHEN `optimizing_status` = 'MINOR_OPTIMIZING' THEN 300
            WHEN `optimizing_status` = 'MAJOR_OPTIMIZING' THEN 200
            WHEN `optimizing_status` = 'FULL_OPTIMIZING' THEN 100
            END,
        `optimizing_status_start_time`, `optimizing_process_id`, `optimizer_group`, `table_config`, `optimizing_config`, `pending_input`,  `table_summary`
FROM table_runtime_backup;

-- database http session handler
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

-- update resource group memory unit
update resource_group set properties = JSON_SET(properties, '$."flink-conf.jobmanager.memory.process.size"', CONCAT(JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.jobmanager.memory.process.size"')), 'MB')) WHERE JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.jobmanager.memory.process.size"')) REGEXP '^[0-9]+$';
update resource_group set properties = JSON_SET(properties, '$."flink-conf.taskmanager.memory.process.size"', CONCAT(JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.taskmanager.memory.process.size"')), 'MB')) WHERE JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.taskmanager.memory.process.size"')) REGEXP '^[0-9]+$';

-- update resource memory unit
update resource set properties = JSON_SET(properties, '$."flink-conf.jobmanager.memory.process.size"', CONCAT(JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.jobmanager.memory.process.size"')), 'MB')) WHERE JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.jobmanager.memory.process.size"')) REGEXP '^[0-9]+$';
update resource set properties = JSON_SET(properties, '$."flink-conf.taskmanager.memory.process.size"', CONCAT(JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.taskmanager.memory.process.size"')), 'MB')) WHERE JSON_UNQUOTE(JSON_EXTRACT(properties, '$."flink-conf.taskmanager.memory.process.size"')) REGEXP '^[0-9]+$';
