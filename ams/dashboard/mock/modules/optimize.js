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
]
