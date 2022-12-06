## Table fressness

数据的新鲜度代表了时效性，在很多论述中，新鲜度是数据质量的重要指标之一，在传统的离线数仓中，更高的成本意味着更好的性能，成本和性能之间是典型的双元悖论，而在高新鲜度的流式数仓中，海量的小文件和更新数据会带来性能下降，新鲜度越高，对性能的影响越大，为了达到满足要求的性能，用户需要支出更高的成本，所以对流式湖仓而言，数据新鲜度，查询性能，成本构成了三元悖论：

<left>
![Fressness, cost and performance](../images/concepts/fressness_cost_performance.png){:height="50%" width="50%"}
</left>

Arctic 通过 AMS 的管理功能以及 Self-optimizing 机制为用户提供三元悖论的调和方案，不同于传统数仓，湖仓表会应用于各种各样的 data pipeline，AI，BI 的场景，怎样度量数据的新鲜度对于数据开发者，分析师以及管理员来说都直观重要，为此 Arctic 学习流计算采用 watermark 的概念来度量表的新鲜度。

## Table watermark

在 Mixed streaming format 中，通过 table watermark 来度量数据的新鲜度。

严格来说，Table watermark 用于描述表的写入进度，具体来说它是表上的一个类型为时间戳的属性，意为小于这个时间戳的数据都已经写入到表中，它一般用来观察表的写入进度，也可以作为下游批量计算任务的触发指标。

Mixed streaming format 通过下面的参数来配置 watermark：

```sql
  'table.event-time-field ' = 'op_time',
  'table.watermark-allowed-lateness-second' = '60'
```

上面的例子中将 op_time 设置为表的事件时间字段，在数据写入时会用写入数据的 op_time 来计算表的水位，同时为了应对写入乱序的问题，设置在计算 watermark 时允许的数据迟到时间为一分钟。不同于流计算，event_time 值小于 watermark 的数据不会被拒绝写入，但也不会影响 watermark 的推进。

可以在 AMS Dashboard 的表详情中看到表当前的水位，也可以在 Terminal 中输入下面的 SQL 来查询表的水位：

```SQL
SHOW TBLPROPERTIES test_db.test_log_store ('watermark.table');
```

也可以通过下面的方式查询表 basestore 的 table watermark，结合 Hive 或 Iceberg 的原生读可以更加灵活：

```SQL
SHOW TBLPROPERTIES test_db.test_log_store ('watermark.base');
```

通过 [Managing tables](../guides/managing-tables.md) 详细了解 watermark 的使用方式。