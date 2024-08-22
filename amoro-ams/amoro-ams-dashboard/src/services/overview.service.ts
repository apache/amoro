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

export function getOverviewSummary() {
  return request.get('ams/v1/overview/summary')
}

export function getOverviewFormat() {
  return request.get('ams/v1/overview/format')
}

export function getOverviewOptimizingStatus() {
  return request.get('ams/v1/overview/optimizing')
}

export function getOverviewOperations() {
  return request.get('ams/v1/overview/operations')
}

export function getTop10TableList(orderBy: string) {
  return request.get(`ams/v1/overview/top10`, { params: { orderBy:orderBy || 'healthScore'} })
}

export function getResourceUsageList(
  startTime: number
  ,
) {
  return request.get(`ams/v1/overview/resource`, { params: { startTime } })
}

export function getDataSizeList(
  startTime: number
  ,
) {
  return request.get(`ams/v1/overview/dataSize`, { params: { startTime } })
}
