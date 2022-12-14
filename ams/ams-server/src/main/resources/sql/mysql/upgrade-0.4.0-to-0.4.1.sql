CREATE TABLE `table_metric_statistics`
(
    `table_identifier` varchar(384) NOT NULL COMMENT 'table full name with catalog.db.table',
    `inner_table`      varchar(64)  NOT NULL COMMENT 'table type like change/base',
    `metric_name`      varchar(256) NOT NULL COMMENT 'metric name',
    `metric_value`     DECIMAL(15,2) COMMENT 'metric value',
    `commit_time`      timestamp    NOT NULL ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'metric commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'metric statistics of table';

CREATE TABLE `metric_statistics_summary`
(
    `metric_name`      varchar(256) NOT NULL COMMENT 'metric name',
    `metric_value`     DECIMAL(15,2) COMMENT 'metric value',
    `commit_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'metric commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'metric statistics summary';

CREATE TABLE `optimizer_metric_statistics`
(
    `optimizer_id`     bigint NOT NULL COMMENT 'optimizer id',
    `subtask_id`       varchar(256)  NOT NULL COMMENT 'optimizer subtask id',
    `metric_name`      varchar(256) NOT NULL COMMENT 'metric name',
    `metric_value`     DECIMAL(15,2) COMMENT 'metric value',
    `commit_time`      timestamp    NOT NULL ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'metric commit time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'metric statistics of optimizer';