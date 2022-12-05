# Trino

## Iceberg format
Iceberg format 是原生的 Iceberg 表，使用 Trino 原生为 Iceberg 提供的 Connector 即可。 相关文档见 [Iceberg Connector](https://trino.io/docs/current/connector/iceberg.html#)

## Mixed format
### 安装

- 在 Trino 的安装包下创建 {trino_home}/plugin/arctic 目录，并把 arctic-trino 的包 trino-arctic-xx-SNAPSHOT.tar.gz 里的内容
  解压到 {trino_home}/plugin/arctic 目录下。
- 在{ trino_home}/etc/catalog 目录下配置 Arctic 的 Catalog 配置文件，例如

```tex
connector.name=arctic
arctic.url=thrift://{ip}:{port}/{catalogName}
```

### 支持的语句

#### 查询整表

采用 Merge-On-Read 的方式读取 Mixed Format，能读取到表的最新数据，例如

```sql
SELECT * FROM "{TABLE_NAME}"
```



#### 查询 BaseStore

在有主键表中支持直接查询 BaseStore ，BaseStore 存储表的存量数据，通常由批计算或者 optimizing 产生，查询到的是表的静态数据，
查询效率非常的高，但是时效性不好。语法如下：

```sql
SELECT * FROM "{TABLE_NAME}#BASE"
```



#### 查询 ChangeStore

在有主键表中支持直接查询 ChangeStore，ChangeStore 存储表的流和变更数据，通常由流计算实时写入，可以通过 ChangeStore 查询到表的变更记录，能查询到多久之前的变更记录由 ChangeStore 的数据过期时间决定。

```sql
SELECT * FROM "{TABLE_NAME}#CHANGE"
```

查出来结果会多三列数据分别是：

- _transaction_id: 数据写入时 AMS 分配的 transaction id。批模式下为每条 SQL 执行时分配，流模式下为每次 checkpoint 分配。
- _file_offset：大小可以表示同一批 _transaction_id 中数据写入的先后顺序。
- _change_action：表示数据的类型有 INSERT，DELETE 两种

#### Trino 和 Arctic 的类型对照

| Arctic type   | Trino type                    |
| :------------- | :---------------------------- |
| `BOOLEAN`      | `BOOLEAN`                     |
| `INT`          | `INTEGER`                     |
| `LONG`         | `BIGINT`                      |
| `FLOAT`        | `REAL`                        |
| `DOUBLE`       | `DOUBLE`                      |
| `DECIMAL(p,s)` | `DECIMAL(p,s)`                |
| `DATE`         | `DATE`                        |
| `TIME`         | `TIME(6)`                     |
| `TIMESTAMP`    | `TIMESTAMP(6)`                |
| `TIMESTAMPTZ`  | `TIMESTAMP(6) WITH TIME ZONE` |
| `STRING`       | `VARCHAR`                     |
| `UUID`         | `UUID`                        |
| `BINARY`       | `VARBINARY`                   |
| `STRUCT(...)`  | `ROW(...)`                    |
| `LIST(e)`      | `ARRAY(e)`                    |
| `MAP(k,v)`     | `MAP(k,v)`                    |

