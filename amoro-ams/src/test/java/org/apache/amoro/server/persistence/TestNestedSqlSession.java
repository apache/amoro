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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class TestNestedSqlSession {

  @Mock private SqlSession sqlSession;
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
    assertEquals(0, getNestCount(session));
    verifyNoMoreInteractions(supplier);
  }

  @Test
  void testClose() throws Exception {
    assertSame(nestedSession, nestedSession.openNestedSession());
    nestedSession.openNestedSession();
    nestedSession.openNestedSession();
    nestedSession.openNestedSession();
    nestedSession.openNestedSession();

    nestedSession.close();
    assertEquals(4, getNestCount(nestedSession));
    nestedSession.close();
    assertEquals(3, getNestCount(nestedSession));
    nestedSession.close();
    assertEquals(2, getNestCount(nestedSession));
    nestedSession.close();
    assertEquals(1, getNestCount(nestedSession));
    nestedSession.close();
    assertEquals(0, getNestCount(nestedSession));
    nestedSession.close();
    assertEquals(-1, getNestCount(nestedSession));
    verify(sqlSession).close();
    assertNull(nestedSession.getSqlSession());
  }

  @Test
  void testCommitWithNestCountGreaterThanZero() {
    nestedSession.openNestedSession();
    nestedSession.commit();
    verifyNoInteractions(sqlSession);
    nestedSession.close();

    nestedSession.commit();
    verify(sqlSession).commit(true);
  }

  @Test
  void testCommitWithNestCountEqualToZero() {
    nestedSession.commit();
    verify(sqlSession).commit(true);
    verifyNoMoreInteractions(sqlSession);
  }

  @Test
  void testCloseAndCommit() {
    nestedSession.openNestedSession();
    nestedSession.openNestedSession();
    nestedSession.commit();
    nestedSession.close();
    verifyNoInteractions(sqlSession);

    nestedSession.commit();
    verifyNoInteractions(sqlSession);
    nestedSession.close();
    verifyNoInteractions(sqlSession);

    nestedSession.commit();
    nestedSession.close();
    verify(sqlSession).commit(true);
    verify(sqlSession).close();
    assertNull(nestedSession.getSqlSession());
  }

  @Test
  void testRollbackWithNestCountGreaterThanZero() {
    nestedSession.openNestedSession();
    nestedSession.rollback();
    verifyNoInteractions(sqlSession);
    nestedSession.close();

    nestedSession.rollback();
    verify(sqlSession).rollback(true);
  }

  @Test
  void testRollbackWithNestCountEqualToZero() {
    nestedSession.rollback();
    verify(sqlSession).rollback(true);
    verifyNoMoreInteractions(sqlSession);
  }

  @Test
  void testCloseAndRollback() {
    nestedSession.openNestedSession();
    nestedSession.openNestedSession();
    nestedSession.commit();
    nestedSession.close();
    verifyNoInteractions(sqlSession);

    nestedSession.rollback();
    verifyNoInteractions(sqlSession);
    nestedSession.close();
    verifyNoInteractions(sqlSession);

    nestedSession.rollback();
    verify(sqlSession).rollback(true);
    nestedSession.close();
    verify(sqlSession).close();
    assertNull(nestedSession.getSqlSession());
  }

  @Test
  void testBeginTransaction() throws Exception {
    assertSame(nestedSession, nestedSession.openNestedSession());
    assertEquals(1, getNestCount(nestedSession));

    nestedSession.openNestedSession();
    assertEquals(2, getNestCount(nestedSession));

    nestedSession.openNestedSession();
    assertEquals(3, getNestCount(nestedSession));

    nestedSession.openNestedSession();
    assertEquals(4, getNestCount(nestedSession));

    nestedSession.openNestedSession();
    assertThrows(IllegalStateException.class, nestedSession::openNestedSession);
  }

  private int getNestCount(NestedSqlSession nestedSession) throws Exception {
    Field field = NestedSqlSession.class.getDeclaredField("nestCount");
    field.setAccessible(true);
    return (int) field.get(nestedSession);
  }
}
