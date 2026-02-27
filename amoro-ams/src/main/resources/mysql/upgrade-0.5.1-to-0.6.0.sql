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

ALTER TABLE `table_identifier` ADD `format` VARCHAR(32) COMMENT 'Table Format' ;

UPDATE `table_identifier` A JOIN `catalog_metadata` B
ON A.catalog_name = B.catalog_name
    SET A.format = REPLACE(json_extract(B.catalog_properties, '$."table-formats"'), '"', '');

ALTER TABLE `table_identifier` MODIFY `format` VARCHAR(32) NOT NULL COMMENT 'Table Format';

UPDATE `table_identifier` i JOIN `table_metadata` m
ON i.table_id = m.table_id
SET i.format = m.format;

ALTER TABLE `table_metadata` DROP COLUMN `format`;