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
    url: '/mock/ams/v1/catalogs',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": [
        {
          "catalogName": "test_catalog",
          "catalogType": "hadoop",
          "storageConfigs": {
            "storage.type": "Hadoop",
            "hive.site": "PGNvbmZpZ3VyYXRpb24+PC9jb25maWd1cmF0aW9uPg==",
            "hadoop.hdfs.site": "PGNvbmZpZ3VyYXRpb24+PC9jb25maWd1cmF0aW9uPg==",
            "hadoop.core.site": "PGNvbmZpZ3VyYXRpb24+PC9jb25maWd1cmF0aW9uPg=="
          },
          "authConfigs": {
            "auth.type": "simple",
            "auth.simple.hadoop_username": "root"
          },
          "catalogProperties": {
            "table.self-optimizing.group": "local",
            "warehouse": "/mnt/dfs/4/warehouse_public",
            "type": "hadoop",
            "table-formats": "ICEBERG"
          },
          "setCatalogName": true,
          "setCatalogType": true,
          "setStorageConfigs": true,
          "setAuthConfigs": true,
          "setCatalogProperties": true,
          "storageConfigsSize": 4,
          "authConfigsSize": 2,
          "catalogPropertiesSize": 4
        }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/catalogs',
    method: 'post',
    response: () => ({"message":"success","code":200,"result":""}),
  },

  {
    url: '/mock/ams/v1/catalogs/test_catalog/databases',
    method: 'get',
    response: () => {
      return { "message": "success", "code": 200, "result": ["db"] }
    },
  },
  {
    url: '/mock/ams/v1/catalogs/test_catalog/databases/db/tables',
    method: 'get',
    response: () => {
      return { "message": "success", "code": 200, "result": [{ "name": "user", "type": "ICEBERG" }] };
    },
  },
  {
    url: '/mock/ams/v1/catalogs/:id',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "name": "test_catalog",
        "type": "hadoop",
        "optimizerGroup": "local",
        "tableFormatList": [
          "ICEBERG"
        ],
        "storageConfig": {
          "storage.type": "Hadoop",
          "hive.site": {
            "fileName": "hive-site.xml",
            "fileUrl": "/ams/v1/catalogs/test_catalog/config/storage-config/hive-site"
          },
          "hadoop.core.site": {
            "fileName": "core-site.xml",
            "fileUrl": "/ams/v1/catalogs/test_catalog/config/storage-config/hadoop-core-site"
          },
          "hadoop.hdfs.site": {
            "fileName": "hdfs-site.xml",
            "fileUrl": "/ams/v1/catalogs/test_catalog/config/storage-config/hadoop-hdfs-site"
          }
        },
        "authConfig": {
          "auth.type": "SIMPLE",
          "auth.simple.hadoop_username": "root"
        },
        "properties": {
          "warehouse": "/mnt/dfs/4/warehouse_public",
          "type": "hadoop"
        },
        "tableProperties": {}
      }
    }),
  },
  {
    url: '/mock/ams/v1/catalogs/:id',
    method: 'put',
    response: () => ({ "message": "success", "code": 200, "result": null }),
  },
  {
    url: '/mock/ams/v1/catalogs/:id',
    method: 'delete',
    response: () => ({ "message": "success", "code": 200, "result": true }),
  },
  {
    url: '/mock/ams/v1/catalogs/:id/delete/check',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": true }),
  },
  {
    url: '/mock/ams/v1/catalog/metastore/types',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": [
        {
          "value": "ams",
          "display": "Arctic Metastore"
        },
        {
          "value": "hive",
          "display": "Hive Metastore"
        },
        {
          "value": "hadoop",
          "display": "Hadoop"
        },
        {
          "value": "glue",
          "display": "Glue"
        },
        {
          "value": "custom",
          "display": "Custom"
        }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/:catalog/dbs/:dbId/tables/:tableName/optimizing-processes/:processesId/tasks',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": [
        {
          "value": "ams",
          "display": "Arctic Metastore"
        },
        {
          "value": "hive",
          "display": "Hive Metastore"
        },
        {
          "value": "hadoop",
          "display": "Hadoop"
        },
        {
          "value": "glue",
          "display": "Glue"
        },
        {
          "value": "custom",
          "display": "Custom"
        }
      ]
    }),
  },
]
