
ALTER TABLE `table_identifier` ADD `format` VARCHAR(32) COMMENT 'Table Format' ;

UPDATE `table_identifier` A JOIN `catalog_metadata` B
ON A.catalog_name = B.catalog_name
    SET A.format = REPLACE(json_extract(B.catalog_properties, '$."table-formats"'), '"', '');

ALTER TABLE `table_identifier` MODIFY `format` VARCHAR(32) NOT NULL COMMENT 'Table Format';

UPDATE `table_identifier` i JOIN `table_metadata` m
ON i.table_id = m.table_id
SET i.format = m.format;

ALTER TABLE `table_metadata` DROP COLUMN `format`;