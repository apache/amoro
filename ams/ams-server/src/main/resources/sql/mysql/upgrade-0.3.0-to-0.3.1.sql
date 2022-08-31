CREATE TABLE `api_tokens`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `apikey`     varchar(256) NOT NULL COMMENT 'openapi client public key',
    `secret`     varchar(256) NOT NULL COMMENT 'The key used by the client to generate the request signature',
    `apply_time` datetime DEFAULT NULL COMMENT 'apply time',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `account_unique` (`apikey`) USING BTREE COMMENT 'account unique'
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='Openapi  secret';

CREATE TABLE `ddl_record`
(
    `table_identifier` varchar(384) NOT NULL,
    `ddl`              mediumtext,
    `ddl_type`         varchar(256) NOT NULL,
    `commit_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `snapshot_info_cache` ADD COLUMN `producer` varchar(64) NOT NULL DEFAULT 'INGESTION';
ALTER TABLE `snapshot_info_cache` ADD COLUMN `file_size` bigint(20) NOT NULL DEFAULT 0;
ALTER TABLE `snapshot_info_cache` ADD COLUMN `file_count` int(11) NOT NULL DEFAULT 0;
ALTER TABLE `snapshot_info_cache` modify COLUMN `table_identifier` varchar(384) NOT NULL;
ALTER TABLE `file_info_cache` ADD COLUMN `producer` varchar(64) NOT NULL DEFAULT 'INGESTION';
ALTER TABLE `file_info_cache` modify COLUMN `table_identifier` varchar(384) NOT NULL;
ALTER TABLE `table_metadata` ADD COLUMN `cur_schema_id` int(11) DEFAULT NULL;
ALTER TABLE `table_transaction_meta` modify COLUMN `table_identifier` varchar(384) NOT NULL;