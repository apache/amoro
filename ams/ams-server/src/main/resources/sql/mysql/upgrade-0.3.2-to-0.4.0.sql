update table_metadata set current_tx_id=0 where current_tx_id is null;
ALTER TABLE `table_metadata` modify COLUMN `current_tx_id` bigint(20) NOT NULL DEFAULT 0 COMMENT 'current transaction id';
ALTER TABLE `table_metadata` modify COLUMN `properties` mediumtext COMMENT 'Table properties';
TRUNCATE optimize_task;
TRUNCATE optimize_file;
ALTER TABLE file_info_cache ADD COLUMN `add_snapshot_sequence` bigint(20) NOT NULL DEFAULT -1 COMMENT 'the snapshot sequence who add this file'
after `delete_snapshot_id`;
ALTER TABLE snapshot_info_cache ADD COLUMN `snapshot_sequence` bigint(20) NOT NULL DEFAULT -1 COMMENT 'snapshot sequence' after `snapshot_id`;