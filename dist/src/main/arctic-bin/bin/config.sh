#!/bin/sh
################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
################################################################################

ulimit -n
CURRENT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"
APP_HOME=$CURRENT_DIR/../
JAVA_HOME_CONFIG=""
XMX_CONFIG="8196"
XMS_CONFIG="8196"
MAX_PERM_CONFIG="128"
RUN_SERVER="com.netease.arctic.ams.server.ArcticMetaStore"
JMX_REMOTE_PORT_CONFIG=""
APPNAME_CONFIG="arctic-rest-server"
JVM_EXTRA_CONFIG="-XX:NewRatio=1 -XX:SurvivorRatio=3"
LANG_CONFIG="en_US.UTF-8"
#========auto deploy config.sh end========

if [ -z "{$JAVA_HOME_CONFIG}" ] && ! type java > /dev/null 2> /dev/null; then
    echo "Please specify JAVA_HOME. Either in config.sh JAVA_HOME_CONFIG or as system-wide JAVA_HOME."
    exit 1
else
    JAVA_HOME=$JAVA_HOME_CONFIG
fi

if [[ -d $JAVA_HOME ]]; then
    JAVA_RUN=$JAVA_HOME/bin/java
else
    JAVA_RUN=java
fi
XMX=$XMX_CONFIG
XMS=$XMS_CONFIG
MAX_PERM=$MAX_PERM_CONFIG

JAVA_OPTS="-server -Xms${XMS}m -Xmx${XMX}m -XX:MaxPermSize=${MAX_PERM}m \
-verbose:gc -XX:+PrintGCDetails \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
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
