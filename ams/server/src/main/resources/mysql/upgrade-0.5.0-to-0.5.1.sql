DELETE FROM `table_optimizing_process` WHERE `status` ='RUNNING';
UPDATE `table_runtime` SET `optimizing_status`  = 'IDLE';
