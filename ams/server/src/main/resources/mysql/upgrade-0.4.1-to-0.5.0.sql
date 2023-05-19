-- container_metadata
ALTER TABLE `container_metadata` CHANGE `name` `container_name` varchar(64);
ALTER TABLE `container_metadata` CHANGE `type` `container_type` varchar(64);

-- catalog_metadata
ALTER TABLE `catalog_metadata` ADD `database_count` INT NOT NULL DEFAULT 0;
ALTER TABLE `catalog_metadata` ADD `table_count` INT NOT NULL DEFAULT 0;
ALTER TABLE `catalog_metadata` CHANGE `catalog_type` `catalog_metastore` VARCHAR(64) NOT NULL;
ALTER TABLE `catalog_metadata` DROP COLUMN `display_name`;

-- database_metadata
ALTER TABLE `database_metadata` ADD `table_count` INT NOT NULL DEFAULT 0;
ALTER TABLE `database_metadata` DROP COLUMN `db_id`;
ALTER TABLE `database_metadata` ADD PRIMARY KEY (`catalog_name`, `db_name`);

DROP TABLE `ddl_record`;
DROP TABLE `file_info_cache`;

-- optimize_group
RENAME TABLE `optimize_group` to `resource_group`;
ALTER TABLE `resource_group` CHANGE `name` `group_name` VARCHAR(50) NOT NULL;
ALTER TABLE `resource_group` CHANGE `container` `container_name` VARCHAR(100) DEFAULT NULL;
ALTER TABLE `resource_group` DROP COLUMN `group_id`;
ALTER TABLE `resource_group` DROP COLUMN `scheduling_policy`;
ALTER TABLE `resource_group` MODIFY COLUMN `properties` AFTER `container_name`;

-- table_identifier
INSERT INTO `table_identifier` (`catalog_name`, `db_name`, `table_name`) SELECT `catalog_name`, `db_name`, `table_name` FROM `table_metadata`;

-- table_metadata
ALTER TABLE `table_metadata` ADD `table_id` bigint(20) NOT NULL COMMENT 'table id' FIRST;
ALTER TABLE `table_metadata` drop PRIMARY KEY;
ALTER TABLE `table_metadata` ADD PRIMARY KEY (`table_id`);
ALTER TABLE `table_metadata` CHANGE `delta_location` `change_location` varchar(256) DEFAULT NULL;
ALTER TABLE `table_metadata` CHANGE `cur_schema_id` `current_schema_id` int(11) NOT NULL DEFAULT 0;
ALTER TABLE `table_metadata` DROP COLUMN `hbase_site`;
ALTER TABLE `table_metadata` DROP COLUMN `current_tx_id`;
UPDATE `table_metadata` JOIN `table_identifier`
ON `table_metadata`.`catalog_name` = `table_identifier`.`catalog_name`
AND `table_metadata`.`db_name` = `table_identifier`.`db_name`
AND `table_metadata`.`table_name` = `table_identifier`.`table_name` SET `table_metadata`.`table_id` = `table_identifier`.`table_id`;

-- platform_file
RENAME TABLE `platform_file_info` to `platform_file`;


