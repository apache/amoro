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
import org.apache.amoro.authentication.TokenAuthenticationProvider;
import org.apache.amoro.authentication.TokenCredential;
import org.apache.amoro.exception.SignatureCheckException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

public class UserDefinedTokenAuthenticationProviderImpl implements TokenAuthenticationProvider {
  private static final Logger LOG =
      LoggerFactory.getLogger(UserDefinedTokenAuthenticationProviderImpl.class);
  public static final String VALID_TOKEN = "token";

  @Override
  public Principal authenticate(TokenCredential credential) throws SignatureCheckException {
    String clientIp = credential.extraInfo().get(TokenCredential.CLIENT_IP_KEY);
    if (VALID_TOKEN.equals(credential.token())) {
      LOG.info("Success log in of token: {} with clientIp: {}", credential.token(), clientIp);
      return new BasicPrincipal("user");
    } else {
      throw new SignatureCheckException("Token is not valid!");
    }
  }
}
