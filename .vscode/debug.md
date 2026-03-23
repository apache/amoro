<!--
 - Licensed to the Apache Software Foundation (ASF) under one
 - or more contributor license agreements.  See the NOTICE file
 - distributed with this work for additional information
 - regarding copyright ownership.  The ASF licenses this file
 - to you under the Apache License, Version 2.0 (the
 - "License"); you may not use this file except in compliance
 - with the License.  You may obtain a copy of the License at
 - 
 -     http://www.apache.org/licenses/LICENSE-2.0
 - 
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and 
 - limitations under the License.
 -
 - Modified by Datazip Inc. in 2026
-->

# Start Fusion In Debug Mode

## Setup Dist Runtime For Local Optimizer
1. Update Java 17 path for your machine in `.vscode/settings.json` (or set Java 17 in user settings).
2. Run `make setup-debug-mode` (starts local deps, runs `mvn clean install -DskipTests`, extracts dist tar, and syncs only `lib/` to `dist/src/main/amoro-bin/lib`).
3. Start AMS from `launch.json` using `AmoroServiceContainer` (or `AmoroServiceContainer (Optimizer Debug)` when optimizer debug flags are needed).
4. Add/create an optimizer through UI (local container/group).
5. Attach optimizer debugger using `OptimizerStandalone` from `launch.json` (default port `5006`).
6. Add breakpoints and verify they are hit.
7. Try Reloading the project in vs code again if any java issue exist (still not fixed ask for help on slack)

## Teardown
- Run `make teardown-debug-mode` to stop local deps and clean extracted runtime.
