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

export interface IOption {
  [propName: string]: any
}
export interface IHttpResponse {
  code: number
  msg: string
  result?: IOption
  data?: IOption
}
export interface UserInfo {
  userName: string
  token?: string
}

export interface IColumns {
  title: string
  dataIndex: string
  key?: string
  ellipsis?: boolean
  width?: number | string
  scopedSlots?: any
  children?: IColumns[]
}

export interface IOptions {
  label: string
  value: string
  [propName: string]: string
}

export interface ILableAndValue {
  label: string
  value: string
}
export interface IMap<T> {
  [key: string]: T
}

export interface IKeyAndValue {
  key: string
  value: string
}
export interface IBaseDetailInfo {
  optimizingStatus: string
  records: string
  tableType: string
  tableName: string
  createTime: string
  size: string
  file: string
  averageFile: string
  tableFormat: string
  hasPartition: boolean
  healthScore: number
}

export interface ResourceUsageItem {
  ts: number
  totalCpu: number
  totalMemory: number
}

export interface DataSizeItem {
  ts: string
  dataSize: number
}

export interface ITopTableItem {
  tableName: string
  tableSize: number
  fileCount: number
  averageFileSize: number
  healthScore: number
}

export interface DetailColumnItem {
  checked?: unknown
  field: string
  type: string
  required: boolean
  comment: string
}

export interface IField {
  field: string
  type: string
  required: boolean
  description: string
}

export interface PartitionColumnItem {
  field: string
  sourceField: string
  transform: string
}

export interface IDetailsInfo {
  pkList: DetailColumnItem[]
  partitionColumnList: PartitionColumnItem[]
  properties: IMap<string>[]
  metrics: IMap<string | number>[]
  schema: DetailColumnItem[]
}
export interface ICompMap {
  Details: string
  Partitions: string
  Snapshots: string
  Operations: string
  Optimizes: string
}
export interface TableBasicInfo {
  catalog: string
  database: string
  tableName: string
}

export interface PartitionItem {
  partition: string
  fileCount: number
  size: string
  lastCommitTime: number | string
  specId: number
}

export interface BreadcrumbPartitionItem {
  file: string
  fsn: number
  fileType: string
  size: string
  commitTime: number | string
  commitId: string
  path: string
}

export interface BreadcrumbSnapshotItem {
  file: string
  fsn: number
  partition: string
  fileType: string
  size: string
  commitTime: number | string
}

export interface FilesStatistics {
  fileCnt: number
  totalSize: number
  averageSize: number
}

export interface BreadcrumbOptimizingItem {
  tableId: number
  processId: number
  taskId: number
  partitionData: string
  status: string
  retryNum: number
  optimizerToken: string
  threadId: number
  thread: string
  startTime: number | string
  endTime: number | string
  costTime: number
  formatCostTime: string
  failReason: string
  summary: Record<string, string>
  inputFiles: FilesStatistics
  outputFiles: FilesStatistics
  inputFilesDesc: string
  outputFilesDesc: string
  properties: Record<string, string>
}

export interface SnapshotItemSummary {
  'total-data-files': number
  'total-delete-files': number
  'total-records': number
  'total-position-deletes': number
  'total-equality-deletes': number
}

export interface SnapshotItem {
  snapshotId: string
  operation: string
  producer: string
  fileCount: number
  records: number
  commitTime: string
  summary: SnapshotItemSummary
  filesSummaryForChart: Record<string, number>
  recordsSummaryForChart: Record<string, number>
}

export interface OverviwOperationItem {
  tableName: string
  tableIdentifier: ITableIdentifier
  operation: string
  ts: number | string
}

export interface OperationItem {
  ts: number | string
  operation: string
}

export interface IHistoryPathInfoItem {
  path: string
  query: IOption
}

export interface GlobalState {
  userInfo: UserInfo
  isShowTablesMenu: boolean
  historyPathInfo: IHistoryPathInfoItem
}

