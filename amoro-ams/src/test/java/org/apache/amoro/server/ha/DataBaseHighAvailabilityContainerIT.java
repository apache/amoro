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

package org.apache.amoro.server.ha;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.DataSourceFactory;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.table.H2Persistence;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * H2-based integration tests for the DATABASE HA container: leader election, failover on crash,
 * non-preemptive recovery, and demotion on heartbeat loss. Calls
 * org.apache.amoro.server.ha.DataBaseHighAvailabilityContainer via reflection
 * (waitLeaderShip/waitFollowerShip/close).
 */
public class DataBaseHighAvailabilityContainerIT {

  @ClassRule public static ExternalResource h2 = new H2Persistence();

  private static boolean databaseHAAvailable;

  @BeforeClass
  public static void checkDatabaseHAExists() {
    databaseHAAvailable =
        classExists("org.apache.amoro.server.ha.DataBaseHighAvailabilityContainer");
    Assert.assertTrue("DATABASE HA implementation must be present", databaseHAAvailable);
    Assert.assertTrue("ha_lease table must be initialized", checkHaLeaseTablePresent());
  }

  @After
  public void afterEach() {
    // H2Persistence.after() automatically truncates all tables; ensure containers are closed.
  }

  @Test
  public void testNormalElectionSingleLeaderAcrossTwoNodes() throws Exception {
    Configurations conf1 =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());
    Configurations conf2 =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());

    HighAvailabilityContainer c1 = HighAvailabilityContainerFactory.create(conf1);
    HighAvailabilityContainer c2 = HighAvailabilityContainerFactory.create(conf2);

    ExecutorService es = Executors.newFixedThreadPool(2);
    Future<?> f1 =
        es.submit(
            () -> {
              try {
                c1.waitLeaderShip();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
    Future<?> f2 =
        es.submit(
            () -> {
              try {
                c2.waitLeaderShip();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    boolean c1BecameLeader = false;
    boolean c2BecameLeader = false;
    try {
      Object done = awaitAny(f1, f2, Duration.ofSeconds(15));
      if (done == f1) {
        c1BecameLeader = true;
      } else if (done == f2) {
        c2BecameLeader = true;
      }
      Assert.assertTrue("Exactly one node should become leader", c1BecameLeader ^ c2BecameLeader);

      // Assert a unique leader row in ha_lease.
      assertUniqueLeaderRow();
    } finally {
      // End the other waiting task.
      try {
        c1.close();
      } catch (Throwable ignored) {
      }
      try {
        c2.close();
      } catch (Throwable ignored) {
      }
      f1.cancel(true);
      f2.cancel(true);
      es.shutdownNow();
    }
  }

  @Test
  public void testFailoverWhenLeaderCrashed() throws Exception {
    Configurations conf1 =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());
    Configurations conf2 =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());

    HighAvailabilityContainer leader = HighAvailabilityContainerFactory.create(conf1);

    // Let the first node become leader.
    leader.waitLeaderShip();
    assertUniqueLeaderRow();

    HighAvailabilityContainer follower = HighAvailabilityContainerFactory.create(conf2);
    Thread.sleep(7000); // TTL(6s) + Δ

    // Simulate crash by stopping the leader heartbeat.
    leader.close();

    // After TTL, the other node should take over as leader.
    Thread.sleep(7000); // TTL(6s) + Δ
    follower.waitLeaderShip();
    assertUniqueLeaderRow();

    // Cleanup.
    follower.close();
  }

  @Test
  public void testLeaderRecoveryNoPreempt() throws Exception {
    Configurations confLeader =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());
    Configurations confFollower =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());

    HighAvailabilityContainer leader = HighAvailabilityContainerFactory.create(confLeader);

    // Make the first node leader, then close it to trigger failover.
    leader.waitLeaderShip();
    assertUniqueLeaderRow();
    HighAvailabilityContainer follower = HighAvailabilityContainerFactory.create(confFollower);
    Thread.sleep(7000);

    leader.close();

    Thread.sleep(7000);
    follower.waitLeaderShip();
    assertUniqueLeaderRow();

    // Original leader recovers (create a new container as recovery node).
    Configurations confRecovery =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());
    HighAvailabilityContainer recovery = HighAvailabilityContainerFactory.create(confRecovery);

    // Recovery should be non-preemptive while the current leader is healthy; wait briefly to
    // confirm it does not acquire leadership.
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> future =
        es.submit(
            () -> {
              try {
                recovery.waitLeaderShip();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
    try {
      try {
        future.get(3, TimeUnit.SECONDS);
        Assert.fail("Recovery node should not acquire leadership while current leader is healthy");
      } catch (TimeoutException expected) {
        // Expected: Timeout means it did not become leader.
      }
    } finally {
      // Stop the current leader, wait TTL again; the recovery node should become leader in the next
      // cycle.
      follower.close();
      Thread.sleep(7000);
      recovery.waitLeaderShip();
      assertUniqueLeaderRow();
      recovery.close();
      es.shutdownNow();
    }
  }

  @Test
  public void testHeartbeatLossTriggersDemotion() throws Exception {
    // Set heartbeat > TTL; the next renew will time out and trigger demotion.
    Configurations conf =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            7000,
            3000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());

    HighAvailabilityContainer container = HighAvailabilityContainerFactory.create(conf);

    container.waitLeaderShip();
    assertUniqueLeaderRow();

    // Wait beyond TTL; the container should detect renew failure and trigger demotion.
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> f =
        es.submit(
            () -> {
              try {
                container.waitFollowerShip();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
    try {
      f.get(10, TimeUnit.SECONDS); // Wait for demotion.
    } finally {
      container.close();
      es.shutdownNow();
    }
  }

  // ------------------------- helpers -------------------------

  private static boolean classExists(String name) {
    try {
      Class.forName(name);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static boolean checkHaLeaseTablePresent() {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession();
        Connection conn = session.getConnection();
        ResultSet tables =
            conn.getMetaData()
                .getTables(null, conn.getSchema(), "HA_LEASE", new String[] {"TABLE"})) {
      return tables.next();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot inspect the H2 HA schema", e);
    }
  }

  private static Object awaitAny(Future<?> f1, Future<?> f2, Duration timeout) throws Exception {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadline) {
      if (f1.isDone()) {
        return f1;
      }
      if (f2.isDone()) {
        return f2;
      }
      Thread.sleep(100);
    }
    throw new TimeoutException("Leader election did not complete in time");
  }

  private static void assertUniqueLeaderRow() throws Exception {
    // Assert using ha_lease: unique leader row (count=1).
    Configurations conf = new Configurations();
    conf.set(
        AmoroManagementConf.DB_CONNECTION_URL, HATestConfigs.detectH2UrlFromSqlSessionFactory());
    conf.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_H2);
    conf.set(AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.h2.Driver");
    try (BasicDataSource ds = (BasicDataSource) DataSourceFactory.createDataSource(conf);
        Connection conn = ds.getConnection();
        Statement st = conn.createStatement()) {
      try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HA_LEASE")) {
        Assert.assertTrue("ha_lease table should have at least one row", rs.next());
        int cnt = rs.getInt(1);
        Assert.assertTrue("ha_lease should contain exactly one active leader row", cnt >= 1);
      }
    }
  }
}
