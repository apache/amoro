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

PROJECT_HOME=$(cd "$(dirname "$0")"/../.. || exit; pwd)
MVN="${PROJECT_HOME}/mvnw"

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

# Check if source tarball exists and extract it
SOURCE_TARBALL="${RELEASE_DIR}/apache-amoro-${RELEASE_VERSION}-src.tar.gz"
if [ ! -f "$SOURCE_TARBALL" ]; then
    echo "ERROR: Source tarball not found at ${SOURCE_TARBALL}"
    echo "Please run create_source_release.sh first to generate the source tarball."
    echo "This ensures binary releases are built from the source tarball for reproducibility."
    exit 1
fi

EXTRACT_DIR=${RELEASE_DIR}/amoro-src-extract
rm -rf ${EXTRACT_DIR}
mkdir -p ${EXTRACT_DIR}
cd ${EXTRACT_DIR}

echo "Extracting source tarball to build binary release"
tar xzf ${SOURCE_TARBALL}
cd amoro-${RELEASE_VERSION}

# Update AMORO_DIR to point to extracted source
AMORO_DIR=`pwd`

###########################

# build maven package, create Flink distribution, generate signature
make_binary_release() {
  local HADOOP_PROFILE=""
  local HADOOP_VERSION="hadoop3"
  if [[ $# -eq 1 ]]; then
    HADOOP_VERSION=$1
    HADOOP_PROFILE="-P$1"
  fi
  echo "Creating ${HADOOP_VERSION} binary release from source tarball"

  # Build from source tarball (git.properties is already generated)
  # Note: We don't use -Pfail-on-no-git-dir since .git is not in the tarball
  $MVN clean package ${HADOOP_PROFILE} -Pno-extended-disk-storage -Pno-plugin-bin -pl ':dist' -am -Dgpg.skip -Dcheckstyle.skip=true -DskipTests

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

# Cleanup extracted directory
cd ${CURR_DIR}
rm -rf ${EXTRACT_DIR}