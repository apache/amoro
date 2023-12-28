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
      code: 200,
      msg: 'success',
      result: [
        'CreateTable',
        'EditTable',
        'DeleteTable'
      ]
    }),
  },
  {
    url: '/mock/ams/v1/terminal/3/result',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          id: 'Result1',
          status: 'Failed',
          columns: [
            'namespace'
          ],
          rowData: [
            [
              'arctic_test'
            ],
            [
              'arctic_test_2'
            ],
            [
              'hellowrld'
            ]
          ]
        },
        {
          id: 'Result2',
          status: 'Failed',
          columns: [
            'namespace'
          ],
          rowData: [
            [
              'arctic_test222222222222222222'
            ],
            [
              'arctic_test_24444444444444'
            ],
            [
              'hellowrld666666666666666666666'
            ]
          ]
        }
      ]
    }),
  },
]
