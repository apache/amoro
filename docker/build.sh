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
FLINK_PACKAGE_VERSION=1.14.6


function usage() {
    cat <<EOF
Usage: $0 [options] [image]
Build for arctic demo docker images.

Images:
    ams                     Build arctic metastore services.
    flink                   Build a flink container with arctic flink connector and other jars for quick start demo
    nn                      Build a hadoop namenode container for quick start demo
    dn                      Build a hadoop datanode container for quick start demo
    all                     Build all of image above.

Options:
    --flink-version         Flink binary release version, default is 1.14.6, format must be x.y.z

EOF
}

function debug() {
    echo $1
}



ACTION=all

i=1;
j=$#;
while [ $i -le $j ]; do
  case $1 in
    ams|flink|namenode|datanode|all)
    ACTION=$1;
    i=$((i+1))
    shift 1
    ;;

    '--flink-version')
    shift 1
    FLINK_PACKAGE_VERSION=$1
    i=$((i+2))
    shift 1
    ;;

    *)
      echo "Unknown args of $1"
      usage
      exit 1
    ;;
  esac
done


function build_ams() {
  echo "Start Build Arctic/AMS Image, Arctic Version: ${ARCTIC_VERSION}"

  if [ ! -f "${ARCTIC_BINARY_PACKAGE}" ]; then
      echo "Arctic Binary Release ${ARCTIC_BINARY_PACKAGE} is not exists, run 'mvn clean package -pl !trino' first. "
      exit 1
  fi

  AMS_IMAGE_RELEASE_PACKAGE=${CURRENT_DIR}/ams/arctic-${ARCTIC_VERSION}-bin.zip
  cp ${ARCTIC_BINARY_PACKAGE} ${AMS_IMAGE_RELEASE_PACKAGE}
  docker build -t arctic163/ams --build-arg ARCTIC_VERSION=${ARCTIC_VERSION} ams/.
}


function build_flink() {
  FLINK_VERSION=`echo $FLINK_PACKAGE_VERSION| grep -oP '\d+.\d+'`
  FLINK_CONNECTOR_BINARY=${ARCTIC_HOME}/flink/v${FLINK_VERSION}/flink-runtime/target/arctic-flink-runtime-${FLINK_VERSION}-${ARCTIC_VERSION}.jar

  echo "Start Build Arctic/Flink Image, Flink Version: ${FLINK_VERSION}, FLINK_PACKAGE_VERSION: ${FLINK_PACKAGE_VERSION}"
  if [ ! -f ${FLINK_CONNECTOR_BINARY} ]; then
      echo "arctic-flink-connector not exists in ${FLINK_CONNECTOR_BINARY}, run 'mvn clean package -pl !trino' first. "
      exit  1
  fi

  FLINK_IMAGE_BINARY=${CURRENT_DIR}/flink/arctic-flink-runtime-${FLINK_VERSION}-${ARCTIC_VERSION}.jar
  cp ${FLINK_CONNECTOR_BINARY}  ${FLINK_IMAGE_BINARY}
  docker build -t arctic163/flink --build-arg ARCTIC_VERSION=${ARCTIC_VERSION} \
    --build-arg FLINK_VERSION=${FLINK_PACKAGE_VERSION} \
    flink/.
}









case "$ACTION" in
  ams)
    build_ams
    ;;
  flink)
    build_flink
    ;;
  namenode)
    ;;
  datanode)
    ;;
  all)
    build_ams
    build_flink
    ;;
  *)
    echo "Unknown image type: $ACTION"
    exit 1
    ;;
esac