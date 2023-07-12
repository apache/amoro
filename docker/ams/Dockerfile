
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
ARG ARCTIC_VERSION=0.5.0
ARG DEBIAN_MIRROR=http://deb.debian.org
ARG PROMETHEUS_AGENT_VERSION=0.18.0

WORKDIR /usr/local/ams

RUN sed -i "s#http://deb.debian.org#${DEBIAN_MIRROR}#g" /etc/apt/sources.list

RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet \
    && apt-get install -y dos2unix \
    && apt-get install -y wget \
    && apt-get clean

ARG AMS_FILE
WORKDIR /usr/local/ams

ADD arctic-${ARCTIC_VERSION}-bin.zip /usr/local/ams/arctic-${ARCTIC_VERSION}-bin.zip
RUN unzip arctic-${ARCTIC_VERSION}-bin.zip
RUN mv arctic-${ARCTIC_VERSION}/ arctic/

WORKDIR /usr/local/ams/arctic

COPY config.sh  ./bin/config.sh
RUN find ./bin -name "*.sh" | xargs dos2unix
RUN mkdir -p ./logs && touch ./logs/ams-info.log

RUN wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/$PROMETHEUS_AGENT_VERSION/jmx_prometheus_javaagent-$PROMETHEUS_AGENT_VERSION.jar
RUN mv jmx_prometheus_javaagent-$PROMETHEUS_AGENT_VERSION.jar ./plugin
ADD conf/prometheus-jmx-config.yaml ./conf/prometheus-jmx-config.yaml

EXPOSE 1630/tcp
EXPOSE 9093/tcp
EXPOSE 8088/tcp

CMD ["bash","-c","./bin/ams.sh start && tail -f ./logs/ams-info.log"]