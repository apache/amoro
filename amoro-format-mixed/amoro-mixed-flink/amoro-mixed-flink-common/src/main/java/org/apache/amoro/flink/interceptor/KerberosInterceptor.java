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

package org.apache.amoro.flink.interceptor;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.amoro.io.AuthenticatedFileIO;

import java.io.Serializable;
import java.lang.reflect.Method;

/** Using cglib proxy to avoid proxy object having different class */
public class KerberosInterceptor implements MethodInterceptor, Serializable {

  private static final long serialVersionUID = 1L;
  private final AuthenticatedFileIO authenticatedFileIO;

  public KerberosInterceptor(AuthenticatedFileIO authenticatedFileIO) {
    this.authenticatedFileIO = authenticatedFileIO;
  }

  @Override
  public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy)
      throws Throwable {
    Object res;
    try {
      res =
          authenticatedFileIO.doAs(
              () -> {
                try {
                  return proxy.invokeSuper(o, args);
                } catch (Throwable e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (RuntimeException e) {
      throw e.getCause();
    }
    return res;
  }
}
