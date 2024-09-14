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

package org.apache.amoro.server.persistence;

import org.apache.amoro.server.exception.AmoroRuntimeException;
import org.apache.amoro.server.exception.PersistenceException;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class PersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(PersistentBase.class);

  protected PersistentBase() {}

  @VisibleForTesting
  protected NestedSqlSession beginSession() {
    return NestedSqlSession.openSession(
        () ->
            SqlSessionFactoryProvider.getInstance()
                .get()
                .openSession(TransactionIsolationLevel.READ_COMMITTED));
  }

  public final <T> Long updateAs(Class<T> mapperClz, Function<T, Number> updateFunction) {
    try (NestedSqlSession session = beginSession()) {
      try {
        T mapper = getMapper(session, mapperClz);
        Number number = updateFunction.apply(mapper);
        session.commit();
        return number.longValue();
      } catch (Throwable t) {
        session.rollback();
        throw AmoroRuntimeException.wrap(t, PersistenceException::new);
      }
    }
  }

  protected final <T> void doAs(Class<T> mapperClz, Consumer<T> consumer) {
    try (NestedSqlSession session = beginSession()) {
      try {
        T mapper = getMapper(session, mapperClz);
        consumer.accept(mapper);
        session.commit();
      } catch (Throwable t) {
        session.rollback();
        throw AmoroRuntimeException.wrap(t, PersistenceException::new);
      }
    }
  }

  protected final <T> void doAsIgnoreError(Class<T> mapperClz, Consumer<T> consumer) {
    try (NestedSqlSession session = beginSession()) {
      try {
        T mapper = getMapper(session, mapperClz);
        consumer.accept(mapper);
        session.commit();
      } catch (Throwable t) {
        LOG.error("Ignore error in doAsIgnoreError", t);
      }
    }
  }

  protected final void doAsTransaction(Runnable... operations) {
    try (NestedSqlSession session = beginSession()) {
      try {
        Arrays.stream(operations).forEach(Runnable::run);
        session.commit();
      } catch (Throwable t) {
        session.rollback();
        throw AmoroRuntimeException.wrap(t, PersistenceException::new);
      }
    }
  }

  protected final <T, R> R getAs(Class<T> mapperClz, Function<T, R> func) {
    try (NestedSqlSession session = beginSession()) {
      try {
        T mapper = getMapper(session, mapperClz);
        return func.apply(mapper);
      } catch (Throwable t) {
        throw AmoroRuntimeException.wrap(t, PersistenceException::new);
      }
    }
  }

  protected final <T> void doAsExisted(
      Class<T> mapperClz,
      Function<T, Integer> func,
      Supplier<? extends AmoroRuntimeException> errorSupplier) {
    try (NestedSqlSession session = beginSession()) {
      try {
        int result = func.apply(getMapper(session, mapperClz));
        if (result == 0) {
          throw errorSupplier.get();
        }
        session.commit();
      } catch (Throwable t) {
        session.rollback();
        throw AmoroRuntimeException.wrap(t, PersistenceException::new);
      }
    }
  }

  protected static <T> T getMapper(NestedSqlSession sqlSession, Class<T> type) {
    Preconditions.checkNotNull(sqlSession);
    return sqlSession.getSqlSession().getMapper(type);
  }
}
