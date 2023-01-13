ALTER TABLE `optimize_group` ADD COLUMN `scheduling_policy`   varchar(20) COMMENT 'Optimize group scheduling policy' after `name`;
