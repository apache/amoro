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
    url: '/mock/ams/v1/overview/summary',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        catalogCnt: 2,
        tableCnt: 37944,
        tableTotalSize: 10585900,
        totalCpu: '6',
        totalMemory: 62464000000
      }
    }),
  },
  {
    url: '/mock/ams/v1/overview/optimizing',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        { value: 40, name: 'Executing' },
        { value: 10, name: 'Committing' },
        { value: 2, name: 'Planning' },
        { value: 3, name: 'Pending' },
        { value: 50, name: 'Idle' },
      ]
    }),
  },
  {
    url: '/mock/ams/v1/overview/top',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        { tableName: 'test_catalog.db.user', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '47',},
        { tableName: 'test_catalog.db.user1', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '52',},
        { tableName: 'test_catalog.db.user2', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '66',},
        { tableName: 'test_catalog.db.user3', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '77',},
        { tableName: 'test_catalog.db.user4', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '78',},
        { tableName: 'test_catalog.db.user5', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '81',},
        { tableName: 'test_catalog.db.user6', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '85',},
        { tableName: 'test_catalog.db.user7', tableSize: '1774', fileCount: '2', averageFileSize: '887', healthScore: '88',},
        { tableName: 'test_catalog.db.user8', tableSize: '0', fileCount: '0', averageFileSize: '0', healthScore: '100',},
        { tableName: 'test_catalog.db.user9', tableSize: '0', fileCount: '0', averageFileSize: '0', healthScore: '100',},
      ]
    }),
  },
  {
    url: '/mock/ams/v1/overview/resource',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [{
        ts: 1724119207500,
        totalCpu: 2,
        totalMemory: 4048000000
      },{
        ts: 1724122892000,
        totalCpu: 5,
        totalMemory: 8096000000
      },{
        ts: 1724126441000,
        totalCpu: 12,
        totalMemory: 32492000000
      },{
        ts: 1724130154000,
        totalCpu: 7,
        totalMemory: 14462000000
      },{
        ts: 1724133605600,
        totalCpu: 26,
        totalMemory: 50176000000
      }]
    }),
  },
  {
    url: '/mock/ams/v1/overview/dataSize',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [{
        ts: 1724119200000,
        dataSize: 1024000
      },{
        ts: 1724122800000,
        dataSize: 2048000
      },{
        ts: 1724126400000,
        dataSize: 4096000
      }]
    }),
  },
]
