/*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

export default [
  {
    url: '/mock/ams/v1/terminal/examples',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": [
        "CreateTable",
        "DeleteTable",
        "EditTable",
        "SetProperties",
        "UnsetProperties",
        "ShowDatabases",
        "ShowTables",
        "Describe"
      ]
    }),
  },
  {
    url: '/mock/ams/v1/terminal/latestInfos',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": { "sessionId": "", "sql": "" } }),
  },
  {
    url: '/mock/ams/v1/terminal/catalogs/:catalogName/execute',
    method: 'post',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "sessionId": "node01d6glc3ei3fi31f35o91w2pnnu27752.node0-SIMPLE-root-test_catalog"
      }
    }),
  },
  {
    url: '/mock/ams/v1/terminal/:sessionId/logs',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "logStatus": "Running",
        "logs": [
          "2024/01/11 16:42:41 new sql script submit, current thread pool state. [Active: 1, PoolSize: 1]",
          "2024/01/11 16:42:41 terminal session dose not exists. create session first",
          "2024/01/11 16:42:41 create a new terminal session.",
          "2024/01/11 16:42:41 fetch terminal session: node01d6glc3ei3fi31f35o91w2pnnu27752.node0-SIMPLE-root-test_catalog",
          "setup session, session factory: com.netease.arctic.server.terminal.local.LocalSessionFactory",
          "spark.sql.catalog.test_catalog.table.self-optimizing.group  local",
          "spark.sql.catalog.test_catalog.table.f  f",
          "spark.sql.catalog.test_catalog.table-formats  ICEBERG",
          "spark.sql.catalog.test_catalog.warehouse  /mnt/dfs/4/warehouse_public",
          "spark.sql.catalog.test_catalog.type  hadoop",
          "spark.sql.catalog.test_catalog  org.apache.iceberg.spark.SparkCatalog",
          "spark.sql.arctic.refresh-catalog-before-usage  true",
          "2024/01/11 16:42:41 session configuration: catalog-url-base => thrift://127.0.0.1:1261",
          "2024/01/11 16:42:41 session configuration: spark.sql.catalog.test_catalog.type => hadoop",
          "2024/01/11 16:42:41 session configuration: spark.sql.arctic.refresh-catalog-before-usage => true",
          "2024/01/11 16:42:41 session configuration: catalog.test_catalog.warehouse => /mnt/dfs/4/warehouse_public",
          "2024/01/11 16:42:41 session configuration: catalog.test_catalog.table.self-optimizing.group => local",
          "2024/01/11 16:42:41 session configuration: catalog.test_catalog.table-formats => ICEBERG",
          "2024/01/11 16:42:41 session configuration: session.catalogs => test_catalog",
          "2024/01/11 16:42:41 session configuration: spark.sql.catalog.test_catalog.table.f => f",
          "2024/01/11 16:42:41 session configuration: spark.sql.catalog.test_catalog.warehouse => /mnt/dfs/4/warehouse_public",
          "2024/01/11 16:42:41 session configuration: catalog.test_catalog.table.f => f",
          "2024/01/11 16:42:41 session configuration: spark.sql.catalog.test_catalog.table.self-optimizing.group => local",
          "2024/01/11 16:42:41 session configuration: session.fetch-size => 1000",
          "2024/01/11 16:42:41 session configuration: session.catalog.test_catalog.connector => iceberg",
          "2024/01/11 16:42:41 session configuration: catalog.test_catalog.type => hadoop",
          "2024/01/11 16:42:41 session configuration: spark.sql.catalog.test_catalog.table-formats => ICEBERG",
          "2024/01/11 16:42:41 session configuration: spark.sql.catalog.test_catalog => org.apache.iceberg.spark.SparkCatalog",
          "2024/01/11 16:42:41  ",
          "2024/01/11 16:42:41 prepare execute statement, line:3",
          "2024/01/11 16:42:41 create table db_name.table_name ( id int, name string, ts timestamp, primary key (id) ) using arctic partitioned by (days(ts)) tblproperties ('table.props' = 'val')"
        ]
      }
    }),
  },
  {
    url: '/mock/ams/v1/terminal/:sessionId/result',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": [] }),
  },
  {
    url: '/mock/ams/v1/terminal/:sessionId/result',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": [] }),
  },
  {
    url: '/mock/ams/v1/terminal/:sessionId/stop',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": null }),
  },

  {
    url: '/mock/ams/v1/terminal/examples/CreateTable',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "create table db_name.table_name (\n    id int,\n    name string, \n    ts timestamp,\n    primary key (id)\n) using arctic \npartitioned by (days(ts)) \ntblproperties ('table.props' = 'val');"
    }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/DeleteTable',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": "drop table db_name.table_name;" }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/EditTable',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "alter table db_name.table_name add column data int ;\nalter table db_name.table_name alter column data bigint ;\nalter table db_name.table_name drop column data;"
    }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/SetProperties',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "alter table db_name.table_name set tblproperties (\n    'comment' = 'A table comment.');"
    }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/UnsetProperties',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "alter table db_name.table_name unset tblproperties (\n    'comment');"
    }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/ShowDatabases',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": "show databases;" }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/ShowTables',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": "show tables;" }),
  },
  {
    url: '/mock/ams/v1/terminal/examples/Describe',
    method: 'get',
    response: () => ({"message":"success","code":200,"result":"desc db_name.table_name;"}),
  },
]
