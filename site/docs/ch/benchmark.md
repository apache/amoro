# Lakehouse 性能对比测试报告

## 测试目标
此次测试目标是比较在数据库 CDC 持续流式摄取的场景下，各个数据湖 Format 的 OLAP benchmark 性能。

## 测试环境
### 机器环境

| 编号 | 操作系统 | cpu线程数 | 内存 | 磁盘类型 |
| ---- | -------- | --------- | ---- | -------- |
| 1    | CentOS 7 | 40        | 256  | SAS      |
| 2    | CentOS 7 | 40        | 256  | SAS      |
| 3    | CentOS 7 | 40        | 256  | SAS      |
| 4    | CentOS 7 | 40        | 256  | SAS      |
| 5    | CentOS 7 | 40        | 256  | SAS      |
| 6    | CentOS 7 | 40        | 256  | SAS      |

### 软件环境

Trino版本：380(用于iceberg和arctic的测试)

Presto: 274(用于Hudi的测试，Trino不支持Hudi的MOR实时查询)

Iceberg版本：0.13  （由于开源的iceberg在trino查询中的性能非常低，所以后面的测试均采用的是内部优化过后的版本，相关优化也提交社区见: https://github.com/apache/iceberg/issues/5245, https://github.com/apache/iceberg/pull/5264）

Arctic版本：0.3

1，2，3机器部署hadoop集群包括hive，yarn，hdfs，最终iceberg和arctic的数据是落在这三台机器上并且负责flink写入任务，mysql服务。

4，5，6机器部署查询Trino查询引擎，部署了3个节点每个节点设置64G内存。

## 测试方案

### CHbenchmark

本次测试规范基于CHbenchmark，CHbenchmark是一个集成了TPCC和TPCH的混合测试标准，测试负载整体上可分成两类：

- 基于TPC-C的5个OLTP型负载：NewOrder, Payment, OrderStatus, Delivery和StockLevel

- 基于TCP-H改写的22个OLAP型负载，其中由于Q15查询和视图相关，故此次测试舍弃了Q15

![OLTP AND OLAP](images/chbenchmark/OLTP-OLAP.png)

### 基于TPC-C的数据构造

基于TPC-C标准，本次测试在 MySQL中构造了原始数据，数据集总共包括12张表，其中TPC-C和TPC-H表的关系如下图所示：

![TPCC AND TPCH](images/chbenchmark/TPCC-TPCH.png)

此外，各个数据表数据规模间的联系如下表所示，其中w表示warehouse的数量。可以观察到中间的new_order,stock等表的数据规模受到warehouse数量的影响，
因此在测试过程中可以通过控制warehouse的数量来调整数据集的规模。

![chbenchmark](images/chbenchmark/chbenchmark.png)

在本次测试中，设置warehouse数量为100，MySQL数据库中对应的初始数据集大小约为10G。下表展示了初始数据集下各张表的数据记录数以及开启一小时TPC-C测试后各张表的数据记录变化情况。

| 表名称     | 初始数据集下表的记录数（行） | 开启一小时TPCC测试后表的记录数（行） |
| ---------- | ---------------------------- | ------------------------------------ |
| warehouse  | 100                          | 100                                  |
| item       | 100000                       | 100000                               |
| stock      | 10000000                     | 10000000                             |
| district   | 1000                         | 1000                                 |
| customer   | 3000000                      | 3000000                              |
| history    | 3000000                      | 3119285（+119285）                   |
| oorder     | 3000000                      | 3124142（+124142）                   |
| new_order  | 893709                       | 907373（+13664）                     |
| order_line | 29996774                     | 31252799（+1256025）                 |
| region     | 5                            | 5                                    |
| nation     | 62                           | 62                                   |
| supplier   | 1000                         | 1000                                 |

### 基于TPC-H的查询场景

TPC-H面向模拟的业务系统设计了22个Query，受限于篇幅，这里列举前3条Query语句作为参考：
```
-- SQL编号：query1 
 select l_returnflag, l_linestatus, sum(l_quantity) as sum_qty, 
        sum(l_extendedprice) as sum_base_price, 
        sum(l_extendedprice * (1 - l_discount)) as sum_disc_price,
        sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge,
        avg(l_quantity) as avg_qty, 
        avg(l_extendedprice) as avg_price, 
        avg(l_discount) as avg_disc,
        count(*) as count_order
        from lineitem
        where l_shipdate <= date '1998-12-01' - interval ? day
        group by l_returnflag, l_linestatus
        order by l_returnflag, l_linestatus;
        
 -- SQL编号：query2 
 select s_acctbal, s_name, n_name, p_partkey, p_mfgr,
        s_address, s_phone, s_comment from part, supplier, partsupp, nation, region
        where p_partkey = ps_partkey and s_suppkey = ps_suppkey
        and p_size = ? and p_type like ? and s_nationkey = n_nationkey and n_regionkey = r_regionkey 
        and r_name = ? and ps_supplycost = (select min(ps_supplycost)
        from partsupp, supplier, nation, region
        where p_partkey = ps_partkey and s_suppkey = ps_suppkey
        and s_nationkey = n_nationkey and n_regionkey = r_regionkey and r_name = ? )
        order by s_acctbal desc, n_name, s_name, p_partkey;
        
 -- SQL编号：query3 
 select l_orderkey,
        sum(l_extendedprice * (1 - l_discount)) as revenue,
        o_orderdate, o_shippriority
        from customer, orders, lineitem
        where c_mktsegment = ? 
        and c_custkey = o_custkey and l_orderkey = o_orderkey 
        and o_orderdate < date ? and l_shipdate > date ?
        group by l_orderkey, o_orderdate, o_shippriority
        order by revenue desc, o_orderdate;
```
### 测试整体流程

