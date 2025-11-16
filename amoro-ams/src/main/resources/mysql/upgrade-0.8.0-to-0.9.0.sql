-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- storage config shared across catalogs
CREATE TABLE `storage_config_metadata`
(
    `config_id`  int(11) NOT NULL AUTO_INCREMENT,
    `config_name` varchar(64) NOT NULL COMMENT 'config name',
    `storage_type` varchar(64) NOT NULL  COMMENT 'storage type',
    `storage_configs`        mediumtext COMMENT 'base64 code of storage configs',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `config_name_index` (`config_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'storage config metadata';
