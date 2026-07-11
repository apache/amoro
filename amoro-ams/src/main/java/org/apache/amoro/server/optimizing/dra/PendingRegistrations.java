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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Requested-but-not-yet-registered optimizer capacity of one resource group. Registration is
 * optimizer-driven — the pod self-registers after boot — so scale-up must count capacity it has
 * already requested to avoid duplicate scale-outs during the boot window, but a request that never
 * registers (image pull failure, exhausted ResourceQuota, crash loop) must not count forever:
 * heartbeat expiry only starts after registration, so entries carry their own boot deadline.
 * Thread-safe: the scale keeper and the registration RPC touch this concurrently.
 */
public class PendingRegistrations {

  private final long timeoutMs;
  private final Map<String, Entry> entries = new ConcurrentHashMap<>();

  private static class Entry {
    private final int threads;
    private final long deadlineMs;

    private Entry(int threads, long deadlineMs) {
      this.threads = threads;
      this.deadlineMs = deadlineMs;
    }
  }

  public PendingRegistrations(long timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  /** Record a resource request whose optimizer has not registered yet. */
  public void requested(String resourceId, int threads, long nowMs) {
    entries.put(resourceId, new Entry(threads, nowMs + timeoutMs));
  }

  /** The optimizer of this resource registered; its capacity is now real. */
  public void registered(String resourceId) {
    entries.remove(resourceId);
  }

  /** The resource request failed synchronously; drop it immediately. */
  public void failed(String resourceId) {
    entries.remove(resourceId);
  }

  /** Total threads still expected to register, pruning entries past their boot deadline. */
  public int pendingThreads(long nowMs) {
    entries.values().removeIf(entry -> nowMs >= entry.deadlineMs);
    return entries.values().stream().mapToInt(entry -> entry.threads).sum();
  }
}
