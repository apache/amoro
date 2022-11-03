# Trino

## 安装

- 在 Trino 的安装包下创建{trino_home}/plugin/arctic目录，并把arctic-trino的包trino-arctic-xx-SNAPSHOT.tar.gz里的内容
  解压到{trino_home}/plugin/arctic目录下。
- 在{trino_home}/etc/catalog目录下配置 Arctic 的 Catalog 配置文件，例如

```tex
connector.name=arctic
arctic.url=thrift://10.196.98.23:18111/{catalogName}
```

其他的读取文件的配置与 Trino 支持的 Iceberg 参数相同



## 支持的语句

### 查询表

采用Merge-On-Read的方式读取 Arctic 表，能读取到表的最新数据，例如

```sql
SELECT * FROM "{TABLE_NAME}"
```



### 查询base表

base 表里是已经完成 optimize 的数据，查询到的是表的静态数据，查询效率非常的高，就是时效性不好，取决于 optimize 的执行间隔，例如

```sql
SELECT * FROM "{TABLE_NAME}#BASE"
```



### 查询change表

change 表里是表的一些列更新操作，支持直接查询 change，查询到的是表的 changeLog，能查询到多久之前的 changeLog 取决于配置的 change 表过期时间

```sql
SELECT * FROM "{TABLE_NAME}#CHANGE"
```

查出来结果会多三列数据分别是：

- _transaction_id: 数据写入时AMS分配的 transaction id。批模式下为每条SQL执行时分配，流模式下为每次checkpoint 分配。
- _file_offset：大小可以表示同一批 _transaction_id 中数据写入的先后顺序。
- _change_action：表示数据的类型有 INSERT，DELETE 两种

### Trino 和 Arctic 的类型对照

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

