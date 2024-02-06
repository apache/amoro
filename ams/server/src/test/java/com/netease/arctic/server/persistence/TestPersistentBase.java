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

package com.netease.arctic.server.persistence;

import static org.mockito.Mockito.never;

import com.netease.arctic.server.exception.UndefinedException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestPersistentBase {

  private final TestMapper mapper = Mockito.mock(TestMapper.class);
  private final NestedSqlSession session = Mockito.mock(NestedSqlSession.class);
  private final SqlSession sqlSession = Mockito.mock(SqlSession.class);
  private final PersistentBase testObject = Mockito.spy(new PersistentBase() {});

  @BeforeEach
  void setUp() {
    Mockito.when(session.getSqlSession()).thenReturn(sqlSession);
    Mockito.when(session.getSqlSession().getMapper(TestMapper.class)).thenReturn(mapper);
    Mockito.doReturn(session).when(testObject).beginSession();
    Mockito.when(mapper.testMethod()).thenReturn("result");
  }

  @AfterEach
  void tearDown() {
    session.close();
  }

  @Test
  public void testDoAs() {
    // call doAs method
    testObject.doAs(TestMapper.class, (TestMapper m) -> m.testMethod());

    // verify mapper method was called and session was committed
    Mockito.verify(mapper, Mockito.times(1)).testMethod();
    Mockito.verify(session, Mockito.times(1)).commit();
    Mockito.verify(session, Mockito.times(1)).close();
    Mockito.verify(session, never()).rollback();
  }

  @Test
  public void testDoAsTransaction() {
    // mock operations
    Runnable operation1 = Mockito.mock(Runnable.class);
    Runnable operation2 = Mockito.mock(Runnable.class);

    // call doAsTransaction method
    testObject.doAsTransaction(operation1, operation2);

    // verify operations were executed and session was committed
    Mockito.verify(operation1, Mockito.times(1)).run();
    Mockito.verify(operation2, Mockito.times(1)).run();
    Mockito.verify(session, Mockito.times(1)).commit();
    Mockito.verify(session, Mockito.times(1)).close();
    Mockito.verify(session, never()).rollback();
  }

  @Test
  public void testDoAsExisted() {
    // mock mapper class
    Mockito.when(mapper.testMethod2()).thenReturn(1);

    // call doAsExisted method
    testObject.doAsExisted(
        TestMapper.class, TestMapper::testMethod2, () -> new UndefinedException("error"));

    // verify mapper method was called, session was committed, and no exception was thrown
    Mockito.verify(mapper, Mockito.times(1)).testMethod2();
    Mockito.verify(session, Mockito.times(1)).commit();
    Mockito.verify(session, never()).rollback();
  }

  @Test
  public void testDoAsNotExisted() {
    // mock mapper class
    Mockito.when(mapper.testMethod2()).thenReturn(0);

    try {
      testObject.doAsExisted(
          TestMapper.class, TestMapper::testMethod2, () -> new UndefinedException("error"));
    } catch (UndefinedException e) {
      Mockito.verify(mapper, Mockito.times(1)).testMethod2();
      Mockito.verify(session, Mockito.times(1)).rollback();
      Mockito.verify(session, never()).commit();
      Assertions.assertEquals("error", e.getMessage());
      return;
    }
    Assertions.fail();
  }

  @Test
  public void testGetAs() {
    // call getAs method
    String result = testObject.getAs(TestMapper.class, TestMapper::testMethod);

    // verify mapper method was called, session was committed, and correct result was returned
    Mockito.verify(mapper, Mockito.times(1)).testMethod();
    Mockito.verify(session, never()).rollback();
    Mockito.verify(session, never()).commit();
    Assertions.assertEquals("result", result);
  }

  // mock mapper interface
  private interface TestMapper {
    String testMethod();

    int testMethod2();
  }
}
