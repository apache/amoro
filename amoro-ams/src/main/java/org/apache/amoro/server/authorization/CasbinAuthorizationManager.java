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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CasbinAuthorizationManager {
  private static final String MODEL_RESOURCE = "authorization/amoro_rbac_model.conf";
  private static final String POLICY_RESOURCE = "authorization/amoro_rbac_policy.csv";

  private final boolean authorizationEnabled;
  private final Enforcer enforcer;

  public CasbinAuthorizationManager(Configurations serviceConfig) {
    this.authorizationEnabled = serviceConfig.get(AmoroManagementConf.AUTHORIZATION_ENABLED);
    this.enforcer = authorizationEnabled ? createEnforcer() : null;
  }

  public boolean isAuthorizationEnabled() {
    return authorizationEnabled;
  }

  public boolean authorize(Set<Role> roles, AuthorizationRequest request) {
    if (!authorizationEnabled) {
      return true;
    }

    if (roles == null || roles.isEmpty()) {
      return false;
    }

    for (Role role : roles) {
      if (Boolean.TRUE.equals(
          enforcer.enforce(
              role.name(),
              request.getResourceType().name(),
              request.getResourceId(),
              request.getPrivilege().name()))) {
        return true;
      }
    }
    return false;
  }

  private static Enforcer createEnforcer() {
    Model model = new Model();
    model.loadModelFromText(readResource(MODEL_RESOURCE));

    Enforcer enforcer = new Enforcer(model);
    for (List<String> policy : readPolicies(POLICY_RESOURCE)) {
      if (!policy.isEmpty() && "p".equals(policy.get(0))) {
        enforcer.addPolicy(policy.subList(1, policy.size()));
      }
    }
    return enforcer;
  }

  private static List<List<String>> readPolicies(String resource) {
    return readResource(resource)
        .lines()
        .map(String::trim)
        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
        .map(
            line ->
                List.of(line.split("\\s*,\\s*")).stream()
                    .map(String::trim)
                    .collect(Collectors.toList()))
        .collect(Collectors.toList());
  }

  private static String readResource(String resource) {
    try (InputStream inputStream =
            CasbinAuthorizationManager.class.getClassLoader().getResourceAsStream(resource);
        InputStreamReader inputStreamReader =
            new InputStreamReader(
                Objects.requireNonNull(inputStream, "Missing resource: " + resource),
                StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load authorization resource: " + resource, e);
    }
  }
}
