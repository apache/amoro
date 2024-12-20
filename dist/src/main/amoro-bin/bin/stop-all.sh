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

# Check for JAVA_HOME and set JPS_PATH accordingly
if [[ -d $JAVA_HOME ]]; then
    JPS_PATH="$JAVA_HOME/bin/jps"
else
    JPS_PATH="jps"  # Fallback to system's PATH
fi

# Define an array of target process names
TARGET_PROCESSES=("StandaloneOptimizer" "AmoroServiceContainer")

# Specify the log file
LOG_FILE="kill_process.log"

# Iterate over each target process name
for TARGET_PROCESS in "${TARGET_PROCESSES[@]}"; do
    # Get the PIDs of all matching target processes using jps
    PIDS=$($JPS_PATH | grep "$TARGET_PROCESS" | awk '{print $1}')

    # Check if any PIDs were found
    if [ -z "$PIDS" ]; then
        echo "$(date): No processes found matching '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
        continue
    fi

    # Iterate through each PID and attempt to kill the process
    echo "$(date): Found processes matching '$TARGET_PROCESS': $PIDS." | tee -a "$LOG_FILE"
    for PID in $PIDS; do
        echo "$(date): Attempting to kill process with PID $PID for '$TARGET_PROCESS'..." | tee -a "$LOG_FILE"
        kill "$PID"

        # Check the kill status
        if [ $? -eq 0 ]; then
            echo "$(date): Successfully killed process with PID $PID for '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
        else
            echo "$(date): Failed to kill process with PID $PID for '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
        fi
    done
done