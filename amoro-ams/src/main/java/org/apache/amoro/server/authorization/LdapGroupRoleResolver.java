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
import org.apache.amoro.server.utils.PreconditionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

class LdapGroupRoleResolver {
  private static final Logger LOG = LoggerFactory.getLogger(LdapGroupRoleResolver.class);

  interface GroupMemberLoader {
    Set<String> loadMembers(String groupDn, String memberAttribute) throws NamingException;
  }

  private final boolean enabled;
  private final Role defaultRole;
  private final String adminGroupDn;
  private final String memberAttribute;
  private final MessageFormat userDnFormatter;
  private final GroupMemberLoader groupMemberLoader;

  static LdapGroupRoleResolver disabled(Role defaultRole) {
    return new LdapGroupRoleResolver(defaultRole);
  }

  /** Production constructor that resolves LDAP group membership through JNDI. */
  LdapGroupRoleResolver(Configurations conf) {
    this(conf, new JndiGroupMemberLoader(conf));
  }

  private LdapGroupRoleResolver(Role defaultRole) {
    this.enabled = false;
    this.defaultRole = defaultRole;
    this.adminGroupDn = "";
    this.memberAttribute = "";
    this.userDnFormatter = new MessageFormat("{0}");
    this.groupMemberLoader = (groupDn, attribute) -> Collections.emptySet();
  }

  LdapGroupRoleResolver(Configurations conf, GroupMemberLoader groupMemberLoader) {
    this.enabled = conf.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ENABLED);
    this.defaultRole = conf.get(AmoroManagementConf.AUTHORIZATION_DEFAULT_ROLE);
    this.groupMemberLoader = groupMemberLoader;

    if (!enabled) {
      this.adminGroupDn = "";
      this.memberAttribute = "";
      this.userDnFormatter = new MessageFormat("{0}");
      return;
    }

    String ldapUrl = conf.get(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL);
    PreconditionUtils.checkNotNullOrEmpty(
        ldapUrl, AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL.key());

    this.adminGroupDn =
        conf.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ADMIN_GROUP_DN);
    PreconditionUtils.checkNotNullOrEmpty(
        adminGroupDn, AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_ADMIN_GROUP_DN.key());

    this.memberAttribute =
        conf.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_GROUP_MEMBER_ATTRIBUTE);
    PreconditionUtils.checkNotNullOrEmpty(
        memberAttribute,
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_GROUP_MEMBER_ATTRIBUTE.key());

    String userDnPattern =
        conf.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_USER_DN_PATTERN);
    PreconditionUtils.checkNotNullOrEmpty(
        userDnPattern, AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_USER_DN_PATTERN.key());
    this.userDnFormatter = new MessageFormat(userDnPattern);
  }

  Role resolve(String username) {
    if (!enabled) {
      return defaultRole;
    }

    String userDn = userDnFormatter.format(new String[] {username});
    try {
      Set<String> members = groupMemberLoader.loadMembers(adminGroupDn, memberAttribute);
      for (String member : members) {
        if (matchesMember(username, userDn, member)) {
          return Role.ADMIN;
        }
      }
    } catch (NamingException e) {
      LOG.error(
          "Failed to query LDAP group {} for user {}. "
              + "This is likely a configuration error (e.g. missing or wrong bind-password, "
              + "incorrect admin-group-dn). Please check the authorization.ldap-role-mapping "
              + "settings in config.yaml.",
          adminGroupDn,
          username,
          e);
      throw new RuntimeException(
          "LDAP role resolution failed: unable to query group '"
              + adminGroupDn
              + "'. Please check the LDAP role-mapping configuration (bind-dn/bind-password/admin-group-dn).",
          e);
    }
    return defaultRole;
  }

  private static boolean matchesMember(String username, String userDn, String member) {
    if (member == null) {
      return false;
    }

    String normalized = member.trim();
    // Support DN-style ("CN=alice,OU=..."), plain username, and uid= prefix formats.
    return normalized.equalsIgnoreCase(userDn)
        || normalized.equalsIgnoreCase(username)
        || normalized.equalsIgnoreCase("uid=" + username)
        || extractCnFromDn(normalized).equalsIgnoreCase(username);
  }

  /** Extract the CN value from a DN string, e.g. "CN=xuba,OU=Employees,..." → "xuba". */
  private static String extractCnFromDn(String dn) {
    if (dn.toUpperCase().startsWith("CN=")) {
      int commaIdx = dn.indexOf(',');
      return commaIdx > 0 ? dn.substring(3, commaIdx) : dn.substring(3);
    }
    return "";
  }

  private static class JndiGroupMemberLoader implements GroupMemberLoader {
    private final String ldapUrl;
    private final String bindDn;
    private final String bindPassword;

    private JndiGroupMemberLoader(Configurations conf) {
      this.ldapUrl = conf.get(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL);
      this.bindDn = conf.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_BIND_DN);
      this.bindPassword =
          conf.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_BIND_PASSWORD);
    }

    @Override
    public Set<String> loadMembers(String groupDn, String memberAttribute) throws NamingException {
      Hashtable<String, Object> env = new Hashtable<>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, ldapUrl);
      env.put("com.sun.jndi.ldap.connect.timeout", "10000");
      env.put("com.sun.jndi.ldap.read.timeout", "10000");
      env.put(Context.REFERRAL, "follow");
      if (!bindDn.isEmpty()) {
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindDn);
        env.put(Context.SECURITY_CREDENTIALS, bindPassword);
      }

      InitialDirContext ldapContext = null;
      try {
        ldapContext = new InitialDirContext(env);
        LOG.debug(
            "Loading LDAP group members by full DN: groupDn={}, memberAttribute={}",
            groupDn,
            memberAttribute);
        Attributes attributes = ldapContext.getAttributes(groupDn, new String[] {memberAttribute});
        return extractMembers(attributes, memberAttribute);
      } finally {
        if (ldapContext != null) {
          try {
            ldapContext.close();
          } catch (NamingException e) {
            LOG.warn("Failed to close LDAP role-mapping context", e);
          }
        }
      }
    }

    /** Extract all member values from an Attributes object. */
    private static Set<String> extractMembers(Attributes attributes, String memberAttribute)
        throws NamingException {
      Attribute memberValues = attributes.get(memberAttribute);
      if (memberValues == null) {
        return Collections.emptySet();
      }

      Set<String> members = new HashSet<>();
      NamingEnumeration<?> values = memberValues.getAll();
      while (values.hasMore()) {
        Object value = values.next();
        if (value != null) {
          members.add(value.toString());
        }
      }
      return members;
    }
  }
}
