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

import request from '@/utils/request'

export function getJobDebugResult(sessionId: string) {
  return request.get(`ams/v1/terminal/${sessionId}/result`)
}

export function getShortcutsList() {
  return request.get('ams/v1/terminal/examples')
}

export function getExampleSqlCode(exampleName: string) {
  return request.get(`ams/v1/terminal/examples/${exampleName}`)
}

export function executeSql(params: {
  catalog: string
  sql: string
}) {
  const { catalog, sql } = params
  return request.post(`ams/v1/terminal/catalogs/${catalog}/execute`, { sql })
}

export function stopSql(sessionId: string) {
  return request.put(`ams/v1/terminal/${sessionId}/stop`)
}

export function getLogsResult(sessionId: string) {
  return request.get(`ams/v1/terminal/${sessionId}/logs`)
}

// get the last executed sql and sessionId
export function getLastDebugInfo() {
  return request.get('ams/v1/terminal/latestInfos')
}
