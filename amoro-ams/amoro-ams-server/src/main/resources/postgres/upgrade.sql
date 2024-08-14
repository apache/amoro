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
ALTER TABLE `table_blocker` ADD COLUMN `prev_blocker_id` bigint(20) NOT NULL DEFAULT -1;
COMMENT ON COLUMN table_blocker.prev_blocker_id IS 'prev blocker id when created';
ALTER TABLE `table_blocker` ADD UNIQUE KEY `uq_prev` (`catalog_name`,`db_name`,`table_name`, `prev_blocker_id`);
