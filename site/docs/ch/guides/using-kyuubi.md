
Terminal 支持对接 Kyuubi, 将 SQL 提交到 Kyuubi 上执行。你只需要按照下面的指示添加 Kyuubi 配置：
```shell
ams:
  terminal:
    backend: kyuubi
    kyuubi.jdbc.url: jdbc:hive2://127.0.0.1:10009/  # kyuubi 的 jdbc 地址
```
在不配置 Kyuubi 的情况下，Terminal 在 AMS 中的内存中执行。

在 Terminal 中执行 SQL，可以参考以下步骤：

- 请先切换 Catalog
- 在编写 SQL 前可以先使用提供的 SQL Shortcuts 功能 帮助你快速构建 SQL
- 编写 SQL
- 点击 执行 按钮运行 SQL；

![terminal](../images/admin/terminal_introduce.png)
