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

-- ADD format to table_identifier
ALTER TABLE table_identifier ADD format VARCHAR(32)  NOT NULL;
COMMENT ON COLUMN table_identifier.format IS 'Format';

UPDATE table_identifier A
SET format = (catalog_properties::json) ->> 'table-formats'
FROM catalog_metadata B
WHERE A.catalog_name = B.catalog_name;

UPDATE table_identifier I
SET format = B.format
FROM table_metadata B
WHERE I.table_id = B.table_id;

ALTER  TABLE table_metadata DROP format;