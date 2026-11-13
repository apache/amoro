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

package org.apache.amoro.server.table;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.table.TableRuntimeFactory;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

class TestTableRuntimeRegistration extends PersistentBase {

  @Test
  void testCreationCallbackAfterDiscoveryRegisteredRuntime() throws Exception {
    RegistrationFixture fixture = new RegistrationFixture(-4424001L);
    ServerTableIdentifier identifier = fixture.identifier;
    InternalCatalog catalog = fixture.catalog;
    DefaultTableService service = fixture.service;
    CountDownLatch creationReady = new CountDownLatch(1);
    CountDownLatch resumeCreation = new CountDownLatch(1);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      // Model the gap after catalog metadata is committed and before the creation callback.
      // Discovery and this callback both reach triggerTableAdded; use the public callback to
      // exercise that shared registration path without starting a background scheduler.
      Future<?> firstRegistration =
          executor.submit(
              () -> {
                creationReady.countDown();
                assertTrue(resumeCreation.await(10, TimeUnit.SECONDS));
                service.onTableCreated(catalog, identifier);
                return null;
              });
      assertTrue(creationReady.await(10, TimeUnit.SECONDS));
      service.onTableCreated(catalog, identifier);
      assertNotNull(getAs(TableRuntimeMapper.class, m -> m.selectRuntime(identifier.getId())));
      resumeCreation.countDown();

      assertDoesNotThrow(() -> firstRegistration.get(10, TimeUnit.SECONDS));
      assertRegisteredOnce(fixture);
    } finally {
      resumeCreation.countDown();
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
      service.dispose();
      doAs(TableRuntimeMapper.class, m -> m.deleteRuntime(identifier.getId()));
    }
  }

  @Test
  void testRegistrationWhileRuntimeIsNotYetPublished() throws Exception {
    RegistrationFixture fixture = new RegistrationFixture(-4424002L);
    CountDownLatch runtimePersisted = new CountDownLatch(1);
    CountDownLatch publishRuntime = new CountDownLatch(1);
    when(fixture.creator.create(any()))
        .thenAnswer(
            invocation -> {
              // insertRuntime has committed, but tableRuntimeMap.put has not happened yet.
              runtimePersisted.countDown();
              assertTrue(publishRuntime.await(30, TimeUnit.SECONDS));
              return fixture.runtime;
            });

    FutureTask<Void> first = registrationTask(fixture);
    FutureTask<Void> second = registrationTask(fixture);
    Thread firstThread = new Thread(first, "first-runtime-registration");
    Thread secondThread = new Thread(second, "second-runtime-registration");
    try {
      firstThread.start();
      assertTrue(runtimePersisted.await(10, TimeUnit.SECONDS));
      assertNotNull(
          getAs(TableRuntimeMapper.class, m -> m.selectRuntime(fixture.identifier.getId())));
      secondThread.start();

      // A correct lock may keep the second caller outside the registration code. Wait for
      // either its result or a lock owned by the first caller, not for both to reach INSERT.
      awaitCompletionOrLockWait(second, secondThread, firstThread);
      publishRuntime.countDown();
      assertDoesNotThrow(() -> first.get(10, TimeUnit.SECONDS));
      assertDoesNotThrow(() -> second.get(10, TimeUnit.SECONDS));
      assertRegisteredOnce(fixture);
    } finally {
      publishRuntime.countDown();
      firstThread.interrupt();
      secondThread.interrupt();
      firstThread.join(10000);
      secondThread.join(10000);
      assertTrue(!firstThread.isAlive() && !secondThread.isAlive());
      fixture.service.dispose();
      doAs(TableRuntimeMapper.class, m -> m.deleteRuntime(fixture.identifier.getId()));
    }
  }

  private static FutureTask<Void> registrationTask(RegistrationFixture fixture) {
    return new FutureTask<>(
        () -> {
          fixture.service.onTableCreated(fixture.catalog, fixture.identifier);
          return null;
        });
  }

  private static void awaitCompletionOrLockWait(
      Future<?> registration, Thread caller, Thread owner) {
    ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (System.nanoTime() < deadline) {
      if (registration.isDone()) {
        return;
      }
      ThreadInfo info = threads.getThreadInfo(caller.getId());
      if (info != null && info.getLockOwnerId() == owner.getId()) {
        return;
      }
      // Poll observable progress; elapsed time alone must not release the first registration.
      LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
    }
    throw new AssertionError("Second registration neither completed nor waited for the first");
  }

  private void assertRegisteredOnce(RegistrationFixture fixture) {
    long runtimeCount =
        getAs(
            TableRuntimeMapper.class,
            m ->
                m.selectAllRuntimes().stream()
                    .filter(meta -> fixture.identifier.getId().equals(meta.getTableId()))
                    .count());
    assertEquals(1L, runtimeCount);
    verify(fixture.creator, times(1)).create(any());
    verify(fixture.runtime, times(1)).registerMetric(any());
    verify(fixture.handler, times(1)).handleTableAdded(fixture.table, fixture.runtime);
  }

  private static class RegistrationFixture {
    private final ServerTableIdentifier identifier;
    private final InternalCatalog catalog = mock(InternalCatalog.class);
    private final AmoroTable<?> table = mock(AmoroTable.class);
    private final TableRuntimeFactory.TableRuntimeCreator creator =
        mock(TableRuntimeFactory.TableRuntimeCreator.class);
    private final TableRuntime runtime = mock(TableRuntime.class);
    private final RuntimeHandlerChain handler = mock(RuntimeHandlerChain.class, CALLS_REAL_METHODS);
    private final DefaultTableService service;

    private RegistrationFixture(long tableId) {
      // Reuse suite persistence, initializing Derby only for standalone execution.
      try {
        SqlSessionFactoryProvider.getInstance().get();
      } catch (IllegalStateException e) {
        new DerbyPersistence();
      }
      identifier =
          ServerTableIdentifier.of(tableId, "registration_race", "db", "tbl", TableFormat.ICEBERG);
      when(table.format()).thenReturn(TableFormat.ICEBERG);
      when(table.properties()).thenReturn(Collections.emptyMap());
      doReturn(table).when(catalog).loadTable("db", "tbl");
      TableRuntimeFactory factory = mock(TableRuntimeFactory.class);
      when(runtime.getFormat()).thenReturn(TableFormat.ICEBERG);
      when(factory.accept(any(), any())).thenReturn(Optional.of(creator));
      when(creator.requiredStateKeys()).thenReturn(Collections.emptyList());
      when(creator.create(any())).thenReturn(runtime);
      service = new DefaultTableService(new Configurations(), mock(CatalogManager.class), factory);
      handler.initialize(Collections.emptyList());
      // Do not start the scheduler: the test controls both registration callers explicitly.
      service.addHandlerChain(handler);
    }
  }
}
