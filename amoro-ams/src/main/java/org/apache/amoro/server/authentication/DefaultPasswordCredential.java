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

import org.apache.amoro.authentication.PasswordCredential;

import java.util.Collections;
import java.util.Map;

public class DefaultPasswordCredential implements PasswordCredential {
  private String username;
  private String password;
  private Map<String, String> extraInfo;

  public DefaultPasswordCredential(
      String username, String password, Map<String, String> extraInfo) {
    this.username = username;
    this.password = password;
    this.extraInfo = extraInfo;
  }

  public DefaultPasswordCredential(String username, String password) {
    this(username, password, Collections.emptyMap());
  }

  @Override
  public String username() {
    return username;
  }

  @Override
  public String password() {
    return password;
  }

  @Override
  public Map<String, String> extraInfo() {
    return null == extraInfo ? Collections.emptyMap() : extraInfo;
  }
}
