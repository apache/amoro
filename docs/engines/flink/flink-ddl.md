---
title: "Flink DDL"
url: flink-ddl
aliases:
    - "flink/ddl"
menu:
    main:
        parent: Flink
        weight: 200
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
# Flink DDL

## Create catalogs

### Flink SQL
The following statement can be executed to create a Flink catalog:

```sql
CREATE CATALOG <catalog_name> WITH (
  'type'='mixed_iceberg',
  `<config_key>`=`<config_value>`
); 
```

Where `<catalog_name>` is the user-defined name of the Flink catalog, and `<config_key>`=`<config_value>` has the following configurations:

| Key              | Default Value | Type    | Required | Description                                                                                                                                                                                                                              |
|------------------|---------------|---------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type             | N/A           | String  | Yes      | Catalog type, validate values are mixed_iceberg and mixed_hive                                                                                                                                                                           |
| metastore.url    | (none)        | String  | No       | The URL for Amoro Metastore is thrift://`<ip>`:`<port>`/`<catalog_name_in_metastore>`.<br>If high availability is enabled for AMS, it can also be specified in the form of zookeeper://{zookeeper-server}/{cluster-name}/{catalog-name}. |
| default-database | default       | String  | No       | The default database to use                                                                                                                                                                                                              |
| property-version | 1             | Integer | No       | Catalog properties version, this option is for future backward compatibility                                                                                                                                                             |
| catalog-type     | N/A           | String  | No       | Metastore type of the catalog, validate values are hadoop, hive, rest, custom                                                                                                                                                            |

The authentication information of AMS catalog can upload configuration files on AMS website,
or specify the authentication information and configuration file paths when creating catalogs with Flink DDL

| Key                                    | Default Value | Type    | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|----------------------------------------|---------------|---------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| properties.auth.load-from-ams          | True          | BOOLEAN | No       | Whether to load security verification configuration from AMS. <br>True: load from AMS; <br>false: do not use AMS configuration. <br>Note: regardless of whether this parameter is configured, as long as the user has configured the auth.*** related parameters below, this configuration will be used for access.                                                                                                                                                   |
| properties.auth.type                   | (none)        | String  | No       | Table security verification type, valid values: simple, kerberos, or not configured. Default not configured, no permission check is required. simple: use the hadoop username, used in conjunction with the parameter 'properties.auth.simple.hadoop_username'; kerberos: configure kerberos permission verification, used in conjunction with the parameters 'properties.auth.kerberos.principal', 'properties.auth.kerberos.keytab', 'properties.auth.kerberos.krb' |
| properties.auth.simple.hadoop_username | (none)        | String  | No       | Access using this hadoop username, required when 'properties.auth.type'='simple'.                                                                                                                                                                                                                                                                                                                                                                                     |
| properties.auth.kerberos.principal     | (none)        | String  | No       | Configuration of kerberos principal, required when 'properties.auth.type'='kerberos'.                                                                                                                                                                                                                                                                                                                                                                                 |
| properties.auth.kerberos.krb.path      | (none)        | String  | No       | The absolute path to the krb5.conf configuration file for kerberos (the local file path of the Flink SQL submission machine, if the SQL task is submitted with the Flink SQL Client, the path is the local path of the same node, e.g. /XXX/XXX/krb5.conf).' required if 'properties.auth.type' = 'kerberos'.                                                                                                                                                         |
| properties.auth.kerberos.keytab.path   | (none)        | String  | No       | The absolute path to the XXX.keytab configuration file for kerberos (the local file path of the Flink SQL submission machine, if the SQL task is submitted with the Flink SQL Client, the path is the local path of the same node, e.g. /XXX/XXX/XXX.keytab).' required if 'properties.auth.type' = 'kerberos'.                                                                                                                                                       |


