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

package com.netease.arctic.ams.server.persistence;

import com.netease.arctic.ams.server.exception.ArcticRuntimeException;
import com.netease.arctic.ams.server.exception.PersistenceException;
import org.apache.ibatis.session.TransactionIsolationLevel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class PersistentBase {
  private static final ConcurrentHashMap<Class<?>, Class<?>> mapperIntfMap = new ConcurrentHashMap<>();

  protected PersistentBase() {
  }

  protected NestedSqlSession beginSession() {
    return NestedSqlSession.openSession(() -> SqlSessionFactoryProvider.getInstance().get()
        .openSession(TransactionIsolationLevel.READ_COMMITTED));
  }

  protected <T> void doAs(Class<T> mapperClz, Consumer<T> consumer) {
    try (NestedSqlSession session = beginSession()) {
      try {
        T mapper = getMapper(session, mapperClz);
        consumer.accept(mapper);
        session.commit();
      } catch (Throwable t) {
        session.rollback();
        throw new PersistenceException(t);
      }
    }
  }

  protected void doAsTransaction(Runnable... operations) {
    for (Runnable runnable : operations) {
      runnable.run();
    }
  }

  protected <T> void doAsExisted(
      Class<T> mapperClz, Function<T, Integer> func,
      Supplier<? extends ArcticRuntimeException> errorSupplier) {
    getAs(mapperClz, func, rows -> rows > 0, errorSupplier);
  }

  protected <T, R> R getAs(Class<T> mapperClz, Function<T, R> func) {
    try (NestedSqlSession session = beginSession()) {
      try {
        T mapper = getMapper(session, mapperClz);
        R result = func.apply(mapper);
        session.commit();
        return result;
      } catch (Throwable t) {
        session.rollback();
        throw t;
      }
    }
  }

  protected <T, R> R getAs(
      Class<T> mapperClz, Function<T, R> func, Predicate<R> predicate,
      Supplier<? extends ArcticRuntimeException> errorSupplier) {
    try (NestedSqlSession session = beginSession()) {
      R result;
      try {
        result = func.apply(getMapper(session, mapperClz));
        session.commit();
      } catch (Throwable t) {
        session.rollback();
        throw new PersistenceException(t);
      }
      if (!predicate.test(result)) {
        throw errorSupplier.get();
      }
      return result;
    }
  }

  protected static <T> T getMapper(NestedSqlSession sqlSession, Class<T> type) {
    return sqlSession.getSqlSession().getMapper(type);
  }
}
