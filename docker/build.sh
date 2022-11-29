#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#  *
#     http://www.apache.org/licenses/LICENSE-2.0
#  *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

CURRENT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"
ARCTIC_HOME="$( cd "$CURRENT_DIR/../" ; pwd -P )"
export ARCTIC_HOME

ARCTIC_VERSION=`cat $ARCTIC_HOME/pom.xml |grep 'arctic-parent' -C 3 |grep -oP '(?<=<version>).*(?=</version>)'`
ARCTIC_BINARY_PACKAGE=${ARCTIC_HOME}/dist/target/arctic-${ARCTIC_VERSION}-bin.zip
AMS_IMAGE_RELEASE_PACKAGE=${CURRENT_DIR}/ams/arctic-${ARCTIC_VERSION}-bin.zip


function build_ams() {
  if [ ! -f "${ARCTIC_BINARY_PACKAGE}" ]; then
      echo "Arctic Binary Release ${ARCTIC_BINARY_PACKAGE} is not exists, run 'mvn clean -pl !trino' first. "
      exit 1
  fi
  cp ${ARCTIC_BINARY_PACKAGE} ${AMS_IMAGE_RELEASE_PACKAGE}
  docker build -t arctic/ams --build-arg ARCTIC_VERSION=${ARCTIC_VERSION} ams/.
}











case "$1" in
  ams)
    build_ams
    ;;
  flink)
    ;;
  namenode)
    ;;
  datanode)
    ;;
  *)
    ;;
esac