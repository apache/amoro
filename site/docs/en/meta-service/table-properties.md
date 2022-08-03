# 表配置

这里列出了当前表的所有参数，可以在[创建表](table-management.md##_2)时指定它的值，也可以通过[修改表](table-properties.md##_4)动态修改它的值。

### 表读取配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| read.split.open-file-cost          | 4194304（4MB）    | 打开文件预估的开销                          |
| read.split.planning-lookback       | 10               | 合并读取任务时同时考虑的任务个数               |
| read.split.target-siz              | 134217728（128MB）| 合并读取任务时的目标大小                     |

### 表写入配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| base.write.format                  | parquet          | basestore的文件格式，只对有主键表有效        |
| change.write.format                | parquet          | changestore的文件格式，只对有主键表有效      |
| write.format.default               | parquet          | 表的默认写入文件格式，只对无主建表有效          |
| base.file-index.hash-bucket        | 4                | basestore hash文件索引的bucket个数         |
| change.file-index.hash-bucket      | 4                | changestore hash文件索引的bucket个数       |
| write.target-file-size-bytes       | 134217728（128MB）| 文件写入时的目标文件大小                     |
| write.upsert.enabled               | false            | 是否开启upsert写入模式，开启后相同主键的多条insert数据会被合并   |
| write.distribution-mode            | hash             | 写入时的数据分区方式，支持none、hash、range                  |
| write.distribution.hash-mode       | auto             | 使用hash的分区写入方式时，使用的hash键，支持primary-key、partition-key、primary-partition-key和auto  |

### 结构优化配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| optimize.enable                    | true             | 是否开启结构优化                  |
| optimize.group                     | default          | 结构优化所属的组                  |
| optimize.quota                     | 0.1              | 表所能占用的结构优化资源量          |
| optimize.num-retries               | 5                | 结构优化失败时的重试次数            |
| optimize.commit.interval           | 60000（1分钟）    | 结构优化的最短提交间隔              |
| optimize.small-file-size-bytes-threshold | 16777216（16MB）| 结构优化时判断是否为小文件的阈值 |
| optimize.major.trigger.max-interval      | 86400000（1天）               | 触发major optimize的最长时间间隔  |
| optimize.major.trigger.delete-file-size-bytes       | 67108864（64MB）   | 触发major optimize的最大delete文件大小 |
| optimize.major.trigger.small-file-count             | 12                | 触发major optimize的小文件数数量 |
| optimize.minor.trigger.max-interval                 | 3600000（1小时）    | 触发minor optimize的最长时间间隔 |
| optimize.minor.trigger.delete-file-count            | 12                | 触发minor optimize的最大delete文件个数 |
| optimize.major.max-task-file-size-byte              | 1073741824（1GB）  | major optimize最大的任务大小 |

### 数据清理相关参数

| 配置名称                                     | 默认值             | 描述                                     |
| ------------------------------------------- | ---------------- | ----------------------------------       |
| table-expire.enable                         | true             | 是否开启的表过期数据自动清理                  |
| change.data.ttl.minutes                     | 10080（7天）      | changestore数据的过期时间                 |
| snapshot.base.keep.minutes                  | 720（12小时）     | basestore历史快照的保留时间                |
| clean-orphan-file.enable                    | false            | 是否开启游离文件自动清理                     |
| clean-orphan-file.min-existing-time-minutes | 2880（2天）       | 清理游离文件时为了防止错误清理正在写入的文件，判断文件最低的存在时间          |

### Logstore相关配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| log-store.enable                   | false            | 是否开启logstore                         |
| log-store.type                     | kafka            | logstore的类型，当前仅支持kafka            |
| log-store.address                  | NULL             | logstore的地址                           |
| log-store.topic                    | NULL             | logstore使用的topic                      |
| log-store.data-format              | json             | logstore中的消息格式，当前仅支持json         |
| log-store.data-version             | v1               | logstore中消息的版本，当前仅支持v1           |



