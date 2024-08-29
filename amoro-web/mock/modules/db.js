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
    url: '/mock/api/v1/as/db/t1/detail',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        tableType: 'NEW_TABLE',
        tableIdentifier: {
          catalog: 'bdmstest_arctic',
          database: 'default',
          tableName: 'zyxtest',
          id: 580
        },
        schema: [
          {
            field: 'id',
            type: 'int',
            comment: '9NY3o9NY3oKew3C9NY3oKew3CKew3C'
          },
          {
            field: 'id2',
            type: 'int',
            comment: 'j3LlkVneOj'
          }
        ],
        pkList: [
          {
            field: 'id1id1id1id1id1id1id1',
            type: 'string',
            comment: 'comment1comment1comment1comment1'
          },
          {
            field: 'id2',
            type: 'string',
            comment: 'comment2'
          }
        ],
        partitionColumnList: [
          {
            field: 'oR08d7C7uS',
            sourceField: 'dN0ELHP0UM',
            transform: 'MhKJ0LJ8Iu'
          },
          {
            field: 'PGU1tfKH5b',
            sourceField: 'u2eqHWhRkk',
            transform: 'eSJU2b9zib'
          },
          {
            field: 'r54KVdiiCX',
            sourceField: '0xwvC4ujZ8',
            transform: 'EFWKEFWKsKPSJUEFWKEFWKsKPSJUsKPSJUEFWKEFWKsKPSJUsKPSJUsKPSJU'
          }
        ],
        properties: {
          EFWKEFWKsKPSJUEFWKEFWKsKPSJUsKPSJUEFWKEFWKsKPSJUsKPSJUsKPSJU: '148'
        },
        metrics: {
          lastCommitTime: 1651301798030,
          size: '12KB',
          file: 'file1',
          averageFileSize: '10KB'
        }
      }
    }),
  },
  {
    url: '/mock/api/v1/as/db/t1/2022-03-02/detail',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          file: 'file',
          fsn: 'fsn1',
          type: 'type1',
          size: '123KB',
          commitTime: 1651301798030,
          commitId: 'commitId',
          path: 'path'
        },
        {
          file: 'file2',
          fsn: 'fsn2',
          type: 'type2',
          size: '1234KB',
          commitTime: 1651301798030,
          commitId: 'commitId2',
          path: 'path2'
        }
      ]
    }),
  },
  {
    url: '/mock/api/v1/as/db/t1/transactions',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        list: [
          {
            id: 'id1',
            fileCount: 200,
            fileSize: 'Bic05xfOhE',
            commitTime: 1651301798030,
            snapshotId: '81213'
          },
          {
            id: 'id2',
            fileCount: 200,
            fileSize: 'OiuK7iAcU1',
            commitTime: 1651301798030,
            snapshotId: '82194'
          }
        ],
        total: 100
      }
    }),
  },
]
