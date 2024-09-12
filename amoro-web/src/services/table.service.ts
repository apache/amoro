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

// import { IOptions } from '@/types/common.type'
import type { ICatalogItem, IMap } from '@/types/common.type'
import request from '@/utils/request'

export function getCatalogList(): Promise<ICatalogItem[]> {
  return request.get('ams/v1/catalogs')
}
export function getDatabaseList(params: {
  catalog: string
  keywords: string
}): Promise<string[]> {
  const { catalog, keywords } = params
  return request.get(`ams/v1/catalogs/${catalog}/databases`, { params: { keywords } })
}

export function getTableList(params: {
  catalog: string
  db: string
  keywords: string
}) {
  const { catalog, db, keywords } = params
  return request.get(`ams/v1/catalogs/${catalog}/databases/${db}/tables`, { params: { keywords } })
}

// get tables detail
export function getTableDetail(
  { catalog = '' as string, db = '' as string, table = '' as string, token = '' as string },
) {
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/details`, { params: { token } })
}

export function getHiveTableDetail(
  { catalog = '' as string, db = '' as string, table = '' as string },
) {
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/hive/details`)
}

export function getUpgradeStatus(
  { catalog = '' as string, db = '' as string, table = '' as string },
) {
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/upgrade/status`)
}
// get partions table
export function getPartitionTable(
  params: {
    catalog: string
    db: string
    table: string
    filter: string
    page: number
    pageSize: number
    token?: string
  },
) {
  const { catalog, db, table, filter, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/partitions`, { params: { filter, page, pageSize, token } })
}

// get partions
export function getPartitions(
  params: {
    catalog: string
    db: string
    table: string
    page: number
    pageSize: number
    token: string
  },
) {
  const { catalog, db, table, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/partitions`, { params: { page, pageSize, token } })
}
// get partions-files
export function getPartitionFiles(
  params: {
    catalog: string
    db: string
    table: string
    partition: string | null
    specId: number
    page: number
    pageSize: number
    token?: string
  },
) {
  const { catalog, db, table, partition, specId, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/partitions/${partition}/files`, { params: { specId, page, pageSize, token } })
}
// get snapshots
export function getSnapshots(
  params: {
    catalog: string
    db: string
    table: string
    page: number
    pageSize: number
    token?: string
    ref: string
    operation: string
  },
) {
  const { catalog, db, table, page, pageSize, token, ref, operation } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/snapshots`, { params: { page, pageSize, token, ref, operation } })
}

// get Snapshot detail
export function getDetailBySnapshotId(
  params: {
    catalog: string
    db: string
    table: string
    snapshotId: string
    page: number
    pageSize: number
    token?: string
  },
) {
  const { catalog, db, table, snapshotId, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/snapshots/${snapshotId}/detail`, { params: { page, pageSize, token } })
}
// get operations
export function getOperations(
  params: {
    catalog: string
    db: string
    table: string
    page: number
    pageSize: number
    token?: string
  },
) {
  const { catalog, db, table, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/operations`, { params: { page, pageSize, token } })
}
// get optimizing processes
export function getOptimizingProcesses(
  params: {
    catalog: string
    db: string
    table: string
    type: string
    status: string
    page: number
    pageSize: number
    token?: string
  },
) {
  const { catalog, db, table, type, status, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/optimizing-processes`, { params: { page, pageSize, token, type, status } })
}

// get optimizing process types
export function getTableOptimizingTypes(
  params: {
    catalog: string
    db: string
    table: string
    token?: string
  },
) {
  const { catalog, db, table, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/optimizing-types`, { params: { token } })
}

// get optimizing taskes
export function getTasksByOptimizingProcessId(
  params: {
    catalog: string
    db: string
    table: string
    processId: number
    page: number
    pageSize: number
    token?: string
  },
) {
  const { catalog, db, table, processId, page, pageSize, token } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/optimizing-processes/${processId}/tasks`, { params: { page, pageSize, token } })
}

export function upgradeHiveTable(
  { catalog = '' as string, db = '' as string, table = '' as string, properties = {} as IMap<string>, pkList = [] as IMap<string>[] },
) {
  return request.post(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/upgrade`, {
    properties,
    pkList,
  })
}

export function getUpgradeProperties() {
  return request.get('ams/v1/upgrade/properties')
}

export function cancelOptimizingProcess(
  { catalog = '' as string, db = '' as string, table = '' as string, processId = '' as string },
) {
  return request.post(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/optimizing-processes/${processId}/cancel`)
}

export function getBranches(params: { catalog: string, db: string, table: string }) {
  const { catalog, db, table } = params
  return request.get(`/ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/branches`)
}

export function getTags(params: { catalog: string, db: string, table: string }) {
  const { catalog, db, table } = params
  return request.get(`/ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/tags`)
}
export function getConsumers(params: { catalog: string, db: string, table: string }) {
  const { catalog, db, table } = params
  return request.get(`/ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/consumers`)
}
