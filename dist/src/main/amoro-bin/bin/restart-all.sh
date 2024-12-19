#!/bin/bash

# Check for JAVA_HOME and set JPS_PATH accordingly
if [[ -d $JAVA_HOME ]]; then
    JPS_PATH="$JAVA_HOME/bin/jps"
else
    JPS_PATH="jps"  # Fallback to system's PATH
fi

# Log file
LOG_FILE="restart_process.log"

# Define an array of target process names
TARGET_PROCESSES=("AmoroServiceContainer" "StandaloneOptimizer")

# Iterate over each target process name
for TARGET_PROCESS in "${TARGET_PROCESSES[@]}"; do
    echo "$(date): Checking for process '$TARGET_PROCESS'..." | tee -a "$LOG_FILE"

    # Find the PID of the target process
    PID=$($JPS_PATH | grep "$TARGET_PROCESS" | awk '{print $1}')

    if [[ -z "$PID" ]]; then
        echo "$(date): No process found matching '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
        continue
    fi

    echo "$(date): Found process '$TARGET_PROCESS' with PID $PID." | tee -a "$LOG_FILE"

    # Retrieve the command line using ps
    CMDLINE=$(ps -o args= -p "$PID")

    if [[ -z "$CMDLINE" ]]; then
        echo "$(date): Failed to retrieve command line for PID $PID." | tee -a "$LOG_FILE"
        continue
    fi

    echo "$(date): Retrieved command line for '$TARGET_PROCESS': $CMDLINE" | tee -a "$LOG_FILE"

    # Kill the process
    echo "$(date): Attempting to kill process with PID $PID for '$TARGET_PROCESS'..." | tee -a "$LOG_FILE"
    kill "$PID"

    # Check if the process was successfully killed
    if [[ $? -eq 0 ]]; then
        echo "$(date): Successfully killed process with PID $PID for '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
    else
        echo "$(date): Failed to kill process with PID $PID for '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
        continue
    fi

    # Restart the process
    echo "$(date): Restarting process '$TARGET_PROCESS' with original command line..." | tee -a "$LOG_FILE"
    eval "$CMDLINE" &

    # Check if the restart was successful
    if [[ $? -eq 0 ]]; then
        echo "$(date): Successfully restarted process '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
    else
        echo "$(date): Failed to restart process '$TARGET_PROCESS'." | tee -a "$LOG_FILE"
    fi
done