### YAML configuration
Refer to the Flink SQL Client [official configuration](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sqlClient.html#environment-files).
Modify the `conf/sql-client-defaults.yaml` file in the Flink directory.
```yaml
catalogs:
- name: <catalog_name>
  type: mixed_iceberg
  metastore.url: ...
  ...
```

## CREATE statement

### CREATE DATABASE
By default, the default-database configuration (default value: default) when creating catalog is used. You can create a database using the following example:

```sql
CREATE DATABASE [catalog_name.]mixed_db;

USE mixed_db;
```

### CREATE TABLE

```sql
CREATE TABLE `mixed_catalog`.`mixed_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP,
    ts3 AS CAST(op_time as TIMESTAMP(3)),
    watermark FOR ts3 AS ts3 - INTERVAL '5' SECOND,
    proc AS PROCTIME(),
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'key' = 'value'
);
```

Currently, most of the syntax supported by [Flink SQL create table](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#create-table) is supported, including:

- PARTITION BY (column1, column2, …): configure Flink partition fields, but Flink does not yet support hidden partitions.
- PRIMARY KEY (column1, column2, …): configure primary keys.
- WITH ('key'='value', …): configure Amoro Table properties.
- computed_column_definition: column_name AS computed_column_expression. Currently, compute column must be listed after all physical columns. 
- watermark_definition: WATERMARK FOR rowtime_column_name AS watermark_strategy_expression, rowtime_column_name must be of type TIMESTAMP(3).  

#### PARTITIONED BY
Create a partitioned table using PARTITIONED BY.
```sql
CREATE TABLE `mixed_catalog`.`new`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
) PARTITIONED BY(op_time) WITH (
    'key' = 'value'
);
```
Amoro tables support hidden partitions, but Flink does not support function-based partitions. Therefore, currently only partitions with the same value can be created through Flink SQL.

Alternatively, tables can be created without creating a Flink catalog:
```sql
CREATE TABLE `test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP,
    proc as PROCTIME(),
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'connector' = 'mixed-format',
    'metastore.url' = '',
    'mixed_format.catalog' = '',
    'mixed_format.database' = '',
    'mixed_format.table' = ''
);
```
where `<metastore.url>` is the URL of the Amoro Metastore, and `mixed_format.catalog`, `mixed_format.database` and `mixed_format.table` are the catalog name, database name and table name of this table under the AMS, respectively.

### CREATE TABLE LIKE
Create a table with the same table structure, partitions, and table properties as an existing table. This can be achieved using CREATE TABLE LIKE.

```sql
CREATE TABLE `mixed_catalog`.`mixed_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
);

CREATE TABLE  `mixed_catalog`.`mixed_db`.`test_table_like` 
    LIKE `mixed_catalog`.`mixed_db`.`test_table`;
```
Further details can be found in [Flink create table like](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#like)

## DROP statement

### DROP DATABASE

```sql
DROP DATABASE catalog_name.mixed_db
```

### DROP TABLE
```sql
DROP TABLE `mixed_catalog`.`mixed_db`.`test_table`;
```

## SHOW statement

### SHOW DATABASES
View all database names under the current catalog:
```sql
SHOW DATABASES;
```

### SHOW TABLES
View all table names in the current database:
```sql
SHOW TABLES;
```

### SHOW CREATE TABLE
View table details:
```sql
SHOW CREATE TABLE;
```

## DESC statement
View table description:
```sql
DESC TABLE;
```

## ALTER statement
Not supported at the moment

## Supported types

### Mixed-Hive data types

| Flink Data Type | Hive Data Type |
|-----------------|----------------|
| STRING          | CHAR(p)        |
| STRING          | VARCHAR(p)     |
| STRING          | STRING         |
| BOOLEAN         | BOOLEAN        |
| INT             | TINYINT        |
| INT             | SMALLINT       |
| INT             | INT            |
| BIGINT          | BIGINT         |
| FLOAT           | FLOAT          |
| DOUBLE          | DOUBLE         |
| DECIMAL(p, s)   | DECIMAL(p, s)  |
| DATE            | DATE           |
| TIMESTAMP(6)    | TIMESTAMP      |
| VARBINARY       | BINARY         |
| ARRAY<T>        | ARRAY<T>       |
| MAP<K, V>       | MAP<K, V>      |
| ROW             | STRUCT         |


### mixed_iceberg data types
| Flink Data Type                   | Mixed-Iceberg Data Type |
|-----------------------------------|-------------------------|
| CHAR(p)                           | STRING                  |
| VARCHAR(p)                        | STRING                  |
| STRING                            | STRING                  |
| BOOLEAN                           | BOOLEAN                 |
| TINYINT                           | INT                     |
| SMALLINT                          | INT                     |
| INT                               | INT                     |
| BIGINT                            | LONG                    |
| FLOAT                             | FLOAT                   |
| DOUBLE                            | DOUBLE                  |
| DECIMAL(p, s)                     | DECIMAL(p, s)           |
| DATE                              | DATE                    |
| TIMESTAMP(6)                      | TIMESTAMP               |
| TIMESTAMP(6) WITH LOCAL TIME ZONE | TIMESTAMPTZ             |
| BINARY(p)                         | FIXED(p)                |
| BINARY(16)                        | UUID                    |
| VARBINARY                         | BINARY                  |
| ARRAY<T>                          | ARRAY<T>                |
| MAP<K, V>                         | MAP<K, V>               |
| ROW                               | STRUCT                  |
| MULTISET<T>                       | MAP<T, INT>             |