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
        totalMemory: 62464
      }
    }),
  },
  {
    url: '/mock/ams/v1/overview/format',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        { value: 70, name: 'Iceberg format' },
        { value: 20, name: 'Mixed-Iceberg' },
        { value: 10, name: 'Mixed-Hive format' },
      ]
    }),
  },
  {
    url: '/mock/ams/v1/overview/optimizing',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        { value: 40, name: 'Full Optimizing' },
        { value: 20, name: 'Major Optimizing' },
        { value: 30, name: 'Minor Optimizing' },
        { value: 10, name: 'Committing' },
        { value: 2, name: 'Planning' },
        { value: 3, name: 'Pending' },
        { value: 50, name: 'Idle' },
      ]
    }),
  },
  {
    url: '/mock/ams/v1/overview/top/tables',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          tableName: 'trino_online_env_hive.spark_test.ctpri',
          size: 12938982,
          fileCnt: 57889
        },
        {
          tableName: 'trino_online_env_hive.spark_test.ctpp_col',
          size: 329043290,
          fileCnt: 79910
        }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/overview/metric/optimize/resource',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        timeLine: [
          '10-09 14:48'
        ],
        usedCpu: [
          '83.24'
        ],
        usedCpuDivision: [
          '1828C/2196C'
        ],
        usedCpuPercent: [
          '83.24%'
        ],
        usedMem: [
          '83.24'
        ],
        usedMemDivision: [
          '1828C/2196C10364G'
        ],
        usedMemPercent: [
          '83.24%'
        ]
      }
    }),
  },
]
