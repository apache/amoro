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

import org.apache.amoro.authentication.BasicPrincipal;
import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.authentication.PasswordCredential;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.AmoroManagementConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultPasswdAuthenticationProvider implements PasswdAuthenticationProvider {
  private static final Logger LOG =
      LoggerFactory.getLogger(DefaultPasswdAuthenticationProvider.class);

  private final String basicAuthUser;
  private final String basicAuthPassword;
  private final Map<String, String> localUsers;

  public DefaultPasswdAuthenticationProvider(Configurations conf) {
    this.basicAuthUser = conf.get(AmoroManagementConf.ADMIN_USERNAME);
    this.basicAuthPassword = conf.get(AmoroManagementConf.ADMIN_PASSWORD);
    this.localUsers = loadLocalUsers(conf);
  }

  @Override
  public BasicPrincipal authenticate(PasswordCredential credential) throws SignatureCheckException {
    String localPassword = localUsers.get(credential.username());
    if (localPassword != null) {
      if (localPassword.equals(credential.password())) {
        return new BasicPrincipal(credential.username());
      }
      throw new SignatureCheckException("Invalid password for user: " + credential.username());
    }

    if (!(basicAuthUser.equals(credential.username())
        && basicAuthPassword.equals(credential.password()))) {
      throw new SignatureCheckException("Failed to authenticate via basic authentication");
    }
    return new BasicPrincipal(credential.username());
  }

  private static Map<String, String> loadLocalUsers(Configurations conf) {
    if (!conf.get(AmoroManagementConf.AUTHORIZATION_ENABLED)) {
      return Collections.emptyMap();
    }

    List<Map<String, String>> users =
        conf.getOptional(AmoroManagementConf.AUTHORIZATION_USERS).orElse(Collections.emptyList());
    return users.stream()
        .filter(DefaultPasswdAuthenticationProvider::hasRequiredLocalAuthFields)
        .collect(
            Collectors.toMap(
                user -> String.valueOf(user.get("username")),
                user -> String.valueOf(user.get("password")),
                (existing, replacement) -> {
                  LOG.warn(
                      "Duplicate authorization.users entry for password auth, keeping last user definition");
                  return replacement;
                }));
  }

  private static boolean hasRequiredLocalAuthFields(Map<String, String> user) {
    if (user.get("username") == null || user.get("password") == null) {
      LOG.warn("Ignore invalid authorization.users entry for password auth: {}", user);
      return false;
    }
    return true;
  }
}
