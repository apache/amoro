ALTER TABLE `table_runtime` ADD `format` varchar(32)  NOT NULL COMMENT 'Format';

UPDATE `table_runtime` A
JOIN `catalog_metadata` B
ON A.catalog_name = B.catalog_name
SET A.format = REPLACE(json_extract(B.catalog_properties, '$."table-formats"'), '"', '');
