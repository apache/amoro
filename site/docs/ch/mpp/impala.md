# Impala
Impala 目前仅支持 Arctic 的 Mixed Hive format 和 Iceberg format 两种 format。

## Mixed Hive format
Arctic 的 Mixed hive format 是一种兼容 Hive 的 format， 实时写入 Mixed hive format 数据会通过 optimize 写入到 Hive。 所以 Impala 可以通过
查询 Hive 方式查询 Mixed hive format 的数据。

## Iceberg format
Native Iceberg 是原生的 Iceberg，Impala 使用原生的 Iceberg 见 [Impala-Iceberg](https://impala.apache.org/docs/build/html/topics/impala_iceberg.html)
