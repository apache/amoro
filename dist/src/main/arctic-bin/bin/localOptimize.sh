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

PRGDIR=$(dirname $0)
PRGDIR=$(cd $PRGDIR;pwd)
source ${PRGDIR}/config.sh
LIB_PATH=$APP_HOME/lib
LOG_DIR=$APP_HOME/logs
export CLASSPATH=$APP_HOME/conf/optimize:$LIB_PATH/:$(find $LIB_PATH/ -type f -name "*.jar" | paste -sd':' -)
$JAVA_RUN -Dlog.home=${LOG_DIR} -Dlog.subdir=localOptimizer-${6} -Xmx$1m com.netease.arctic.optimizer.local.LocalOptimizer -a $2 -q $3 -p $4 --heart-beat $5 -id $6