![logo](site/overrides/images/arctic_logo_for_git.png)

Welcome to arctic, arctic is a streaming lake warehouse system open sourced by NetEase.
 Arctic adds more real-time capabilities on top of iceberg and hive, and provides stream-batch unified, out-of-the-box metadata services for dataops, 
allowing Data lakes much more usable and practical.
## What is arctic
Arctic is a streaming lakehouse service built on top of apache iceberg table format. 
Through arctic, users could benefit optimized CDC、streaming update、fresh olap etc. on engines like flink, spark, and trino. 
Combined with efficient offline processing capabilities of data lakes, arctic can serve more scenarios where streaming and batch are fused. 
At the same time, the function of self-optimization、concurrent conflict resolution and standard management tools could effectively reduce the burden on users in data lake management and optimization.
![Introduce](site/overrides/images/arctic_introduce.png)

Arctic services are presented by deploying AMS, which can be considered as a replacement for HMS (Hive Metastore), or HMS for iceberg. 
Arctic uses iceberg as the base table format, but instead of hacking the iceberg implementation, it uses iceberg as a lib. 
Arctic's open overlay architecture can help large-scale offline data lakes quickly upgraded to real-time data lakes, without worrying about compatibility issues with the original data lakes, 
allowing data lakes to meet more real-time analysis, real-time risk control, Real-time training, feature engineering and other scenarios.
## Arctic features

* Efficient streaming updates based on primary keys
* Data auto bucketing and self-optimized for performance and efficiency
* Encapsulating data lake and message queue into a unified table to achieve lower-latency computing
* Provide standardized metrics, dashboard and related management tools for streaming lakehouse
* Support spark and flink to read and write data, support trino to query data
* 100% compatible with iceberg / hive table format and syntax
* Provide transactional guarantee for streaming and batch concurrent writing

## Modules

Arctic contains modules as below:

- `arctic-core` contains core abstractions and common implementions for other modules
- `arctic-flink` is the module for integrating with Apache Flink (use arctic-flink-runtime for a shaded version)
- `arctic-spark` is the module for integrating with Apache Spark (use arctic-spark-runtime for a shaded version)
- `arctic-trino` now provides query integrating with apache trino, built on JDK11
- `arctic-optimizing` exposes optimizing container/group api and provides default implemetion
- `arctic-ams` is arctic meta service module
  - `ams-api` contains ams thrift api
  - `ams-dashboard` is the dashboard frontend for ams
  - `ams-server` is the backend server for ams

## Building

Arctic is built using Maven with Java 1.8 and Java 11(only for `trino` module).

* To build Trino module need config `toolchains.xml` in `${user.home}/.m2/` dir, the content is
```
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>11</version>
            <vendor>sun</vendor>
        </provides>
        <configuration>
            <jdkHome>${yourJdk11Home}</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```
* To invoke a build and run tests: `mvn package -P toolchain`
* To skip tests: `mvn -DskipTests package -P toolchain`

## Engines supported

Arctic support multiple processing engines as below:

| Processing Engine | Version           |
| ----------------- | ----------------- |
| Flink             | 1.12.x and 1.14.x |
| Spark             | 3.1+              |
| Trino             | 380               |

## Quickstart

Visit [https://arctic.netease.com/ch/getting-started/](https://arctic.netease.com/ch/getting-started/) to quickly explore what arctic can do.
