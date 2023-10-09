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
PROJECT_HOME="$( cd "$CURRENT_DIR/../" ; pwd -P )"
export PROJECT_HOME

cd $CURRENT_DIR

AMORO_VERSION=`cat $PROJECT_HOME/pom.xml | grep 'amoro-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}'`
AMORO_BINARY_PACKAGE=${PROJECT_HOME}/dist/target/amoro-${AMORO_VERSION}-bin.zip
FLINK_VERSION=1.15.3
HADOOP_VERSION=2.10.2
DEBIAN_MIRROR=http://deb.debian.org
APACHE_ARCHIVE=https://archive.apache.org/dist
OPTIMIZER_TARGET=${PROJECT_HOME}/ams/optimizer/target
OPTIMIZER_JOB=${OPTIMIZER_TARGET}/amoro-ams-optimizer-${AMORO_VERSION}-jar-with-dependencies.jar
AMORO_TAG=$AMORO_VERSION
ALSO_MAKE=true

function usage() {
    cat <<EOF
Usage: $0 [options] [image]
Build for Amoro demo docker images.

Images:
    quickdemo               Build Amoro QuickStart Image, for run flink ingestion job in quick-demo http://amoro.netease.com/quick-demo/
    namenode                Build a hadoop namenode container for quick start demo.
    datanode                Build a hadoop datanode container for quick start demo.
    optimizer-flink         Build official Amoro optimizer deployed with flink engine for production environments.
    amoro                   Build official Amoro image used for production environments.

Options:
    --flink-version         Flink binary release version, default is 1.15.3, format must be x.y.z
    --hadoop-version        Hadoop binary release version, default is 2.10.2, format must be x.y.z
    --apache-archive        Apache Archive url, default is https://archive.apache.org/dist
    --debian-mirror         Mirror url of debian, default is http://deb.debian.org
    --optimizer-job         Location of optimizer job
    --tag                   Tag for amoro/optimizer-flink/quickstart image.
    --also-make             Also make amoro when build quickdemo, if set to false, it will pull from hub or use exists dependency.
EOF
}


ACTION=amoro

i=1;
j=$#;
while [ $i -le $j ]; do
  case $1 in
    quickdemo|namenode|datanode|optimizer-flink|amoro)
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

    "--optimizer-job")
    shift 1
    OPTIMIZER_JOB=$1
    i=$((i+2))
    ;;

    "--tag")
    shift 1
    AMORO_TAG=$1
    i=$((i+2))
    ;;

    "--also-make")
    shift 1
    ALSO_MAKE=$1
    i=$((i+2))
    ;;

    *)
      echo "Unknown args of $1"
      usage
      exit 1
    ;;
  esac
done

FLINK_MAJOR_VERSION=${FLINK_VERSION%.*}

function print_env() {
  echo "SET FLINK_VERSION=${FLINK_VERSION}"
  echo "SET FLINK_MAJOR_VERSION=${FLINK_MAJOR_VERSION}"
  echo "SET HADOOP_VERSION=${HADOOP_VERSION}"
  echo "SET APACHE_ARCHIVE=${APACHE_ARCHIVE}"
  echo "SET DEBIAN_MIRROR=${DEBIAN_MIRROR}"
  echo "SET AMORO_VERSION=${AMORO_VERSION}"
  echo "SET AMORO_TAG=${AMORO_TAG}"
}

# print_image $IMAGE_NAME $TAG
function print_image() {
   IMAGE_REF=$1
   IMAGE_TAG=$2
   echo "=============================================="
   echo "          $IMAGE_REF:$IMAGE_TAG               "
   echo "=============================================="
   echo "Start Build ${IMAGE_REF}:${IMAGE_TAG} Image"
}


