update table_metadata set current_tx_id=0 where current_tx_id is null;
ALTER TABLE `table_metadata` modify COLUMN `current_tx_id` bigint(20) NOT NULL DEFAULT 0 COMMENT 'current transaction id';
ALTER TABLE `table_metadata` modify COLUMN `properties` mediumtext COMMENT 'Table properties';
ALTER TABLE `optimize_task` ADD COLUMN `eq_delete_files` int(11) DEFAULT NULL COMMENT 'Eq-Delete file cnt';
ALTER TABLE `optimize_task` ADD COLUMN `eq_delete_file_size` bigint(20) DEFAULT NULL COMMENT 'Eq-Delete file size in bytes';
ALTER TABLE `optimize_history` ADD COLUMN `eq_delete_file_cnt_before` int(11) NOT NULL COMMENT 'Eq-Delete file cnt before optimizing';
ALTER TABLE `optimize_history` ADD COLUMN `eq_delete_file_size_before` bigint(20) NOT NULL COMMENT 'Eq-Delete file size in bytes before optimizing';
ALTER TABLE `optimize_file` CHANGE `file_type` `content_type` varchar(32) NOT NULL COMMENT 'File type: BASE_FILE, INSERT_FILE, EQ_DELETE_FILE, POS_DELETE_FILE, FILE_SCAN_TASK';