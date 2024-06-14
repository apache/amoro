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

package org.apache.iceberg.flink.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.amoro.table.TableMetaStore;
import org.apache.iceberg.flink.krb.interceptor.KerberosInterceptor;
import org.apache.iceberg.flink.krb.interceptor.KerberosInvocationHandler;
import org.apache.iceberg.flink.krb.interceptor.ProxyFactory;

/**
 * A proxy util wraps an object with the kerberos authenticate ability by {@link
 * KerberosInvocationHandler}.
 */
public class ProxyUtil {
  private ProxyUtil() {}

  public static <T> Object getProxy(T obj, KerberosInvocationHandler<T> handler) {
    return handler.getProxy(obj);
  }

  public static <T> Object getProxy(T obj, TableMetaStore tableMetaStore) {
    KerberosInvocationHandler<T> handler = new KerberosInvocationHandler<>(tableMetaStore);
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
      Class<T> clazz, TableMetaStore tableMetaStore, Class[] argumentTypes, Object[] arguments) {
    return getProxy(clazz, new KerberosInterceptor(tableMetaStore), argumentTypes, arguments);
  }

  public static <T> ProxyFactory<T> getProxyFactory(
      Class<T> clazz, TableMetaStore tableMetaStore, Class[] argumentTypes, Object[] arguments) {
    return new ProxyFactory<T>(
        clazz, new KerberosInterceptor(tableMetaStore), argumentTypes, arguments);
  }
}
