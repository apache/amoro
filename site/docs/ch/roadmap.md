# Roadmap

Arctic 预计每2-3个月发布一个大版本，每1个月发布一个小版本，每个版本都会有一个发布计划，发布计划会在 [Github Milestones](https://github.com/NetEase/arctic/milestones) 中发布。

## 2022年发布计划

| 版本                                                     | 发布日期          |
| --------------------------------------------------------| ---------------- |
| [0.3.1](https://github.com/NetEase/arctic/milestone/1)  | 2022.08          |
| [0.3.2](https://github.com/NetEase/arctic/milestone/2)  | 2022.09          |
| [0.4.0](https://github.com/NetEase/arctic/milestone/3)  | 2022.11          |
| [0.5.0](https://github.com/NetEase/arctic/milestone/4)  | 2022.12          |

## 开发中

| 特性                                                     | 发布版本        |   跟踪  |
| --------------------------------------------------------| -------------- |--------|
|Support Hive table                                       | 0.3.1          | [ARCTIC-38](https://github.com/NetEase/arctic/issues/38) |
|Use arctic table as a dimension table with Flink         | 0.3.1          | [ARCTIC-94](https://github.com/NetEase/arctic/issues/94) |
|Support Flink 1.15 version                               | 0.3.1          | [ARCTIC-166](https://github.com/NetEase/arctic/issues/166) |
|Support delete/update/merge into for Hive table          | 0.3.2          | [ARCTIC-173](https://github.com/NetEase/arctic/issues/173) |
|Support MOR with Flink Engine in runtime batch mode      | 0.3.2          | [ARCTIC-5](https://github.com/NetEase/arctic/issues/5) |
|Expose benchmark code and docker environment             | 0.3.2          | [ARCTIC-265](https://github.com/NetEase/arctic/issues/265) |
|Support manage already existing Iceberg table            | 0.4.0          | [ARCTIC-260](https://github.com/NetEase/arctic/issues/260) |
|Support Spark 2.3、2.4、3.2、3.3 version                  | 0.4.0          | [ARCTIC-261](https://github.com/NetEase/arctic/issues/261) |
|AMS terminal on Kyuubi                                   | 0.4.0          | [ARCTIC-262](https://github.com/NetEase/arctic/issues/262) |
|Unify ChangeStore and LogStore for Flink source          | 0.4.0          | [ARCTIC-264](https://github.com/NetEase/arctic/issues/264) |
|Support upsert partial fields (inline upsert) of arctic table with Flink  | 0.5.0 | [ARCTIC-256](https://github.com/NetEase/arctic/issues/256) |
|Support Pulsar as a new type for LogStore                | 0.5.0          | [ARCTIC-266](https://github.com/NetEase/arctic/issues/266) |
|Support RocketMQ as a new type for LogStore              | 0.5.0          | [ARCTIC-267](https://github.com/NetEase/arctic/issues/267) |

## 讨论中

* Support sort key and continuous z-order optimization
* Support aggregate key
* Support manage Hudi table
* Support materialized view
* Support table watermark to determine table freshness