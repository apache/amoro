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

package org.apache.amoro.server.authentication;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.authentication.PasswordCredential;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Principal;

/**
 * TestSuite to test amoro's LDAP Authentication provider with an in-process LDAP Server (Apache
 * Directory Server instance).
 *
 * <p>refer to https://directory.apache.org/apacheds/advanced-ug/7-embedding-apacheds.html
 */
@RunWith(FrameworkRunner.class)
@CreateLdapServer(
    transports = {@CreateTransport(protocol = "LDAP"), @CreateTransport(protocol = "LDAPS")})
@CreateDS(partitions = {@CreatePartition(name = "test", suffix = "dc=test,dc=com")})
@ApplyLdifFiles({
  "ldap/users.ldif",
})
public class LdapPasswdAuthenticationProviderTest extends AbstractLdapTestUnit {

  @Test
  public void testAuthenticate() throws Exception {
    assertTrue(ldapServer.isStarted());
    assertTrue(ldapServer.getPort() > 0);

    Configurations conf = new Configurations();
    String ldapUrl = "ldap://localhost:" + ldapServer.getPort();
    conf.set(
        AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_PROVIDER,
        LdapPasswdAuthenticationProvider.class.getName());
    conf.set(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL, ldapUrl + " " + ldapUrl);
    conf.set(
        AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_USER_PATTERN,
        "cn={0},ou=Users,dc=test,dc=com");

    PasswdAuthenticationProvider provider =
        HttpAuthenticationFactory.getPasswordAuthenticationProvider(
            conf.get(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_PROVIDER), conf);

    // Test successful authentication with correct password
    PasswordCredential correctCredential = new DefaultPasswordCredential("user1", "12345");
    Principal principal = provider.authenticate(correctCredential);
    assertTrue(principal.getName().equals("user1"));

    // Test successful authentication with incorrect password
    assertThrows(
        SignatureCheckException.class,
        () -> {
          provider.authenticate(new DefaultPasswordCredential("user1", "123456"));
        });

    // Test successful authentication with incorrect user
    assertThrows(
        SignatureCheckException.class,
        () -> {
          provider.authenticate(new DefaultPasswordCredential("user2", "12345"));
        });
  }
}
