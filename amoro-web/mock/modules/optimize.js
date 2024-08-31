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
    url: '/mock/ams/v1/optimize/resourceGroups/get',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        { occupationCore: 1, occupationMemory: 12, resourceGroup: { name: 'testName', container: 'container1', properties: { key1: 'value1' } } }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/optimize/containers/get',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: ['container1', 'container2']
    }),
  },
  {
    url: '/mock/ams/v1/optimize/optimizerGroups/:groups/tables',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "tableIdentifier": {
              "id": 1,
              "catalog": "test_catalog",
              "database": "db",
              "tableName": "user",
              "format": "ICEBERG",
              "identifier": {
                "catalog": "test_catalog",
                "database": "db",
                "tableName": "user",
                "setCatalog": true,
                "setDatabase": true,
                "setTableName": true
              }
            },
            "tableName": "test_catalog.db.user",
            "optimizeStatus": "idle",
            "duration": 1195501081,
            "fileCount": 0,
            "fileSize": 0,
            "quota": 0.1,
            "quotaOccupation": 0.0,
            "groupName": "local"
          }
        ],
        "total": 1
      }
    }),
  },
  {
    url: '/mock/ams/v1/optimize/optimizerGroups/local/optimizers',
    method: 'post',
    response: () => ({ "message": "success", "code": 200, "result": "success to scaleOut optimizer" }),
  },
  {
    url: '/mock/ams/v1/optimize/resourceGroups',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": [
        {
          "resourceGroup": {
            "name": "local",
            "container": "localContainer",
            "properties": {
              "memory": "1024"
            }
          },
          "occupationCore": 0,
          "occupationMemory": 0
        },
        {
          "resourceGroup": {
            "name": "local11",
            "container": "localContainer",
            "properties": {
              "memory": "1024"
            }
          },
          "occupationCore": 0,
          "occupationMemory": 0
        },
        {
          "resourceGroup": {
            "name": "local22",
            "container": "localContainer",
            "properties": {
              "memory": "1024"
            }
          },
          "occupationCore": 0,
          "occupationMemory": 0
        }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/optimize/resourceGroups',
    method: 'put',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "The optimizer group has been successfully updated."
    }),
  },
  {
    url: '/mock/ams/v1/optimize/resourceGroups',
    method: 'post',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "The optimizer group has been successfully created."
    }),
  },
  {
    url: '/mock/ams/v1/optimize/resourceGroups/:id/delete/check',
    method: 'get',
    response: () => ({ "message": "success", "code": 200, "result": true }),
  },
  {
    url: '/mock/ams/v1/optimize/resourceGroups/:id',
    method: 'delete',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": "The optimizer group has been successfully deleted."
    }),
  },
  {
    url: '/mock/ams/v1/optimize/optimizerGroups/all/optimizers',
    method: 'get',
    response: () => ({
      "message": "success",
      "code": 200,
      "result": {
        "list": [
          {
            "token": "183c4ce4-a006-4d18-b8b3-4f9bee3e7527",
            "startTime": 1715862320085,
            "touchTime": 1715862530475,
            "jobId": null,
            "groupName": "local",
            "coreNumber": 1,
            "memory": 8192,
            "jobStatus": "RUNNING",
            "container": "localContainer"
          }
        ],
        "total": 1
      }
    }),
  },
]
