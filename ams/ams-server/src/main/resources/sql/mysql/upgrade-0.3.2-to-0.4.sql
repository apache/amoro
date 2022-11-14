TRUNCATE optimize_task;
TRUNCATE optimize_file;
ALTER TABLE file_info_cache ADD COLUMN `add_snapshot_sequence` bigint(20) NOT NULL DEFAULT -1 COMMENT 'the snapshot sequence who add this file' 
after `delete_snapshot_id`;
ALTER TABLE snapshot_info_cache ADD COLUMN `snapshot_sequence` bigint(20) NOT NULL DEFAULT -1 COMMENT 'snapshot sequence' after `snapshot_id`;