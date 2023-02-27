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

function get_ip() {
    check_ip $(ip route get 1 | awk '{print $NF;exit}')
    if [ $? -eq 1 ]; then
      check_ip $(ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"|awk 'NR==1')
      if [ $? -eq 1 ]; then
          check_ip $(hostname -i | awk '{print $1}')
      fi
    fi
}

function check_ip() {
    IP=$1
    VALID_CHECK=$(echo $IP|awk -F. '$1<=255&&$2<=255&&$3<=255&&$4<=255{print "yes"}')
    if echo $IP|grep -E "^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$" >/dev/null; then
        if [ $VALID_CHECK == "yes" ]; then
          THRIFT_IP=$IP
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

get_ip
$JAVA_RUN com.netease.arctic.ams.server.repair.RepairMain thrift://$THRIFT_IP:$THRIFT_PORT/$1