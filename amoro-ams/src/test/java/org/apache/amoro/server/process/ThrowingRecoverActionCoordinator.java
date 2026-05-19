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

import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;

/**
 * Mock coordinator whose {@code recoverTableProcess} always fails, used to verify that a single
 * un-recoverable process record cannot abort the whole AMS startup (see AMORO-4223).
 */
public class ThrowingRecoverActionCoordinator extends MockActionCoordinator {

  public ThrowingRecoverActionCoordinator(ExecuteEngine executeEngine) {
    super(executeEngine);
  }

  @Override
  public TableProcess recoverTableProcess(
      TableRuntime tableRuntime, TableProcessStore processStore) {
    throw new RecoverProcessFailedException(
        "Unsupported action for test coordinator: " + processStore.getAction());
  }
}
