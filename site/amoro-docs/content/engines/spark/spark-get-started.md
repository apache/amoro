---
title: "Spark Getting Started"
url: spark-getting-started
aliases:
    - "spark/getting-started"
menu:
    main:
        parent: Spark
        weight: 100
---
<!--
 - Licensed to the Apache Software Foundation (ASF) under one or more
 - contributor license agreements.  See the NOTICE file distributed with
 - this work for additional information regarding copyright ownership.
 - The ASF licenses this file to You under the Apache License, Version 2.0
 - (the "License"); you may not use this file except in compliance with
 - the License.  You may obtain a copy of the License at
 -
 -   http://www.apache.org/licenses/LICENSE-2.0
 -
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
# Spark Getting Started
# Iceberg Format

The Iceberg Format can be accessed using the Connector provided by Iceberg.
Refer to the documentation at [Iceberg Spark Connector](https://iceberg.apache.org/docs/latest/getting-started/)
for more information.

# Paimon Format

The Paimon Format can be accessed using the Connector provided by Paimon.
Refer to the documentation at [Paimon Spark Connector](https://paimon.apache.org/docs/master/engines/spark3/)
for more information.

# Mixed Format


To use Amoro in a Spark shell, use the --packages option:

```bash
spark-shell --packages org.apache.amoro:amoro-mixed-spark-3.3-runtime:0.7.0
```

> If you want to include the connector in your Spark installation, add the `amoro-mixed-spark-3.3-runtime` Jar to
> Spark's `jars` folder.

## Adding catalogs

```
${SPARK_HOME}/bin/spark-sql \
    --conf spark.sql.extensions=org.apache.amoro.spark.MixedFormatSparkExtensions \
    --conf spark.sql.catalog.local_catalog=org.apache.amoro.spark.MixedFormatSparkCatalog \
    --conf spark.sql.catalog.local_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME}
```

> Amoro manages the Catalog through AMS, and Spark catalog needs to be mapped to Amoro Catalog via URL,
> in the following format:
> `thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME}`,
> The mixed-format-spark-connector will automatically download the Hadoop site configuration file through
> the thrift protocol for accessing the HDFS cluster

>
> The AMS_PORT is the port number of the AMS service's thrift API interface, with a default value of 1260
> The AMS_CATALOG_NAME is the name of the Catalog you want to access on AMS.

Regarding detailed configurations for Spark, please refer to [Spark Configurations](../spark-configuration/)


## Creating a table

In Spark SQL command line, you can execute a create table command using the `CREATE TABLE` statement.

Before executing a create table operation, please make sure to create the `database` first.

```
-- switch to mixed catalog defined in spark conf
use local_catalog;

-- create databsae first 
create database if not exists test_db;
```

Then switch to the newly created database and perform the create table operation.

```
use test_db;

-- create a table with 3 columns
create table test1 (id int, data string, ts timestamp) using mixed_iceberg;

-- create a table with hidden partition
create table test2 (id int, data string, ts timestamp) using mixed_iceberg partitioned by (days(ts));

-- create a table with hidden partition and primary key
create table test3 (id int, data string, ts timestamp, primary key(id)) using mixed_iceberg partitioned by (days(ts));
```

For more information on Spark DDL related to tables, please refer to [Spark DDL](../spark-ddl/)

## Writing to the table

If you are using Spark SQL, you can use the `INSERT OVERWRITE` or `INSERT` SQL statement to write data to an Amoro table.

```
-- insert values into unkeyed table
insert into test2 values 
( 1, "aaa", timestamp('2022-1-1 00:00:00')),
( 2, "bbb", timestamp('2022-1-2 00:00:00')),
( 3, "bbb", timestamp('2022-1-3 00:00:00'));

-- dynamic overwrite table 
insert overwrite test3 values 
( 1, "aaa", timestamp('2022-1-1 00:00:00')),
( 2, "bbb", timestamp('2022-1-2 00:00:00')),
( 3, "bbb", timestamp('2022-1-3 00:00:00'));
```


> If you are using Static Overwrite, you cannot define transforms on partition fields.

Alternatively, you can use the DataFrame API to write data to an Amoro table within a JAR job.

``` 
val df = spark.read().load("/path-to-table")
df.writeTo('test_db.table1').overwritePartitions()
```

For more information on writing to tables, please refer to [Spark Writes](../spark-writes/)

## Reading from the table

To query the table using `SELECT` SQL statements

``` 
select count(1) as count, data 
from test2 
group by data;
```

For table with primary keys defined, you can query on `ChangeStore` by `.change`

``` 
select count(1) as count, data
from test_db.test3.change group by data;
```


For more information on reading from tables, please refer to [Spark Queries](../spark-queries/)
