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
import { ICatalogItem } from '@/types/common.type'
import request from '@/utils/request'

export function getCatalogList(): Promise<ICatalogItem[]> {
  return request.get('ams/v1/catalogs')
}
export function getDatabaseList(catalog: string): Promise<string[]> {
  return request.get(`ams/v1/catalogs/${catalog}/databases`)
}

export function getTableList(params: {
  catalog: string
  db: string
}): Promise<string[]> {
  const { catalog, db } = params
  return request.get(`ams/v1/catalogs/${catalog}/databases/${db}/tables`)
}

// get tables detail
export function getTableDetail(
  { catalog = '' as string, db = '' as string, table = '' as string }
) {
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/details`)
}
// get partions table
export function getPartitionTable(
  params: {
    catalog: string
    db: string,
    table: string,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/partitions`, { params: { page, pageSize } })
}

// get partions
export function getPartitions(
  params: {
    catalog: string
    db: string,
    table: string,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/partitions`, { params: { page, pageSize } })
}
// get partions-files
export function getPartitionFiles(
  params: {
    catalog: string
    db: string,
    table: string,
    partition: string | null,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, partition, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/partitions/${partition}/files`, { params: { page, pageSize } })
}
// get Transactions
export function getTransactions(
  params: {
    catalog: string
    db: string,
    table: string,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/transactions`, { params: { page, pageSize } })
}

// get TransactionId detail
export function getDetailByTransactionId(
  params: {
    catalog: string
    db: string,
    table: string,
    transactionId: string,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, transactionId, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/transactions/${transactionId}/detail`, { params: { page, pageSize } })
}
// get operations
export function getOperations(
  params: {
    catalog: string
    db: string,
    table: string,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/operations`, { params: { page, pageSize } })
}
// get optimizes
export function getOptimizes(
  params: {
    catalog: string
    db: string,
    table: string,
    page: number
    pageSize: number
  }
) {
  const { catalog, db, table, page, pageSize } = params
  return request.get(`ams/v1/tables/catalogs/${catalog}/dbs/${db}/tables/${table}/optimize`, { params: { page, pageSize } })
}
