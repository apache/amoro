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

ARCTIC_VERSION=`cat $ARCTIC_HOME/pom.xml | grep 'arctic-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}'`
ARCTIC_BINARY_PACKAGE=${ARCTIC_HOME}/dist/target/arctic-${ARCTIC_VERSION}-bin.zip
FLINK_VERSION=1.15.3
HADOOP_VERSION=2.10.2
DEBIAN_MIRROR=http://deb.debian.org
APACHE_ARCHIVE=https://archive.apache.org/dist


function usage() {
    cat <<EOF
Usage: $0 [options] [image]
Build for arctic demo docker images.

Images:
    ams                     Build arctic metastore services.
    flink                   Build a flink container with arctic flink connector and other jars for quick start demo
    namenode                Build a hadoop namenode container for quick start demo
    datanode                Build a hadoop datanode container for quick start demo
    all                     Build all of image above.

Options:
    --flink-version         Flink binary release version, default is 1.15.3, format must be x.y.z
    --hadoop-version        Hadoop binary release version, default is 2.10.2, format must be x.y.z
    --apache-archive        Apache Archive url, default is https://archive.apache.org/dist
    --debian-mirror         Mirror url of debian, default is http://deb.debian.org
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
    FLINK_VERSION=$1
    i=$((i+2))
    shift 1
    ;;

    "--hadoop-version")
    shift 1
    HADOOP_VERSION=$1
    i=$((i+2))
    shift 1
    ;;

    "--apache-archive")
    shift 1
    APACHE_ARCHIVE=$1
    i=$((i+2))
    shift 1
    ;;

    "--debian-mirror")
    shift 1
    DEBIAN_MIRROR=$1
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

function print_env() {
  echo "SET FLINK_VERSION=${FLINK_VERSION}"
  echo "SET HADOOP_VERSION=${HADOOP_VERSION}"
  echo "SET APACHE_ARCHIVE=${APACHE_ARCHIVE}"
  echo "SET DEBIAN_MIRROR=${DEBIAN_MIRROR}"
  echo "SET ARCTIC_VERSION=${ARCTIC_VERSION}"
}


function build_ams() {
  echo "=============================================="
  echo "               arctic163/ams                "
  echo "=============================================="
  echo "Start Build arctic163/ams Image, Arctic Version: ${ARCTIC_VERSION}"

  if [ ! -f "${ARCTIC_BINARY_PACKAGE}" ]; then
      echo "Arctic Binary Release ${ARCTIC_BINARY_PACKAGE} is not exists, run 'mvn clean package -pl !trino' first. "
      exit 1
  fi

  set -x
  AMS_IMAGE_RELEASE_PACKAGE=${CURRENT_DIR}/ams/arctic-${ARCTIC_VERSION}-bin.zip
  cp ${ARCTIC_BINARY_PACKAGE} ${AMS_IMAGE_RELEASE_PACKAGE}
  # dos2unix ${CURRENT_DIR}/ams/config.sh
  docker build -t arctic163/ams --build-arg ARCTIC_VERSION=${ARCTIC_VERSION} \
    --build-arg DEBIAN_MIRROR=${DEBIAN_MIRROR} \
    ams/.
  
  if [ $? == 0 ]; then
      IMAGE_ID=`docker images |grep 'arctic163/ams' |grep 'latest' |awk '{print $3}' `
      docker tag ${IMAGE_ID} arctic163/ams:${ARCTIC_VERSION}
  fi
}


function build_flink() {
  echo "=============================================="
  echo "               arctic163/flink                 "
  echo "=============================================="
  FLINK_MAJOR_VERSION=`echo $FLINK_VERSION| grep -oE '[0-9]+.[0-9]+'`
  FLINK_CONNECTOR_BINARY=${ARCTIC_HOME}/flink/v${FLINK_MAJOR_VERSION}/flink-runtime/target/arctic-flink-runtime-${FLINK_MAJOR_VERSION}-${ARCTIC_VERSION}.jar

  echo "Start Build arctic163/flink Image, Flink Version: ${FLINK_VERSION}"
  if [ ! -f ${FLINK_CONNECTOR_BINARY} ]; then
      echo "arctic-flink-connector not exists in ${FLINK_CONNECTOR_BINARY}, run 'mvn clean package -pl !trino' first. "
      exit  1
  fi

  set -x
  FLINK_IMAGE_BINARY=${CURRENT_DIR}/flink/arctic-flink-runtime-${FLINK_VERSION}-${ARCTIC_VERSION}.jar
  cp ${FLINK_CONNECTOR_BINARY}  ${FLINK_IMAGE_BINARY}
  docker build -t arctic163/flink \
    --build-arg FLINK_VERSION=${FLINK_VERSION} \
    --build-arg APACHE_ARCHIVE=${APACHE_ARCHIVE} \
    --build-arg DEBIAN_MIRROR=${DEBIAN_MIRROR} \
    flink/.

  if [ $? == 0 ]; then
        IMAGE_ID=`docker images |grep 'arctic163/flink' |grep 'latest' |awk '{print $3}' `
        docker tag ${IMAGE_ID} arctic163/flink:${ARCTIC_VERSION}
    fi
}


function build_namenode() {
  echo "=============================================="
  echo "               arctic163/namenode     "
  echo "=============================================="
  echo "Start Build arctic163/namenode Image"

  set -x

  find ./namenode -name "*.sh" | dos2unix
  docker build -t arctic163/namenode \
    --build-arg HADOOP_VERSION=${HADOOP_VERSION} \
    --build-arg APACHE_ARCHIVE=${APACHE_ARCHIVE} \
    --build-arg DEBIAN_MIRROR=${DEBIAN_MIRROR} \
    namenode/.
}

function build_datanode() {
  echo "=============================================="
  echo "               arctic163/datanode     "
  echo "=============================================="
  echo "Start Build arctic163/datanode Image"

  set -x

  find ./datanode -name "*.sh" | dos2unix
  docker build -t arctic163/datanode \
    --build-arg HADOOP_VERSION=${HADOOP_VERSION} \
    --build-arg APACHE_ARCHIVE=${APACHE_ARCHIVE} \
    --build-arg DEBIAN_MIRROR=${DEBIAN_MIRROR} \
    datanode/.
}





case "$ACTION" in
  ams)
    print_env
    build_ams
    ;;
  flink)
    print_env
    build_flink
    ;;
  namenode)
    print_env
    build_namenode
    ;;
  datanode)
    print_env
    build_datanode
    ;;
  all)
    print_env
    build_ams
    build_flink
    build_namenode
    build_datanode
    ;;
  *)
    echo "Unknown image type: $ACTION"
    exit 1
    ;;
esac