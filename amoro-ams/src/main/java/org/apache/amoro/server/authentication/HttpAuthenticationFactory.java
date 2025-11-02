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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.spi.PasswdAuthenticationProvider;
import org.apache.amoro.utils.DynConstructors;

public class HttpAuthenticationFactory {
  public static PasswdAuthenticationProvider getPasswordAuthenticationProvider(
      String providerClass, Configurations conf) {
    return createAuthenticationProvider(providerClass, PasswdAuthenticationProvider.class, conf);
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
}
