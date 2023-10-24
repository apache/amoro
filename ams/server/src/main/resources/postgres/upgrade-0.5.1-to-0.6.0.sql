-- ADD format to table_identifier
ALTER TABLE table_identifier ADD format VARCHAR(32)  NOT NULL;
COMMENT ON COLUMN table_identifier.format IS 'Format';

UPDATE table_identifier A
SET format = (catalog_properties::json) ->> 'table-formats'
FROM catalog_metadata B
WHERE A.catalog_name = B.catalog_name;

UPDATE table_identifier I
SET format = B.format
FROM table_metadata B
WHERE I.table_id = B.table_id;

ALTER  TABLE table_metadata DROP format;