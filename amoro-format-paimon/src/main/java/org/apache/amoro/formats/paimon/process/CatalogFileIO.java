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

package org.apache.amoro.formats.paimon.process;

import org.apache.amoro.shade.guava32.com.google.common.base.Throwables;
import org.apache.amoro.shade.guava32.com.google.common.reflect.AbstractInvocationHandler;
import org.apache.amoro.shade.guava32.com.google.common.reflect.Reflection;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.paimon.fs.FileIO;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;

/** Binds native FileIO calls, including storage-specific method overrides, to the catalog user. */
final class CatalogFileIO extends AbstractInvocationHandler {
  private final FileIO delegate;
  private final UserGroupInformation user;

  private CatalogFileIO(FileIO delegate, UserGroupInformation user) {
    this.delegate = delegate;
    this.user = user;
  }

  static FileIO bindToCurrentUser(FileIO io) throws IOException {
    return Reflection.newProxy(
        FileIO.class, new CatalogFileIO(io, UserGroupInformation.getCurrentUser()));
  }

  @Override
  protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
    return user.doAs(
        (PrivilegedExceptionAction<Object>)
            () -> {
              try {
                return method.invoke(delegate, args);
              } catch (InvocationTargetException e) {
                // Preserve FileIO's checked exceptions, including FileNotFoundException used by
                // expiration.
                Throwable cause = e.getCause();
                Throwables.throwIfInstanceOf(cause, IOException.class);
                Throwables.throwIfUnchecked(cause);
                throw new IllegalStateException(cause);
              }
            });
  }
}
