---
title: "How To Benchmark"
url: benchmark-guide
disableSidebar: true
---
# Benchmark Guild

This guilde introduces detailed steps for executing the benchmark to validate performance of various data lake formats. 

By following the steps in the guild, you can learn about the analytical performance of different data lake table format. At the same time, you can flexibly adjust the test scenarios to obtain test results that better suit your actual scenario.

## Deploy testing environment

### Deploy by Docker

With [Docker-Compose](https://docs.docker.com/compose/), you can quickly set up an environment for performing the benchmark.
The detailed steps reference: [Lakehouse-benchmark](https://github.com/NetEase/lakehouse-benchmark/tree/master/docker/benchmark).

### Deploy manually

Alternatively, you can manually deploy the following components to set up the test environment：

| Component  | Version  | Description |  Installation Guide|
|:----------|:----------|:--------|:--------|
| MySQL    | 5.7+    | MySQL is used to generate TPC-C data for synchronization to data lakes. | [MySQL Installation Guide](https://dev.mysql.com/doc/mysql-installation-excerpt/5.7/en/) |
| Hadoop   | 2.3.7+  | Hadoop is used to provide the storage for data lakes. | [Ambari](https://ambari.apache.org/)    |
| Trino    | 380     | Trino is used to execute TPC-H queries for Iceberg and Mixed-Iceberg format tables. | [Trino Installation Guide](https://trino.io/docs/current/installation/deployment.html) |
| Amoro Trino Connector | 0.4.0 | To query Mixed-Iceberg Format tables in Trino, you need to install and configure the Amoro connector in Trino. | [Amoro Trino Connector](https://amoro.netease.com/docs/latest/trino/)    |
| Iceberg Trino Connector | 0.13.0 | To query Iceberg Format tables in Trino, you need to install and configure the Iceberg connector in Trino. | [Iceberg Trino Connector](https://trino.io/docs/current/connector/iceberg.html)    |
| Presto    | 274 | Presto is used to execute TPC-H queries for Hudi format tables. | [Presto Installation Guide](https://prestodb.io/docs/current/installation/deployment.html) |
| Hudi Presto Connector | 0.11.1  | To query Iceberg Format tables in Trino, you need to install and configure the Iceberg connector in Presto. | [Hudi Presto Connector](https://prestodb.io/docs/current/connector/hudi.html)    |
| AMS    | 0.4.0 | Amoro Management Service, support self-optimizing on tables during the test. | [AMS Installation Guide](https://amoro.netease.com/docs/latest/deployment/) |
| data-lake-benchmark | 21  | The core program of Benchmark which is responsible for generating test data, executing the testing process, and generating test results. | [Data Lake Benchmark](https://github.com/NetEase/data-lake-benchmark#readme)    |
| lakehouse-benchmark-ingestion | 1.0  | Data synchronization tool based on Flink-CDC which can synchronize data from  database to data lake in real-time. | [Lakehouse Benchmark Ingestion](https://github.com/NetEase/lakehouse-benchmark-ingestion)    |


## Benchmark steps
1. Configure the configuration file `config/mysql/sample_chbenchmark_config.xml` file of program `data-lake-benchmark`. Fill in the information of MySQL and parameter `scalefactor`.  `scalefactor` represents the number of warehouses, which controls the overall data volume. Generally, choose 10 or 100.

2. Generate static data into MySQL with command：
  ```shell
  java -jar lakehouse-benchmark.jar -b tpcc,chbenchmark -c config/mysql/sample_chbenchmark_config.xml --create=true --load=true
  ```

3. Configure the configuration file `config/ingestion-conf.yaml` file of program `lakehouse-benchmark-ingestion`. Fill in the information of MySQL.

4. Start the ingestion job to synchronize data form MySQL to data lake tables witch command:
  ```shell
  java -cp lakehouse-benchmark-ingestion-1.0-SNAPSHOT.jar com.netease.arctic.benchmark.ingestion.MainRunner -confDir [confDir] -sinkType [arctic/iceberg/hudi] -sinkDatabase [dbName]
  ```

5. Execute TPC-H benchmark on static data with command:
  ```shell
  java -jar lakehouse-benchmark.jar -b chbenchmarkForTrino -c config/trino/trino_chbenchmark_config.xml --create=false --load=false --execute=true
  ```

6. Execute TPC-C program to continuously write data into MYSQL witch command:
  ```shell
  java -jar lakehouse-benchmark.jar -b tpcc,chbenchmark -c config/mysql/sample_chbenchmark_config.xml --execute=true -s 5
  ```

7. Execute TPC-H benchmark on dynamic data with command:
  ```shell
  java -jar lakehouse-benchmark.jar -b chbenchmarkForTrino -c config/trino/trino_chbenchmark_config.xml --create=false --load=false --execute=true
  ```

8. Obtain the benchmark results in the `result` directory of the `data-lake-benchmark` project.
   
9. Repeat step 7 to obtain benchmark results for different points in time.