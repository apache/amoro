ALTER TABLE `table_metadata`
    ADD COLUMN `table_schema` mediumtext COMMENT 'Table schema json' AFTER `krb_principal`,
    ADD COLUMN `bucket_mode` varchar(64) DEFAULT NULL COMMENT 'Paimon bucket mode' AFTER `table_schema`,
    ADD COLUMN `num_buckets` int(11) DEFAULT NULL COMMENT 'Paimon bucket count' AFTER `bucket_mode`,
    ADD COLUMN `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `num_buckets`,
    ADD COLUMN `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `snapshot_id` varchar(64) DEFAULT NULL COMMENT 'Latest snapshot id' AFTER `current_schema_id`,
    ADD COLUMN `table_comment` varchar(512) DEFAULT NULL COMMENT 'Table comment' AFTER `snapshot_id`;
