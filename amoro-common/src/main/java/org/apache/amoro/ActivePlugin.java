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

package org.apache.amoro;

import org.apache.amoro.config.ConfigurationManager;

import java.util.Map;

/** Plugins that need to initialize and close */
public interface ActivePlugin extends AmoroPlugin {

  /**
   * Initialize and open the plugin
   *
   * @param properties plugin properties
   */
  void open(Map<String, String> properties);

  /**
   * Initialize and open the plugin with a {@link ConfigurationManager}.
   *
   * <p>The default implementation delegates to {@link #open(Map)} to keep existing plugins
   * compatible. New plugins can override this method to make use of dynamic configuration.
   *
   * @param properties plugin properties
   * @param configurationManager configuration manager used to fetch dynamic overrides
   */
  default void open(Map<String, String> properties, ConfigurationManager configurationManager) {
    open(properties);
  }

  /** Close the plugin */
  void close();
}
