## Create Catalogs
### Flink SQL
通过执行下述语句可以创建 flink catalog

```sql
CREATE CATALOG <catalog_name> WITH (
  'type'='arctic',
  `<config_key>`=`<config_value>`
); 
```

其中 `<catalog_name>` 为用户自定义的 flink catalog 名称， `<config_key>`=`<config_value>` 有如下配置：

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|metastore.url|(none)|String|是|Arctic Metastore 的 URL，thrift://`<ip>`:`<port>`/`<catalog_name_in_metastore>`|
|default-database<img width=100/>|default|String|否|默认使用的数据库|
|property-version|1|Integer|否|Catalog properties 版本，此选项是为了将来的向后兼容性|
| properties.auth.load-from-ams | True | BOOLEAN | 否 |是否从 AMS 加载安全校验的配置。 true:从AMS加载；false:不使用AMS的配置。注意：不管该参数是否配置，只要用户配置了下述 auth.*** 相关参数，就会使用该配置进行访问|
| properties.auth.type | (none) | String | 否 | 表安全校验类型，有效值：simple、kerberos 或不配置。默认不配置，无需权限检验。simple：使用 hadoop 用户名，搭配参数'properties.auth.simple.hadoop_username'使用； kerberos：配置 kerberos 权限校验，搭配参数'properties.auth.kerberos.principal','properties.auth.kerberos.keytab','properties.auth.kerberos.krb'使用 |
| properties.auth.simple.hadoop_username | (none) | String | 否 | 使用该 hadoop username 访问，'properties.auth.type'='simple'时必填。|
| properties.auth.kerberos.principal | (none) | String | 否 | kerberos 的 principal 配置，'properties.auth.type'='kerberos'时必填。|
| properties.auth.kerberos.krb.path | (none) | String | 否 | kerberos 的 krb5.conf 配置文件的绝对路径（Flink SQL提交机器的本地文件路径，如用 Flink SQL Client提交SQL任务，该路径为同节点的本地路径，如 /XXX/XXX/krb5.conf）。'properties.auth.type'='kerberos'时必填。|
| properties.auth.kerberos.keytab.path | (none) | String | 否 | kerberos 的 XXX.keytab 配置文件的绝对路径（Flink SQL提交机器的本地文件路径，如用 Flink SQL Client提交SQL任务，该路径为同节点的本地路径，如 /XXX/XXX/XXX.keytab）。'properties.auth.type'='kerberos'时必填。|

### 通过 YAML 配置
参考 Flink Sql Client [官方配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sqlClient.html#environment-files)。
修改 flink 目录中的 conf/sql-client-defaults.yaml 文件。
```yaml
catalogs:
- name: <catalog_name>
  type: arctic
  metastore.url: ...
  ...
```

## DDL 语句

### CREATE DATABASE
默认使用创建 catalog 时的 default-database 配置（默认值：default）。可使用下述例子创建数据库：
    
```sql
CREATE DATABASE [catalog_name.]arctic_db;

USE arctic_db;
```
### DROP DATABASE
    
```sql
DROP DATABASE catalog_name.arctic_db
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

目前支持 [Flink Sql 建表](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#create-table)的大多数语法，包括：

- PARTITION BY (column1, column2, ...)：配置 Flink 分区字段，但 Flink 还未支持隐藏分区
- PRIMARY KEY (column1, column2, ...)：配置主键
- WITH ('key'='value', ...)：配置 Arctic Table 的属性

目前不支持计算列、watermark 字段的配置。
    
### PARTITIONED BY
使用 PARTITIONED BY 创建分区表。
```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
) PARTITIONED BY(op_time) WITH (
    'key' = 'value'
);
```
Arctic 表支持隐藏分区，但 Flink 不支持函数计算的分区，因此目前通过 Flink Sql 只能创建相同值的分区。
### CREATE TABLE LIKE
创建一个与已有表相同表结构、分区、表属性的表，可使用 CREATE TABLE LIKE

```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
);

CREATE TABLE  `arctic_catalog`.`arctic_db`.`test_table_like` 
    LIKE `arctic_catalog`.`arctic_db`.`test_table`;
```
更多细节可参考 [Flink create table like](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#like)

### DROP TABLE
```sql
DROP TABLE `arctic_catalog`.`arctic_db`.`test_table`;
```

## SHOW 语句

### SHOW DATABASES
查看当前 catalog 下所有数据库名称：
```sql
SHOW DATABASES;
```

### SHOW TABLES
查看当前数据库的所有表名称：
```sql
SHOW TABLES;
```

## Supported Types

### Mixed Hive Data Types

| Flink Data Type    | Hive Data Type    |
|-----|-----|
| STRING    | CHAR(p)    |
| STRING    | VARCHAR(p)    |
| STRING    | STRING    |
| BOOLEAN    | BOOLEAN    |
| INT    | TINYINT    |
| INT    | SMALLINT    |
| INT    | INT    |
| BIGINT    | BIGINT    |
| FLOAT    | FLOAT     |
| DOUBLE    | DOUBLE    |
| DECIAML(p, s)    | DECIAML(p, s)    |
| DATE    | DATE    |
| TIMESTAMP(6)    | TIMESTAMP    |
| VARBINARY    | BYNARY    |
| ARRAY<T>     | ARRAY<T>     |
| MAP<K, V>    | MAP<K, V>     |
| ROW    | STRUCT       |


### Mixed Iceberg Data Types
| Flink Data Type    | Mixed Iceberg Data Type    |
|-----|-----|
| CHAR(p)    | STRING    |
| VARCHAR(p)    | STRING    |
| STRING    | STRING    |
| BOOLEAN    | BOOLEAN    |
| TINYINT    | INT    |
| SMALLINT    | INT    |
| INT    | INT    |
| BIGINT    | LONG    |
| FLOAT    | FLOAT     |
| DOUBLE    | DOUBLE    |
| DECIAML(p, s)    | DECIAML(p, s)    |
| DATE    | DATE    |
| TIMESTAMP(6)    | TIMESTAMP    |
| TIMESTAMP(6) WITH LCOAL TIME ZONE    | TIMESTAMPTZ    |
| BINARY(p)    | FIXED(p)    |
| BINARY(16)    | UUID       |
| VARBINARY    | BYNARY    |
| ARRAY<T>     | ARRAY<T>     |
| MAP<K, V>    | MAP<K, V>     |
| ROW    | STRUCT       |
| MULTISET<T>    | MAP<T, INT>       |
