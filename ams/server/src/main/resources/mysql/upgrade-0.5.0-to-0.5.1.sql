UPDATE `table_optimizing_process` SET `status`  = 'CLOSED' WHERE `status` ='RUNNING';
UPDATE `table_runtime` SET `optimizing_status`  = 'IDLE';

ALTER TABLE `table_runtime` ADD `format` varchar(32)  NOT NULL COMMENT "format";
