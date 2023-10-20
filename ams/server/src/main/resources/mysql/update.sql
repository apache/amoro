
ALTER TABLE `table_identifier` ADD `format` VARCHAR(32) COMMENT 'Table Format' ;
UPDATE `table_identifier` A JOIN `catalog_metadata` B
ON A.catalog_name = B.catalog_name
    SET A.format = REPLACE(json_extract(B.catalog_properties, '$."table-formats"'), '"', '');
ALTER TABLE `table_identifer` MODIFY `format` VARCHAR(32) NOT NULL COMMENT 'Table Format';
