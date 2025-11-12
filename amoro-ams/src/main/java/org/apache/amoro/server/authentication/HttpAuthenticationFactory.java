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

import static org.apache.amoro.authentication.TokenCredential.CLIENT_IP_KEY;

import io.javalin.core.util.Header;
import io.javalin.http.Context;
import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.authentication.TokenAuthenticationProvider;
import org.apache.amoro.authentication.TokenCredential;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.utils.DynConstructors;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HttpAuthenticationFactory {
  public static final String BEARER_TOKEN_SCHEMA = "BEARER";

  public static PasswdAuthenticationProvider getPasswordAuthenticationProvider(
      String providerClass, Configurations conf) {
    return createAuthenticationProvider(providerClass, PasswdAuthenticationProvider.class, conf);
  }

  public static TokenAuthenticationProvider getBearerAuthenticationProvider(
      String providerClass, Configurations conf) {
    return createAuthenticationProvider(providerClass, TokenAuthenticationProvider.class, conf);
  }

  private static <T> T createAuthenticationProvider(
      String className, Class<T> expected, Configurations conf) {
    try {
      return DynConstructors.builder(expected)
          .impl(className, Configurations.class)
          .impl(className)
          .<T>buildChecked()
          .newInstance(conf);
    } catch (Exception e) {
      throw new IllegalStateException(className + " must extend of " + expected.getName());
    }
  }

  public static TokenCredential getBearerTokenCredential(
      Context context, String proxyClientIpHeader) {
    return new DefaultTokenCredential(
        getBearerToken(context), getCredentialExtraInfo(context, proxyClientIpHeader));
  }

  /**
   * Extracts the Bearer token from the HTTP Authorization header in the request context. Returns
   * the token string if present and valid, otherwise returns null.
   */
  private static String getBearerToken(Context context) {
    String authorization = context.header(Header.AUTHORIZATION);
    if (authorization != null) {
      String[] parts = authorization.trim().split("\\s+", 2);
      if (parts.length == 2 && BEARER_TOKEN_SCHEMA.equalsIgnoreCase(parts[0])) {
        return parts[1].trim();
      }
    }
    return null;
  }

  private static Map<String, String> getCredentialExtraInfo(
      Context context, String proxyClientIpHeader) {
    return Collections.singletonMap(
        CLIENT_IP_KEY,
        Optional.ofNullable(context.header(proxyClientIpHeader)).orElse(context.ip()));
  }
}