1. 基于TPC-C标准在MySQL中生成初始的数据集

2. 使用Flink-CDC将MySQL中的数据导入到Iceberg/Arctic/Hudi表中

3. 开启TPC-C测试，OLTP型负载会不断生成增量数据，同时Flink-CDC将这部分增量数据实时同步到Iceberg/Arctic表中

4. 基于TPC-H标准，通过Trino进行查询，并记录每个查询花费的时间以及所有查询的平均查询时间

![over design](images/chbenchmark/benchmark-over-design-cn.png)

## 测试结果

## 静态数据
![arctic iceberg static performence](images/chbenchmark/arctic-iceberg-100-warehouse-static-performence.png)

上图表示100个warehouse数据量下，纯静态数据没有更新的情况下，10个查询并发Arctic和Iceberg查询性能比较，通过上图可以看出每个Query的查询耗时非常接近。

## 动态持续查询性能

![Arctic Iceberg Hudi 100 warehouse performence with TPCC time](images/chbenchmark/Arctic-Iceberg-Hudi_100_warehouse_performence_with_TPCC_time.png)

在测试时间内TPCC持续进行，横轴表示的是查询的时间范围，纵轴表示21个查询（去除Q15）的平均时间，基础静态数据量为100个warehouse, 查询并发数是10，其中optimize表示Arctic有小文件合并的场景，no_optimize表示Arctic没有小文件合并的场景。 其中90分钟和120分钟没有Iceberg的数据是因为Iceberg已经无法跑出结果，打爆了Trino集群。

根据图上显示所知，在有optimize的情况下查询性能最好且不随同步数据时间的增长而增长，而no_optimize的场景下查询时间会逐渐增长，Iceberg的性能表现最差且随着时间增长查询时间大幅增长

具体query详情见下图：

![Arctic Iceberg Hudi 100 warehouse performence on TPCC 0-30 minutes](images/chbenchmark/Arctic-Iceberg-Hudi_100_warehouse_performence_on_TPCC_0-30_minutes.png)

![Arctic Iceberg Hudi 100 warehouse performence on TPCC 30-60 minutes](images/chbenchmark/Arctic-Iceberg-Hudi_100_warehouse_performence_on_TPCC_30-60_minutes.png)

![Arctic Iceberg Hudi 100 warehouse performence on TPCC 60-90 minutes](images/chbenchmark/Arctic-Iceberg-Hudi_100_warehouse_performence_on_TPCC_60-90_minutes.png)

![Arctic Iceberg Hudi 100 warehouse performence on TPCC 90-120 minutes](images/chbenchmark/Arctic-Iceberg-Hudi_100_warehouse_performence_on_TPCC_90-120_minutes.png)

# 小结

- 静态数据情况下Arctic和Iceberg的查询性能几乎相同，但是随着TPCC的进行，CDC数据的增多，Iceberg的查询性能急剧下降，没有optimize的Arctic缓慢下降，有optimize的Arctic性能几乎保持不变。
- hudi的性能因为写入任务自带optimize，所以查询性能也能很好的收敛，总体是优于不带optimize的Arctic，弱于带optimize的Arctic
- 静态数据情况下也测试了icebrg和delta lake的性能比对，当我们采用 delta 和 iceberg 开源版本默认的参数，对比下来 delta 确实惊艳，
  平均响应时间 delta 比 iceberg 快 1.4 倍左右，但我们注意到默认参数中有两个重要的区别：
  1. Trino 下 delta 和 iceberg 的默认压缩算法不同，trino写入 iceberg 默认的压缩算法是 ZSTD,而写入delta 默认的压缩算法是 SNAPPY， 
     ZSTD 具有比 SNAPPY 更高的压缩比，通过实际观测ZSTD压缩出来的文件大小只有 SNAPPY 大小的 60%，但是在查询时SNAPPY对于CPU更友好，查询效率更高.
  2. Delta 和 iceberg 默认 read-target-size 不同，delta 默认32m，iceberg 默认 128m，plan 阶段组装更小的文件可以在执行计划采用更多并发度，
     当然这会带来更多资源消耗，从实践上看 32m 的文件大小对响应时间敏感的数据分析而言或许是更好的选择。
  
  将 delta 和 iceberg 的压缩算法设置相同，read-target-size 设置为 32m，实测下来 tpch 平均响应时间不再有差别，从原理上看，排除占比极低的元数据读取和 plan 时间，
  在相同的配置下，benchmark 测试的主要是 parquet 这类文件格式的 IO 性能，没有差异是比较合理的。
   