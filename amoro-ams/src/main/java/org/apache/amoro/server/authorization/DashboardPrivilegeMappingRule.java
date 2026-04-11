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

package org.apache.amoro.server.authorization;

import java.util.Collections;
import java.util.Set;

class DashboardPrivilegeMappingRule {
  private final Set<String> methods;
  private final Set<String> paths;
  private final Set<String> prefixes;
  private final AuthorizationRequest authorizationRequest;

  DashboardPrivilegeMappingRule(
      Set<String> methods,
      Set<String> paths,
      Set<String> prefixes,
      AuthorizationRequest authorizationRequest) {
    this.methods = methods == null ? Collections.emptySet() : methods;
    this.paths = paths == null ? Collections.emptySet() : paths;
    this.prefixes = prefixes == null ? Collections.emptySet() : prefixes;
    this.authorizationRequest = authorizationRequest;
  }

  boolean matches(String method, String path) {
    boolean methodMatched = methods.isEmpty() || methods.contains(method);
    boolean pathMatched = paths.contains(path) || prefixes.stream().anyMatch(path::startsWith);
    return methodMatched && pathMatched;
  }

  AuthorizationRequest getAuthorizationRequest() {
    return authorizationRequest;
  }
}
