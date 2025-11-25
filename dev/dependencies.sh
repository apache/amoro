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

set -o pipefail
set -e
set -x

export LC_ALL=C

PWD=$(cd "$(dirname "$0")"/.. || exit; pwd)

MVN="${PWD}/mvnw"

function build_classpath() {
  $MVN clean install -DskipTests --no-snapshot-updates -pl dist -am "$@"
  $MVN dependency:build-classpath --no-snapshot-updates -pl dist -am "$@" |\
    grep -v "INFO\|WARN" | \
    tail -1 | \
    tr ":" "\n" | \
    awk -F '/' '{
      artifact_id=$(NF-2);
      version=$(NF-1);
      jar_name=$NF;
      classifier_start_index=length(artifact_id"-"version"-") + 1;
      classifier_end_index=index(jar_name, ".jar") - 1;
      classifier=substr(jar_name, classifier_start_index, classifier_end_index - classifier_start_index + 1);
      print artifact_id"/"version"/"classifier"/"jar_name
    }' | grep -v "amoro" | sort >> "${DEP_PR}"
}

function check_diff() {
    set +e
    the_diff=$(diff "${DEP}" "${DEP_PR}")
    set -e
    rm -rf "${DEP_PR}"
    if [[ -n "${the_diff}" ]]; then
        echo "Dependency List Changed Detected: "
        echo "${the_diff}"
        echo "To update the dependency file, run './dev/dependencies.sh --replace'."
        exit 1
    fi
}

HADOOP_SPARK_PROFILES=('hadoop2' 'spark-3.5')
for HADOOP_SPARK_PROFILE in "${HADOOP_SPARK_PROFILES[@]}";
do
  HADOOP_MAJOR_VERSION=$("$MVN" help:evaluate -Dexpression=hadoop.version -P"$HADOOP_SPARK_PROFILE" 2>/dev/null \
      | grep -v "INFO" \
      | grep -v "WARNING" \
      | tail -n 1 \
      | cut -d. -f1)
  SPARK_MAJOR_VERSION=$("$MVN" help:evaluate -Dexpression=spark.major.version -P"$HADOOP_SPARK_PROFILE" 2>/dev/null\
      | grep -v "INFO"\
      | grep -v "WARNING"\
      | tail -n 1)
  mkdir -p "${PWD}/dev/deps"
  DEP_PR="${PWD}/dev/deps/dependencies-hadoop-${HADOOP_MAJOR_VERSION}-spark-${SPARK_MAJOR_VERSION}.tmp"
  DEP="${PWD}/dev/deps/dependencies-hadoop-${HADOOP_MAJOR_VERSION}-spark-${SPARK_MAJOR_VERSION}"
  rm -rf "${DEP_PR}"
  build_classpath "-P${HADOOP_SPARK_PROFILE}"
  if [[ "$1" == "--replace" ]]; then
      rm -rf "${DEP}"
      mv "${DEP_PR}" "${DEP}"
  else
      check_diff
  fi
done
