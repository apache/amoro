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

package org.apache.iceberg.flink.krb.interceptor;

import org.apache.amoro.table.TableMetaStore;
import org.apache.iceberg.flink.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Proxy for iceberg-flink class. To support kerberos. Using jdk proxy can surrogate an instance
 * which already exists.
 *
 * @param <T> proxy class type
 */
public class KerberosInvocationHandler<T> implements InvocationHandler, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(KerberosInvocationHandler.class);

  private static final long serialVersionUID = 1L;
  private final TableMetaStore tableMetaStore;
  private T obj;

  public KerberosInvocationHandler(TableMetaStore tableMetaStore) {
    this.tableMetaStore = tableMetaStore;
  }

  public Object getProxy(T obj) {
    this.obj = obj;
    return Proxy.newProxyInstance(
        obj.getClass().getClassLoader(), ReflectionUtil.getAllInterface(obj.getClass()), this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object res;
    try {
      res =
          tableMetaStore.doAs(
              () -> {
                try {
                  method.setAccessible(true);
                  return method.invoke(obj, args);
                } catch (Throwable e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (Exception e) {
      LOG.error("Failed to invoke method {} with args.{}", method.getName(), args, e.getCause());
      throw e.getCause();
    }
    return res;
  }
}
