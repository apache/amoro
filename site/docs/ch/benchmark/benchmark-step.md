# Ch-benchmark 性能测试流程

## Docker 部署

Benchmark 提供了一套 Docker 容器，可以帮助用户跑单机版测试熟悉流程。工程地址 [benchmark-url](https://github.com/NetEase/lakehouse-benchmark)。
具体说明见[benchmark-docker](https://github.com/NetEase/lakehouse-benchmark/tree/master/docker/benchmark)

## 物理机部署

按照下面的表格说明依次部署 benchmark 依赖组件：

| 组件  | 说明  |
|:----------|:----------|
| MySQL    | MySQL 用于生产 TPCC 数据然后通过同步工具同步到 Arctic，Hudi，Iceberg 等数据湖中。本文档使用5.7，安装方式参考附录    |
| Hadoop    | Hadoop体系包含 Hdfs,Yarn,Hive。安装方式有很多，可以选择 [Ambari](https://ambari.apache.org/) 安装    |
| Trino    | Trino 用于执行 Benchmark 中的 TPCH 查询，当前使用 380 版本，部署见：[Trino-Install](https://trino.io/docs/current/installation/deployment.html)    |
| Trino-Arctic    | 在 Trino 中查询 Arctic 表需要在 trino 中安装配置 Arctic 插件：[Arctic-Plugin-Install](https://arctic.netease.com/ch/mpp/trino/)    |
| Trino-Iceberg    | 如果需要测试 Iceberg 需要配置 Iceberg 插件：[Iceberg-Plugin-Install](https://trino.io/docs/current/connector/iceberg.html)    |
| Presto    | Presto 也是用来执行 Benchmark 中的TPCH 查询，他和 Trino 最初是同一个团队开发。 Hudi 的 rt 表的查询也就是实时 MOR 查询目前不支持 Trino，只支持 Presto,如果需要测试 Hudi 建议安装配置 Presto    |
| Presto-Hudi    | 在 Presto 中安装配置 Hudi 插件：[Hudi-Plugin-Install](https://prestodb.io/docs/current/connector/hudi.html)    |
| AMS    | Arctic 的管理，当前使用0.4版本，如果需要测试 Arctic 需要安装此服务，并且按照 [Managing optimizers](../guides/managing-optimizers.md) 配置启动 optimizers   |
| data-lake-benchmark    | Benchmark 的核心工具，负责生成 TPCC 数据进 Mysql 和通过 Trino 执行 AP 标准查询语句，最后输出 Benchmark 结果，该工具需要 jdk17：[Oltpbench-Install](https://github.com/NetEase/data-lake-benchmark#readme)    |
| lakehouse-benchmark-ingestion    | 基于 Flink-CDC 实现的数据同步工具，能够将数据库中的数据实时同步到数据湖，支持 Iceberg、Arctic、Hudi，使用说明请参考：[benchmark-ingestion-install](https://github.com/NetEase/lakehouse-benchmark-ingestion)    |


## Benchmark 执行流程
### Arctic 与 Iceberg 测试
1. 把 Mysql 信息配置进 data-lake-benchmark 的 config/mysql/sample_chbenchmark_config.xml 文件中。其中 "scalefactor" 表示的 warehouse 数量用于控制整体数据量的，一般选择10或者100。
2. 执行 data-lake-benchmark 命令往 Mysql 生成全量数据，命令如下：
  ```
  java -jar lakehouse-benchmark.jar -b tpcc,chbenchmark -c config/mysql/sample_chbenchmark_config.xml --create=true --load=true
  ```
3. 执行完成后 MySQL 的指定数据库下面就能看到这 12 张表。
4. 开启数据同步程序，将数据库的数据实时同步到数据湖：
    - 下载 [lakehouse-benchmark-ingestion](https://github.com/NetEase/lakehouse-benchmark-ingestion) 项目代码，参考该项目的快速开始部分，构建项目得到 lakehouse-benchmark-ingestion-1.0-SNAPSHOT.jar 和 conf 目录
    - 修改 conf 目录下的 ingestion-conf.yaml ，填写配置项信息
    - 通过：
      ```
      java -cp lakehouse-benchmark-ingestion-1.0-SNAPSHOT.jar com.netease.arctic.benchmark.ingestion.MainRunner -confDir [confDir] -sinkType [arctic/iceberg/hudi] -sinkDatabase [dbName]
      ```
      命令启动数据同步工具。命令行的参数说明请参考[该项目的说明文档](https://github.com/NetEase/lakehouse-benchmark-ingestion)
    - 通过 Flink Web UI ，通过 source 算子的 Records Sent 指标观察数据同步的情况，当该指标不再增加时，表示全量数据同步完成
5. 当全量同步完成，可以用 data-lake-benchmark 工具进行全量静态数据的测试。首先把 Trino 信息配置进 data-lake-benchmark 的 config/trino/trino_chbenchmark_config.xml中,主要是 url 要改成当前 Trino 的地址，
  还有 works.work.time 参数表示 Benchmark 运行时间，单位是秒，全量测试时间可以短一点10分钟左右就行。命令如下：
  ```
  java -jar lakehouse-benchmark.jar -b chbenchmarkForTrino -c config/trino/trino_chbenchmark_config.xml --create=false --load=false --execute=true
  ```
6. 再次启动 data-lake-benchmark 程序向 Mysql 里面生产增量数据，这些数据会通过已经运行的数据同步工具源源不断写入 Arctic，
  需要修改 config/mysql/sample_chbenchmark_config.xml配置文件中的works.work.time参数控制生成时间，一般半小时，命令如下：
  ```
  java -jar lakehouse-benchmark.jar -b tpcc,chbenchmark -c config/mysql/sample_chbenchmark_config.xml --execute=true -s 5
  ```
7. 在mysql生产增量数据的同时，启动 data-lake-benchmark 的 TPCH 性能测试命令，这样可以测试动态带有删除更新数据的性能：
  ```
  java -jar lakehouse-benchmark.jar -b chbenchmarkForTrino -c config/trino/trino_chbenchmark_config.xml --create=false --load=false --execute=true
  ```
8. 重复上两个步骤就可以得到增量30分钟，60分钟，90分钟，120分钟的性能测试报告。

### Hudi 测试
上述测试流程在测试 Hudi 的时候需要做一些变动:

1. 首先 Hudi 的 rt 表也就是走 MOR 读取的表只有 Presto 支持，所以需要用 Presto 作为最终的 ap 引擎，
   需要使用 config/trino/presto_chbenchmark_config.xml 配置

2. Hudi 使用 Hive 的元数据的时候需要额外添加一些依赖： [Hudi Hive sync](https://hudi.apache.org/docs/syncing_metastore)
  

3. Hudi 的表名带有后缀，ro 表示读优化表，rt 表示全量表，可以在执行 data-lake-benchmark 程序之前设置环境变量如：
   ```
   export tpcc_name_suffix=_rt
   ```
   这样所有实际执行查询的表都会带上 _rt 后缀

### 测试结果
data-lake-benchmark 跑完以后会生成一个 results 目录，测试结果都在里面，关注两个文件：
- .summary.json 文件，这里面的 Average Latency 项显示的是本次性能测试的平均相应时间
- .statistic.csv 文件，里面记录了每个 Query 类型的最大，最小，平均耗时

## 附录
### MySQL安装

MySQL 下载部署

```shell
cd /tmp/
wget  http://nos-yq.126.net/innosql-release/mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz
tar -xzvf mysql-5.7.20-linux-glibc2.12-x86_64.tar.gz
groupadd -r mysql && useradd -r -g mysql mysql 
cp -r /tmp/mysql-5.7.20-linux-glibc2.12-x86_64/home/mysql 
ln -s /home/mysql /usr/local/mysql
mkdir /ebs
mkdir -p /ebs/mysql_data
mkdir -p /ebs/tmp_dir
chown -R mysql.mysql /ebs/mysql_data
chown -R mysql.mysql /ebs/tmp_dir
```

初始化 MySQL

```
/usr/local/mysql/bin/mysqld  --defaults-file=/ebs/config/my.cnf --initialize-insecure
/usr/local/mysql/bin/mysqld  --defaults-file=/ebs/config/my.cnf --user=mysql & 
```

登录 MySQL，设置组复制信息

```
mycli -S /tmp/mysql.sock

INSTALL PLUGIN group_replication SONAME 'group_replication.so';
SET SQL_LOG_BIN=0; CREATE USER rpl_user@'%'; GRANT REPLICATION SLAVE ON *.* TO rpl_user@'%' IDENTIFIED BY 'rpl_pass'; SET SQL_LOG_BIN=1; CHANGE MASTER TO MASTER_USER='rpl_user', MASTER_PASSWORD='rpl_pass' FOR CHANNEL 'group_replication_recovery'
```


MySQL config 参考

```shell
[mysqld]
server-id                                            = 1
auto_increment_increment                             = 1
auto_increment_offset                                = 1
autocommit                                           = ON
back_log                                             = 3000
basedir                                              = /usr/local/mysql
binlog_cache_size                                    = 65536
binlog_checksum                                      = NONE
binlog_format                                        = ROW
binlog_gtid_simple_recovery                          = TRUE
binlog_row_image                                     = full
binlog_rows_query_log_events                         = ON
binlog_stmt_cache_size                               = 32768
character_set_server                                 = utf8mb4
connect_timeout                                      = 10
datadir                                              = /ebs/mysql_data
default-time_zone                                    = '+8:00'
div_precision_increment                              = 4
enforce_gtid_consistency                             = ON
eq_range_index_dive_limit                            = 200
event_scheduler                                      = ON
expire_logs_days                                     = 7
general_log                                          = OFF
group_concat_max_len                                 = 1024
gtid_mode                                            = ON
innodb_adaptive_flushing                             = ON
innodb_adaptive_hash_index                           = OFF
innodb_autoextend_increment                          = 128
innodb_autoinc_lock_mode                             = 2
innodb_buffer_pool_dump_at_shutdown                  = ON
innodb_buffer_pool_dump_pct                          = 40
innodb_buffer_pool_instances                         = 4
innodb_buffer_pool_load_at_startup                   = ON
innodb_change_buffering                              = all
innodb_checksum_algorithm                            = crc32
innodb_concurrency_tickets                           = 5000
innodb_data_file_path                                = ibdata1:512M:autoextend
innodb_file_format                                   = Barracuda
innodb_file_format_max                               = Barracuda
innodb_flush_log_at_trx_commit                       = 1
innodb_flush_method                                  = O_DIRECT
innodb_flush_neighbors                               = 0
innodb_ft_max_token_size                             = 84
innodb_ft_min_token_size                             = 3
innodb_io_capacity                                   = 4000
innodb_io_capacity_max                               = 8000
innodb_large_prefix                                  = ON
innodb_lock_wait_timeout                             = 5
innodb_log_buffer_size                               = 8388608
innodb_log_file_size                                 = 2147483648
innodb_log_files_in_group                            = 4
innodb_lru_scan_depth                                = 1024
innodb_max_dirty_pages_pct                           = 75
innodb_old_blocks_pct                                = 37
innodb_old_blocks_time                               = 1000
innodb_online_alter_log_max_size                     = 134217728
innodb_open_files                                    = 4096
innodb_page_cleaners                                 = 4
innodb_print_all_deadlocks                           = ON
innodb_purge_batch_size                              = 300
innodb_purge_threads                                 = 4
innodb_read_ahead_threshold                          = 56
innodb_read_io_threads                               = 4
innodb_rollback_on_timeout                           = OFF
innodb_stats_method                                  = nulls_equal
innodb_stats_on_metadata                             = OFF
innodb_stats_sample_pages                            = 64
innodb_strict_mode                                   = ON
innodb_table_locks                                   = ON
innodb_temp_data_file_path                           = ibtmp1:12M:autoextend:max:115200M
innodb_thread_concurrency                            = 0
innodb_thread_sleep_delay                            = 10000
innodb_write_io_threads                              = 4
interactive_timeout                                  = 1800
log_bin                                              = mysql-bin.log
log_bin_trust_function_creators                      = OFF
log_bin_use_v1_row_events                            = OFF
log_error                                            = mysql-err.log
log_output                                           = FILE
log_queries_not_using_indexes                        = OFF
log_slave_updates                                    = ON
log_slow_admin_statements                            = ON
log_slow_slave_statements                            = ON
log_throttle_queries_not_using_indexes               = 0
log_timestamps                                       = SYSTEM
long_query_time                                      = 0.1
loose-group_replication_bootstrap_group              = OFF
loose-group_replication_flow_control_mode            = QUOTA
loose-group_replication_member_weight                = 30
loose-group_replication_single_primary_mode          = ON
loose-group_replication_start_on_boot                = OFF
loose-group_replication_unreachable_majority_timeout = 10
loose-group_replication_xcom_cache_size_limit        = 1000000000
loose-rpl_semi_sync_master_commit_after_ack          = 1
loose-rpl_semi_sync_master_enabled                   = 1
loose-rpl_semi_sync_master_keepsyncrepl              = false
loose-rpl_semi_sync_master_timeout                   = 100
loose-rpl_semi_sync_master_trysyncrepl               = false
loose-rpl_semi_sync_slave_enabled                    = 1
loose-statistics_exclude_db                          = `mysql;performance_schema;information_schema;test;PERFORMANCE_SCHEMA;INFORMATION_SCHEMA`
loose-statistics_expire_duration                     = 7
loose-statistics_plugin_status                       = 0
lower_case_table_names                               = 0
master_info_repository                               = TABLE
max_allowed_packet                                   = 16777216
max_connect_errors                                   = 10000
max_length_for_sort_data                             = 1024
max_prepared_stmt_count                              = 16382
max_write_lock_count                                 = 102400
min_examined_row_limit                               = 100
net_read_timeout                                     = 30
net_retry_count                                      = 10
net_write_timeout                                    = 60
open_files_limit                                     = 65534
performance-schema-instrument                        = 'memory/%=COUNTED'
performance_schema                                   = ON
pid_file                                             = mysql.pid
port                                                 = 3331
query_alloc_block_size                               = 8192
query_cache_limit                                    = 1048576
query_cache_size                                     = 3145728
query_cache_type                                     = 0
query_prealloc_size                                  = 8192
relay-log                                            = mysqld-relay-bin
relay_log_info_repository                            = TABLE
relay_log_recovery                                   = ON
secure_file_priv                                     = 
show_compatibility_56                                = ON
slave-parallel-type                                  = LOGICAL_CLOCK
slave-parallel-workers                               = 16
slave_preserve_commit_order                          = ON
slave_rows_search_algorithms                         = INDEX_SCAN,HASH_SCAN
slow_launch_time                                     = 2
slow_query_log                                       = ON
slow_query_log_file                                  = mysql-slow.log
slow_query_type                                      = 1
socket                                               = /tmp/mysql.sock
sql_mode                                             = 
sync_binlog                                          = 1
table_definition_cache                               = 2048
table_open_cache                                     = 2048
tmp_table_size                                       = 2097152
tmpdir                                               = /ebs/tmp_dir
transaction-isolation                                = READ-COMMITTED
transaction_write_set_extraction                     = XXHASH64
user                                                 = mysql
user_list_string                                     = rdsadmin@localhost,rdsadmin@127.0.0.1
wait_timeout                                         = 1800
max_connections                                      = 2730
innodb_buffer_pool_size                              = 13743895320
loose-group_replication_group_name                   = 44d67452-8f7c-11ea-b38d-3cfdfea21f00
server-id                                            = 1
loose-group_replication_group_seeds                  = 192.168.0.4:34901,192.168.0.5:34901,192.168.0.6:34901
loose-group_replication_ip_whitelist                 = 192.168.0.0/24
loose-group_replication_local_address                = 192.168.0.4:34901
report_host                                          = 192.168.0.4
core-file
innodb_file_per_table
skip-name-resolve
skip-slave-start
skip_external_locking
```
