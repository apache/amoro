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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RoleResolverTest {

  @Test
  public void testDisabledAuthorizationDefaultsToAdmin() {
    Configurations conf = new Configurations();
    RoleResolver resolver = new RoleResolver(conf);
    assertEquals(Role.ADMIN, resolver.resolve("viewer"));
  }

  @Test
  public void testResolveConfiguredUserAndFallbackRoles() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "admin");
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(AmoroManagementConf.AUTHORIZATION_ADMIN_USERS, Arrays.asList("ldap_admin", " "));
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(
            localUser("operator", "secret", "ADMIN"), localUser("viewer", "v", "READ_ONLY")));

    RoleResolver resolver = new RoleResolver(conf);

    assertEquals(Role.ADMIN, resolver.resolve("operator"));
    assertEquals(Role.READ_ONLY, resolver.resolve("viewer"));
    assertEquals(Role.ADMIN, resolver.resolve("ldap_admin"));
    assertEquals(Role.ADMIN, resolver.resolve("admin"));
    assertEquals(Role.READ_ONLY, resolver.resolve("ldap_viewer"));
    assertEquals(Role.READ_ONLY, resolver.resolve(" "));
  }

  @Test
  public void testResolveLdapUserFromAdminWhitelist() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "local_admin");
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(AmoroManagementConf.AUTHORIZATION_ADMIN_USERS, Arrays.asList("alice", "bob"));

    RoleResolver resolver = new RoleResolver(conf);

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
    assertEquals(Role.READ_ONLY, resolver.resolve("charlie"));
  }

  @Test
  public void testLocalUserRoleTakesPriorityOverLdapGroupRoleMapping() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL, "ldap://ldap.example.com:389");
    conf.set(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED, true);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ADMIN_GROUP_DN,
        "cn=amoro-admins,ou=groups,dc=example,dc=com");
    conf.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_USER_DN_PATTERN,
        "uid={0},ou=people,dc=example,dc=com");
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(localUser("alice", "viewer123", "READ_ONLY")));

    RoleResolver resolver =
        new RoleResolver(
            conf,
            new LdapGroupRoleResolver(
                conf,
                (groupDn, memberAttribute) ->
                    Collections.singleton("uid=alice,ou=people,dc=example,dc=com")));

    assertEquals(Role.READ_ONLY, resolver.resolve("alice"));
  }

  @Test
  public void testDisabledAuthorizationIgnoresEnabledLdapRoleMapping() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, false);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED, true);

    RoleResolver resolver = new RoleResolver(conf);

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
  }

  @Test
  public void testInvalidConfiguredRoleFailsWithHelpfulMessage() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(localUser("viewer", "viewer123", "writer")));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new RoleResolver(conf));

    assertEquals(
        "Invalid role 'writer' configured for authorization user 'viewer'", exception.getMessage());
  }

  @Test
  public void testDuplicateConfiguredUsersKeepLastDefinition() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(
            localUser("viewer", "viewer123", "READ_ONLY"),
            localUser("viewer", "viewer123", "ADMIN")));

    RoleResolver resolver = new RoleResolver(conf);

    assertEquals(Role.ADMIN, resolver.resolve("viewer"));
  }

  @Test
  public void testEnabledAuthorizationWithoutExplicitUsersFallsBackToDefaultRole() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "admin");
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);

    RoleResolver resolver = new RoleResolver(conf);

    assertEquals(Role.READ_ONLY, resolver.resolve("viewer"));
  }

  @Test
  public void testInvalidUserEntriesAreIgnored() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(invalidUserWithMissingUsername("viewer123", "ADMIN")));

    RoleResolver resolver = new RoleResolver(conf);

    assertEquals(Role.READ_ONLY, resolver.resolve("viewer"));
  }

  private static Map<String, String> localUser(String username, String password, String role) {
    Map<String, String> user = new HashMap<>();
    user.put("username", username);
    user.put("password", password);
    user.put("role", role);
    return user;
  }

  private static Map<String, String> invalidUserWithMissingUsername(String password, String role) {
    Map<String, String> user = new HashMap<>();
    user.put("password", password);
    user.put("role", role);
    return user;
  }
}
