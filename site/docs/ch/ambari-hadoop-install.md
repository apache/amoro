# 使用Ambari部署Hadoop手册
## 1.依赖软件安装
### 1.1 权限申请
> 1）所有节点申请 sudo /usr/sbin/ambari-agent,/usr/bin/rpm,/usr/bin/yum权限。  
2）Ambari Server节点申请 sudo /usr/sbin/ambari-server权限。   
### 1.2 Ambari Server节点上安装依赖软件
> sudo yum install -y postgresql.x86_64 postgresql-server.x86_64 python-devel.x86_64 zlib-devel mysql-connector-java gnu-free-sans-fonts.noarch mariadb.x86_64
### 1.3 安装Mysql，略
## 2.安装Ambari
### 2.1 下载地址
> http://archive.apache.org/dist/ambari/ambari-2.5.1/  
### 2.2 Ambari Server安装
> sudo rpm -ivh ambari-server-2.5.1.0.rpm
### 2.3 预制数据
> mysql -h <Mysql服务ip> -u <用户名> -P<Mysql服务端口> -p<Mysql数据库密码> -D <数据库名> -e "source /var/lib/ambari-server/resources/Ambari-DDL-MySQL-CREATE.sql"
### 2.4 配置Ambari
> sudo ambari-server setup  
> 命令行交互式形式进行配置，主要设置包括：启动用户、JDK版本、AmbariServer数据库配置等。
### 2.5 启动Ambari Server
> sudo ambari-server start  
> sudo ambari-server setup --jdbc-db=mysql --jdbc-driver=/usr/share/java/mysql-connector-java.jar  
### 2.6 登陆页面
> http://hostname:8080  
### 2.7 Ambari Agent安装
> sudo rpm -ivh ambari-agent-2.5.1.0.rpm 
> sudo ambari-agent reset <ambari_server_hostname>
### 2.8 启动Ambari Agent
> sudo ambari-agent start
## 3.安装Hadoop
### 3.1 准备工作
> 规划ZooKeeper、Hdfs、Yarn服务数据目录、日志目录存储路径。
### 3.2 进入ambari server页面，按以下步骤安装集群
> 1）设置一个集群名字，点下一步。  
2）选择安装版本，Use Public Repository， Skip Repository Base URL validation (Advanced)，点击下一步。  
3）填入agent服务器列表，此处使用hostname全名(FQDN)，选择Perform manual registration on hosts and do not use SSH，点击下一步。  
注意：节点上需要手动安装ambari agent服务。  
4）等待Confirm Hosts完成，如果失败，检查节点的Agent是否安装成功，如果是安装成功的，点击retry再试一次。点击下一步。  
5）全选服务进行安装，点击下一步。  
6）按照部署架构，编排Master服务，点击下一步。  
7）按照部署架构，选择Slave服务，选择一台计算节点安装client服务。  
### 3.3 修改组件配置
> 1）Misc配置修改
设置组件包下载地址，需要确保Ambari集群节点与软件仓库节点的连通性。  
2）Zookeeper配置主要修改内容  
Server进程内存设置是否合理。  
ZooKeeper服务日志目录是否放到指定目录。  
Server的数据目录是否放到指定目录。  
3）HDFS配置主要修改内容  
HDFS服务日志是否需要放到其它地方，默认在/var/log/hadoop-hdfs目录下  
NameNode的数据目录是否放到指定目录。  
DataNode的数据目录是否放到指定目录。  
NameNode进程内存设置是否合理。  
DataNode进程内存设置是否合理。  
4）Yarn配置主要修改内容  
Yarn服务日志是否放到指定目录。  
NodeManager的数据目录是否放到指定目录。  
ResourceManager进程内存设置是否合理。  
NodeManager进程内存设置是否合理。  
NodeManager节点可用于任务运行的CPU、内存数量是否合理。  
ResourceManager资源队列设置。  
5）Hive配置主要修改内容
> HiveMetasotre依赖mysql地址。  
HiveMetasotre的数据目录是否放到指定目录。    
HiveServer的数据目录是否放到指定目录。    
HiveMetasotre进程内存设置是否合理。    
HiveServer进程内存设置是否合理。  
6）点击下一步 确认组件没错，点击Deploy进行安装    
### 3.4 部署HDFS HA
>1）进行HDFS HA部署，Service->actions->enable namenode ha。  
2）设置HDFS NameSpace名字，点击下一步。  
3）按照部署架构选择另一个NN机器，以及JN机器，点击下一步。  
4）设置JN的数据目录：/srv/hadoop/0/hadoop/hdfs/journal，点击下一步。  
5）申请sudo /usr/bin/su的权限，包括namenode主备节点。  
6）按照提示执行命令（注意观察命令的执行机器，命令需要在不同NameNode机器上执行），一步一步往下即可。  
### 3.5 部署Yarn HA
>1）Service->Yarn->actions->enable resourcemanager ha。  
2）按照部署架构选择另一个RM节点即可，点击下一步，按照提示下一步，直到完成。  
### 3.6 部署Kerberos
> 1）Admin->Kerberos->Enable Kerberos。  
2）选择手动方式安装keberos  
3）填入先前部署的Kerberos相关信息，点击下一步。 
4）Install and Test Kerberos Client执行完成，点击下一步，检查配置信息，下一步，直到完成。  


