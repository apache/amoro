/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.manager;

import static com.netease.arctic.ams.api.Environments.AMORO_HOME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netease.arctic.ams.api.events.Event;
import com.netease.arctic.ams.api.events.EventEmitter;
import com.netease.arctic.ams.api.events.EventType;
import com.netease.arctic.server.events.LoggingEventEmitter;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TestEventsManager {

  @Mock private EventEmitterTest emitter1;
  @Mock private EventEmitterTest emitter2;

  private EventsManager manager;

  @BeforeEach
  public void build() {
    Map<String, String> expectedProperties =
        Collections.singletonMap(
            "impl", "com.netease.arctic.server.manager.TestEventsManager$EventEmitterTest");
    manager =
        new EventsManager("config/path") {
          protected Map<String, String> loadProperties(String pluginName) {
            return expectedProperties;
          }
        };
    manager.install("emitter1");
    manager.install("emitter2");
    emitter1 = (EventEmitterTest) manager.get("emitter1");
    emitter2 = (EventEmitterTest) manager.get("emitter2");
    emitter1.setAccept(EventType.allTypes());
  }

  @AfterEach
  public void tearDown() {
    manager.close();
  }

  @Test
  public void testInitialize() {
    System.setProperty(
        AMORO_HOME,
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath());
    EventsManager testManager = new EventsManager();
    testManager.initialize();

    assertNotNull(testManager.get(LoggingEventEmitter.NAME));
  }

  @Test
  public void testEmit() {
    Event<?> event = null;

    manager.emit(event);
    assertTrue(emitter1.isEmitted());
    assertFalse(emitter2.isEmitted());
  }

  public static class EventEmitterTest implements EventEmitter {

    private boolean emitted = false;
    private Set<EventType<?>> accepts = ImmutableSet.of();

    @Override
    public String name() {
      return "emitter1";
    }

    @Override
    public void open(Map<String, String> properties) {}

    @Override
    public void emit(Event<?> metrics) {
      emitted = true;
    }

    @Override
    public Set<EventType<?>> accepts() {
      return accepts;
    }

    public void setAccept(Set<EventType<?>> accepts) {
      this.accepts = accepts;
    }

    public boolean isEmitted() {
      return emitted;
    }

    @Override
    public void close() {}
  }
}
