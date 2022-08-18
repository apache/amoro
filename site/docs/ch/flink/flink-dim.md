## 维表 Join
### 简述

Flink SQL 官方支持多种 Join 方式，见 [官方文档](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/dev/table/sql/queries/joins/)
对于 Flink 官方 Regular Join、Interval Join、Temporal Join，用户可以按照上述 Flink 官方的文档正常使用。
对于将 Arctic 表作为维表的 Lookup Join 场景，请按本文档的描述配置使用。

**未来会基于 Arctic log-store 实现更实时的维表**

### 使用说明

注意：维表必须定义主键，并且 Join 条件必须包含所有主键字段。

以下是 Arctic 作为维表 Join 的使用方法：

```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled=true;

-- 创建一张 Arctic 表
CREATE TABLE IF NOT EXISTS arctic_catalog.default_db.`user` (
    id   INT,
    name STRING,
    age  INT,
    PRIMARY KEY (id) NOT ENFORCED
);

-- 创建一张主表，如果有指定 watermark 的需求，则可以自由指定，如下所示：
CREATE TABLE orders (
    order_id    STRING,
    price       DECIMAL(32,2),
    user_id     INT,
    order_time  TIMESTAMP(3),
    WATERMARK FOR order_time AS order_time
) WITH (/* ... */);
-- 如果数据中没有 eventTime，则需要将 proctime 作为 watermark。如下所示：
CREATE TABLE orders (
    order_id    STRING,
    price       DECIMAL(32,2),
    user_id     INT,
    order_time  AS LOCALTIMESTAMP,
    WATERMARK FOR order_time AS order_time
) WITH (/* ... */);

-- 给 Arctic 表添加 watermark。"opt" 可以是任意名称，但不能与 arctic_catalog.default_db.`user` 已有字段同名
CREATE TABLE user_dim (
    opt TIMESTAMP(3),
    WATERMARK FOR opt AS opt
) LIKE arctic_catalog.default_db.`user`;

SELECT order_id, price, user_id, name, age 
FROM orders
LEFT JOIN user_dim /*+OPTIONS('streaming'='true', 'dim-table.enable'='true')*/
    FOR SYSTEM_TIME AS OF orders.order_time
ON orders.user_id = user_dim.id

```

