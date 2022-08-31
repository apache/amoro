# Roadmap

Arctic 预计每2-3个月发布一个大版本，每1个月发布一个小版本，每个版本都会有一个发布计划，发布计划会在 [Github Milestones](https://github.com/NetEase/arctic/milestones) 中发布。

## 2022年发布计划

| 版本                                                     | 发布日期          |
| --------------------------------------------------------| ---------------- |
| [0.3.1](https://github.com/NetEase/arctic/milestone/1)  | 2022.08          |
| [0.3.2](https://github.com/NetEase/arctic/milestone/2)  | 2022.09          |
| [0.4](https://github.com/NetEase/arctic/milestone/3)    | 2022.11          |
| [0.5](https://github.com/NetEase/arctic/milestone/4)    | 2022.12          |

## 开发中

| 特性                                                     | 发布版本        |   跟踪  |
| --------------------------------------------------------| -------------- |--------|
|Using arctic table as a dimension table with flink       | 0.3.1          | [ARCTIC-94](https://github.com/NetEase/arctic/issues/94) |
|Supporting Flink 1.15 version                            | 0.3.1          | [ARCTIC-166](https://github.com/NetEase/arctic/issues/166) |
|Supporting delete/update/merge into for hive table       | 0.3.2          | [ARCTIC-173](https://github.com/NetEase/arctic/issues/173) |
|Expose benchmark code and docker environment             | 0.3.2          | [ARCTIC-265](https://github.com/NetEase/arctic/issues/265) |
|Supporting insert part of columns of arctic table with flink in streaming mode(streaming inline upsert) | 0.4 | [Arctic-256](https://github.com/NetEase/arctic/issues/256) |
|Supporting manage already existing iceberg table         | 0.4            | [ARCTIC-260](https://github.com/NetEase/arctic/issues/260) |
|Supporting Spark 2.3、2.4、3.2、3.3 version               | 0.4            | [ARCTIC-261](https://github.com/NetEase/arctic/issues/261) |
|AMS terminal on kyuubi                                   | 0.4            | [ARCTIC-262](https://github.com/NetEase/arctic/issues/262) |
|Unifying Changestore and Logstore for flink source       | 0.4            | [ARCTIC-264](https://github.com/NetEase/arctic/issues/264) |
|Supporting Pulsar as a new type for Logstore             | 0.5            | [ARCTIC-266](https://github.com/NetEase/arctic/issues/266) |
|Supporting RocketMQ as a new type for Logstore           | 0.5            | [ARCTIC-267](https://github.com/NetEase/arctic/issues/267) |

## 讨论中

* Supporting sort key and z-order
* Supporting aggregate key
* Supporting manage Hudi table
* Supporting materialized view