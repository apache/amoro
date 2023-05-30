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
|metastore.url|(none)|String|是|Arctic Metastore 的 URL，thrift://`<ip>`:`<port>`/`<catalog_name_in_metastore>`；如果 AMS 开启了[高可用](../guides/deployment.md#_6)，也可以通过 zookeeper://{zookeeper-server}/{cluster-name}/{catalog-name} 的形式进行指定 |
|default-database<img width=100/>|default|String|否|默认使用的数据库|
|property-version|1|Integer|否|Catalog properties 版本，此选项是为了将来的向后兼容性|

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
