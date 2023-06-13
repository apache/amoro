
Arctic supports multiple Table Formats, if using the Iceberg Format, the runtime jars provided by [Iceberg community](https://iceberg.apache.org/releases/) can be used directly. If using Mixed Format, Arctic provides implementation packages for multiple engines, and the supported engines are as follows.

### Compatibility Matrix for Mixed Format

| Engine        |     Version       | Batch Read  | Batch Write | Batch Overwrite | Streaming Read | Streaming Write | Create Table | Alter Table |
|---------------|-------------------|-------------|-------------|-----------------|----------------|-----------------|--------------|-------------|
| Flink         | 1.12,1.14,1.15    |   &#x2714   |   &#x2714   |       &#x2716   |      &#x2714   |       &#x2714   |    &#x2714   |   &#x2716   |
| Spark         | 3.1-3.3           |   &#x2714   |   &#x2714   |       &#x2714   |      &#x2716   |       &#x2716   |    &#x2714   |   &#x2714   |
| Hive          | ?                 |   &#x2714   |   &#x2716   |       &#x2714   |      &#x2716   |       &#x2716   |    &#x2716   |   &#x2714   |
| Trino         | 406               |   &#x2714   |   &#x2716   |       &#x2716   |      &#x2716   |       &#x2716   |    &#x2716   |   &#x2716   |
| Impala        | ?                 |   &#x2714   |   &#x2716   |       &#x2716   |      &#x2716   |       &#x2716   |    &#x2716   |   &#x2716   |