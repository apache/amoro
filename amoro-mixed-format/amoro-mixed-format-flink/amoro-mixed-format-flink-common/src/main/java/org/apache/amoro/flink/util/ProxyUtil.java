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

package org.apache.amoro.flink.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.amoro.flink.interceptor.KerberosInterceptor;
import org.apache.amoro.flink.interceptor.KerberosInvocationHandler;
import org.apache.amoro.flink.interceptor.ProxyFactory;
import org.apache.amoro.io.AuthenticatedFileIO;

/**
 * A proxy util wraps an object with the kerberos authenticate ability by {@link
 * KerberosInvocationHandler}.
 */
public class ProxyUtil {

  public static <T> Object getProxy(T obj, KerberosInvocationHandler<T> handler) {
    return handler.getProxy(obj);
  }

  public static <T> Object getProxy(T obj, AuthenticatedFileIO authenticatedFileIO) {
    KerberosInvocationHandler<T> handler = new KerberosInvocationHandler<>(authenticatedFileIO);
    return getProxy(obj, handler);
  }

  public static <T> T getProxy(
      Class<T> clazz, MethodInterceptor interceptor, Class[] argumentTypes, Object[] arguments) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(clazz);
    enhancer.setCallback(interceptor);
    return (T) enhancer.create(argumentTypes, arguments);
  }

  public static <T> T getProxy(
      Class<T> clazz,
      AuthenticatedFileIO authenticatedFileIO,
      Class[] argumentTypes,
      Object[] arguments) {
    return getProxy(clazz, new KerberosInterceptor(authenticatedFileIO), argumentTypes, arguments);
  }

  public static <T> ProxyFactory<T> getProxyFactory(
      Class<T> clazz,
      AuthenticatedFileIO authenticatedFileIO,
      Class[] argumentTypes,
      Object[] arguments) {
    return new ProxyFactory<T>(
        clazz, new KerberosInterceptor(authenticatedFileIO), argumentTypes, arguments);
  }
}
