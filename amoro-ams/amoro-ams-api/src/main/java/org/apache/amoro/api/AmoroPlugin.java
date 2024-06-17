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

package org.apache.amoro.api;

/**
 * Plugin interface for all pluggable services including MetricEmitter, TableProcessFactory, etc.
 */
public interface AmoroPlugin {

  /**
   * Get the plugin name. Besides, the plugin name should be unique and less than 15 characters in
   * kubernetes. Because the plugin name will be used as the kubernetes service name which is hereby
   * normatively defined as <a
   * href="RFC-6335">https://datatracker.ietf.org/doc/html/rfc6335#section-5.1</a>
   *
   * @return plugin name
   */
  String name();
}