export interface ICatalogItem {
  catalogName: string
  catalogType: string
}
export interface IDebugResult {
  status: string
  columns: string[]
  rowData: (string | null)[][]
  id: string
}

export interface IGroupItem {
  optimizerGroupId: number
  optimizerGroupName: string
  label: string
  value: string
}

export interface IGroupItemInfo {
  occupationCore: number
  occupationMemory: number | string
  unit: string
}

export interface ITableIdentifier {
  catalog: string
  database: string
  tableName: string
  id: number
}
export interface IOptimizeTableItem {
  tableName: string
  optimizeStatus: string
  fileCount: number
  fileSize: number
  quota: number
  quotaOccupation: number
  quotaOccupationDesc: string
  duration: number
  durationDesc: string
  durationDisplay: string
  fileSizeDesc: string
  tableIdentifier: ITableIdentifier
  tableNameOnly?: string
}

export interface IIOptimizeGroupItem {
  resourceGroup: {
    name: string
    container: string
    properties: { [prop: string]: string }
  }
  occupationCore: number
  occupationMemory: number
  name: string
  container: string
  resourceOccupation: string
}

export interface IOptimizeResourceTableItem {
  touchTime: string
  startTime: string
  index: number
  jobId: number
  jobStatus: string
  coreNumber: number
  memory: number
  jobmanagerUrl: string
  parallelism: number
  jobType: string
  groupName: string
  resourceAllocation: string
}

export interface IOverviewSummary {
  catalogCnt: number
  tableCnt: number
  tableTotalSize: number
  totalCpu: string
  totalMemory: number
  displayTotalSize?: string
  displayTotalMemory?: string
  totalSizeUnit?: string
  totalMemoryUnit?: string
}
export interface ITimeInfo {
  yTitle: string
  colors: string[]
  name: string[]
}

export interface IChartLineData {
  timeLine: string[]
  data1: number[] | string[]
  data2: number[] | string[]
}

export interface IOptimizeGroup {
  container: string
  name: string
  properties: IMap<any>
  innerPropertiesArray?: any[]
}

export interface IContainerSetting {
  name: string
  classpath: string
  properties: IMap<string>
  optimizeGroup: IOptimizeGroup[]
  propertiesArray?: IMap<any>[]
}

export interface IResourceUsage {
  timeLine: string[]
  usedCpu: string[]
  usedCpuDivision: string[]
  usedCpuPercent: string[]
  usedMem: string[]
  usedMemDivision: string[]
  usedMemPercent: string[]
}

export enum debugResultBgcMap {
  Created = '#f5f5f5',
  Failed = '#fff2f0',
  Finished = '#f6ffed',
  // eslint-disable-next-line ts/no-duplicate-enum-values
  Canceled = '#f5f5f5',
}

export enum upgradeStatusMap {
  failed = 'FAILED',
  upgrading = 'UPGRADING',
  success = 'SUCCESS',
  none = 'NONE', // can upgrade
}

export enum tableTypeIconMap {
  ICEBERG = 'iceberg',
  ARCTIC = 'amoro',
  HIVE = 'hive',
  PAIMON = 'paimon',
  HUDI = 'hudi',
}

export type ILineChartOriginalData = Record<string, Record<string, number>>

export enum branchTypeMap {
  BRANCH = 'branch',
  TAG = 'tag',
  CONSUMER = 'consumer',
}

export interface IBranchItem {
  value: string
  label: string
  type: branchTypeMap
  amoroCurrentSnapshotsOfTable?: SnapshotItem
}

export interface IServiceBranchItem {
  name: string
  snapshotId: number
  minSnapshotsToKeep: number | null
  maxSnapshotAgeMs: number | null
  maxRefAgeMs: number | null
  type: branchTypeMap
  consumerId: string
  amoroCurrentSnapshotsOfTable: SnapshotItem
}

export enum operationMap {
  ALL = 'all',
  OPTIMIZING = 'optimizing',
  NONOPTIMIZING = 'non-optimizing',
}
