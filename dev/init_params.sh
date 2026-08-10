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
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# Usage: source dev/init_params.sh

# Test environment endpoints.
HMS_URI="thrift://115.190.129.225:9090"
EMR_SERVERLESS_ENDPOINT="https://open.volcengineapi.com"
TOS_ENDPOINT="https://tos-cn-beijing.volces.com"
IAM_ENDPOINT="https://iam.volcengineapi.com"
REGION="cn-beijing"

# Credentials must already be present under either pair of names. Never commit their values.
LAS_SERVICE_AK="${LAS_SERVICE_AK:-${ASSUME_ROLE_ACCESS_KEY:-}}"
LAS_SERVICE_SK="${LAS_SERVICE_SK:-${ASSUME_ROLE_SECRET_KEY:-}}"
if [ -z "${LAS_SERVICE_AK}" ] || [ -z "${LAS_SERVICE_SK}" ]; then
  echo "LAS_SERVICE_AK/LAS_SERVICE_SK are required" >&2
  return 1 2>/dev/null || exit 1
fi

# Configuration names consumed by the current branch.
export AMS_LAS_INTEGRATION_ENABLED=true
export AMS_LAS_INTEGRATION_HMS__URI="${HMS_URI}"
export AMS_LAS_INTEGRATION_EMR__SERVERLESS__ENDPOINT="${EMR_SERVERLESS_ENDPOINT}"
export AMS_LAS_INTEGRATION_TOS__ENDPOINT="${TOS_ENDPOINT}"
export AMS_LAS_INTEGRATION_IAM__ENDPOINT="${IAM_ENDPOINT}"
export AMS_LAS_INTEGRATION_REGION="${REGION}"
export LAS_SERVICE_AK
export LAS_SERVICE_SK

# Proton uses these fixed aliases for the same service credential pair.
export ASSUME_ROLE_ACCESS_KEY="${LAS_SERVICE_AK}"
export ASSUME_ROLE_SECRET_KEY="${LAS_SERVICE_SK}"

echo "LAS/HMS3 test parameters loaded"
echo "  HMS: ${HMS_URI}"
echo "  EMR Serverless: ${EMR_SERVERLESS_ENDPOINT}"
echo "  TOS: ${TOS_ENDPOINT}"
echo "  IAM: ${IAM_ENDPOINT}"
