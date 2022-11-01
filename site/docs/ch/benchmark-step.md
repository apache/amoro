# 环境
## 基础软件
### mysql
mysql主要是用来生产tpcc数据然后通过同步工具同步到arctic，hudi，iceberg等数据湖中。本文档使用5.7，mysql8也可以。
安装可以参考下列文档：[mysql-install](mysql-install.md),此为mysql mgr集群模式，用户可以只安装一个节点即可。

### hadoop
hadoop体系包含hdfs,yarn,hive。安装方式有很多，可以选择ambari安装，安装文档可以参考：[ambari-install](ambari-hadoop-install.md)

### trino
trino主要是用来最终执行tpch查询，是最主要的一环。当前使用380版本

[trino-install](https://trino.io/docs/current/installation/deployment.html)

如果要测试arctic需要安装配置arctic插件：

[arctic-plugin-install](mysql-install.md)

如果需要测试iceberg需要配置iceberg插件：

[iceberg-plugin-install](https://trino.io/docs/current/connector/iceberg.html)

### presto
hudi的rt表的查询也就是实时查询目前不支持trino，只支持presto,如果需要测试hudi需要安装配置presto。

[presto-install](https://prestodb.io/docs/current/installation/deployment.html)

配置hudi插件：

[hudi-plugin-install](https://prestodb.io/docs/current/connector/hudi.html)

### ams
arctic的元数据服务，当前使用0.4版本，如果需要测试arctic需要安装此服务

[ams-install](meta-service/dashboard.md)

根据文档描述，配置一个optimize-group例如名称"benchmark-group"，然后在页面![ams-install](images/chbenchmark-step/start-optimize.png)

点击Scale-Out按钮启动optimize服务。

##测试工具

### data-lake-benchmark
benchmark的核心工具，负责生成tpcc数据进mysql和通过trino执行ap标准查询语句，最后输出benchmark结果。注意这个工具需要java17。

[oltpbench-install](https://github.com/NetEase/data-lake-benchmark#readme)

###同步工具
暂略

#测试流程
- 把mysql信息配置进oltpbench的config/mysql/sample_chbenchmark_config.xml文件中。其中"scalefactor"表示的warehouse数量用于控制整体数据量的，一般选择10或者100。
- 执行data-lake-benchmark命令往mysql生成全量数据，命令如下：
  ```
  java -jar oltpbench2.jar -b tpcc,chbenchmark -c config/mysql/sample_chbenchmark_config.xml --create=true --load=true
  ```
  执行完了以后mysql的指定数据库下面就能看到这12张表：warehouse,item,stock,district,customer,history,oorder,new_order,order_line,region,nation,supplier
- 同步工具步骤暂略。
- 当全量同步完了，这时候可以用data-lake-benchmark工具进行全量静态数据的测试。首先把trino信息配置进data-lake-benchmark的config/trino/sample_chbenchmark_config.xml中,主要是url要改成当前trino的地址，
  还有works.work.time参数表示benchmark运行时间，单位是秒，全量测试时间可以短一点10分钟左右就行。命令如下：
  ```
  java -jar oltpbench2.jar -b chbenchmarkForTrino -c config/trino/sample_chbenchmark_config.xml --create=false --load=false --execute=true
  ```
- 再次启动data-lake-benchmark程序向mysql里面生产增量数据，这些数据会通过已经运行的数据同步工具源源不断写入arctic，
  需要修改config/mysql/sample_chbenchmark_config.xml配置文件中的works.work.time参数控制生成时间，一般半小时，命令如下：
  ```
  java -jar oltpbench2.jar -b tpcc,chbenchmark -c config/mysql/sample_chbenchmark_config.xml --execute=true -s 5
  ```
- 在mysql生产增量数据的同时，启动data-lake-benchmark的tpch性能测试命令，其中执行时间要和生成增量的时间相同：
  ```
  java -jar oltpbench2.jar -b chbenchmarkForTrino -c config/trino/sample_chbenchmark_config.xml --create=false --load=false --execute=true
  ```
- 重复上两个步骤就可以得到增量30分钟，60分钟，90分钟，120分钟的性能测试报告。

##hudi
上述测试流程在测试hudi的时候需要做一些补充:
1. 首先hudi的rt表也就是走MOR读取的表只有presto支持，所以需要用presto作为最终的ap引擎，
   config/trino/sample_chbenchmark_config.xml里面的driver参数需要改成"com.facebook.presto.jdbc.PrestoDriver"
2. hudi使用hive的元数据的时候需要额外添加一些依赖，官网描述见[hudi](https://hudi.apache.org/docs/syncing_metastore)
   主要是:

   ![hudi-sync](images/chbenchmark-step/hudi-sync.png)
3. hudi的表名是带有后缀的，ro表示读优化表，rt表示全量表，可以在执行data-lake-benchmark程序之前设置环境变量如：
   ```
   export tpcc_name_suffix=_rt
   ```
## 测试结果
data-lake-benchmark跑完以后会生成一个results目录，测试结果都在里面，主要关注两个文件，第一：.summary.json文件，
这里面的Average Latency项显示的是本次性能测试的平均相应时间，第二：.statistic.csv文件，里面记录了每个Query类型的最大，最小，平均耗时。