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
    export $AMORO_LOG_DIR=${AMORO_HOME}/logs
fi
if [ -z "$AMORO_CONF_DIR" ]; then
    export AMORO_CONF_DIR=${AMORO_HOME}/conf
fi


AMORO_ENV_SH="${AMORO_CONF_DIR}/env.sh"
if [ ! -f ${AMORO_ENV_SH} ]; then
   echo "no env.sh found in ${AMORO_CONF_DIR}"
   exit 1
fi

export JVM_XMX_CONFIG=$JVM_XMX
export JVM_XMS_CONFIG=$JVM_XMS
export JMX_REMOTE_PORT_CONFIG=$JMX_REMOTE_PORT
export JVM_EXTRA_CONFIG=$JVM_EXTRA

. "$AMORO_ENV_SH"

# User-set environment variables have higher priority.
if [ -n "$JVM_XMX_CONFIG" ]; then
  export JVM_XMX_CONFIG=$JVM_XMX
fi
if [ -n "$JVM_XMS_CONFIG" ]; then
  export JVM_XMS_CONFIG=$JVM_XMS
fi
if [ -n "$JMX_REMOTE_PORT_CONFIG" ]; then
  export JMX_REMOTE_PORT_CONFIG=$JMX_REMOTE_PORT
fi
if [ -n "$JVM_EXTRA_CONFIG" ]; then
  export JVM_EXTRA_CONFIG=$JVM_EXTRA
fi

# LANG is a system environment variable with lower priority.
if [ -n "${LANG_CONFIG}" ];then
  export LANG=${LANG_CONFIG}
fi


