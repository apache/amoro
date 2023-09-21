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
AMORO_HOME="$( cd "$CURRENT_DIR/../" ; pwd -P )"
export AMORO_HOME

PRGDIR=$(dirname $0)
PRGDIR=$(cd $PRGDIR;pwd)
source ${PRGDIR}/config.sh


CURRENT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"
APP_HOME=$CURRENT_DIR/../


if [[ -d $JAVA_HOME ]]; then
    JAVA_RUN=$JAVA_HOME/bin/java
else
    JAVA_RUN=java
fi
XMX=$XMX_CONFIG
XMS=$XMS_CONFIG

JAVA_OPTS="-server -Xloggc:$AMORO_HOME/logs/gc.log -XX:+IgnoreUnrecognizedVMOptions -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M \
-Xms${XMS}m -Xmx${XMX}m \
-verbose:gc -XX:+PrintGCDetails \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.net=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED \
--add-opens=java.base/sun.security.action=ALL-UNNAMED \
--add-opens=java.base/sun.util.calendar=ALL-UNNAMED \
"


if [ ! -z "$JMX_REMOTE_PORT_CONFIG" ];then
        JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.port=${JMX_REMOTE_PORT_CONFIG} "
fi

if [ -z "${LANG_CONFIG}" ];then
        export LANG=zh_CN.GBK
else
        export LANG=${LANG_CONFIG}
fi

#LD_LIBRARY_PATH
if [ -d "$APP_HOME/solib" ]; then
        export LD_LIBRARY_PATH=$APP_HOME/solib
fi

nss_java_agent_path=`ls -1 $APP_HOME/conf/sentry-javaagent-home/sentry-javaagent-premain-*.jar 2>/dev/null | tail -n1`
nss_java_agent_lib=$APP_HOME/lib
if [ -f "$nss_java_agent_path" ]; then
   JAVA_OPTS="${JAVA_OPTS} -javaagent:$nss_java_agent_path"
   JAVA_OPTS="${JAVA_OPTS} -Dsentry_collector_libpath=$nss_java_agent_lib"
fi

if [ ! -z "$JVM_EXTRA_CONFIG" ];then
    JAVA_OPTS="${JAVA_OPTS} ${JVM_EXTRA_CONFIG}"
fi

RUN_SERVER="com.netease.arctic.server.ArcticServiceContainer"
WORKDIR=$AMORO_HOME
RUNSERVER=$RUN_SERVER
LIB_PATH=$AMORO_HOME/lib
WORKDIR=$AMORO_HOME
LOG_DIR=${WORKDIR}/logs
STDERR_LOG=${WORKDIR}/logs/app.log.err
PID=${WORKDIR}/run/app.pid

if [ ! -d "$LOG_DIR" ]; then
    mkdir "$LOG_DIR"
fi

if [ ! -d "${WORKDIR}/run" ]; then
    mkdir "${WORKDIR}/run"
fi

if [ ! -f $PID_PATH ];then
    touch $PID_PATH
fi

if [ ! -f $STDERR_LOG ];then
    touch $STDERR_LOG
fi

if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xms512m -Xmx512m -verbose:gc -XX:+PrintGCDetails"
fi

if [ -z "$RUNSERVER" ]; then
    echo "RunServer not exist, please check !"
    exit 1
fi

if [ -z "$AMORO_CONF_DIR" ]; then
    AMORO_CONF_DIR=$AMORO_HOME/conf/
fi

export CLASSPATH=$AMORO_CONF_DIR:$LIB_PATH/:$(find $LIB_PATH/ -type f -name "*.jar" | paste -sd':' -)
CMDS="$JAVA_RUN -Dlog.home=${LOG_DIR} -Dlog.dir=${LOG_DIR} -Duser.dir=${AMORO_HOME}  $JAVA_OPTS ${RUNSERVER}"
#0:pid bad and proc OK;   1:pid ok and proc bad;    2:pid bad
function status(){
    test -e ${PID} || return 2
    test -n "$(ps -p $(cat ${PID}) -o pid=)" && return 0 || return 1
}

function start() {
  nohup ${CMDS} >/dev/null 2>>${STDERR_LOG} &
    if [ $? -ne 0 ]; then
        echo "start failed."
    fi
    echo $! > ${PID}; sleep 1.5
    if status ; then
        echo "process start success."; return 0
    else
        echo "process start failed."; return 1
    fi
}
function stop() {
    status && kill $(cat ${PID})
    if ! status; then
        rm -f ${PID};
        echo "stop success."; return 0
    fi
    
    kill_times=0
    while status
    do
        sleep 1
        let kill_times++
        if [ ${kill_times} -eq 10 ]
        then
            kill -9 $(cat ${PID})
            sleep 3; break
        fi
    done

    if status; then
        echo "stop failed. process is still running."; return 1
    else
        rm -f ${PID};
        echo "stop success."; return 0
    fi
}

case "$1" in
    start)
        status;
        status_return=$?
        if [ $status_return -eq 2 ]; then
            echo 'starting app server.'
            start
        elif [ $status_return -eq 0 ]; then
            echo "alreadly running. start app failed." 
        else 
            echo "the pid file exists but porc is down; will delete ths pidfile ${PID} and starting app server."
            start
        fi
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
    restart)
        stop && sleep 3 && start
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
        cat $PID
        ;;
    *)
        echo "Usage $0 start|stop|restart|status|pid"
        exit 1
        ;;
esac
