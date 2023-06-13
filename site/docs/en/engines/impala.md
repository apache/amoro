# Impala
Impala currently only supports two formats for Arctic: Mixed Hive format and Iceberg format.

## Mixed Hive format
Arctic's Mixed Hive format is a format compatible with Hive. Real-time writing of data in the Mixed Hive format will be optimized and written to Hive. 
Therefore, Impala can query Mixed Hive format data through Hive's query methods.

## Iceberg format
Native Iceberg refers to the original Iceberg format，[Impala-use-native-iceberg](https://impala.apache.org/docs/build/html/topics/impala_iceberg.html)
