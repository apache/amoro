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


AMORO_TAG=master-snapshot


CURRENT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"


DOCKER_COMPOSE="${CURRENT_DIR}/docker-compose.yml"
HADOOP_CONF="${CURRENT_DIR}/hadoop-config"
VOLUMES_DIR="${CURRENT_DIR}/volumes"

function usage() {
    cat <<EOF
Usage: $0 [options] [command]
Build for Amoro demo docker images.

Commands:
    start                   Setup demo cluster
    stop                    Stop demo cluster and remove containers, volume data will be kept.
    clean                   clean volume data.

Options:
    -v    --version         Setup Amoro image version. default is ${AMORO_TAG}

EOF
}


COMMAND=none

i=1;
j=$#;
while [ $i -le $j ]; do
    case $1 in
      start|stop|clean)
      COMMAND=$1;
      i=$((i+1))
      shift 1
      ;;

      "-v"|"--version")
      shift 1
      AMORO_TAG=$1
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
  echo "Write docker-compose file to $DOCKER_COMPOSE"
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
      - CORE_CONF_hadoop_proxyuser_amoro_hosts=*
      - CORE_CONF_hadoop_proxyuser_amoro_groups=*
      - HDFS_CONF_dfs_replication=1
      - HDFS_CONF_dfs_permissions_enabled=false
      - HDFS_CONF_dfs_webhdfs_enabled=true
    networks:
      - amoro_network
    ports:
      - 10070:50070
      - 8020:8020
    volumes:
      - ${HADOOP_CONF}:/etc/hadoop
      - ${VOLUMES_DIR}/namenode:/hadoop/dfs/name

  datanode:
    image: arctic163/datanode
    container_name: datanode
    environment:
      - CLUSTER_NAME=demo-cluster
    hostname: datanode
    volumes:
      - ${HADOOP_CONF}:/etc/hadoop
      - ${VOLUMES_DIR}/datanode:/hadoop/dfs/data
    networks:
      - amoro_network
    ports:
      - 10075:50075
      - 10010:50010
    depends_on:
      - namenode 

  amoro:
    image: arctic163/amoro:${AMORO_TAG}
    container_name: amoro
    ports:
      - 1630:1630
      - 1260:1260
    networks:
      - amoro_network
    volumes:
      - ${VOLUMES_DIR}/amoro:/tmp/amoro
    command: "ams"
    tty: true
    stdin_open: true

  quickdemo:
    image: arctic163/quickdemo:${AMORO_TAG}
    container_name: quickdemo
    ports:
      - 8081:8081
    networks:
      - amoro_network
    tty: true
    stdin_open: true

networks:
  amoro_network:
    driver: bridge
EOT
}


function start() {
  echo "SET AMORO_VERSION=${AMORO_TAG}"

  echo "generate docker compose"
  if [ ! -f "$DOCKER_COMPOSE" ]; then
    create_docker_compose
  fi

  echo "start cluster"
  docker-compose up -d
}

function stop() {
  docker-compose down
}

function clean() {
  test -f "$DOCKER_COMPOSE" && rm "$DOCKER_COMPOSE"
  test -d "${HADOOP_CONF}" && rm "${HADOOP_CONF}" -rf
  test -d "${VOLUMES_DIR}" && rm "${VOLUMES_DIR}" -rf
}

set +x

case "$COMMAND" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  clean)
    clean
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
