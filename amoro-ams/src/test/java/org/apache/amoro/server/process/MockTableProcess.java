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
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;

/** Mock table process for tests. */
public class MockTableProcess extends TableProcess {

  /**
   * Construct with runtime only.
   *
   * @param tableRuntime table runtime
   */
  MockTableProcess(TableRuntime tableRuntime) {
    super(tableRuntime);
  }

  /**
   * Construct with runtime and store.
   *
   * @param tableRuntime table runtime
   * @param tableProcessStore process store
   */
  MockTableProcess(TableRuntime tableRuntime, TableProcessStore tableProcessStore) {
    super(tableRuntime, tableProcessStore);
  }

  /** Close mock process. */
  @Override
  protected void closeInternal() {}
}
