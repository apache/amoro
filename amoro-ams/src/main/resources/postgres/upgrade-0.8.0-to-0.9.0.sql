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

-- If you have any changes to the AMS database, please record them in this file.
-- We will confirm the corresponding version of these upgrade scripts when releasing.

-- STORAGE CONFIG SHARED ACROSS CATALOGS
CREATE TABLE storage_config_metadata (
    config_id serial PRIMARY KEY,
    config_name varchar(64) NOT NULL,
    storage_type varchar(64) NOT NULL,
    storage_configs text
);
CREATE UNIQUE INDEX config_name_index ON storage_config_metadata (config_name);

COMMENT ON TABLE storage_config_metadata IS 'Storage config metadata';
COMMENT ON COLUMN storage_config_metadata.config_id IS 'Config id';
COMMENT ON COLUMN storage_config_metadata.config_name IS 'Config name';
COMMENT ON COLUMN storage_config_metadata.storage_type IS 'Storage type';
COMMENT ON COLUMN storage_config_metadata.storage_configs IS 'Base64 code of storage configs';