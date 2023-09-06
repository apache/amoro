
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

ARG AMORO_VERSION=0.6.0-SNAPSHOT
ARG DEBIAN_MIRROR=http://deb.debian.org

WORKDIR /usr/local/ams

RUN sed -i "s#http://deb.debian.org#${DEBIAN_MIRROR}#g" /etc/apt/sources.list

RUN apt update \
    && apt-get install -y vim \
    && apt-get install -y netcat \
    && apt-get install -y net-tools \
    && apt-get install -y telnet \
    && apt-get install -y dos2unix \
    && apt-get clean



WORKDIR /usr/local/ams
ADD amoro-${AMORO_VERSION}-bin.zip /usr/local/ams/amoro-${AMORO_VERSION}-bin.zip
RUN unzip amoro-${AMORO_VERSION}-bin.zip \
    && rm -f amoro-${AMORO_VERSION}-bin.zip
WORKDIR /usr/local/ams/amoro-${AMORO_VERSION}
COPY localOptimize.sh  ./bin/localOptimize.sh
RUN find ./bin -name "*.sh" | xargs dos2unix
RUN mkdir -p ./logs && touch ./logs/ams-info.log
RUN ln -s /usr/local/ams/amoro-${AMORO_VERSION} /usr/local/amoro


ENV AMORO_HOME=/usr/local/amoro