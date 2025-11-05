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

package org.apache.amoro.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.runtime.testutils.InMemoryReporter;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;

public class MiniClusterResource {
  private static final int DEFAULT_TM_NUM = 1;
  private static final int DEFAULT_PARALLELISM = 4;
  public static final Configuration DISABLE_CLASSLOADER_CHECK_CONFIG;

  private MiniClusterResource() {}

  public static MiniClusterWithClientResource createWithClassloaderCheckDisabled() {
    return new MiniClusterWithClientResource(
        (new MiniClusterResourceConfiguration.Builder())
            .setNumberTaskManagers(1)
            .setNumberSlotsPerTaskManager(4)
            .setConfiguration(DISABLE_CLASSLOADER_CHECK_CONFIG)
            .build());
  }

  public static MiniClusterWithClientResource createWithClassloaderCheckDisabled(
      InMemoryReporter inMemoryReporter) {
    Configuration configuration = new Configuration(DISABLE_CLASSLOADER_CHECK_CONFIG);
    inMemoryReporter.addToConfiguration(configuration);
    return new MiniClusterWithClientResource(
        (new MiniClusterResourceConfiguration.Builder())
            .setNumberTaskManagers(1)
            .setNumberSlotsPerTaskManager(4)
            .setConfiguration(configuration)
            .build());
  }

  static {
    DISABLE_CLASSLOADER_CHECK_CONFIG =
        (new Configuration()).set(CoreOptions.CHECK_LEAKED_CLASSLOADER, false);
  }
}
