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
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/details',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "tableType": "ICEBERG",
        "tableIdentifier": {
          "catalog": "test_catalog",
          "database": "db",
          "tableName": "user"
        },
        "schema": [
          {
            "field": "id",
            "type": "int",
            "required": false,
            "comment": null
          },
          {
            "field": "name",
            "type": "string",
            "required": false,
            "comment": null
          },
          {
            "field": "ts",
            "type": "timestamptz",
            "required": false,
            "comment": null
          }
        ],
        "pkList": [],
        "partitionColumnList": [
          {
            "field": "ts_day",
            "sourceField": "ts",
            "transform": "day",
            "fieldId": 1000,
            "sourceFieldId": 3
          }
        ],
        "properties": {
          "owner": "root",
          "self-optimizing.group": "local"
        },
        "changeMetrics": null,
        "baseMetrics": {
          "lastCommitTime": 1703586944652,
          "totalSize": "1.79KB",
          "baseWatermark": null,
          "averageFileSize": "918.00B",
          "fileCount": 2
        },
        "tableSummary": {
          "file": 2,
          "size": "1.79KB",
          "tableFormat": "Iceberg(V1)",
          "averageFile": "918.00B",
          "records":24,
          "optimizingStatus":"IDLE",
          "healthScore":100,
        },
        "baseLocation": "/mnt/dfs/4/warehouse_public/db/user",
        "filter": null,
        "createTime": 0,
        "creator": null,
        "tableWatermark": null,
        "baseWatermark": null
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/partitions',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "partition": "ts_day=2022-07-02",
            "specId": 0,
            "fileCount": 1,
            "fileSize": 921,
            "lastCommitTime": 1703586944652,
            "size": "921.00B"
          },
          {
            "partition": "ts_day=2022-07-01",
            "specId": 0,
            "fileCount": 1,
            "fileSize": 916,
            "lastCommitTime": 1703586944652,
            "size": "916.00B"
          }
        ],
        "total": 2
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/branches',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "name": "main",
            "snapshotId": 6289200084786695285,
            "minSnapshotsToKeep": null,
            "maxSnapshotAgeMs": null,
            "maxRefAgeMs": null,
            "type": "branch"
          }
        ],
        "total": 1
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/tags',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": { "list": [], "total": 0 } }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/snapshots',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "snapshotId": "6289200084786695285",
            "fileCount": 2,
            "fileSize": "1.79KB",
            "records": 3,
            "commitTime": 1703586944652,
            "operation": "overwrite",
            "producer": "INGESTION",
            "summary": {
              "added-data-files": "2",
              "total-equality-deletes": "0",
              "added-records": "3",
              "replace-partitions": "true",
              "total-records": "3",
              "spark.app.id": "local-1703586930757",
              "changed-partition-count": "2",
              "total-position-deletes": "0",
              "added-files-size": "1.79KB",
              "total-delete-files": "0",
              "total-files-size": "1.79KB",
              "total-data-files": "2"
            },
            "recordsSummaryForChart": {
              "total-records": "3",
              "eq-delete-records": "0",
              "pos-delete-records": "0"
            },
            "filesSummaryForChart": {
              "delete-files": "0",
              "total-files": "2",
              "data-files": "2"
            },
            "originalFileSize": 1837
          }
        ],
        "total": 1
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/operations',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": { "list": [], "total": 0 } }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/partitions/:filter/files',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "commitId": "3015968602476930240",
            "fileType": "BASE_FILE",
            "commitTime": 1715861982880,
            "size": "883.00B",
            "partition": "ts_day=2022-07-01",
            "specId": 0,
            "path": "/tmp/local_iceberg/db/user/data/ts_day=2022-07-01/00042-1-179d061d-6fe9-4945-b6a6-f8c088595412-00001.parquet",
            "file": "00042-1-179d061d-6fe9-4945-b6a6-f8c088595412-00001.parquet",
            "fileSize": 883,
            "operation": null
          }
        ],
        "total": 1
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/snapshots/:snapshotId/detail',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "commitId": "3015968602476930240",
            "fileType": "BASE_FILE",
            "commitTime": 1715861982880,
            "size": "883.00B",
            "partition": "ts_day=2022-07-01",
            "specId": 0,
            "path": "/tmp/local_iceberg/db/user/data/ts_day=2022-07-01/00042-1-179d061d-6fe9-4945-b6a6-f8c088595412-00001.parquet",
            "file": "00042-1-179d061d-6fe9-4945-b6a6-f8c088595412-00001.parquet",
            "fileSize": 883,
            "operation": "add"
          },
          {
            "commitId": "3015968602476930240",
            "fileType": "BASE_FILE",
            "commitTime": 1715861982880,
            "size": "891.00B",
            "partition": "ts_day=2022-07-02",
            "specId": 0,
            "path": "/tmp/local_iceberg/db/user/data/ts_day=2022-07-02/00082-2-179d061d-6fe9-4945-b6a6-f8c088595412-00001.parquet",
            "file": "00082-2-179d061d-6fe9-4945-b6a6-f8c088595412-00001.parquet",
            "fileSize": 891,
            "operation": "add"
          }
        ],
        "total": 2
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/optimizing-processes',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "tableId": 1,
            "catalogName": "local_iceberg",
            "dbName": "db",
            "tableName": "user",
            "processId": 1715862413340,
            "startTime": 1715862413340,
            "optimizingType": "MINOR",
            "status": "SUCCESS",
            "failReason": null,
            "duration": 1531,
            "successTasks": 2,
            "totalTasks": 2,
            "runningTasks": 0,
            "finishTime": 1715862414871,
            "inputFiles": {
              "fileCnt": 16,
              "totalSize": 14192,
              "averageSize": 887
            },
            "outputFiles": {
              "fileCnt": 2,
              "totalSize": 2064,
              "averageSize": 1032
            },
            "summary": {
              "input-data-files(rewrite)": "16",
              "input-data-size(rewrite)": "13.86KB",
              "input-data-records(rewrite)": "24",
              "output-data-files": "2",
              "output-data-size": "2.02KB",
              "output-data-records": "24"
            }
          }
        ],
        "total": 1
      }
    }),
  },
  {
    url: '/mock/ams/v1/tables/catalogs/test_catalog/dbs/db/tables/user/operations',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "ts": 1715862487163,
            "operation": "ALTER TABLE user ADD COLUMNS (age bigint)"
          }
        ],
        "total": 1
      }
    }),
  },
]
