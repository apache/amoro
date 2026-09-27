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

# macOS tools encode extended attributes / resource forks as AppleDouble
# (._*) entries: bsdtar writes them into archives it creates and rsync may
# materialize them as sidecar files. They made the 0.9.0-rc10 source release
# unbuildable on Linux (Checkstyle parsed ._*.java as Java sources), so keep
# them out of the archive and verify afterwards. Harmless on Linux.
export COPYFILE_DISABLE=1

# The post-archive verification needs Python 3 (macOS `tar -t` folds ._*
# entries back into metadata instead of listing them, so the raw entry names
# must be inspected). Check before any side effects so a missing interpreter
# cannot leave a half-built release directory or a blocking temp clone.
if ! python3 -c "import tarfile" > /dev/null 2>&1; then
  echo "ERROR: python3 (with the standard-library tarfile module) is required" \
       "to verify the source archive. Please install Python 3 first."
  exit 1
fi

###########################

cd ..

AMORO_DIR=`pwd`
RELEASE_DIR=${AMORO_DIR}/tools/releasing/release

echo "Creating source package"

mkdir -p ${RELEASE_DIR}

# Clone into a per-run exclusive directory: concurrent runs must not share
# a clone path, and this run's cleanup must never delete a clone belonging
# to another (still running) invocation.
CLONE_PARENT=$(mktemp -d "${RELEASE_DIR}/amoro-clone.XXXXXX")
CLONE_DIR=${CLONE_PARENT}/amoro-tmp-clone

# Remove this run's temporary directory no matter how the script exits, so
# failed runs do not leave clone directories piling up in release/.
trap 'rm -rf "${CLONE_PARENT}"' EXIT

# create a temporary git clone to ensure that we have a pristine source release
git clone ${AMORO_DIR} ${CLONE_DIR}
cd ${CLONE_DIR}

rsync -a \
  --exclude ".git" --exclude ".gitignore" \
  --exclude ".github" --exclude "/site/" --exclude "target" \
  --exclude ".idea" --exclude "*.iml" --exclude ".DS_Store" --exclude "._*" \
  --exclude "*/dependency-reduced-pom.xml" \
  . amoro-$RELEASE_VERSION

tar czf ${RELEASE_DIR}/apache-amoro-${RELEASE_VERSION}-src.tar.gz \
  --exclude='._*' --exclude='.DS_Store' \
  amoro-$RELEASE_VERSION

# Verify the archive carries no macOS metadata entries. Do not use
# `tar -t` for this: on macOS bsdtar converts ._ entries back to metadata
# instead of listing them, hiding exactly the pollution we are checking for.
# The raw entry names must be inspected instead.
MACOS_METADATA_COUNT=$(python3 -c "
import tarfile
names = tarfile.open('${RELEASE_DIR}/apache-amoro-${RELEASE_VERSION}-src.tar.gz').getnames()
print(sum(1 for n in names
          if '/._' in n or n.startswith('._')
          or '__MACOSX' in n or n.endswith('.DS_Store')))
")
if [ "${MACOS_METADATA_COUNT}" != "0" ]; then
  echo "ERROR: source archive contains ${MACOS_METADATA_COUNT} macOS metadata " \
       "entries (._* AppleDouble, __MACOSX or .DS_Store); aborting."
  exit 1
fi

gpg --armor --detach-sig ${RELEASE_DIR}/apache-amoro-$RELEASE_VERSION-src.tar.gz
cd ${RELEASE_DIR}
$SHASUM apache-amoro-$RELEASE_VERSION-src.tar.gz > apache-amoro-$RELEASE_VERSION-src.tar.gz.sha512

cd ${CURR_DIR}
rm -rf "${CLONE_PARENT}"