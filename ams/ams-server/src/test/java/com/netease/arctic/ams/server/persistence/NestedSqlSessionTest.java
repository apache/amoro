package com.netease.arctic.ams.server.persistence;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
public class NestedSqlSessionTest {

  @Test
  public void testNestedTransactions() {
    SqlSession sqlSession = mock(SqlSession.class);
    Supplier<SqlSession> sessionSupplier = () -> sqlSession;

    NestedSqlSession session = NestedSqlSession.openSession(sessionSupplier);
    verify(session, times(1)).beginTransaction();
    verifyNoMoreInteractions(sqlSession);

    // Test commit with nested transaction
    session.beginTransaction();
    session.beginTransaction();
    session.commit();
    verifyNoMoreInteractions(sqlSession);
    session.commit();
    verify(sqlSession, times(1)).commit();
    verifyNoMoreInteractions(sqlSession);

    // Test rollback with nested transaction
    session.beginTransaction();
    session.beginTransaction();
    session.rollback();
    verify(sqlSession, times(1)).rollback(true);
    verifyNoMoreInteractions(sqlSession);
    session.rollback();
    verifyNoMoreInteractions(sqlSession);
  }

  @Test(expected = IllegalStateException.class)
  public void testRollbackWithNoTransaction() {
    SqlSession sqlSession = mock(SqlSession.class);
    Supplier<SqlSession> sessionSupplier = () -> sqlSession;

    NestedSqlSession session = NestedSqlSession.openSession(sessionSupplier);
//    verify(sqlSession, times(1)).beginTransaction();
    verifyNoMoreInteractions(sqlSession);

    session.rollback();
    verify(sqlSession, times(1)).rollback(true);
  }

  @Test(expected = IllegalStateException.class)
  public void testBeginTransactionAfterClose() {
    SqlSession sqlSession = mock(SqlSession.class);
    Supplier<SqlSession> sessionSupplier = () -> sqlSession;

    NestedSqlSession session = NestedSqlSession.openSession(sessionSupplier);
    verify(session, times(1)).beginTransaction();
    verifyNoMoreInteractions(sqlSession);

    session.close();
    session.beginTransaction();
  }

  @Test(expected = IllegalStateException.class)
  public void testMaxNestedTransaction() {
    SqlSession sqlSession = mock(SqlSession.class);
    Supplier<SqlSession> sessionSupplier = () -> sqlSession;

    NestedSqlSession session = NestedSqlSession.openSession(sessionSupplier);
    verify(session, times(1)).beginTransaction();
    verifyNoMoreInteractions(sqlSession);

    for (int i = 0; i < NestedSqlSession.MAX_NEST_BEGIN_COUNT; i++) {
      session.beginTransaction();
    }
    verifyNoMoreInteractions(sqlSession);

    session.beginTransaction();
  }
}
