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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.junit.jupiter.api.Test;

import javax.naming.NamingException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LdapGroupRoleResolverTest {

  @Test
  public void testResolveAdminFromUserDnMember() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                Collections.singleton("uid=alice,ou=people,dc=example,dc=com"));

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
  }

  @Test
  public void testResolveAdminFromUsernameMember() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(), (groupDn, memberAttribute) -> Collections.singleton("alice"));

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
  }

  @Test
  public void testResolveAdminFromCnDnMember() {
    // Cisco AD stores members as full CNs: "CN=xuba,OU=Employees,OU=Cisco Users,DC=cisco,DC=com"
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                Collections.singleton("CN=alice,OU=Employees,OU=Cisco Users,DC=cisco,DC=com"));

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
  }

  @Test
  public void testResolveAdminFromMemberUidStyleValue() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(), (groupDn, memberAttribute) -> Collections.singleton("uid=alice"));

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
  }

  @Test
  public void testResolveAdminIsCaseInsensitiveForDnMembers() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                Collections.singleton("UID=Alice,OU=People,DC=Example,DC=Com"));

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
  }

  @Test
  public void testThrowsWhenLookupFails() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) -> {
              throw new NamingException("ldap unavailable");
            });

    RuntimeException ex = assertThrows(RuntimeException.class, () -> resolver.resolve("alice"));
    assertTrue(ex.getMessage().contains("LDAP role resolution failed"));
  }

  @Test
  public void testRequireLdapGroupConfigWhenEnabled() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED, true);
    conf.set(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL, "ldap://ldap.example.com:389");

    assertThrows(IllegalArgumentException.class, () -> new LdapGroupRoleResolver(conf));
  }

  @Test
  public void testRoleResolverFallsBackToLdapGroupMembership() {
    Configurations conf = baseConfig();
    RoleResolver resolver =
        new RoleResolver(
            conf,
            new LdapGroupRoleResolver(
                conf,
                (groupDn, memberAttribute) -> {
                  Set<String> members = new HashSet<>();
                  members.add("uid=alice,ou=people,dc=example,dc=com");
                  return members;
                }));

    assertEquals(Role.ADMIN, resolver.resolve("alice"));
    assertEquals(Role.READ_ONLY, resolver.resolve("charlie"));
  }

  @Test
  public void testResolveNonAdminUserFallsBackToDefaultRole() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(), (groupDn, memberAttribute) -> Collections.singleton("bob"));

    assertEquals(Role.READ_ONLY, resolver.resolve("alice"));
  }

  private static Configurations baseConfig() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE, Role.READ_ONLY);
    conf.set(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED, true);
    conf.set(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL, "ldap://ldap.example.com:389");
    conf.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ADMIN_GROUP_DN,
        "cn=amoro-admins,ou=groups,dc=example,dc=com");
    conf.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_USER_DN_PATTERN,
        "uid={0},ou=people,dc=example,dc=com");
    return conf;
  }
}
