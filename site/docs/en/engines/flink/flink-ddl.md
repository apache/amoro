## Create Catalogs
### Flink SQL
The following statement can be executed to create a Flink catalog:

```sql
CREATE CATALOG <catalog_name> WITH (
  'type'='arctic',
  `<config_key>`=`<config_value>`
); 
```

Where `<catalog_name>` is the user-defined name of the Flink catalog, and `<config_key>`=`<config_value>` has the following configurations:

| Key                                    | Default Value | Type    | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|----------------------------------------|---------------|---------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| metastore.url                          | (none)        | String  | Yes      | Arctic Metastore 的 URL，thrift://`<ip>`:`<port>`/`<catalog_name_in_metastore>`；如果 AMS 开启了[高可用](../guides/deployment.md#_6)，也可以通过 zookeeper://{zookeeper-server}/{cluster-name}/{catalog-name} 的形式进行指定                                                                                                                                                                                                                                                                  |
| default-database<img width=100/>       | default       | String  | No       | The default database to use                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| property-version                       | 1             | Integer | No       | Catalog properties version, this option is for future backward compatibility                                                                                                                                                                                                                                                                                                                                                                                          |
| properties.auth.load-from-ams          | True          | BOOLEAN | No       | Whether to load security verification configuration from AMS. True: load from AMS; false: do not use AMS configuration. Note: regardless of whether this parameter is configured, as long as the user has configured the auth.*** related parameters below, this configuration will be used for access.                                                                                                                                                               |
| properties.auth.type                   | (none)        | String  | No       | Table security verification type, valid values: simple, kerberos, or not configured. Default not configured, no permission check is required. simple: use the hadoop username, used in conjunction with the parameter 'properties.auth.simple.hadoop_username'; kerberos: configure kerberos permission verification, used in conjunction with the parameters 'properties.auth.kerberos.principal', 'properties.auth.kerberos.keytab', 'properties.auth.kerberos.krb' |
| properties.auth.simple.hadoop_username | (none)        | String  | No       | Access using this hadoop username, required when 'properties.auth.type'='simple'.                                                                                                                                                                                                                                                                                                                                                                                     |
| properties.auth.kerberos.principal     | (none)        | String  | No       | Configuration of kerberos principal, required when 'properties.auth.type'='kerberos'.                                                                                                                                                                                                                                                                                                                                                                                 |
| properties.auth.kerberos.krb.path      | (none)        | String  | No       | The absolute path to the krb5.conf configuration file for kerberos (the local file path of the Flink SQL submission machine, if the SQL task is submitted with the Flink SQL Client, the path is the local path of the same node, e.g. /XXX/XXX/krb5.conf).' required if 'properties.auth.type' = 'kerberos'.                                                                                                                                                         |
| properties.auth.kerberos.keytab.path   | (none)        | String  | No       | The absolute path to the XXX.keytab configuration file for kerberos (the local file path of the Flink SQL submission machine, if the SQL task is submitted with the Flink SQL Client, the path is the local path of the same node, e.g. /XXX/XXX/XXX.keytab).' required if 'properties.auth.type' = 'kerberos'.                                                                                                                                                       |

### YAML configuration
Refer to the Flink Sql Client [official configuration](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sqlClient.html#environment-files).
Modify the conf/sql-client-defaults.yaml file in the Flink directory.
```yaml
catalogs:
- name: <catalog_name>
  type: arctic
  metastore.url: ...
  ...
```

## CREATE statement

### CREATE DATABASE
By default, the default-database configuration (default value: default) when creating a catalog is used. You can create a database using the following example:

```sql
CREATE DATABASE [catalog_name.]arctic_db;

USE arctic_db;
```

### CREATE TABLE

```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'key' = 'value'
);
```

Currently, most of the syntax supported by [Flink Sql create table](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#create-table) is supported, including:

- PARTITION BY (column1, column2, …): Configure Flink partition fields, but Flink does not yet support hidden partitions. 
- PRIMARY KEY (column1, column2, …): Configure primary keys. 
- WITH ('key'='value', …): Configure Arctic Table properties.

Currently, configuration of computed columns and watermark fields is not supported.
    
#### PARTITIONED BY
Create a partitioned table using PARTITIONED BY.
```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
) PARTITIONED BY(op_time) WITH (
    'key' = 'value'
);
```
Arctic tables support hidden partitions, but Flink does not support function-based partitions. Therefore, currently only partitions with the same value can be created through Flink Sql.

### CREATE TABLE LIKE
Create a table with the same table structure, partitions, and table properties as an existing table. This can be achieved using CREATE TABLE LIKE.

```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
);

CREATE TABLE  `arctic_catalog`.`arctic_db`.`test_table_like` 
    LIKE `arctic_catalog`.`arctic_db`.`test_table`;
```
Further details can be found in [Flink create table like](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#like)

## DROP statement

### DROP DATABASE

```sql
DROP DATABASE catalog_name.arctic_db
```

### DROP TABLE
```sql
DROP TABLE `arctic_catalog`.`arctic_db`.`test_table`;
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

## DESC 语句
View table description:
```sql
DESC TABLE;
```

## ALTER 语句
Not supported at the moment

## Supported Types

### Mixed Hive Data Types

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
| DECIAML(p, s)   | DECIAML(p, s)  |
| DATE            | DATE           |
| TIMESTAMP(6)    | TIMESTAMP      |
| VARBINARY       | BYNARY         |
| ARRAY<T>        | ARRAY<T>       |
| MAP<K, V>       | MAP<K, V>      |
| ROW             | STRUCT         |


### Mixed Iceberg Data Types
| Flink Data Type                   | Mixed Iceberg Data Type |
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
| DECIAML(p, s)                     | DECIAML(p, s)           |
| DATE                              | DATE                    |
| TIMESTAMP(6)                      | TIMESTAMP               |
| TIMESTAMP(6) WITH LCOAL TIME ZONE | TIMESTAMPTZ             |
| BINARY(p)                         | FIXED(p)                |
| BINARY(16)                        | UUID                    |
| VARBINARY                         | BYNARY                  |
| ARRAY<T>                          | ARRAY<T>                |
| MAP<K, V>                         | MAP<K, V>               |
| ROW                               | STRUCT                  |
| MULTISET<T>                       | MAP<T, INT>             |
