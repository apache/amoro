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

package org.apache.amoro.server;

import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.ResourceMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestOptimizerResourceGroupConcurrency extends AMSServiceTestBase {

  private static final long BLOCKED_ASSERTION_TIMEOUT_MS = 500;
  private static final long COMPLETION_TIMEOUT_SECONDS = 5;

  @Test
  public void testDeletingMissingGroupRemainsIdempotent() {
    String groupName = "missing-" + UUID.randomUUID();

    Assertions.assertDoesNotThrow(() -> optimizerManager().deleteResourceGroup(groupName));
    Assertions.assertDoesNotThrow(() -> optimizerManager().deleteResourceGroup(groupName));
    Assertions.assertNull(optimizerManager().getResourceGroup(groupName));
  }

  @Test(timeout = 30_000)
  public void testDeleteFirstMakesConcurrentRegistrationFail() throws Exception {
    String groupName = "d-first-" + UUID.randomUUID();
    createGroup(groupName);
    OptimizerRegisterInfo registerInfo = buildRegisterInfo(groupName);
    CountDownLatch registrationStarted = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<String> registration = null;

    try {
      try (SqlSession deleteSession = openSession(false)) {
        ResourceMapper deleteMapper = deleteSession.getMapper(ResourceMapper.class);
        Assertions.assertEquals(
            groupName, deleteMapper.selectResourceGroupNameForUpdate(groupName));

        registration =
            executor.submit(
                () -> {
                  registrationStarted.countDown();
                  return optimizingService().authenticate(registerInfo);
                });
        Assertions.assertTrue(
            registrationStarted.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        Future<String> blockedRegistration = registration;
        Assertions.assertThrows(
            TimeoutException.class,
            () -> blockedRegistration.get(BLOCKED_ASSERTION_TIMEOUT_MS, TimeUnit.MILLISECONDS));

        deleteMapper.deleteResourceGroup(groupName);
        deleteSession.commit();
      }

      Future<String> completedRegistration = registration;
      ExecutionException failure =
          Assertions.assertThrows(
              ExecutionException.class,
              () -> completedRegistration.get(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
      Assertions.assertEquals(ObjectNotExistsException.class, failure.getCause().getClass());
      Assertions.assertNull(optimizerManager().getResourceGroup(groupName));
      Assertions.assertTrue(optimizerManager().listOptimizers(groupName).isEmpty());
    } finally {
      if (registration != null) {
        registration.cancel(true);
      }
      try {
        shutdown(executor);
      } finally {
        cleanGroup(groupName);
      }
    }
  }

  @Test(timeout = 30_000)
  public void testRegistrationFirstMakesConcurrentDeletionFail() throws Exception {
    String groupName = "r-first-" + UUID.randomUUID();
    createGroup(groupName);
    OptimizerInstance optimizer = new OptimizerInstance(buildRegisterInfo(groupName), "local");
    CountDownLatch deletionStarted = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<?> deletion = null;

    try {
      try (SqlSession registrationSession = openSession(false)) {
        ResourceMapper resourceMapper = registrationSession.getMapper(ResourceMapper.class);
        Assertions.assertEquals(
            groupName, resourceMapper.selectResourceGroupNameForUpdate(groupName));
        registrationSession.getMapper(OptimizerMapper.class).insertOptimizer(optimizer);

        deletion =
            executor.submit(
                () -> {
                  deletionStarted.countDown();
                  optimizerManager().deleteResourceGroup(groupName);
                });
        Assertions.assertTrue(deletionStarted.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        Future<?> blockedDeletion = deletion;
        Assertions.assertThrows(
            TimeoutException.class,
            () -> blockedDeletion.get(BLOCKED_ASSERTION_TIMEOUT_MS, TimeUnit.MILLISECONDS));

        registrationSession.commit();
      }

      Future<?> completedDeletion = deletion;
      ExecutionException failure =
          Assertions.assertThrows(
              ExecutionException.class,
              () -> completedDeletion.get(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
      Assertions.assertEquals(RuntimeException.class, failure.getCause().getClass());
      Assertions.assertTrue(failure.getCause().getMessage().contains("currently in use"));
      Assertions.assertNotNull(optimizerManager().getResourceGroup(groupName));
      Assertions.assertTrue(
          optimizerManager().listOptimizers(groupName).stream()
              .anyMatch(record -> optimizer.getToken().equals(record.getToken())));
    } finally {
      if (deletion != null) {
        deletion.cancel(true);
      }
      try {
        shutdown(executor);
      } finally {
        cleanGroup(groupName);
      }
    }
  }

  private void createGroup(String groupName) {
    ResourceGroup resourceGroup = new ResourceGroup.Builder(groupName, "local").build();
    optimizerManager().createResourceGroup(resourceGroup);
    optimizingService().createResourceGroup(resourceGroup);
  }

  private OptimizerRegisterInfo buildRegisterInfo(String groupName) {
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    registerInfo.setProperties(new HashMap<>());
    registerInfo.setThreadCount(1);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(groupName);
    registerInfo.setResourceId("resource-" + UUID.randomUUID());
    registerInfo.setStartTime(System.currentTimeMillis());
    return registerInfo;
  }

  private SqlSession openSession(boolean autoCommit) {
    return SqlSessionFactoryProvider.getInstance().get().openSession(autoCommit);
  }

  private void cleanGroup(String groupName) {
    try {
      optimizingService().deleteResourceGroup(groupName);
    } catch (RuntimeException ignored) {
      // The queue may already have been removed by the group watcher.
    }
    try (SqlSession session = openSession(true)) {
      OptimizerMapper optimizerMapper = session.getMapper(OptimizerMapper.class);
      optimizerMapper.selectAll().stream()
          .filter(optimizer -> groupName.equals(optimizer.getGroupName()))
          .map(OptimizerInstance::getToken)
          .forEach(optimizerMapper::deleteOptimizer);
      session.getMapper(ResourceMapper.class).deleteResourceGroup(groupName);
    }
  }

  private void shutdown(ExecutorService executor) throws InterruptedException {
    executor.shutdownNow();
    Assertions.assertTrue(executor.awaitTermination(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
  }
}
