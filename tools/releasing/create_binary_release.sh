#!/usr/bin/env bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##
## Variables with defaults (if not overwritten by environment)
##
SKIP_GPG=${SKIP_GPG:-false}
MVN=${MVN:-mvn}

if [ -z "${RELEASE_VERSION:-}" ]; then
    echo "RELEASE_VERSION was not set."
    exit 1
fi

# fail immediately
set -o errexit
set -o nounset
# print command before executing
set -o xtrace

CURR_DIR=`pwd`
if [[ `basename $CURR_DIR` != "tools" ]] ; then
  echo "You have to call the script from the tools/ dir"
  exit 1
fi

if [ "$(uname)" == "Darwin" ]; then
    SHASUM="shasum -a 512"
else
    SHASUM="sha512sum"
fi

cd ..

AMORO_DIR=`pwd`
RELEASE_DIR=${AMORO_DIR}/tools/releasing/release
mkdir -p ${RELEASE_DIR}

###########################

# build maven package, create Flink distribution, generate signature
make_binary_release() {
  local HADOOP_PROFILE=""
  local HADOOP_VERSION="hadoop3"
  if [[ $# -eq 1 ]]; then
    HADOOP_VERSION=$1
    HADOOP_PROFILE="-P$1"
  fi
  echo "Creating ${HADOOP_VERSION} binary release"

  # enable release profile here (to check for the maven version)
  $MVN clean package ${HADOOP_PROFILE} -Pno-extended-disk-storage -Pfail-on-no-git-dir -pl ':dist' -am -Dgpg.skip -Dcheckstyle.skip=true -DskipTests

  local TARGET_FILE="apache-amoro-${RELEASE_VERSION}-bin-${HADOOP_VERSION}.tar.gz"
  cp dist/target/apache-amoro-${RELEASE_VERSION}-bin.tar.gz ${RELEASE_DIR}/${TARGET_FILE}
  cd ${RELEASE_DIR}

  # Sign sha the tgz
  if [ "$SKIP_GPG" == "false" ] ; then
    gpg --armor --detach-sig ${TARGET_FILE}
  fi
  $SHASUM ${TARGET_FILE} > "${TARGET_FILE}.sha512"

  cd ${AMORO_DIR}
}

make_binary_release "hadoop3"
make_binary_release "hadoop2"