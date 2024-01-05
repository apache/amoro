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
    url: '/mock/ams/v1/login/current',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      "result": {
        "userName": "admin",
        "loginTime": "1703839452053"
      }
    }),
  },
  {
    url: '/mock/ams/v1/login',
    method: 'post',
    response: () => ({
      code: 200,
      msg: 'success',
      result: 'success'
    }),
  },
  {
    url: '/mock/ams/v1/versionInfo',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        "version": "0.7.0-SNAPSHOT",
        "commitTime": "2023-12-26T13:59:42+0800"
      }
    }),
  },
  {
    url: '/mock/ams/v1/upgrade/properties',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        key1: 'koYg4SDRzM',
        key2: 'T3ScQHN0hE'
      }
    })
  },
]
