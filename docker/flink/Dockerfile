
FROM openjdk:8u332-jdk

ARG ARCTIC_VERSION=0.3.2
ARG RELEASE=v0.3.2-rc1

ARG FLINK_BINARY_VERSION=1.12
ARG FLINK_VERSION=1.12.7
ARG SCALA_VERSION=2.12
ARG HADOOP_VERSION=2.7.5
ARG HIVE_EXE_VERSION=2.1.1

ENV FLINK_HOME=/usr/local/flink

RUN apt update && \
    apt-get install -y netcat && \
    apt-get install -y vim && \
    apt-get install -y net-tools && \
    apt-get install -y telnet && \
    apt-get clean

RUN wget https://archive.apache.org/dist/flink/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz && \
    tar -zxvf flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz -C /usr/local && \
    ln -s /usr/local/flink-${FLINK_VERSION} ${FLINK_HOME} && \
    rm flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz && \
    wget https://repo1.maven.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-10.0/flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar -P ${FLINK_HOME}/lib && \
    wget https://github.com/NetEase/arctic/releases/download/${RELEASE}/arctic-flink-runtime-${FLINK_BINARY_VERSION}-${ARCTIC_VERSION}.jar -P ${FLINK_HOME}/lib && \
    wget https://repo1.maven.org/maven2/org/apache/hive/hive-exec/${HIVE_EXE_VERSION}/hive-exec-${HIVE_EXE_VERSION}.jar -P ${FLINK_HOME}/lib && \
    cp ${FLINK_HOME}/examples/table/ChangelogSocketExample.jar lib

WORKDIR ${FLINK_HOME}



