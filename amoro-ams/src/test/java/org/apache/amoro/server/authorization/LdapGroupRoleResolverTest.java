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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class LdapGroupRoleResolverTest {

  @Test
  public void testResolveRoleFromUserDnMember() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                "cn=amoro-service-admins,ou=groups,dc=example,dc=com".equals(groupDn)
                    ? Collections.singleton("uid=alice,ou=people,dc=example,dc=com")
                    : Collections.emptySet());

    assertEquals(Collections.singleton(Role.SERVICE_ADMIN), resolver.resolve("alice"));
  }

  @Test
  public void testResolveRoleFromUsernameMember() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                "cn=amoro-service-admins,ou=groups,dc=example,dc=com".equals(groupDn)
                    ? Set.of("alice")
                    : Collections.emptySet());

    assertEquals(Collections.singleton(Role.SERVICE_ADMIN), resolver.resolve("alice"));
  }

  @Test
  public void testResolveRoleFromCnDnMember() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                "cn=amoro-service-admins,ou=groups,dc=example,dc=com".equals(groupDn)
                    ? Set.of("CN=alice,OU=Employees,OU=Cisco Users,DC=cisco,DC=com")
                    : Collections.emptySet());

    assertEquals(Collections.singleton(Role.SERVICE_ADMIN), resolver.resolve("alice"));
  }

  @Test
  public void testResolveRoleFromMemberUidStyleValue() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                "cn=amoro-service-admins,ou=groups,dc=example,dc=com".equals(groupDn)
                    ? Set.of("uid=alice")
                    : Collections.emptySet());

    assertEquals(Collections.singleton(Role.SERVICE_ADMIN), resolver.resolve("alice"));
  }

  @Test
  public void testResolveRoleIsCaseInsensitiveForDnMembers() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                "cn=amoro-service-admins,ou=groups,dc=example,dc=com".equals(groupDn)
                    ? Set.of("UID=Alice,OU=People,DC=Example,DC=Com")
                    : Collections.emptySet());

    assertEquals(Collections.singleton(Role.SERVICE_ADMIN), resolver.resolve("alice"));
  }

  @Test
  public void testResolveViewerGroupRole() {
    LdapGroupRoleResolver resolver =
        new LdapGroupRoleResolver(
            baseConfig(),
            (groupDn, memberAttribute) ->
                "cn=amoro-viewers,ou=groups,dc=example,dc=com".equals(groupDn)
                    ? Set.of("bob")
                    : Collections.emptySet());

    assertEquals(Collections.singleton(Role.VIEWER), resolver.resolve("bob"));
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
    conf.set(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED, true);
    conf.set(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL, "ldap://ldap.example.com:389");

    assertThrows(IllegalArgumentException.class, () -> new LdapGroupRoleResolver(conf));
  }

  private static Configurations baseConfig() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED, true);
    conf.set(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL, "ldap://ldap.example.com:389");
    conf.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_USER_DN_PATTERN,
        "uid={0},ou=people,dc=example,dc=com");
    conf.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_GROUPS,
        Arrays.asList(
            group("cn=amoro-service-admins,ou=groups,dc=example,dc=com", "SERVICE_ADMIN"),
            group("cn=amoro-viewers,ou=groups,dc=example,dc=com", "VIEWER")));
    return conf;
  }

  private static Map<String, String> group(String groupDn, String role) {
    return Map.of("group-dn", groupDn, "role", role);
  }
}
