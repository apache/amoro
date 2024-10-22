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
TRUNCATE TABLE table_blocker;
ALTER TABLE table_blocker DROP INDEX table_index;
ALTER TABLE table_blocker ADD COLUMN prev_blocker_id bigint(20) NOT NULL DEFAULT -1;
COMMENT ON COLUMN table_blocker.prev_blocker_id IS 'prev blocker id when created';
ALTER TABLE table_blocker ADD UNIQUE KEY uq_prev (catalog_name, db_name, table_name, prev_blocker_id);

-- ADD COLUMN table_summary FOR TABLE_RUNTIME
ALTER TABLE table_runtime ADD COLUMN table_summary TEXT;
COMMENT ON COLUMN table_runtime.table_summary IS 'Table summary data';

ALTER TABLE table_runtime RENAME TO table_runtime_backup;
CREATE TABLE table_runtime (LIKE table_runtime_backup INCLUDING ALL)

ALTER TABLE table_runtime ALTER COLUMN optimizing_status optimizing_status_code INT DEFAULT 700;
CREATE INDEX idx_optimizer_status_and_time ON table_runtime(optimizing_status_code, optimizing_status_start_time DESC);

INSERT INTO table_runtime(
    table_id,catalog_name, db_name, table_name, current_snapshot_id,current_change_snapshotId, last_optimized_snapshotId,
    last_optimized_change_snapshotId, last_major_optimizing_time, last_minor_optimizing_time, last_full_optimizing_time,
    optimizing_status_code, optimizing_status_start_time, optimizing_process_id, optimizer_group, table_config,
    optimizing_config, pending_input)
SELECT  table_id,catalog_name, db_name, table_name, current_snapshot_id,current_change_snapshotId, last_optimized_snapshotId,
        last_optimized_change_snapshotId, last_major_optimizing_time, last_minor_optimizing_time, last_full_optimizing_time,
        CASE
            WHEN optimizing_status = 'IDLE' THEN 700
            WHEN optimizing_status = 'PENDING' THEN 600
            WHEN optimizing_status = 'PLANNING' THEN 500
            WHEN optimizing_status = 'COMMITTING' THEN 400
            WHEN optimizing_status = 'MINOR_OPTIMIZING' THEN 300
            WHEN optimizing_status = 'MAJOR_OPTIMIZING' THEN 200
            WHEN optimizing_status = 'FULL_OPTIMIZING' THEN 100
            END,
        optimizing_status_start_time, optimizing_process_id, optimizer_group, table_config, optimizing_config, pending_input
FROM table_runtime_backup;