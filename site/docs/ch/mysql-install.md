### 物理机部署mysql



>  mysql链接

```
 http://nos-yq.126.net/innosql-release/mysql-5.7.20-v3e-linux-x86_64.tar.gz

```



#### 步骤

```shell
# mysql mgr版本必须要有3台或者3台以上奇数节点
# 先设置配置文件,my.cnf 参考下面的demo
# 配置文件中 需要修改的地方

server-id                                            = 1  # 每个机器需要不一样
loose-group_replication_group_seeds                  = 192.168.0.4:34901,192.168.0.5:34901,192.168.0.6:34901 #按实际修改
loose-group_replication_ip_whitelist                 = 192.168.0.0/24  # 按实际修改
loose-group_replication_local_address                = 192.168.0.4:34901   # 本地ip
report_host                                          = 192.168.0.4   # 本地ip





cd /tmp/
wget  http://nos-yq.126.net/innosql-release/mysql-5.7.20-v3e-linux-x86_64.tar.gz
tar -xzvf mysql-5.7.20-v3e-linux-x86_64.tar.gz
groupadd -r mysql && useradd -r -g mysql mysql 
cp -r /tmp/mysql-5.7.20-v3e-linux-x86_64 /home/mysql 
ln -s /home/mysql /usr/local/mysql
mkdir /ebs
mkdir -p /ebs/mysql_data
mkdir -p /ebs/tmp_dir
chown -R mysql.mysql /ebs/mysql_data
chown -R mysql.mysql /ebs/tmp_dir

# 初始化mysql
/usr/local/mysql/bin/mysqld  --defaults-file=/ebs/config/my.cnf --initialize-insecure
/usr/local/mysql/bin/mysqld  --defaults-file=/ebs/config/my.cnf --user=mysql & 

# 登录mysql，设置组复制信息
mycli -S /tmp/mysql.sock

INSTALL PLUGIN group_replication SONAME 'group_replication.so';
SET SQL_LOG_BIN=0; CREATE USER rpl_user@'%'; GRANT REPLICATION SLAVE ON *.* TO rpl_user@'%' IDENTIFIED BY 'rpl_pass'; SET SQL_LOG_BIN=1; CHANGE MASTER TO MASTER_USER='rpl_user', MASTER_PASSWORD='rpl_pass' FOR CHANNEL 'group_replication_recovery';

# 以上操作 3台节点都相同



# 选一个节点做主节点，开启group
SET GLOBAL group_replication_bootstrap_group=ON;
START GROUP_REPLICATION;
SET GLOBAL group_replication_bootstrap_group=OFF;

# 其他节点
START GROUP_REPLICATION;


```



#### 部署成功后检查

```shell
# 查询复制情况
use performance_schema;
select * from replication_group_members;
```

看到对应的节点都在同一个组中，部署成功。

![image-20201230145036967](https://notepic.nos-eastchina1.126.net/image-20201230145036967.png)



#### 参考 config

```shell
[mysqld]
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




