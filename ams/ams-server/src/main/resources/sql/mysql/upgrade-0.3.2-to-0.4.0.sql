ALTER TABLE `optimize_task` ADD COLUMN `eq_delete_files` int(11) DEFAULT NULL COMMENT 'Eq-Delete file cnt';
ALTER TABLE `optimize_task` ADD COLUMN `eq_delete_file_size` bigint(20) DEFAULT NULL COMMENT 'Eq-Delete file size in bytes';
ALTER TABLE `optimize_history` ADD COLUMN `eq_delete_file_cnt_before` int(11) NOT NULL COMMENT 'Eq-Delete file cnt before optimizing';
ALTER TABLE `optimize_history` ADD COLUMN `eq_delete_file_size_before` bigint(20) NOT NULL COMMENT 'Eq-Delete file size in bytes before optimizing';
ALTER TABLE `optimize_file` MODIFY COLUMN `file_type` varchar(32) NOT NULL COMMENT 'File type: BASE_FILE, INSERT_FILE, EQ_DELETE_FILE, POS_DELETE_FILE, NATIVE_EQ_DELETE_FILE';