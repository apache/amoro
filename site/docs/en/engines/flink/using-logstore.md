Due to the limitations of traditional offline data warehouse architectures in supporting real-time business needs, real-time data warehousing has experienced rapid evolution in recent years. In the architecture of real-time data warehousing, Apache Kafka is often used as the storage system for real-time data. However, this also brings about the issue of data disconnection between offline data warehouses.

Developers often need to pay attention to data stored in HDFS as well as data in Kafka, which increases the complexity of business development. Therefore, Arctic proposes the addition of an optional parameter, "LogStore enabled" (`log-store.enabled`), to the table configuration. This allows for retrieving data with sub-second and minute-level latency by operating on a single table while ensuring the eventual consistency of data from both sources.

## overview

### Prerequisites for using LogStore

|	Flink    |	Kafka    |  Pulsar	|	
|-----	|-----	|-----	|			
|	Flink 1.12	|	&#x2714	|	&#x2714	|
|	Flink 1.14	|	&#x2714	|	&#x2716	|
|	Flink 1.15	|	&#x2714	|	&#x2716	|



When creating an Arctic table, LogStore needs to be enabled.

- You can create a table after selecting a specific Catalog on the Arctic [Dashboard](http://localhost:1630) - Terminal page

```sql

create table db.log_table (

id int,

name string,

ts timestamp,

primary key (id)

) using arctic

tblproperties (

“log-store.enabled“ = “true“,

“log-store.topic“=“topic_log_test“,

“log-store.address“=“localhost:9092“

);

```

- You can also use Flink SQL to create tables in Flink-SQL-Client

```sql

-- First use the use catalog command to switch to the arctic catalog.

create table db.log_table (

id int,

name string,
 

ts timestamp,

primary key (id) not enforced

) with (

'log-store.enabled' = 'true',

'log-store.topic'='topic_log_test',

'log-store.address'='localhost:9092');

```

### Double write LogStore and FileStore

![Introduce](../../images/flink/double-write.png){:height=“70%“ width=“70%“}

Arctic Connector writes data to LogStore and ChangeStore at the same time through double-write operations, without opening Kafka transactions to ensure data consistency between the two, because opening transactions will bring a few minutes of delay to downstream tasks (the specific delay time depends on upstream tasks checkpoint interval).

When an upstream task restarts or a failover occurs, it causes redundant data to be sent to the LogStore. Downstream tasks will identify and roll back this part of redundant data to ensure eventual data consistency.

For the configuration of LogStore, please refer to [here](../../configurations.md#logstore-configurations), and for the configuration of consuming Kafka, please refer to [here](flink-dml.md#logstore).

> Currently only the Apache Flink engine implements the dual-write LogStore and FileStore.

### Enable consistent read

```sql

select * from arctic.db.arctic

/*+ OPTIONS('arctic.read.mode'='log','log-store.consistency-guarantee.enabled'='true') */;

-- Or enable consistent read when creating the table

create table catalog.db.arctic (

...

) WITH (

'log-store.enabled' = 'true',

'log-store.topic'='topic_log_test',

'log-store.address'='localhost:9092',

'log-store.consistency-guarantee.enabled'='true'

);

```

### Hint Options

|Key|Default|Type|Required| Description                                                                                                                                                                                                                                                                                |
|--- |--- |--- |--- |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|log.consumer.changelog.modes| all-kinds | String |No| The RowKind that will be generated when reading log data Type, supported: all-kinds, append-only. <br>all-kinds: will read the cdc data, including +I/-D/-U/+U; <br>append-only: Only Insert data will be generated, and this configuration is recommended when reading without a primary key. |

### Limitation

- When the consistency guarantee is enabled for downstream tasks, the Cumulate Window Agg Operator cannot be included, because this operator cannot handle Update_before/Delete data.

- The downstream storage cannot process Delete data, for example, the downstream task is an ETL task, only Append data to the HDFS.
