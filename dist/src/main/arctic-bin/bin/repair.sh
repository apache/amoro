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
ARCTIC_HOME="$( cd "$CURRENT_DIR/../" ; pwd -P )"
export ARCTIC_HOME

LIB_PATH=$ARCTIC_HOME/lib
export CLASSPATH=$ARCTIC_HOME/conf/:$LIB_PATH/:$(find $LIB_PATH/ -type f -name "*.jar" | paste -sd':' -)

if [[ -d $JAVA_HOME ]]; then
    JAVA_RUN=$JAVA_HOME/bin/java
else
    JAVA_RUN=java
fi

THRIFT_PORT=$(cat $ARCTIC_HOME/conf/config.yaml | grep "arctic.ams.thrift.port" | awk '{print $2}')

function usage() {
    cat <<EOF
Usage: $0 [option]
You just need to specify only one of the options:

Options:
    -t        Your thrift URL.
              Pattern: thrift://{AMS_HOST}:{AMS_PORT}/{AMS_CATALOG_NAME}
              Example: thrift://localhost:1260/catalog_name

    -c        Your catalog name.
              We'll read {AMS_PORT} from {ARCTIC_HOME}/conf/config.yaml
              and use localhost as {AMS_HOST}

If you specify both options, the latter will override the former
EOF
}

while getopts 't:c:' OPT; do
    case $OPT in
        t) THRIFT_URL="$OPTARG";;
        c) THRIFT_URL=thrift://localhost:$THRIFT_PORT/$OPTARG;;
        ?) usage;;
    esac
done

if [ -z "$THRIFT_URL" ]; then
    THRIFT_URL=thrift://localhost:$THRIFT_PORT
fi

$JAVA_RUN com.netease.arctic.ams.server.repair.RepairMain $THRIFT_URL