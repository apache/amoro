# Upgrading Arctic Flink And Compatibility

Arctic Flink 任务通常需要长时间地运行。而 Arctic Flink 版本需要不断迭代升级优化，如 Bug 修复、Feature 支持、API 优化等。

本文档描述了如何将线上运行的 Arctic Flink 任务迁移至新的版本，及版本间的兼容性。

## API 废弃规则
注释 Deprecated 的类、方法、变量等代码，将保留三个大的 Arctic 版本，并在第四个版本删除。请保证在此期间升级至最新的 API。

## 版本兼容及迁移

- 0.4.1 版本开始对 Logstore 的 API 进行重构，迁移至 Flink FLIP-27 的新接口。对于使用 Arctic Flink 版本 <= 0.4.0 的读 Arctic Log-store（Kafka） 任务，在使用旧的 checkpoint 状态升级到新的版本时，需注意：
    1. 需要设置 Sql Hint 参数来使用废弃的 API 运行：log-store.kafka.compatible.enabled = true，见 [读 Logstore](flink-dml.md#Logstore 实时数据)。否则可能出现部分数据重复的现象。
    2. 如有条件，可以升级至新的 API 来运行。在使用旧版本 Arctic Flink 的任务运行时，将 Kafka 的数据断流一小断时间，即在一段时间内，不往 Logstore Kafka 中写入数据来保证任务已经成功 checkpoint 并完成 Kafka Offset 的提交。然后停止任务，升级至 Arctic Flink 新的版本，从前面的状态中恢复任务。之后再恢复上游 Logstore Kafka 的正常写入。