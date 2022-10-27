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

import { IOverviewSummary, ITopTableItem, IResourceUsage } from '@/types/common.type'
import request from '@/utils/request'

export function getOverviewSummary(): Promise<IOverviewSummary[]> {
  return request.get('ams/v1/overview/summary')
}

export function getTopTables(params: {
  order?: string
  orderBy?: string
  count?: number
}): Promise<ITopTableItem[]> {
  const { order, orderBy, count } = params
  return request.get('ams/v1/overview/top/tables', {
    params: {
      order: order || 'desc',
      orderBy: orderBy || 'size',
      count: count || 10
    }
  })
}

export function getOptimizeResource(params: {
  startTime: number
  endTime: number
}): Promise<IResourceUsage> {
  const { startTime, endTime } = params
  return request.get('ams/v1/overview/metric/optimize/resource', {
    params: {
      startTime,
      endTime
    }
  })
}
