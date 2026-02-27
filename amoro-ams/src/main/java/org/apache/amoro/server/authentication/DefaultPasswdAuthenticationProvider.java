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

public class DefaultPasswdAuthenticationProvider implements PasswdAuthenticationProvider {
  private String basicAuthUser;
  private String basicAuthPassword;

  public DefaultPasswdAuthenticationProvider(Configurations conf) {
    this.basicAuthUser = conf.get(AmoroManagementConf.ADMIN_USERNAME);
    this.basicAuthPassword = conf.get(AmoroManagementConf.ADMIN_PASSWORD);
  }

  @Override
  public BasicPrincipal authenticate(PasswordCredential credential) throws SignatureCheckException {
    if (!(basicAuthUser.equals(credential.username())
        && basicAuthPassword.equals(credential.password()))) {
      throw new SignatureCheckException("Failed to authenticate via basic authentication");
    }
    return new BasicPrincipal(credential.username());
  }
}
