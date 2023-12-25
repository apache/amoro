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
    url: '/mock/ams/v1/settings/system',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        'arctic.ams.server-host.prefix': '127.0.0.1',
        'arctic.ams.thrift.port': '18112'
      }
    }),
  },
  {
    url: '/mock/ams/v1/settings/containers',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          name: 'flinkcontainer',
          type: 'flink',
          properties: {
            'properties.FLINK_HOME': '/home/arctic/flink-1.12.7/'
          },
          optimizeGroup: [{
            name: 'flinkOp',
            tmMemory: '1024',
            jmMemory: 'sdsa'
          }]
        },
        {
          name: 'flinkcontainer2',
          type: 'flink',
          properties: {
            'properties.FLINK_HOME': '/home/arctic/flink-1.12.7/'
          },
          optimizeGroup: [{
            name: 'flinkOp',
            tmMemory: '1024',
            jmMemory: 'sdsa2'
          }]
        }
      ]
    }),
  },
]
