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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.authentication.TokenAuthenticationProvider;
import org.apache.amoro.authentication.TokenCredential;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.AmoroManagementConf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpAuthenticationFactoryTest {
  @Test
  public void testPasswordAuthenticationProvider() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "admin");
    conf.set(AmoroManagementConf.ADMIN_PASSWORD, "password");

    assertThrows(
        IllegalStateException.class,
        () -> {
          HttpAuthenticationFactory.getPasswordAuthenticationProvider(
              "NonExistentProviderClass", conf);
        });

    PasswdAuthenticationProvider passwdAuthenticationProvider =
        HttpAuthenticationFactory.getPasswordAuthenticationProvider(
            DefaultPasswdAuthenticationProvider.class.getName(), conf);

    assert passwdAuthenticationProvider
        .authenticate(new DefaultPasswordCredential("admin", "password"))
        .getName()
        .equals("admin");

    assertThrows(
        SignatureCheckException.class,
        () -> {
          passwdAuthenticationProvider.authenticate(
              new DefaultPasswordCredential("admin", "invalidPassword"));
        });
    assertThrows(
        SignatureCheckException.class,
        () -> {
          passwdAuthenticationProvider.authenticate(
              new DefaultPasswordCredential("nonAdmin", "password"));
        });
  }

  @Test
  public void testPasswordAuthenticationProviderWithLocalUsers() throws Exception {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "admin");
    conf.set(AmoroManagementConf.ADMIN_PASSWORD, "password");
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(localUser("viewer", "viewer123", "READ_ONLY")));

    PasswdAuthenticationProvider passwdAuthenticationProvider =
        HttpAuthenticationFactory.getPasswordAuthenticationProvider(
            DefaultPasswdAuthenticationProvider.class.getName(), conf);

    assert passwdAuthenticationProvider
        .authenticate(new DefaultPasswordCredential("viewer", "viewer123"))
        .getName()
        .equals("viewer");
  }

  @Test
  public void testPasswordAuthenticationProviderIgnoresLocalUsersWhenAuthorizationDisabled() {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "admin");
    conf.set(AmoroManagementConf.ADMIN_PASSWORD, "password");
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, false);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(localUser("viewer", "viewer123", "READ_ONLY")));

    PasswdAuthenticationProvider passwdAuthenticationProvider =
        HttpAuthenticationFactory.getPasswordAuthenticationProvider(
            DefaultPasswdAuthenticationProvider.class.getName(), conf);

    assertThrows(
        SignatureCheckException.class,
        () ->
            passwdAuthenticationProvider.authenticate(
                new DefaultPasswordCredential("viewer", "viewer123")));
  }

  @Test
  public void testPasswordAuthenticationProviderKeepsLastDuplicateLocalUser() throws Exception {
    Configurations conf = new Configurations();
    conf.set(AmoroManagementConf.ADMIN_USERNAME, "admin");
    conf.set(AmoroManagementConf.ADMIN_PASSWORD, "password");
    conf.set(AmoroManagementConf.AUTHORIZATION_ENABLED, true);
    conf.set(
        AmoroManagementConf.AUTHORIZATION_USERS,
        Arrays.asList(
            localUser("viewer", "viewer123", "READ_ONLY"),
            localUser("viewer", "viewer456", "READ_ONLY")));

    PasswdAuthenticationProvider passwdAuthenticationProvider =
        HttpAuthenticationFactory.getPasswordAuthenticationProvider(
            DefaultPasswdAuthenticationProvider.class.getName(), conf);

    assertThrows(
        SignatureCheckException.class,
        () ->
            passwdAuthenticationProvider.authenticate(
                new DefaultPasswordCredential("viewer", "viewer123")));
    assert passwdAuthenticationProvider
        .authenticate(new DefaultPasswordCredential("viewer", "viewer456"))
        .getName()
        .equals("viewer");
  }

  @Test
  public void testBearerTokenAuthenticationProvider() {
    Configurations conf = new Configurations();
    assertThrows(
        IllegalStateException.class,
        () -> {
          HttpAuthenticationFactory.getBearerAuthenticationProvider(
              "NonExistentProviderClass", conf);
        });

    TokenAuthenticationProvider tokenAuthenticationProvider =
        HttpAuthenticationFactory.getBearerAuthenticationProvider(
            UserDefinedTokenAuthenticationProviderImpl.class.getName(), conf);

    assert tokenAuthenticationProvider
        .authenticate(
            new DefaultTokenCredential(
                "token", Collections.singletonMap(TokenCredential.CLIENT_IP_KEY, "localhost")))
        .getName()
        .equals("user");
    assertThrows(
        SignatureCheckException.class,
        () -> {
          tokenAuthenticationProvider.authenticate(
              new DefaultTokenCredential(
                  "invalidToken",
                  Collections.singletonMap(TokenCredential.CLIENT_IP_KEY, "localhost")));
        });
  }

  private static Map<String, String> localUser(String username, String password, String role) {
    Map<String, String> user = new HashMap<>();
    user.put("username", username);
    user.put("password", password);
    user.put("role", role);
    return user;
  }
}
