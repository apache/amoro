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

package org.apache.amoro.server.process;

import org.apache.amoro.Action;
import org.apache.amoro.ActivePlugin;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;

public interface ActionCoordinator extends ActivePlugin {

  String PROPERTY_PARALLELISM = "parallelism";

  SnowflakeIdGenerator SNOWFLAKE_ID_GENERATOR = new SnowflakeIdGenerator();

  boolean formatSupported(TableFormat format);

  int parallelism();

  Action action();

  String executionEngine();

  long getNextExecutingTime(TableRuntime tableRuntime);

  boolean enabled(TableRuntime tableRuntime);

  long getExecutorDelay();

  TableProcess createTableProcess(TableRuntime tableRuntime);

  TableProcess recoverTableProcess(TableRuntime tableRuntime, TableProcessMeta meta);

  TableProcess cancelTableProcess(TableRuntime tableRuntime, TableProcess process);

  TableProcess retryTableProcess(TableProcess process);
}
