#!/usr/bin/env bash
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

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <optimizer-image> (run Maven package first)" >&2
  exit 1
fi

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_DIR=$(cd "$SCRIPT_DIR/../.." && pwd)
OPTIMIZER_JARS=("$PROJECT_DIR"/amoro-optimizer/amoro-optimizer-flink/target/*-jar-with-dependencies.jar)
if [[ ${#OPTIMIZER_JARS[@]} -ne 1 || ! -f "${OPTIMIZER_JARS[0]}" ]]; then
  echo "Run a clean package of amoro-optimizer-flink before testing the image." >&2
  exit 1
fi

TEST_DIR=$(mktemp -d)
trap 'rm -rf "$TEST_DIR"' EXIT
"${JAVA_HOME:+$JAVA_HOME/bin/}javac" --release 11 -cp "${OPTIMIZER_JARS[0]}" \
  -d "$TEST_DIR" "$SCRIPT_DIR/TestFlinkOptimizerImage.java"
chmod a+rx "$TEST_DIR"
chmod a+r "$TEST_DIR"/*.class

docker run --rm --network none \
  --entrypoint java \
  -v "$TEST_DIR:/test:ro" \
  "$1" -cp '/opt/flink/lib/*:/test' TestFlinkOptimizerImage
