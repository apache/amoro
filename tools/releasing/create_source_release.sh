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
SKIP_GPG=${SKIP_GPG:-false}
GPG_PASSPHRASE=${GPG_PASSPHRASE:-}

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

###########################

cd ..

AMORO_DIR=`pwd`
RELEASE_DIR=${AMORO_DIR}/tools/releasing/release
CLONE_DIR=${RELEASE_DIR}/amoro-tmp-clone

sign_artifact() {
  local artifact_path=$1

  if [ "$SKIP_GPG" != "false" ] ; then
    return
  fi

  if [ -n "$GPG_PASSPHRASE" ] ; then
    gpg --batch --yes --pinentry-mode loopback --passphrase "$GPG_PASSPHRASE" \
      --armor --detach-sig "$artifact_path"
  else
    gpg --armor --detach-sig "$artifact_path"
  fi
}

create_release_git_properties() {
  local resource_dir=$1
  local git_commit_id_full
  local git_commit_id_abbrev
  local git_commit_time
  local git_build_time

  git_commit_id_full=$(git rev-parse HEAD)
  git_commit_id_abbrev=$(git rev-parse --short HEAD)
  git_commit_time=$(git log -1 --format=%cI HEAD)
  git_build_time=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

  mkdir -p "${resource_dir}"
  cat > "${resource_dir}/git.properties" <<EOF
git.build.time=${git_build_time}
git.build.version=${RELEASE_VERSION}
git.commit.id.abbrev=${git_commit_id_abbrev}
git.commit.id.full=${git_commit_id_full}
git.commit.time=${git_commit_time}
EOF
}

echo "Creating source package"

mkdir -p ${RELEASE_DIR}

# create a temporary git clone to ensure that we have a pristine source release
git clone ${AMORO_DIR} ${CLONE_DIR}
cd ${CLONE_DIR}

rsync -a \
  --exclude ".git" --exclude ".gitignore" \
  --exclude ".github" --exclude "target" \
  --exclude ".idea" --exclude "*.iml" --exclude ".DS_Store" \
  --exclude "._*" --exclude "*/._*" \
  --exclude "*/dependency-reduced-pom.xml" \
  . amoro-$RELEASE_VERSION

# Create release metadata as a source resource so binaries built from the source
# tarball preserve version information without packaging build outputs in source.
create_release_git_properties "amoro-$RELEASE_VERSION/amoro-ams/src/main/resources/amoro"

tar czf ${RELEASE_DIR}/apache-amoro-${RELEASE_VERSION}-src.tar.gz amoro-$RELEASE_VERSION

sign_artifact "${RELEASE_DIR}/apache-amoro-$RELEASE_VERSION-src.tar.gz"

cd ${RELEASE_DIR}
$SHASUM apache-amoro-$RELEASE_VERSION-src.tar.gz > apache-amoro-$RELEASE_VERSION-src.tar.gz.sha512

cd ${CURR_DIR}
rm -rf ${CLONE_DIR}
