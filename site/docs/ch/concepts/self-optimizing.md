## Introduction

Lakehouse 具有开放和低耦合的特性，数据和文件交给用户通过各类引擎维护，这样的架构在 T+1 的场景下看起来还好，但随着越来越多的人关注如何将 Lakehouse 应用于流式数仓，实时分析场景，问题变得难以调和，比如：

- 流式写入带来海量的小文件
- CDC ingestion 和流式更新产生过量的 delta 数据
- 应用新型数据湖格式会带来孤儿文件和过期快照

上述任何问题都会给数据分析的性能和成本带来严重影响，为此 Arctic 引入 self-optimizing 机制，目标是将基于新型 table format 打造像数据库，传统数仓一样开箱即用的流式湖仓服务，Self-optimizing 包含但不限于文件合并，去重，排序，孤儿文件和过期快照的清理。

Self-optimizing 的架构与工作机制如下图所示：

![Self-optimizing architecture](../images/concepts/self-optimizing_arch.png){:height="80%" width="80%"}

Optimizer 是 self-optimizing 的执行组件，是由 AMS 管理的常驻进程，AMS 会负责发现和计划湖仓表的自优化任务，并实时调度给 optimizer 分布式执行，最后由 AMS 负责提交优化结果，Arctic 通过 Optimizer Group 对 Optimizers 实现物理隔离。

Arctic 的 self-optimizing 的核心特性有：

- 自动、异步与透明 — 后台持续检测文件变化，异步分布式执行优化任务，对用户透明无感
- 资源隔离和共享 —允许资源在表级隔离和共享，以及设置资源配额
- 灵活可扩展的部署方式 — 执行节点支持多种部署方式，便捷的扩缩容

## Self-optimizing mechanism

在数据写入过程中，可能会产生写放大和读放大两类情况：

- 读放大 — 由于写入过程中产生过量的小文件，或 delete 文件与 insert 文件产生了过多的映射（如果你是 Iceberg v2 format 用户，对这个问题可能不陌生），如果 optimizing 的调度频率跟不上小文件产生的速度，会严重拖慢文件读取性能
- 写放大 — 频繁地调度 optimizing 会让存量数据被频繁合并和重写，造成 CPU/IO/Memoery 的资源竞争和浪费，拖慢 optimizing 的速度，也会进一步引发读放大

为了缓解读放大需要频繁执行 optimizing，但是频繁 optimizing 会导致写放大，Self-optimizing 的设计需要在读放大和写放大之间提供最佳的 trade off，Arctic 的 self-optimizing 借鉴了 java 虚拟机分代垃圾回收算法，将文件按照大小分为 Fragment 和 Segment，将 Fragment 和 Segement 上执行的不同 self-optimizing 过程分为 minor 和 major 两种类型，为此 Arctic v0.4 引入了两个参数来定义 Fragment 和 Segment：

```SQL
-- self-optimizing 的文件目标大小
self-optimizing.target-size = 128;
-- self-optimizing 处理的 fragment 文件阈值
self-optimizing.fragment-ratio = 8
```

self-optimizing.target-size 定义了 major optimizing 的目标输出大小，默认 128m，self-optimizing.fragment-ratio 定义了 fragment 文件阈值在 target-size 中的占比，8 代表着 target-size 的 1/8，对应 128m 的 target-size 默认 fragement 阈值为 16m，小于 16m 是 fragment 文件，大于 16m 是 segment 文件，如下图所示：

![Minor optimizing](../images/concepts/minor_optimizing.png){:height="80%" width="80%"}

Minor optimizing 的目标是将 fragment 文件尽可能快地合并为 segment 文件，以缓解读放大，所以当 fragment 文件积累到一定量时， minor optimizing 会较为频繁的调度执行。
Major optimizing 会同时处理 segment 和 fragment 文件，在这个过程中会按照主键消除部分或全部的重复数据，如果是有主键表，相比 minor optimizing 通过 major optimizing 一般可以更为明显地提升读取性能，并且更低的 major 调度频率可以有效缓解写放大问题。Full optimizing 会将 target space 内所有文件合并成一个文件，是 major optimizing 的一种特殊情况：

![Major optimizing](../images/concepts/major_optimizing.png){:height="80%" width="80%"}

Minor、major 和 full optimizing 的输入输出关系如下表所示：

| Self-optimizing type  | Input space  | Output space  | Input file types  | Output file types  |
|:----------|:----------|:----------|:----------|:----------|
| minor    | fragment    | fragment/segment    | insert, delete    | insert, deletee    |
| major    | fragment, segment    | segment    | insert, delete    | insert, delete    |
| full    | fragment, segment    | segment    | insert, delete   | insert    |


## Self-optimizing quota

如果你使用的是不可更新的表，如日志，传感器数据，并且已经习惯于 Iceberg 提供的 optimize 指令，可以考虑通过下面的配置关闭表上的 self-optimizing 功能：

```SQL
self-optimizing.enabled = false;
```

如果表配置了主键，支持 CDC 摄取和流式更新，比如数据库同步表，或者按照维度聚合过的表，建议开启 self-optimizing 功能。单张表的 self-optimizing 资源用量通过在表上配置 quota 参数来管理：

```SQL
-- self-optimizing 能够使用的最大 CPU 数量，可以取小数
self-optimizing.quota = 1;
```

Quota 定义了单张表可以使用的最大 CPU 用量，但 self-optimizing 实际是分布式执行，真实的资源用量是按实际执行时间动态管理的过程，在 optimizing 管理界面，可以通过 quota occupy 这个指标查看单张表的动态 quota 占用，从设计目标看，quota occupy 指标应当动态趋于 100%。 

在平台中可能出现超售和超买两种情况：

- 超买 — 若所有 optimizer 配置超过所有表配置的 quota 总和，quota occupy 可能动态趋于 100% 以上
- 超卖 — 若所有 optimizer 配置低于所有配置表的 quota 总和，quota occupy 应当动态趋于 100% 以下
