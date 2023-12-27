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
    url: '/mock/ams/v1/tables/bdmstest_arctic/ndc_test_db/realtime_dw_inventory_poc_wt_3141/partitions',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        list: [
          {
            partition: '2022-03-02',
            fileCount: 16,
            size: '123KB',
            lastCommitTime: 1651301798030
          },
          {
            partition: '2022-03-03',
            fileCount: 20,
            size: '1234KB',
            lastCommitTime: 1651301798030
          }
        ],
        total: 2
      }
    }),
  },
  {
    url: '/mock/api/v1/tables/as/db/t1/optimize',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        list: [
          {
            tableIdentifier: {
              catalog: 'arctic_online_new',
              database: 'tmp_music',
              tableName: 'dim_moyi_itm_gift_base_dd',
              id: 205
            },
            compactRange: 'Partition',
            recordId: 103354,
            visibleTime: 1651301798030,
            commitTime: 1651301798030,
            planTime: 1651301798030,
            duration: 18173,
            parallelism: 10,
            totalFilesStatBeforeCompact: {
              fileCnt: 5,
              totalSize: 78539,
              averageSize: 15707
            },
            insertFilesStatBeforeCompact: {
              fileCnt: 4,
              totalSize: 66763,
              averageSize: 16690
            },
            deleteFilesStatBeforeCompact: {
              fileCnt: 0,
              totalSize: 0,
              averageSize: 0
            },
            baseFilesStatBeforeCompact: {
              fileCnt: 1,
              totalSize: 11776,
              averageSize: 11776
            },
            totalFilesStatAfterCompact: {
              fileCnt: 4,
              totalSize: 67985,
              averageSize: 16996
            },
            snapshotInfo: {
              snapshotId: 9197286040231952000,
              operation: null,
              totalSize: 7369863,
              totalFiles: 506,
              totalRecords: 653,
              addedFiles: null,
              addedFilesSize: 67985,
              addedRecords: 142,
              removedFilesSize: 11776,
              removedFiles: 1,
              removedRecords: 1
            },
            partitionCnt: 1,
            partitions: 'dt=2022-04-29',
            baseTableMaxFileSequence: '{dt=2022-04-29=12, dt=2022-04-28=7}'
          },
          {
            tableIdentifier: {
              catalog: 'arctic_online_new',
              database: 'tmp_music',
              tableName: 'dim_moyi_itm_gift_base_dd',
              id: 205
            },
            compactRange: 'Partition',
            recordId: 103354,
            visibleTime: 1651204980005,
            commitTime: 1651204980005,
            planTime: 1651204961832,
            duration: 18173,
            totalFilesStatBeforeCompact: {
              fileCnt: 5,
              totalSize: 78539,
              averageSize: 15707
            },
            insertFilesStatBeforeCompact: {
              fileCnt: 4,
              totalSize: 66763,
              averageSize: 16690
            },
            deleteFilesStatBeforeCompact: {
              fileCnt: 0,
              totalSize: 0,
              averageSize: 0
            },
            baseFilesStatBeforeCompact: {
              fileCnt: 1,
              totalSize: 11776,
              averageSize: 11776
            },
            totalFilesStatAfterCompact: {
              fileCnt: 4,
              totalSize: 67985,
              averageSize: 16996
            },
            snapshotInfo: {
              snapshotId: 9197286040231952000,
              operation: null,
              totalSize: 7369863,
              totalFiles: 506,
              totalRecords: 653,
              addedFiles: null,
              addedFilesSize: 67985,
              addedRecords: 142,
              removedFilesSize: 11776,
              removedFiles: 1,
              removedRecords: 1
            },
            partitionCnt: 1,
            partitions: 'dt=2022-04-29',
            baseTableMaxFileSequence: '{dt=2022-04-29=12, dt=2022-04-28=7}'
          },
          {
            tableIdentifier: {
              catalog: 'arctic_online_new',
              database: 'tmp_music',
              tableName: 'dim_moyi_itm_gift_base_dd',
              id: 205
            },
            compactRange: 'Partition',
            recordId: 103354,
            visibleTime: 1651204980005,
            commitTime: 1651204980005,
            planTime: 1651204961832,
            duration: 18173,
            totalFilesStatBeforeCompact: {
              fileCnt: 5,
              totalSize: 78539,
              averageSize: 15707
            },
            insertFilesStatBeforeCompact: {
              fileCnt: 4,
              totalSize: 66763,
              averageSize: 16690
            },
            deleteFilesStatBeforeCompact: {
              fileCnt: 0,
              totalSize: 0,
              averageSize: 0
            },
            baseFilesStatBeforeCompact: {
              fileCnt: 1,
              totalSize: 11776,
              averageSize: 11776
            },
            totalFilesStatAfterCompact: {
              fileCnt: 4,
              totalSize: 67985,
              averageSize: 16996
            },
            snapshotInfo: {
              snapshotId: 9197286040231952000,
              operation: null,
              totalSize: 7369863,
              totalFiles: 506,
              totalRecords: 653,
              addedFiles: null,
              addedFilesSize: 67985,
              addedRecords: 142,
              removedFilesSize: 11776,
              removedFiles: 1,
              removedRecords: 1
            },
            partitionCnt: 1,
            partitions: 'dt=2022-04-29',
            baseTableMaxFileSequence: '{dt=2022-04-29=12, dt=2022-04-28=7}'
          }
        ],
        total: 7
      }
    }),
  },
]
