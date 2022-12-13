
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8u332-jdk

ARG FLINK_VERSION=1.12.7
ARG FLINK_HADOOP_VERSION=2.8.3-10.0
ARG HIVE_VERSION=2.1.1
ARG APACHE_ARCHIVE=https://archive.apache.org/dist
ARG MAVEN_URL=https://repo.maven.apache.org/maven2
ARG DEBIAN_MIRROR=http://deb.debian.org

RUN sed -i "s#http://deb.debian.org#${DEBIAN_MIRROR}#g" /etc/apt/sources.list

RUN apt update \
    && apt-get install -y netcat \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet


RUN mkdir /usr/local/flink \
  && cd /usr/local/flink \
  && wget ${APACHE_ARCHIVE}/flink/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_2.12.tgz \
  && tar -zxvf flink-${FLINK_VERSION}-bin-scala_2.12.tgz

RUN cd /usr/local/flink/flink-${FLINK_VERSION}/lib \
    && cp ../examples/table/ChangelogSocketExample.jar . \
    && wget ${MAVEN_URL}/org/apache/flink/flink-shaded-hadoop-2-uber/${FLINK_HADOOP_VERSION}/flink-shaded-hadoop-2-uber-${FLINK_HADOOP_VERSION}.jar \
    && wget ${MAVEN_URL}/org/apache/hive/hive-exec/${HIVE_VERSION}/hive-exec-${HIVE_VERSION}.jar

ENV FLINK_HOME=/usr/local/flink/flink-${FLINK_VERSION} \
    PATH=${PATH}:${FLINK_HOME}/bin

WORKDIR ${FLINK_HOME}

EXPOSE 8081/tcp

ADD arctic-flink-runtime-*.jar /usr/local/flink/flink-${FLINK_VERSION}/lib/
COPY flink-conf.yaml ${FLINK_HOME}/conf/flink-conf.yaml

CMD ["/bin/bash", "-c", "./bin/start-cluster.sh && tail -f ./log/`ls log | grep flink--standalonesession | grep log`"]


