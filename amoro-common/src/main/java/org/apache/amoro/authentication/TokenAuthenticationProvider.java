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

package org.apache.amoro.authentication;

import org.apache.amoro.exception.SignatureCheckException;

import java.security.Principal;

public interface TokenAuthenticationProvider {
  /**
   * TokenAuthenticationProvider is used by the Amoro server authentication layer to validate Bearer
   * tokens, such as JWT (JSON Web Token), provided in client requests. If the token is invalid,
   * expired, or fails signature verification, a {@link SignatureCheckException} should be thrown to
   * deny access.
   *
   * @param credential The Bearer token credential (e.g., JWT) received in the connection request
   * @return The {@link Principal} associated with the authenticated token
   * @throws SignatureCheckException If the token is invalid, expired, or fails verification
   */
  Principal authenticate(TokenCredential credential) throws SignatureCheckException;
}
