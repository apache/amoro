#!/bin/sh
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

if [[ -d $JAVA_HOME ]]; then
    JAVA_RUN=$JAVA_HOME/bin/java
else
    JAVA_RUN=java
fi

if [ -z "$AMORO_LOG_DIR" ]; then
    export AMORO_LOG_DIR="${AMORO_HOME}/logs"
fi
if [ -z "$AMORO_CONF_DIR" ]; then
    export AMORO_CONF_DIR="${AMORO_HOME}/conf"
fi


AMORO_ENV_SH="${AMORO_CONF_DIR}/env.sh"
if [ ! -f ${AMORO_ENV_SH} ]; then
   echo "no env.sh found in ${AMORO_CONF_DIR}"
   exit 1
fi

JVM_PROPERTIES=${AMORO_CONF_DIR}/jvm.properties
parseJvmArgs() {
  ARG=$1
  value=$(cat "$JVM_PROPERTIES" | grep "$ARG=" | sed -e "s/$ARG=\(.*\)/\1/")
  return "$value"
}

JVM_XMX_CONFIG=$(parseJvmArgs "xmx")
JVM_XMS_CONFIG=$(parseJvmArgs "xms")
JMX_REMOTE_PORT_CONFIG=$(parseJvmArgs "jmx.remote.port")
JVM_EXTRA_CONFIG=$(parseJvmArgs "extra.options")

export JVM_XMX_CONFIG
export JVM_XMS_CONFIG
export JMX_REMOTE_PORT_CONFIG
export JVM_EXTRA_CONFIG
