# Optimizers

## 概念

### Optimizer

Optimizer 是结构优化（Optimize）功能的执行组件，结构优化时数据的转化、合并等文件读写操作，都依赖 Optimizer 来完成。

Optimizer 的功能比较单一，只负责结构优化功能 plan、execute、commit 三个步骤中的 execute 部分。

具体来说，Optimizer 需要完成从 AMS 获取待执行的任务、执行任务、将任务执行结果上报给 AMS 三个步骤。

每个 Optimizer 都是一个实际运行的服务，它的运行方式不受限制，可以是一个本地进程，也可以是运行在分布式资源管理系统（如 yarn）上的作业。

### Optimizing Container

Optimizing Container（Optimizing 容器）是 Optimizer 的运行容器，面向解决 Optimizer 的加载、停止等调度问题。

不同的 Optimizing Container 可以实现 Optimizer 不同的运行方式，Arctic 已经提供了 LocalOptimizer 和 FlinkOptimizer 两种容器实现，分别支持 Optimizer 以本地进程和 Flink 作业的方式运行。

Optimizing Container 本身不是服务，它只代表不同的调度实现方案。

### Optimizer Group

Optimizer Group（Optimizer 资源组）是为了划分 Optimizer 资源而引入的概念，一个 Optimize Group 包含了若干个属于统一资源池的 Optimizer 服务。

启动一个 Optimizer 服务，必须指定其所属的 Optimizer Group，这也限定了一个 Optimizer 服务只能属于一个 Optimizer Group。

创建 Optimizer Group 需要指定它的 Optimizing Container，即定义这个Optimizer Group 下的 Optimizer 的运行方式，当前一个 Optimizer Group 只能设置一个运行方式。

## Optimizer 的资源隔离和共享

Optimizer Group 可以解决不同 Arctic 表之间 Optimizer 资源的隔离和共享问题： 

归属于同一个 Optimizer Group 的 Arctic 表，会共享其中的 Optimizer 资源；而不同 Optimizer Group 的 Arctic 表，它们的 
Optimizer 资源是彼此隔离的。

一张 Arctic 表只能归属于一个 Optimizer Group，这个属性是可以修改的；如果 Arctic 表未指定 Optimizer Group，将归属于名称为 'default' 的默认 Optimizer Group。

修改 Arctic 表 Optimize Group 属性的方式如下

```sql
alter table test_db.test_table set tblproperties (
    'optimize.group' = 'optimize-group-name');
```

### 共享 Optimizer 资源的均衡

归属于同一个 Optimize Group 的多张 Arctic 表，共享这部分 Optimizer 资源，用 quota（配额）来规定每张表占用资源的多少。

quota 表示一段时间内累计占用cpu的时间占比，计算方式是：并发数 * 实际执行时间 / 总时间。

quota 使用率表示一段时间之内的 quota 实际消耗值与设定值的比例。

默认情况下，Optimize Group 下所有表占用资源的权重是相同的，每张 Arctic 表设定的 quota 均为 0.1，可以修改这一配置，改变表的权重，修改方式如下

```sql
alter table test_db.test_table set tblproperties (
    'optimize.quota' = '0.2');
```

Optimizer 资源是动态均衡的，目前的动态均衡策略如下：

当有 Optimizer 资源闲置时，根据此时 quota 使用率对 Arctic 表进行优先级排序，quota 使用率越低优先级越高，优先级最高的 Arctic 表会优先尝试进行 Optimize，如果该表没有 Optimize 的需要，则第二优先级的表尝试执行，依此类推。

上述 quota 使用率的计算是动态变化的，时间区间从上次 optimize 开始时间算起，因此随着 optimize 的持续推进，quota 的计算区间也不断推进，反映出最近的资源占用情况。

在这样的设计下，Optimize 的效果也随着 Optimizer 资源的多少动态变化，提供的 Optimizer 资源越多，表的 Optimize 就越激进，数据时效性也就越高，文件治理效果也就越好。


## Optimizing Container 的实现和扩展

Arctic 已经提供了两种 Optimizing Container 的实现，分别是 LocalOptimizer 和 Flink Optimizer，用户也可以自定义新的 Optimizer Container。

### LocalOptimizer

LocalOptimizer 通过本地进程方式启动的 Optimizer 的一种方式，支持 Optimize 任务的多线程执行，在演示阶段或单机部署的场景下，LocalOptimizer 是最简单的一种调度方式。

具体启动方式和相关配置是否要介绍？

### FlinkOptimizer

FlinkOptimizer 是通过 Flink 作业启动 Optimizer 的一种方式，借助 Flink 可以方便地将 Optimizer 部署在 yarn 集群上，从而支持大规模数据场景下的使用。

Flink Optimizer的拓扑是否要介绍？

### 自定义 Optimizing Container

是否要展开介绍实现方式？