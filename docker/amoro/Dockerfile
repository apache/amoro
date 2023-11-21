
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


# Usage:
#   Run the docker command below under project dir.
#      docker build \
#        --file docker/amoro/Dockerfile \
#        --tag arctic163/amoro:tagname
#        .

FROM eclipse-temurin:8-jdk-jammy as builder

# Add the entire project to the build container, unzip it,
# and remove flink-optimizer to reduce the container size.

ADD . /workspace/amoro
WORKDIR /workspace/amoro

RUN apt-get update \
    && apt-get install -y unzip

RUN AMORO_VERSION=`cat pom.xml | grep 'amoro-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}'` \
    && cp dist/target/*.zip /usr/local \
    && unzip /usr/local/amoro-${AMORO_VERSION}-bin.zip -d /usr/local \
    && rm /usr/local/amoro-${AMORO_VERSION}/plugin/optimizer/flink -rf \
    && mv /usr/local/amoro-${AMORO_VERSION} /usr/local/amoro \
    && rm -rf /workspace/amoro


FROM eclipse-temurin:8-jdk-jammy

ENV AMORO_HOME /usr/local/amoro
ENV AMORO_CONF_DIR ${AMORO_HOME}/conf
ENV LOG_LEVEL info
EXPOSE 1630 1260 1261

COPY ./docker/amoro/entrypoint.sh /
RUN chmod +x /entrypoint.sh
COPY --from=builder "/usr/local/amoro" "/usr/local/amoro"

WORKDIR ${AMORO_HOME}

RUN cd ${AMORO_HOME}/lib \
    && wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar

ENTRYPOINT ["/entrypoint.sh"]
CMD ["help"]

