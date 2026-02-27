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

package org.apache.amoro.server.utils;

import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.rest.RestClient;

import java.util.concurrent.ExecutorService;

public class FlinkClientUtil {

  /**
   * Get Flink RestClusterClient.
   *
   * @param configuration {@link org.apache.flink.configuration.RestOptions}
   */
  public static RestClusterClient<String> getRestClusterClient(Configuration configuration)
      throws Exception {
    return new RestClusterClient<>(configuration, "Amoro-optimizer-session-cluster");
  }

  /**
   * Get Flink RestClient.
   *
   * @param configuration {@link org.apache.flink.configuration.RestOptions}
   */
  public static RestClient getRestClient(Configuration configuration, ExecutorService service)
      throws Exception {
    return new RestClient(configuration, service);
  }
}
