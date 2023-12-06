-- If you have any changes to the AMS database, please record them in this file.
-- We will confirm the corresponding version of these upgrade scripts when releasing.

ALTER TABLE table_optimizing_process ALTER COLUMN optimizing_type DROP NOT NULL;
COMMENT ON COLUMN table_optimizing_process.optimizing_type IS 'Optimize type: Minor, Major, Full';