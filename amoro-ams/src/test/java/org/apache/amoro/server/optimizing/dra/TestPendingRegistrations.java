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

package org.apache.amoro.server.optimizing.dra;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PendingRegistrations}: requested-but-not-yet-registered optimizer capacity.
 * Registration is optimizer-driven (the pod self-registers after boot), so a request that never
 * registers — image pull failure, exhausted ResourceQuota, crash loop — must not count as capacity
 * forever: it would freeze scale-up below the real demand. Heartbeat expiry cannot cover this
 * window because it only starts after registration.
 */
public class TestPendingRegistrations {

  private static final long TIMEOUT_MS = 180_000L; // boot timeout, must exceed pod boot time
  private static final long T0 = 0L;

  @Test
  void requestedThreadsArePending() {
    PendingRegistrations pending = new PendingRegistrations(TIMEOUT_MS);
    pending.requested("r1", 4, T0);
    pending.requested("r2", 4, T0);
    Assertions.assertEquals(8, pending.pendingThreads(T0 + 1_000));
  }

  @Test
  void registrationRemovesThePendingEntry() {
    PendingRegistrations pending = new PendingRegistrations(TIMEOUT_MS);
    pending.requested("r1", 4, T0);
    pending.requested("r2", 4, T0);
    pending.registered("r1");
    Assertions.assertEquals(4, pending.pendingThreads(T0 + 1_000));
  }

  @Test
  void requestFailureRemovesThePendingEntryImmediately() {
    PendingRegistrations pending = new PendingRegistrations(TIMEOUT_MS);
    pending.requested("r1", 4, T0);
    pending.failed("r1");
    Assertions.assertEquals(0, pending.pendingThreads(T0 + 1_000));
  }

  @Test
  void entriesExpireAfterTheBootTimeout() {
    PendingRegistrations pending = new PendingRegistrations(TIMEOUT_MS);
    pending.requested("r1", 4, T0);
    // Within the timeout the phantom capacity is accepted: evicting a legitimately booting pod
    // would cause duplicate scale-ups, which is worse than a few conservative rounds.
    Assertions.assertEquals(4, pending.pendingThreads(T0 + TIMEOUT_MS - 1));
    Assertions.assertEquals(0, pending.pendingThreads(T0 + TIMEOUT_MS));
  }

  @Test
  void expiryIsPerEntry() {
    PendingRegistrations pending = new PendingRegistrations(TIMEOUT_MS);
    pending.requested("r1", 4, T0);
    pending.requested("r2", 2, T0 + 60_000);
    Assertions.assertEquals(2, pending.pendingThreads(T0 + TIMEOUT_MS));
  }

  @Test
  void unknownResourceIdIsIgnored() {
    PendingRegistrations pending = new PendingRegistrations(TIMEOUT_MS);
    pending.requested("r1", 4, T0);
    pending.registered("unknown");
    pending.failed("also-unknown");
    Assertions.assertEquals(4, pending.pendingThreads(T0 + 1_000));
  }
}
