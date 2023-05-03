package com.netease.arctic.ams.server.persistence;

import com.netease.arctic.ams.server.exception.ArcticRuntimeException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PersistentBaseTest {
  private static final class TestMapper {
    int doSomething() {
      return 1;
    }
  }

  private static final class TestException extends RuntimeException {
  }

  private static final class TestRuntimeException extends ArcticRuntimeException {
  }

  private static final class TestSupplier implements Supplier<TestRuntimeException> {
    @Override
    public TestRuntimeException get() {
      return new TestRuntimeException();
    }
  }

  private static final class TestPredicate implements Predicate<Integer> {
    @Override
    public boolean test(Integer integer) {
      return integer > 0;
    }
  }

  private static final class TestConsumer implements Consumer<TestMapper> {
    @Override
    public void accept(TestMapper testMapper) {
      testMapper.doSomething();
    }
  }

  private final SqlSession sqlSession = Mockito.mock(SqlSession.class);
  private final PersistentBase persistentBase = Mockito.spy(PersistentBase.class);

  @Test
  void testDoAs() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());

    persistentBase.doAs(TestMapper.class, new TestConsumer());

    verify(sqlSession).commit();
    verify(sqlSession, never()).rollback();
  }

  @Test
  void testDoAsWithException() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doThrow(new TestException()).when(testMapper).doSomething();

    try {
      persistentBase.doAs(TestMapper.class, new TestConsumer());
    } catch (Exception e) {
      // ignore
    }

    verify(sqlSession, never()).commit();
    verify(sqlSession).rollback();
  }

  @Test
  void testDoAsTransaction() {
    Runnable runnable1 = Mockito.mock(Runnable.class);
    Runnable runnable2 = Mockito.mock(Runnable.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();

    persistentBase.doAsTransaction(runnable1, runnable2);

    verify(runnable1).run();
    verify(runnable2).run();
    verify(sqlSession).commit();
    verify(sqlSession, never()).rollback();
  }

  @Test
  void testDoAsTransactionWithException() {
    Runnable runnable1 = Mockito.mock(Runnable.class);
    Runnable runnable2 = Mockito.mock(Runnable.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doThrow(new TestException()).when(runnable1).run();

    try {
      persistentBase.doAsTransaction(runnable1, runnable2);
    } catch (Exception e) {
      // ignore
    }

    verify(runnable1).run();
    verify(runnable2, never()).run();
    verify(sqlSession, never()).commit();
    verify(sqlSession).rollback();
  }

  @Test
  void testDoAsExisted() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doReturn(1).when(testMapper).doSomething();

    persistentBase.doAsExisted(TestMapper.class, TestMapper::doSomething, new TestSupplier());

    verify(sqlSession).commit();
    verify(sqlSession, never()).rollback();
  }

  @Test
  void testDoAsExistedWithException() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doReturn(0).when(testMapper).doSomething();

    try {
      persistentBase.doAsExisted(TestMapper.class, TestMapper::doSomething, new TestSupplier());
    } catch (Exception e) {
      // ignore
    }

    verify(sqlSession, never()).commit();
    verify(sqlSession).rollback();
  }

  @Test
  void testGetAs() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doReturn(1).when(testMapper).doSomething();

    int result = persistentBase.getAs(TestMapper.class, TestMapper::doSomething);

    assert result == 1;
    verify(sqlSession).commit();
    verify(sqlSession, never()).rollback();
  }

  @Test
  void testGetAsWithException() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doThrow(new TestException()).when(testMapper).doSomething();

    try {
      persistentBase.getAs(TestMapper.class, TestMapper::doSomething);
    } catch (Exception e) {
      // ignore
    }

    verify(sqlSession, never()).commit();
    verify(sqlSession).rollback();
  }

  @Test
  void testGetAsWithPredicate() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doReturn(1).when(testMapper).doSomething();

    int result = persistentBase.getAs(TestMapper.class, TestMapper::doSomething, new TestPredicate(), new TestSupplier());

    assert result == 1;
    verify(sqlSession).commit();
    verify(sqlSession, never()).rollback();
  }

  @Test
  void testGetAsWithPredicateAndException() {
    TestMapper testMapper = Mockito.mock(TestMapper.class);

    Mockito.doReturn(sqlSession).when(persistentBase).beginSession();
    Mockito.doReturn(testMapper).when(persistentBase).getMapper(any(), any());
    Mockito.doReturn(0).when(testMapper).doSomething();

    try {
      persistentBase.getAs(TestMapper.class, TestMapper::doSomething, new TestPredicate(), new TestSupplier());
    } catch (Exception e) {
      // ignore
    }

    verify(sqlSession, never()).commit();
    verify(sqlSession).rollback();
  }
}
