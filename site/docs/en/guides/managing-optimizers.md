
As your business grows and more tables need optimizers, you need to expand your optimizer resources. Conversely, if you created too many optimizer resources at the beginning, you can release some of them to reduce the resource usage. Before you do that, you need to click Optimizers and go to the administration page.
![optimizer-manage](../images/admin/optimizer_management.png)

## Optimizer Container
Before using Self-optimizing, you need to configure the container type and optimize group in the configuration file. container itself is not a service, it only represents a specific set of runtime environment configuration, and the scheduling scheme of optimizer in that runtime environment. container includes three types: flink, local, and external.

### LocalContainer
The type of Local Container is local, which is a way to start Optimizer by local process and supports multi-threaded execution of Optimizer tasks. It is recommended to be used only in demo or local deployment scenarios. If the environment variable for jdk is not configured, the user can configure java_home to point to the jdk root directory. If already configured, this configuration item can be ignored.

```shell
containers:
  # arctic optimizer container config.sh
  - name: localContainer
    type: local
    properties:
      java_home: /opt/java   # jdk enviroment
```
### FlinkContainer
Flink Container of type flink is a way to start Optimizer through Flink jobs. With Flink, you can easily deploy Optimizer on yarn clusters to support large-scale data scenarios. To use flink type, you need to add a container of type flink, and the configuration items of container include.

- FLINK_HOME, download the Flink installation package and unzip it. Take Flink-1.12.7 as an example, download https://archive.apache.org/dist/flink/flink-1.12.7/flink-1.12.7-bin-scala_2.12.tgz , assuming that it is extracted to /opt/ directory, then configure the value /opt/ flink-1.12.7/
- Since the Flink distribution does not come with the hadoop compatible package flink-shaded-hadoop-2-uber-x.y.z.jar, you need to download it and copy it to the FLINK_HOME/lib directory. The flink-shaded-hadoop-2-uber-2.7.5-10.0.jar is generally sufficient and can be downloaded at: https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop- 2-uber/2.7.5-10.0/flink-shaded-hadoop-2-uber-2.7.5-10.0.jar
- HADOOP_CONF_DIR, which holds the configuration files for the hadoop cluster (including hdfs-site.xml, core-site.xml, yarn-site.xml ). If the hadoop cluster has kerberos authentication enabled, you need to prepare an additional krb5.conf and a keytab file for the user to submit tasks
- JVM_ARGS, you can configure flink to run additional configuration parameters, here is an example of configuring krb5.conf, specify the address of krb5.conf to be used by Flink when committing via -Djava.security.krb5.conf=/opt/krb5.conf
- HADOOP_USER_NAME, the username used to submit tasks to yarn
- FLINK_CONF_DIR, the directory where flink_conf.yaml is located
```shell
containers:
  - name: flinkContainer
    type: flink
    properties:
      FLINK_HOME: /opt/flink/        #flink install home
      HADOOP_CONF_DIR: /etc/hadoop/conf/       #hadoop config dir
      HADOOP_USER_NAME: hadoop       #hadoop user submit on yarn
      JVM_ARGS: -Djava.security.krb5.conf=/opt/krb5.conf       #flink launch jvm args, like kerberos config when ues kerberos
      FLINK_CONF_DIR: /etc/hadoop/conf/        #flink config dir
```
### ExternalContainer

External Container of type external, starting from *0.4.0*, supports submitting optimizer tasks from outside AMS, provided that the user adds a container of type external to the configuration file.
```shell
containers:
   - name: externalContainer
     type: external
     properties:
```
Users can submit optimizer tasks in their own Flink task development platform or local Flink environment with the following configuration. The main parameters include:
```shell
flink run -m yarn-cluster  -ytm {EXECUTOR_TASKMANAGER_MEMORY} -yjm {EXECUTOR_JOBMANAGER_MEMORY}  -c com.netease.arctic.optimizer.flink.FlinkOptimizer  {ARCTIC_HOME}/plugin/optimize/OptimizeJob.jar -a {AMS_THRIFT_SERVER_URL} -qn {OPTIMIZE_GROUP_NAME} -p {EXECUTOR_PARALLELISM} -m {EXECUTOR_MEMORY}  --heart-beat 60000
```
相关参数说明如下表：

| 参数                             | 说明                                                         |
| -------------------------------- | ------------------------------------------------------------ |
| -ytm EXECUTOR_TASKMANAGER_MEMORY | Flink Task TM Memory Size                                       |
| -yjm EXECUTOR_JOBMANAGER_MEMORY  | Flink Task JM Memory Size                                       |
| ARCTIC_HOME                      | Arctic  root directory                                                 |
| -a AMS_THRIFT_SERVER_URL         | The address of the AMS thrift service, for example: thrift://10.196.98.23:18112, can be obtained from the config.yaml configuration. |
| -qn OPTIMIZE_GROUP_NAME          | The name of the optimize group is configured in config.yaml and can also be found in the front-end Optimizer-->Optimizer group, the container in this group must be of external type. |
| -p EXECUTOR_PARALLELISM          | Flink Task Parallelism                                             |
| -m EXECUTOR_MEMORY               | Execution memory, the sum of JM's memory and TM's memory for the Flink task, is used to report to AMS to count the optimizer's resource usage. |
| --heart-beat               | optimizer heartbeat reporting interval  |

## Optimizer Group
Optimizer Group (Optimizer resource group) is a concept introduced to divide Optimizer resources. An Optimizer Group can contain several optimizers with the same container type to facilitate the expansion and contraction of the resource group.

- name, the name of the optimizer group, which can be seen in the list of optimizer groups on the front-end page.
- container, the name of a container configured in containers.
- scheduling_policy, the scheduler group scheduling policy, the default value is quota, it will be scheduled according to the quota resources configured for each table, the larger the table quota is, the more optimizer resources it can take. There is also a configuration balanced that will balance the scheduling of each table, the longer the table has not been optimized, the higher the scheduling priority will be.
- properties, the default configuration under this group, is used as a configuration parameter for tasks when the optimize page is scaled out.

```shell
optimize_group:
  - name: flinkOp
    # container name, should be in the names of containers  
    container: flinkContainer
    # quota/balanced
    scheduling_policy: balanced
    properties:
      taskmanager.memory: 2048
      jobmanager.memory: 1024
```
## Optimizer Capacity Expansion and Reduction

You can click Scale-Out in the upper-right corner of the optimizers module in the dashboard to achieve fast scaling, set the optimizer group to be scaled by Resource Group, and configure the concurrency of a single optimizer task by Parallelism, and click OK to finish creating. A new record with the status of STARTING will be added to the list, and if it runs normally, the status will change to RUNNING when the page is refreshed.
![optimize-scale-out](../images/admin/optimizer_scale.png)
Users can also flexibly submit Optimizer tasks in their own scheduling platforms. For information on the parameters required for Optimizer tasks, please refer to the Deployment section [external](#external) .

Users can release an Optimizer task by clicking on `Release` for the record in the Optimizers list on the dashboard.

![release optimizer](../images/admin/optimizer_release.png)
???+ attention

    > Currently, only Optimizer tasks scaled through the dashboard can be released.