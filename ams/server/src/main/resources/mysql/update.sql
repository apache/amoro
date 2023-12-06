-- If you have any changes to the AMS database, please record them in this file.
-- We will confirm the corresponding version of these upgrade scripts when releasing.

ALTER TABLE table_optimizing_process MODIFY optimizing_type VARCHAR(10) DEFAULT NULL COMMENT 'Optimize type: Minor, Major, Full';