function build_quickstart() {
  IMAGE_REF="arctic163/quickstart"
  IMAGE_TAG=$AMORO_TAG
  print_image "$IMAGE_REF" "$IMAGE_TAG"
  FLINK_CONNECTOR_BINARY=${PROJECT_HOME}/flink/v${FLINK_MAJOR_VERSION}/flink-runtime/target/amoro-flink-runtime-${FLINK_MAJOR_VERSION}-${AMORO_VERSION}.jar

  if [ ! -f "${AMORO_BINARY_PACKAGE}" ]; then
      echo "Amoro Binary Release ${AMORO_BINARY_PACKAGE} is not exists, run 'mvn clean package -pl !trino' first. "
      exit 1
  fi
  if [ ! -f "${FLINK_CONNECTOR_BINARY}" ]; then
      echo "amoro-flink-connector not exists in ${FLINK_CONNECTOR_BINARY}, run 'mvn clean package -pl !trino' first. "
      exit  1
  fi

  set -x
  AMS_IMAGE_RELEASE_PACKAGE=${CURRENT_DIR}/quickstart/amoro-${AMORO_VERSION}-bin.zip
  FLINK_IMAGE_BINARY=${CURRENT_DIR}/quickstart/amoro-flink-runtime-${FLINK_VERSION}-${AMORO_VERSION}.jar
  cp ${AMORO_BINARY_PACKAGE} ${AMS_IMAGE_RELEASE_PACKAGE}
  cp ${FLINK_CONNECTOR_BINARY}  ${FLINK_IMAGE_BINARY}
  # dos2unix ${CURRENT_DIR}/quickstart/config.sh
  docker build -t $IMAGE_REF:$IMAGE_TAG \
    --build-arg AMORO_VERSION=${AMORO_VERSION} \
    --build-arg DEBIAN_MIRROR=${DEBIAN_MIRROR} \
    --build-arg APACHE_ARCHIVE=${APACHE_ARCHIVE} \
    --build-arg FLINK_VERSION=${FLINK_VERSION} \
    quickstart/.
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

function build_optimizer_flink() {
    IMAGE_REF=arctic163/optimizer-flink${FLINK_MAJOR_VERSION}
    IMAGE_TAG=$AMORO_TAG
    echo "=============================================="
    echo "           arctic163/optimizer-flink     "
    echo "=============================================="
    echo "Start Build ${IMAGE_REF}:${IMAGE_TAG} Image"

    FLINK_OPTIMIZER_JOB=${OPTIMIZER_JOB}

    if [ ! -f "${FLINK_OPTIMIZER_JOB}" ]; then
      BUILD_CMD="mvn clean package -pl ams/optimizer -am -e -DskipTests"
      echo "flink optimizer job not exists in ${FLINK_OPTIMIZER_JOB}"
      echo "please check the file or run '${BUILD_CMD}' first. "
      exit  1
    fi

    cp $FLINK_OPTIMIZER_JOB $CURRENT_DIR/optimizer-flink/OptimizeJob.jar
    docker build -t ${IMAGE_REF}:${IMAGE_TAG} \
      --build-arg FLINK_VERSION=$FLINK_VERSION \
      optimizer-flink/.
}

function build_amoro() {
  IMAGE_REF=arctic163/amoro
  IMAGE_TAG=$AMORO_TAG
  print_image $IMAGE_REF $IMAGE_TAG

  DIST_FILE=${PROJECT_HOME}/dist/target/amoro-${AMORO_VERSION}-bin.zip

  if [ ! -f "${DIST_FILE}" ]; then
    BUILD_CMD="mvn clean package -am -e -pl dist -DskipTests "
    echo "Amoro dist package is not exists in ${DIST_FILE}"
    echo "please check file or run '$BUILD_CMD' first"
  fi

  cp $DIST_FILE $CURRENT_DIR/amoro/amoro-${AMORO_VERSION}-bin.zip
  docker build -t ${IMAGE_REF}:${IMAGE_TAG} \
    --build-arg AMORO_VERSION=${AMORO_VERSION} \
    amoro/.
  return $?
}

function build_quickdemo() {
    IMAGE_REF=arctic163/quickdemo
    IMAGE_TAG=$AMORO_TAG

    print_image $IMAGE_REF "$IMAGE_TAG"


    FLINK_CONNECTOR_BINARY=${PROJECT_HOME}/flink/v${FLINK_MAJOR_VERSION}/flink-runtime/target/amoro-flink-runtime-${FLINK_MAJOR_VERSION}-${AMORO_VERSION}.jar

    if [ ! -f "${FLINK_CONNECTOR_BINARY}" ]; then
        echo "amoro-flink-connector not exists in ${FLINK_CONNECTOR_BINARY}, run 'mvn clean package -pl !trino' first. "
        exit  1
    fi

    if [ "${ALSO_MAKE}" == "true" ]; then
        echo "Build dependency Amoro image."
        build_amoro
        if [ "$?" -ne 0 ]; then
          echo "Build required Amor image failed."
          exit 1
        fi
    fi

    set -x
    FLINK_IMAGE_BINARY=${CURRENT_DIR}/quickdemo/amoro-flink-runtime-${FLINK_VERSION}-${AMORO_VERSION}.jar
    cp ${FLINK_CONNECTOR_BINARY}  ${FLINK_IMAGE_BINARY}
    # dos2unix ${CURRENT_DIR}/quickstart/config.sh
    docker build -t $IMAGE_REF:$IMAGE_TAG \
      --build-arg AMORO_TAG=${AMORO_TAG} \
      --build-arg AMORO_VERSION=${AMORO_VERSION} \
      --build-arg DEBIAN_MIRROR=${DEBIAN_MIRROR} \
      --build-arg APACHE_ARCHIVE=${APACHE_ARCHIVE} \
      --build-arg FLINK_VERSION=${FLINK_VERSION} \
      quickdemo/.
}


case "$ACTION" in
  quickdemo)
    print_env
    build_quickdemo
    ;;
  namenode)
    print_env
    build_namenode
    ;;
  datanode)
    print_env
    build_datanode
    ;;
  optimizer-flink)
    print_env
    build_optimizer_flink
    ;;
  amoro)
    print_env
    build_amoro
    ;;
  *)
    echo "Unknown image type: $ACTION"
    exit 1
    ;;
esac