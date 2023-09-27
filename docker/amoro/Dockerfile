
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

ARG AMORO_VERSION=master-SNAPSHOT
ARG APACHE_ARCHIVE=https://archive.apache.org/dist
ARG MAVEN_URL=https://repo.maven.apache.org/maven2/
ARG DEBIAN_MIRROR=http://deb.debian.org

WORKDIR /usr/local
ADD amoro-${AMORO_VERSION}-bin.zip /usr/local/amoro-${AMORO_VERSION}-bin.zip
RUN unzip amoro-${AMORO_VERSION}-bin.zip \
    && rm /usr/local/amoro-${AMORO_VERSION}/plugin/optimize -rf




FROM openjdk:8u332-slim-buster
COPY entrypoint.sh /

ARG AMORO_VERSION=master-SNAPSHOT


COPY --from=0 "/usr/local/amoro-${AMORO_VERSION}" "/usr/local/amoro"
RUN chmod +x /entrypoint.sh

WORKDIR /usr/local/amoro

ENV AMORO_HOME /usr/local/amoro
ENV LOG_LEVEL info
EXPOSE 1630 1260 1261
ENTRYPOINT ["/entrypoint.sh"]
CMD ["help"]

