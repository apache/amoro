
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

ARG ARCTIC_VERSION=0.4.0-SNAPSHOT
ARG RELEASE=v0.3.2-rc1
ARG DEBIAN_MIRROR=http://deb.debian.org

WORKDIR /usr/local/ams

RUN sed -i "s#http://deb.debian.org#${DEBIAN_MIRROR}#g" /etc/apt/sources.list

RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y net-tools \
    && apt-get install -y telnet \
    && apt-get install -y dos2unix \
    && apt-get clean

ARG AMS_FILE
WORKDIR /usr/local/ams

ADD arctic-${ARCTIC_VERSION}-bin.zip /usr/local/ams/arctic-${ARCTIC_VERSION}-bin.zip
RUN unzip arctic-${ARCTIC_VERSION}-bin.zip \
  && sed -i '/local_catalog/d' /usr/local/ams/arctic-${ARCTIC_VERSION}/conf/derby/ams-init.sql

WORKDIR /usr/local/ams/arctic-${ARCTIC_VERSION}

COPY config.sh  ./bin/config.sh
RUN find ./bin -name "*.sh" | xargs dos2unix
RUN mkdir -p ./logs && touch ./logs/ams-info.log


EXPOSE 1630/tcp
EXPOSE 1260/tcp

CMD ["bash","-c","./bin/ams.sh start && tail -f ./logs/ams-info.log"]


