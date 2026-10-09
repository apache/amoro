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
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <base-revision> <head-revision> <path-regex>" >&2
  exit 2
fi

base_revision=$1
head_revision=$2
path_regex=$3

changed_files=$(git diff --name-only "${base_revision}" "${head_revision}")
grep -Eq "${path_regex}" <<< "${changed_files}"
