## Overview

Table format (aka. format)最早由 Iceberg 提出，table format 可以描述为：

- 定义了表和文件的关系，任何引擎都可以根据 table format 查询和检索数据文件
- Iceberg / Delta / Hudi 这类新型 format 进一步定义了表与快照，快照与文件的关系，表上所有写操作会产生新快照，所有表的读操作都面向快照，快照为数据湖带来了 MVCC、ACID 以及 Transaction 的能力。

此外，[Iceberg](https://Iceberg.apache.org/) 这类新型 table format 还提供了 schema evolve、hidden partiton、data skip 等众多高级特性，[Hudi](https://hudi.apache.org/)、[Delta](https://delta.io/) 在具体功能上可能有所差异，但我们看到在过去两年的迭代中，table format 的标准随着三个开源项目的功能趋同在逐步确立。

对用户，Arctic 的设计目标是开箱即用的湖仓系统，而在系统内部，Arctic 的设计理念是将不同 table format 作为数据湖的 storage engine 来使用，这种设计模式多见于 MySQL、ClickHouse 这样的开源系统。
Arctic 选择了最早提出 table format 概念的 Iceberg 作为基础，在不魔改社区代码的前提下，为用户提供了一套可以兼容 Hive format，并且在流和更新场景下更加优化的 Mixed format。Iceberg format 和 Mixed format 各有优势，用户可以根据需求灵活选择，并且都能享受到 Arctic 开箱即用的体验。

### Feature Matrix

|     format      | Read | Write | Update | Overwrite | Merge Into | Delete | Batch Upsert | Streaming Upset | CDC  Ingestion | Primary Key | Time  Travel |
|:---------------:|:----:|:-----:|:------:|:---------:|:----------:|:------:|:------------:|:---------------:|:--------------:|:-----------:|:------------:|
|     Iceberg     |  ✅   |   ✅   |   ✅    |     ✅     |     ✅      |   ✅    |      ❌       |        ✅        |       ❌        |      ❌      |      ✅       |
| Mixed-Streaming |  ✅   |   ✅   |   ✅    |     ✅     |     ✅      |   ✅    |      ✅       |        ✅        |       ✅        |      ✅      |      ❌       |

