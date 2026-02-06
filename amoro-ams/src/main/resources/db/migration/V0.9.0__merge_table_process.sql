-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- ============================================================================
-- Amoro Schema Upgrade: Merge table_process and table_process_state
-- JIRA: AMORO-3951
--
-- This script migrates data from table_process_state to table_process and
-- deprecates the table_process_state table.
-- ============================================================================

-- Step 1: Migrate data from table_process_state to table_process
-- This step copies all records from table_process_state that don't already
-- exist in table_process (based on process_id)
INSERT INTO table_process (
    process_id,
    table_id,
    external_process_identifier,
    status,
    process_type,
    process_stage,
    execution_engine,
    retry_number,
    create_time,
    finish_time,
    fail_message,
    process_parameters,
    summary
)
SELECT
    tps.process_id,
    tps.table_id,
    '' as external_process_identifier,
    tps.status,
    tps.action as process_type,
    tps.status as process_stage,
    'AMORO' as execution_engine,
    COALESCE(tps.retry_num, 0) as retry_number,
    tps.start_time as create_time,
    tps.end_time as finish_time,
    tps.fail_reason as fail_message,
    '' as process_parameters,
    tps.summary
FROM table_process_state tps
WHERE NOT EXISTS (
    SELECT 1 FROM table_process tp
    WHERE tp.process_id = tps.process_id
);

-- Step 2: Rename old table as backup
-- This preserves the old table for rollback purposes if needed
ALTER TABLE table_process_state RENAME TO table_process_state_backup;
