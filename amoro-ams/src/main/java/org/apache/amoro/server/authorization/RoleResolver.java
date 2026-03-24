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
import java.util.LinkedHashSet;
import java.util.Set;

public class RoleResolver {
  private static final Logger LOG = LoggerFactory.getLogger(RoleResolver.class);

  private final boolean authorizationEnabled;
  private final String defaultRole;
  private final String adminUsername;
  private final LdapGroupRoleResolver ldapGroupRoleResolver;

  public RoleResolver(Configurations serviceConfig) {
    this(serviceConfig, createLdapGroupRoleResolver(serviceConfig));
  }

  RoleResolver(Configurations serviceConfig, LdapGroupRoleResolver ldapGroupRoleResolver) {
    this.authorizationEnabled = serviceConfig.get(AmoroManagementConf.AUTHORIZATION_ENABLED);
    this.defaultRole =
        serviceConfig
            .getOptional(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE)
            .map(String::trim)
            .filter(role -> !role.isEmpty())
            .orElse(Role.VIEWER);
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
        : LdapGroupRoleResolver.disabled();
  }

  public boolean isAuthorizationEnabled() {
    return authorizationEnabled;
  }

  public Set<String> resolve(String username) {
    if (!authorizationEnabled) {
      return Collections.singleton(Role.SERVICE_ADMIN);
    }

    Set<String> roles = new LinkedHashSet<>();
    if (adminUsername.equals(username)) {
      roles.add(Role.SERVICE_ADMIN);
    }
    roles.addAll(ldapGroupRoleResolver.resolve(username));
    if (roles.isEmpty()) {
      roles.add(defaultRole);
    }

    return Collections.unmodifiableSet(roles);
  }
}
