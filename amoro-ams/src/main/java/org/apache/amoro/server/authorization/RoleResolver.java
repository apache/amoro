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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleResolver {
  private static final Logger LOG = LoggerFactory.getLogger(RoleResolver.class);

  private final boolean authorizationEnabled;
  private final Role defaultRole;
  private final Map<String, Role> localUserRoles;
  private final Set<String> adminUsers;
  private final String adminUsername;
  private final LdapGroupRoleResolver ldapGroupRoleResolver;

  public RoleResolver(Configurations serviceConfig) {
    this(serviceConfig, createLdapGroupRoleResolver(serviceConfig));
  }

  RoleResolver(Configurations serviceConfig, LdapGroupRoleResolver ldapGroupRoleResolver) {
    this.authorizationEnabled = serviceConfig.get(AmoroManagementConf.AUTHORIZATION_ENABLED);
    this.defaultRole = serviceConfig.get(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE);
    this.localUserRoles = loadLocalUserRoles(serviceConfig);
    this.adminUsers =
        serviceConfig.getOptional(AmoroManagementConf.AUTHORIZATION_ADMIN_USERS)
            .orElse(Collections.emptyList()).stream()
            .map(String::trim)
            .filter(user -> !user.isEmpty())
            .collect(Collectors.toSet());
    this.adminUsername = serviceConfig.get(AmoroManagementConf.ADMIN_USERNAME);
    this.ldapGroupRoleResolver = ldapGroupRoleResolver;
  }

  private static LdapGroupRoleResolver createLdapGroupRoleResolver(Configurations serviceConfig) {
    boolean authorizationEnabled = serviceConfig.get(AmoroManagementConf.AUTHORIZATION_ENABLED);
    boolean ldapRoleMappingEnabled =
        serviceConfig.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED);
    if (!authorizationEnabled && ldapRoleMappingEnabled) {
      LOG.warn(
          "Ignore http-server.authorization.ldap-role-mapping configuration because http-server.authorization.enabled is false");
    }

    return authorizationEnabled
        ? new LdapGroupRoleResolver(serviceConfig)
        : LdapGroupRoleResolver.disabled(
            serviceConfig.get(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE));
  }

  public boolean isAuthorizationEnabled() {
    return authorizationEnabled;
  }

  public Role resolve(String username) {
    if (!authorizationEnabled) {
      return Role.ADMIN;
    }

    Role localRole = localUserRoles.get(username);
    if (localRole != null) {
      return localRole;
    }

    if (adminUsers.contains(username) || adminUsername.equals(username)) {
      return Role.ADMIN;
    }

    return ldapGroupRoleResolver.resolve(username);
  }

  private static Map<String, Role> loadLocalUserRoles(Configurations serviceConfig) {
    List<Map<String, String>> users =
        serviceConfig
            .getOptional(AmoroManagementConf.AUTHORIZATION_USERS)
            .orElse(Collections.emptyList());
    return users.stream()
        .filter(RoleResolver::hasRequiredRoleFields)
        .collect(
            Collectors.toMap(
                user -> user.get("username"),
                user -> parseRole(user.get("username"), user.get("role")),
                (existing, replacement) -> {
                  LOG.warn(
                      "Duplicate authorization.users entry for role resolution, keeping last user definition");
                  return replacement;
                }));
  }

  private static boolean hasRequiredRoleFields(Map<String, String> user) {
    if (user.get("username") == null || user.get("role") == null) {
      LOG.warn("Ignore invalid authorization.users entry for role resolution: {}", user);
      return false;
    }
    return true;
  }

  private static Role parseRole(String username, String roleValue) {
    try {
      return Role.valueOf(roleValue.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid role '%s' configured for authorization user '%s'", roleValue, username),
          e);
    }
  }
}
