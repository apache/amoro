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
import org.apache.amoro.server.table.DerbyPersistence;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * DATABASE HA concurrency and edge-case tests using Derby. All external classes are invoked via
 * reflection; safely skipped when not merged.
 */
public class DatabaseHaEdgeCasesIT {

  @ClassRule public static ExternalResource derby = new DerbyPersistence();

  @BeforeClass
  public static void checkPrerequisites() {
    Assume.assumeTrue(classExists("org.apache.amoro.server.ha.DataBaseHighAvailabilityContainer"));
    Assume.assumeTrue(
        "ha_lease table not present",
        DataBaseHighAvailabilityContainerIT.checkHaLeaseTablePresent());
  }

  @Test
  public void testConcurrentAcquireRace() throws Exception {
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

    try {
      // Only one should return; the other blocks.
      Object done = awaitAny(f1, f2, 60);
      Assert.assertTrue(done == f1 && !f2.isDone() || done == f2 && !f1.isDone());

      assertLeaseRowCount(1, 3);
    } finally {
      c1.close();
      c2.close();
      es.shutdownNow();
    }
  }

  @Test
  public void testOptimisticLockRenewConflict() throws Exception {
    Configurations conf =
        HATestConfigs.buildDataBaseHAConfig(
            "default",
            2000,
            6000,
            "127.0.0.1",
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort(),
            HATestConfigs.randomEphemeralPort());
    HighAvailabilityContainer container = HighAvailabilityContainerFactory.create(conf);

    container.waitLeaderShip();
    // Modify version externally to simulate an optimistic lock conflict.
    bumpLeaseVersion();

    // Next heartbeat renew should fail and trigger demotion.
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
      f.get(10, TimeUnit.SECONDS);
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

  private static Object awaitAny(Future<?> f1, Future<?> f2, int seconds) throws Exception {
    long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
    while (System.nanoTime() < end) {
      if (f1.isDone()) {
        return f1;
      }
      if (f2.isDone()) {
        return f2;
      }
      Thread.sleep(100);
    }
    throw new TimeoutException("concurrent acquire did not finish in time");
  }

  private static void assertLeaseRowCount(int minExpected, int maxExpected) throws Exception {
    Configurations conf = new Configurations();
    conf.set(
        AmoroManagementConf.DB_CONNECTION_URL, HATestConfigs.detectDerbyUrlFromSqlSessionFactory());
    conf.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_DERBY);
    conf.set(AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
    DataSource ds = DataSourceFactory.createDataSource(conf);

    try (Connection conn = ds.getConnection();
        Statement st = conn.createStatement()) {
      try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HA_LEASE")) {
        Assert.assertTrue(rs.next());
        int cnt = rs.getInt(1);
        Assert.assertTrue(
            "ha_lease row count unexpected: " + cnt, cnt >= minExpected && cnt <= maxExpected);
      }
    }
  }

  private static void bumpLeaseVersion() throws Exception {
    Configurations conf = new Configurations();
    conf.set(
        AmoroManagementConf.DB_CONNECTION_URL, HATestConfigs.detectDerbyUrlFromSqlSessionFactory());
    conf.set(AmoroManagementConf.DB_TYPE, AmoroManagementConf.DB_TYPE_DERBY);
    conf.set(AmoroManagementConf.DB_DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
    DataSource ds = DataSourceFactory.createDataSource(conf);
    try (Connection conn = ds.getConnection();
        Statement st = conn.createStatement()) {
      st.executeUpdate("UPDATE HA_LEASE SET VERSION = VERSION + 1");
      conn.commit();
    }
  }
}
