package com.netease.arctic.server.persistence;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

public class NestedSqlSessionTest {

  @Mock
  private SqlSession sqlSession;
  private NestedSqlSession nestedSession;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    nestedSession = new NestedSqlSession(sqlSession);
  }

  @AfterEach
  void tearDown() {
    nestedSession.close();
  }

  @Test
  void testGetSqlSession() {
    assertSame(sqlSession, nestedSession.getSqlSession());
  }

  @Test
  void testOpenSession() throws Exception {
    nestedSession.close();
    Supplier<SqlSession> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn(sqlSession);

    NestedSqlSession session = NestedSqlSession.openSession(supplier);
    assertNotNull(session);
    verify(supplier).get();
    assertEquals(1, getNestCount(session));
    verifyNoMoreInteractions(supplier);
  }

  @Test
  void testClose() {
    nestedSession.close();
    verify(sqlSession).close();
    assertNull(nestedSession.getSqlSession());
  }

  @Test
  void testCommitWithNestCountGreaterThanZero() {
    nestedSession.beginTransaction();
    nestedSession.beginTransaction();
    nestedSession.commit(true);
    verifyZeroInteractions(sqlSession);

    nestedSession.commit(true);
    verify(sqlSession).commit(true);
  }

  @Test
  void testCommitWithNestCountEqualToZero() {
    nestedSession.commit(true);
    verifyZeroInteractions(sqlSession);
  }

  @Test
  void testRollbackWithNestCountGreaterThanZero() {
    nestedSession.beginTransaction();
    nestedSession.beginTransaction();
    nestedSession.rollback(true);
    verify(sqlSession).rollback(true);

    nestedSession.rollback(true);
    verifyZeroInteractions(sqlSession);
  }

  @Test
  void testRollbackWithNestCountEqualToZero() {
    nestedSession.rollback(true);
    verifyZeroInteractions(sqlSession);
  }

  @Test
  void testBeginTransaction() throws Exception {
    assertSame(nestedSession, nestedSession.beginTransaction());
    assertEquals(1, getNestCount(nestedSession));

    nestedSession.beginTransaction();
    assertEquals(2, getNestCount(nestedSession));

    nestedSession.beginTransaction();
    assertEquals(3, getNestCount(nestedSession));

    nestedSession.beginTransaction();
    assertEquals(4, getNestCount(nestedSession));

    assertThrows(IllegalStateException.class, nestedSession::beginTransaction);
  }

  private int getNestCount(NestedSqlSession nestedSession) throws Exception {
    Field field = NestedSqlSession.class.getDeclaredField("nestCount");
    field.setAccessible(true);
    return (int) field.get(nestedSession);
  }
}
