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
ALTER TABLE `table_runtime` MODIFY COLUMN `optimizing_status_start_time` TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Table optimize status start time';

-- Update processId to SnowflakeId
UPDATE `table_optimizing_process` SET `process_id` = `process_id` /10 << 13;
UPDATE `task_runtime` SET `process_id` = `process_id` /10 << 13;
UPDATE `optimizing_task_quota` SET `process_id` = `process_id` /10 << 13;
UPDATE `table_runtime` SET `optimizing_process_id` = `optimizing_process_id` /10 << 13;

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

CREATE TABLE `table_process`
(
    `process_id`                    bigint(20) NOT NULL COMMENT 'table process id',
    `table_id`                      bigint(20) NOT NULL COMMENT 'table id',
    `status`                        varchar(64) NOT NULL COMMENT 'Table optimizing status',
    `process_type`                  varchar(64) NOT NULL COMMENT 'Process action type',
    `process_stage`                 varchar(64) NOT NULL COMMENT 'Process current stage',
    `execution_engine`              varchar(64) NOT NULL COMMENT 'Execution engine',
    `create_time`                   timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'First plan time',
    `finish_time`                   timestamp NULL DEFAULT NULL COMMENT 'finish time or failed time',
    `fail_message`                  mediumtext DEFAULT NULL COMMENT 'Error message after task failed',
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

INSERT INTO `table_process`
(`process_id`, `table_id`, `status`, `process_type`,
`process_stage`, `execution_engine`, `create_time`, `finish_time`, `fail_message`, `summary`)
SELECT p.`process_id`, p.`table_id`, p.`status`, p.`optimizing_type`,
CASE
            WHEN t.`optimizing_status_code` = 700 THEN 'IDLE'
            WHEN t.`optimizing_status_code` = 600 THEN 'PENDING'
            WHEN t.`optimizing_status_code` = 500 THEN 'PLANNING'
            WHEN t.`optimizing_status_code` = 400 THEN 'COMMITTING'
            WHEN t.`optimizing_status_code` = 300 THEN 'MINOR_OPTIMIZING'
            WHEN t.`optimizing_status_code` = 200 THEN 'MAJOR_OPTIMIZING'
            WHEN t.`optimizing_status_code` = 100 THEN 'FULL_OPTIMIZING'
END,
 'AMORO', p.`plan_time`, p.`end_time`, p.`fail_reason`, p.`summary`
FROM `table_optimizing_process` p JOIN `table_runtime` t ON p.table_id = t.table_id;

INSERT INTO `optimizing_process_state`
(`process_id`, `table_id`, `target_snapshot_id`, `target_change_snapshot_id`, `rewrite_input`, `from_sequence`, `to_sequence`)
SELECT `process_id`, `table_id`, `target_snapshot_id`, `target_change_snapshot_id`, `rewrite_input`, `from_sequence`, `to_sequence`
FROM `table_optimizing_process`;

DROP TABLE IF EXISTS `table_optimizing_process`;
