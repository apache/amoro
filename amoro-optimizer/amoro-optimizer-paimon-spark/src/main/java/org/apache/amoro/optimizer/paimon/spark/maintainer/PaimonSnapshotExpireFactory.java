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

package org.apache.amoro.optimizer.paimon.spark.maintainer;

import org.apache.amoro.maintainer.MaintainerExecutor;
import org.apache.amoro.maintainer.MaintainerExecutorFactory;

import java.util.Map;

/** Factory for creating PaimonSnapshotExpireExecutor instances. */
public class PaimonSnapshotExpireFactory
    implements MaintainerExecutorFactory<PaimonSnapshotExpireInput> {

  private static final long serialVersionUID = 1L;

  private Map<String, String> properties;

  /** Default constructor required for DynConstructors. */
  public PaimonSnapshotExpireFactory() {
    // Required for DynConstructors
  }

  @Override
  public void initialize(Map<String, String> properties) {
    this.properties = properties;
  }

  @Override
  public MaintainerExecutor<?, ?> createExecutor(PaimonSnapshotExpireInput input) {
    return new PaimonSnapshotExpireExecutor(input);
  }
}
