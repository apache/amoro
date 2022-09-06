
FROM openjdk:8u332-jdk

RUN apt update \
    && apt-get install -y netcat \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet

WORKDIR /usr/local/arctic_flink
#You need to download flink-1.12.2-bin-scala_2.11.tgz by yourself and put it in the same directory as Dockerfile
COPY flink-1.12.7-bin-scala_2.12.tgz /usr/local/arctic_flink
ARG FLINK_VERSION=1.12.7
ARG SCALA_VERSION=2.12
ARG APACHE_FLINK_URL=mirrors.cloud.tencent.com/apache/flink
ARG HADOOP_VERSION=2.7.5
RUN tar -zxvf flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz \
    && wget https://repo1.maven.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-10.0/flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar \
    && wget https://github.com/NetEase/arctic/releases/download/v0.3.1-rc1/arctic-flink-runtime-1.12-0.3.1.jar \
    && cd flink-${FLINK_VERSION} \
    && cp examples/table/ChangelogSocketExample.jar lib \
    && cp ../arctic-flink-runtime-1.12-0.3.1.jar lib \
    && cp ../flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar lib
#You need to download hive-exec-2.1.1.jar by yourself and put it in the same directory as Dockerfile
COPY hive-exec-2.1.1.jar /usr/local/arctic_flink/flink-${FLINK_VERSION}/lib

ENV FLINK_HOME=/usr/local/arctic_flink/flink-${FLINK_VERSION} \
    PATH=${PATH}:${FLINK_HOME}/bin
WORKDIR ${FLINK_HOME}
COPY flink-conf.yaml ${FLINK_HOME}/conf/flink-conf.yaml

CMD ["/bin/bash"]


