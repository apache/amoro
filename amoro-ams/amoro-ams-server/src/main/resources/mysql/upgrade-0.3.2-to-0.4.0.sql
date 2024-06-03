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

CREATE TABLE `platform_file_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'file id',
  `file_name` varchar(100) NOT NULL COMMENT 'file name',
  `file_content_b64` mediumtext NOT NULL COMMENT 'file content encoded with base64',
  `file_path` varchar(100) DEFAULT NULL COMMENT 'may be hdfs path , not be used now',
  `add_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'add timestamp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='store files info saved in the platform';
update table_metadata set current_tx_id=0 where current_tx_id is null;
ALTER TABLE `table_metadata` modify COLUMN `current_tx_id` bigint(20) NOT NULL DEFAULT 0 COMMENT 'current transaction id';
ALTER TABLE `table_metadata` modify COLUMN `properties` mediumtext COMMENT 'Table properties';
TRUNCATE optimize_task;
TRUNCATE optimize_file;
ALTER TABLE file_info_cache ADD COLUMN `add_snapshot_sequence` bigint(20) NOT NULL DEFAULT -1 COMMENT 'the snapshot sequence who add this file'
after `delete_snapshot_id`;
ALTER TABLE snapshot_info_cache ADD COLUMN `snapshot_sequence` bigint(20) NOT NULL DEFAULT -1 COMMENT 'snapshot sequence' after `snapshot_id`;
ALTER TABLE file_info_cache DROP COLUMN `watermark`;
ALTER TABLE `optimize_file` CHANGE `file_type` `content_type` varchar(32) NOT NULL COMMENT 'File type: BASE_FILE, INSERT_FILE, EQ_DELETE_FILE, POS_DELETE_FILE';
ALTER TABLE `optimize_file` MODIFY COLUMN file_content MEDIUMBLOB NULL COMMENT 'File bytes after serialization';

ALTER TABLE `database_metadata` MODIFY COLUMN `db_name` varchar(128) NOT NULL COMMENT 'database name';
ALTER TABLE `optimize_history` MODIFY COLUMN `db_name` varchar(128) NOT NULL COMMENT 'Database name';
ALTER TABLE `optimize_history` MODIFY COLUMN `table_name` varchar(128) NOT NULL COMMENT 'Table name';
ALTER TABLE `optimize_task` MODIFY COLUMN `db_name` varchar(128) NOT NULL COMMENT 'Database name';
ALTER TABLE `optimize_task` MODIFY COLUMN `table_name` varchar(128) NOT NULL COMMENT 'Table name';
ALTER TABLE `table_metadata` MODIFY COLUMN `db_name` varchar(128) NOT NULL COMMENT 'Database name';
ALTER TABLE `table_metadata` MODIFY COLUMN `table_name` varchar(128) NOT NULL COMMENT 'Table name';
ALTER TABLE `optimize_table_runtime` MODIFY COLUMN `db_name` varchar(128) NOT NULL COMMENT 'Database name';
ALTER TABLE `optimize_table_runtime` MODIFY COLUMN `table_name` varchar(128) NOT NULL COMMENT 'Table name';
ALTER TABLE `optimize_task_history` MODIFY COLUMN `db_name` varchar(128) NOT NULL COMMENT 'Database name';
ALTER TABLE `optimize_task_history` MODIFY COLUMN `table_name` varchar(128) NOT NULL COMMENT 'Table name';
ALTER TABLE `optimize_task` ADD COLUMN `min_change_transaction_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT 'Min change transaction id' after `max_change_transaction_id`;
