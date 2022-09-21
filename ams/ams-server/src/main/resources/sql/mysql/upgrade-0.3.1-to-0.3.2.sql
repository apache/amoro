DROP TABLE IF EXISTS `optimize_task_history`;
CREATE TABLE `optimize_task_history`
(
    `task_attempt_id`   varchar(40) NOT NULL COMMENT 'Task attempt id',
    `task_history_id` varchar(40) NOT NULL COMMENT 'Task history id',
    `task_group_id`   varchar(40) NOT NULL COMMENT 'Task group id',
    `task_trace_id`   varchar(40) NOT NULL COMMENT 'Task trace id',
    `catalog_name`    varchar(64) NOT NULL COMMENT 'Catalog name',
    `db_name`         varchar(64) NOT NULL COMMENT 'Database name',
    `table_name`      varchar(64) NOT NULL COMMENT 'Table name',
    `start_time`      datetime(3) DEFAULT NULL COMMENT 'Task start time',
    `end_time`        datetime(3) DEFAULT NULL COMMENT 'Task end time',
    `cost_time`       bigint(20) DEFAULT NULL COMMENT 'Task cost time',
    `queue_id`        int(11) DEFAULT NULL COMMENT 'Task queue id',
    PRIMARY KEY (`task_attempt_id`),
    KEY `table_task_history_id_end_time_index` (`catalog_name`, `db_name`, `table_name`, `task_history_id`, `end_time`),
    KEY `queue_id_time_index` (`queue_id`, `start_time`, `end_time`),
    KEY `table_time_index` (`catalog_name`, `db_name`, `table_name`, `start_time`, `end_time`),
    KEY `table_id_index` (`catalog_name`, `db_name`, `table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'History of each optimize task';

ALTER TABLE `optimize_task` drop COLUMN `is_delete_pos_delete`;