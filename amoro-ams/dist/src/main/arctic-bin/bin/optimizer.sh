#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

CURRENT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"

. $CURRENT_DIR/load-config.sh

LIB_PATH=$AMORO_HOME/lib
export CLASSPATH=$AMORO_CONF_DIR/optimize:$LIB_PATH/:$(find $LIB_PATH/ -type f -name "*.jar" | paste -sd':' -)
if [ -z "$(find $LIB_PATH/ -type f -name "*.jar" | paste -sd':' -)" ]; then
  echo "Launching the localOptimize task lacks relevant jars, please check" >&2
  exit 1
fi

ARGS=${@:3}

if [ -z "$OPTIMIZER_LOG_DIR_NAME" ]; then
  OPTIMIZER_LOG_DIR=$AMORO_LOG_DIR/optimizer-$(date +%s)
else
  OPTIMIZER_LOG_DIR=$AMORO_LOG_DIR/$OPTIMIZER_LOG_DIR_NAME
fi
STDERR_LOG=${OPTIMIZER_LOG_DIR}/optimizer.log.err

if [ ! -d "${OPTIMIZER_LOG_DIR}" ]; then
  mkdir -p "${OPTIMIZER_LOG_DIR}"
fi
if [ ! -f $STDERR_LOG ];then
    touch $STDERR_LOG
fi

JAVA_OPTS="-Xmx$2m -Dlog.home=${OPTIMIZER_LOG_DIR}"
RUN_SERVER="org.apache.amoro.optimizer.standalone.StandaloneOptimizer"
CMDS="$JAVA_RUN $JAVA_OPTS $RUN_SERVER $ARGS"

start() {
  nohup $CMDS > /dev/null 2>"$STDERR_LOG" &
  echo "Amoro Optimizer started in background with PID $!"
}

start-foreground() {
  $CMDS
}

#0:pid bad and proc OK;   1:pid ok and proc bad;    2:pid bad
function status(){
    test -e ${PID} || return 2
    test -n "$(ps -p $(cat ${PID}) -o pid=)" && return 0 || return 1
}

case "$1" in
  start)
    start
    ;;
  start-foreground)
    start-foreground
    ;;
  stop)
    status;
    if [ $? -ne 0 ]; then
        echo "proc not running."
    else
        echo 'stopping app server.'
        stop
    fi
    ;;
  status)
    status;
    if [ $? -eq 0 ];then
        echo 'running.'
    else
        echo 'not running.'
        exit 1
    fi
    ;;
  pid)
    status
    if [ $? -eq 0 ]; then
      cat $PID
    else
      echo "not running"
    fi
    ;;
  *)
    echo "Usage: $0 {start|start-foreground|stop|status|pid} <memory_limit_in_MB> [additional_args]"
    exit 1
    ;;
esac