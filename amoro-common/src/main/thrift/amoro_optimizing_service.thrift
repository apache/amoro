/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

namespace java org.apache.amoro.api

include "amoro_commons.thrift"

struct OptimizingTask {
    1: OptimizingTaskId taskId;
    2: optional binary taskInput;
    3: optional map<string, string> properties;
}

struct OptimizingTaskId {
    1: i64 processId;
    2: i32 taskId;
}

struct OptimizingTaskResult {
    1: OptimizingTaskId taskId;
    2: i32 threadId;
    3: optional binary taskOutput;
    4: optional string errorMessage;
    5: optional map<string, string> summary;
}

struct OptimizerRegisterInfo {
    1: optional string resourceId;
    2: i32 threadCount;
    3: i32 memoryMb;
    4: i64 startTime;
    5: string groupName;
    6: optional map<string, string> properties;
}

service OptimizingService {

    void ping()

    void touch(1: string authToken) throws(1: amoro_commons.AmoroException e1)

    OptimizingTask pollTask(1: string authToken, 2: i32 threadId)
            throws (1: amoro_commons.AmoroException e1)

    void ackTask(1: string authToken, 2: i32 threadId, 3: OptimizingTaskId taskId)
            throws(1: amoro_commons.AmoroException e1)

    void completeTask(1: string authToken, 2: OptimizingTaskResult taskResult)
            throws (1: amoro_commons.AmoroException e1)

    string authenticate(1: OptimizerRegisterInfo registerInfo)
            throws (1: amoro_commons.AmoroException e1)
}
