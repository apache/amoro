# Repair
Arctic 提供了一个应对 Arctic 表不可用问题的临时解决方案以及时恢复表的可用性，避免因表的长时间不可用而造成较大的损失。

## 表不可用场景
以下是几种表不可用场景及修复方案的梳理：
### File 文件丢失
File 文件丢失包括 DataFile, Equality Delete File, Position Delete File 三种文件的丢失。  
原因：造成这种情况的原因多为清理孤立文件或过期文件时清理逻辑的 Bug ，或者人为运维错误误删除等。  
修复方案：
- 先去 trash 目录下寻找相同文件名的文件，如果找到则恢复回收站数据到数据相应目录。
- 如果 trash 目录下找不到那么提供给用户两个可选项：
  - 直接在元数据中删除该文件
  - 回退到某个可用版本
> 在 Mixed-Hive format 中，Iceberg 索引到的文件被 Hive 类似 Insert overwrite 相关操作删除。但是很难区分是 Hive 操作造成的，还是文件非正常丢失。所以在此背景下，仅提供 Hive sync 功能尝试同步元数据。

### Manifest 丢失
原因：造成这种情况的原因多为清理孤立文件或过期文件时清理逻辑的 Bug ，或者人为运维错误误删除等。  
修复方案：
- 先去 trash 目录下寻找相同文件名的文件，如果找到则恢复回收站数据到数据相应目录。
- 如果 trash 目录下找不到那么提供给用户两个可选项。
  - 删除 ManifestList 中此条 Manifest 记录
  - 回退到最近不含有该 Manifest 文件的版本。

### ManifestList 丢失
原因：造成这种情况的原因多为清理孤立文件或过期文件时清理逻辑的 Bug ，或者人为运维错误误删除等。  
修复方案：
- 先去 trash 目录下寻找相同文件名的文件，如果找到则恢复回收站数据到数据相应目录。
- 如果 trash 目录下找不到那么提供给用户回退到后一个版本选项。

### Metadata丢失
原因：造成这种情况的原因多为清理孤立文件或过期文件时清理逻辑的 Bug ，或者人为运维错误误删除等。主要形式是 version-hit.txt 文件里面的版本对应的 metadata 文件丢失. 如果 version-hit.txt 不存在或者损坏那么将不需要修复并会自动回退到最大可用版本。  
修复方案：
- 先去 trash 目录下寻找相同文件名的文件，如果找到则恢复回收站数据到数据相应目录。
- 如果 trash 目录下找不到且还有 metadata 文件存在那么就会删除 version-hit.txt 文件，这样表会自动回退版本，如果已经不存在可用的 metadata 文件，那么清除表。

## 使用说明
Repair 工具会跟随 AMS 部署并提供脚本以启动 Repair 工具，启动后会进入一个交互式终端。
### 脚本启动
AMS 部署目录的 bin 目录下会有 Repair 工具的脚本，名为 `repair.sh`。  
脚本提供三个选项，其中 '-t' 和 '-c' 参数只用指定其中一个即可：
```
    -t        您的 thrift 地址.
              模版: thrift://{AMS_HOST}:{AMS_PORT}/{AMS_CATALOG_NAME}
              例子: -t thrift://localhost:1260/catalog_name

    -c        您的 catalog 名称.
              我们将从 {ARCTIC_HOME}/conf/config.yaml 中读取 thrift 端口号并用 localhost 作为 {AMS_HOST} 来组成一个完整的 thrift 地址
              例子: -c local_catalog

    -m        给 Repair 工具分配的内存大小.
              单位: GB
              默认值: 2GB (建议不小于 2GB)
              例子: -m 3 (意味着将分配 3GB 内存给 Repair 工具)
```
在 AMS 部署的目录下你可以通过如下命令来启动 Repair 工具：
```shell
$ ./bin/repair.sh       #直接启动 Repair 工具，将从 $ARCTIC_HOME/conf/config.yaml 中读取 thrift 端口号并用 localhost 作为地址，不设置 catalog
$ ./bin/repair.sh -t thrift://localhost:1260/local_catalog          #指定 thrift 地址
$ ./bin/repair.sh -t thrift://localhost:1260                        #指定 thrift 地址（不包含 catalog ）
$ ./bin/repair.sh -c local_catalog                                  #指定 catalog 名称
$ ./bin/repair.sh -t thrift://localhost:1260/local_catalog -m 4     #指定 thrift 地址和 4GB 内存大小
$ ./bin/repair.sh -h                                                #获取帮助
```

### Repair 工具使用
Repair 工具支持如下命令，您也可以通过在工具中输入 'help' 来获取帮助：
#### SHOW
`SHOW [ CATALOGS | DATABASES | TABLES ]`

用于展示 catalogs, databases 或 tables 列表
```
SHOW CATALOGS
SHOW DATABASES
SHOW TABLES
```

#### USE
`USE [ ${catalog_name} | ${database_name} ]`

使用 Catalog 或 Database
```
USE local_catalog
USE my_database
```

#### ANALYZE
`ANALYZE ${table_name}`

分析表以找出丢失的文件并提供修复选项，${table_name} 支持二元组和三元组
```
ANALYZE user
ANALYZE db.user
ANALYZE catalog.db.user
```
返回的结果为如下形式：
```
FILE_LOSE:
      hdfs://xxxx/xxxx/xxx
YOU CAN:
      FIND_BACK
      SYNC_METADATA
      ROLLBACK:
           597568753507019307
           512339827482937422
```

#### Repair
Repair 命令目前提供三种方式进行修复
- `REPAIR ${table_name} THROUGH FIND_BACK`：在回收站中找回文件
- `REPAIR ${table_name} THROUGH SYNC_METADATA`：在元数据中删除不存在的文件或同步元数据
- `REPAIR ${table_name} THROUGH ROLLBACK ${snapshot_id}`：回滚到不含丢失文件的版本
- `REPAIR ${table_name} THROUGH DROP_TABLE`：清除表的元数据
```
REPAIR user THROUGH FIND_BACK              
REPAIR user THROUGH SYNC_METADATA          
REPAIR user THROUGH ROLLBACK 597568753507019307
REPAIR user THROUGH DROP_TABLE
```

#### OPTIMIZE
`OPTIMIZE [ STOP | START ] ${table_name}`

停止或启动指定表的 Optimize 服务
```
OPTIMIZE STOP user
OPTIMIZE START user
```

#### TABLE
`TABLE ${table_name} [ REFRESH | SYNC_HIVE_METADATA | SYNC_HIVE_DATA | SYNC_HIVE_DATA_FORCE | DROP_METADATA ]`

对指定表进行操作，REFRESH:刷新表的文件缓存，SYNC_HIVE_METADATA: 同步 Hive 兼容表的元数据，SYNC_HIVE_DATA: 同步 Hive兼容表的数据，
DROP_METADATA: 删除 Arctic 表的元数据，删除完了数据文件还存在，用户可以从当前数据重构这张表.
```
REFRESH FILE_CACHE user 
```

#### QUIT
`QUIT`

退出 Repair 工具

> Tips: Repair 工具中所有关键字都是大小写不敏感的
> 
> 安全的恢复表的步骤是：'OPTIMIZE STOP -> TABLE REFRESH -> REPAIR -> TABLE REFRESH -> OPTIMIZE START'



