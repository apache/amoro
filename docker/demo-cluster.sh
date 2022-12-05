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


PROJECT_VERSION=0.4.0


CURRENT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"
ARCTIC_HOME="$( cd "$CURRENT_DIR/../" ; pwd -P )"

ARCTIC_POM=${ARCTIC_HOME}/pom.xml

if [ -f "${ARCTIC_POM}" ];then
  echo "Current dir in arctic project. parse version from ${ARCTIC_POM}"
  PROJECT_VERSION=`cat ${ARCTIC_POM} | grep 'arctic-parent' -C 3 | grep -Eo '<version>.*</version>' | awk -F'[><]' '{print $3}'`
fi



function usage() {
    cat <<EOF
Usage: $0 [options] [command]
Build for arctic demo docker images.

Commands:
    start                   Setup demo cluster
    stop                    Stop demo cluster and clean dockers

Options:
    -v    --version         Setup Arctic image version. default is ${PROJECT_VERSION}

EOF
}


COMMAND=none

i=1;
j=$#;
while [ $i -le $j ]; do
    case $1 in
      start|stop)
      COMMAND=$1;
      i=$((i+1))
      shift 1
      ;;

      "-v"|"--version")
      shift 1
      PROJECT_VERSION=$1
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


function create_docker_compose() {
  if [ -f "docker-compose.yml" ]; then
      echo "clean up older docker-compose.yml"
      rm docker-compose.yml
  fi

  cat <<EOT >> docker-compose.yml
version: "3"
services:
  namenode:
    image: arctic163/namenode
    hostname: namenode
    container_name: namenode
    environment:
      - CLUSTER_NAME=demo-cluster
      - CORE_CONF_hadoop_http_staticuser_user=root
      - CORE_CONF_hadoop_proxyuser_arctic_hosts=*
      - CORE_CONF_hadoop_proxyuser_arctic_groups=*
      - HDFS_CONF_dfs_replication=1
      - HDFS_CONF_dfs_permissions_enabled=false
      - HDFS_CONF_dfs_webhdfs_enabled=true
    networks:
      - arctic_network
    ports:
      - 10070:50070
      - 8020:8020
    volumes:
      - ./hadoop-config:/etc/hadoop

  datanode:
    image: arctic163/datanode
    container_name: datanode
    environment:
      - CLUSTER_NAME=demo-cluster
    hostname: datanode
    volumes:
      - ./hadoop-config:/etc/hadoop
    networks:
      - arctic_network
    ports:
      - 10075:50075
      - 10010:50010
    depends_on:
      - namenode 

  ams:
    image: arctic163/ams:${PROJECT_VERSION}
    container_name: ams
    ports:
      - 1630:1630
      - 1260:1260
    environment:
      - XMS_CONFIG=128
    networks:
      - arctic_network
    tty: true
    stdin_open: true 

  flink:
    image: arctic163/flink:${PROJECT_VERSION}
    container_name: flink
    ports:
      - 8081:8081
    networks:
      - arctic_network
    depends_on:
      - ams 

  mysql:
    container_name: mysql
    hostname: mysql
    image: mysql:8.0
    command: mysqld --max-connections=500
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_ROOT_HOST: "%"
      MYSQL_DATABASE: oltpbench
    networks:
      - arctic_network
    ports:
      - "3306:3306" 

  lakehouse-benchmark:
    image: arctic163/lakehouse-benchmark:latest
    container_name: lakehouse-benchmark
    hostname: lakehouse-benchmark
    depends_on:
      - mysql
    networks:
      - arctic_network
    tty: true
    stdin_open: true 

  lakehouse-benchmark-ingestion:
    image: arctic163/lakehouse-benchmark-ingestion:${PROJECT_VERSION}
    container_name: lakehouse-benchmark-ingestion
    hostname: lakehouse-benchmark-ingestion
    privileged: true
    networks:
      - arctic_network
    volumes:
      - ./ingestion-config/ingestion-conf.yaml:/usr/lib/lakehouse_benchmark_ingestion/conf/ingestion-conf.yaml
    depends_on:
      - mysql
      - ams
    ports:
      - 8082:8081
    tty: true
    stdin_open: true 

networks:
  arctic_network:
    driver: bridge
EOT
}

function create_ingestion_conf() {
  INGESTION_CONF_DIR=./ingestion-config
  INGESTION_CONF=${INGESTION_CONF_DIR}/ingestion-conf.yaml

  mkdir -p ${INGESTION_CONF_DIR}

  if [ -f ${INGESTION_CONF} ]; then
      echo "clean old file ${INGESTION_CONF}"
      rm ${INGESTION_CONF}
  fi

  cat <<EOT >> ${INGESTION_CONF}
source.type: mysql
source.database.name: oltpbench
source.username: root
source.password: password
source.hostname: mysql
source.port: 3306
source.table.name: *
source.parallelism: 8
arctic.metastore.url: thrift://ams:1260/demo_catalog
arctic.optimize.group.name: default
EOT
}


function start() {
  echo "SET ARCTIC_VERSION=${PROJECT_VERSION}"
  echo "generate ingestion conf"
  create_ingestion_conf

  echo "generate docker compose"
  create_docker_compose

  test -d ./hadoop-config && rm -rf ./hadoop-config
  echo "start cluster"
  docker-compose up -d
}

function stop() {
  docker-compose down
}

set +x

case "$COMMAND" in
  start)
    start
    ;;
  stop)
    stop
    ;;

  none)
    usage
    exit 1
    ;;
  *)
    echo "Unknown command type: $COMMAND"
    exit 1
    ;;
esac
