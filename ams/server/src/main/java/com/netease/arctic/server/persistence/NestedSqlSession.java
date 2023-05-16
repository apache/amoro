package com.netease.arctic.server.persistence;

import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.io.Closeable;
import java.util.function.Supplier;

public final class NestedSqlSession implements Closeable {
  protected static final int MAX_NEST_BEGIN_COUNT = 5;
  private static final ThreadLocal<NestedSqlSession> sessions = new ThreadLocal<>();
  private int nestCount = 0;
  private SqlSession sqlSession;

  public static NestedSqlSession openSession(Supplier<SqlSession> sessionSupplier) {
    NestedSqlSession session = sessions.get();
    if (session == null) {
      sessions.set(new NestedSqlSession(sessionSupplier.get()));
    }
    return sessions.get().beginTransaction();
  }

  protected SqlSession getSqlSession() {
    return sqlSession;
  }

  private NestedSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  NestedSqlSession beginTransaction() {
    Preconditions.checkState(sqlSession != null, "session already closed");
    Preconditions.checkState(++nestCount < MAX_NEST_BEGIN_COUNT, "beginTransaction() has not " +
        "been properly called for nest count is " + nestCount);
    return this;
  }

  public void commit() {
    Preconditions.checkState(sqlSession != null, "session already closed");
    try {
      if (nestCount > 0) {
        if (--nestCount == 0) {
          sqlSession.commit(true);
        }
      }
    } catch (Exception e) {
      nestCount = 0;
      throw e;
    }
  }

  public void rollback() {
    if (nestCount > 0) {
      Preconditions.checkState(sqlSession != null, "session already closed");
      nestCount = 0;
      sqlSession.rollback(true);
    }
  }

  public void close() {
    if (nestCount == 0 && sqlSession != null) {
      sqlSession.close();
      sqlSession = null;
      sessions.set(null);
      nestCount = -1;
    }
  }
}
