CREATE TABLE `platform_file_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'file id',
  `file_name` varchar(100) NOT NULL COMMENT 'file name',
  `file_content_b64` text NOT NULL COMMENT 'file content encoded with base64',
  `file_path` varchar(100) DEFAULT NULL COMMENT 'may be hdfs path , not be used now',
  `add_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'add timestamp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='store files info saved in the platform';