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

import com.netease.arctic.ams.api.metrics.MetricType;
import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMetricsManager {

  @Mock
  private MetricsEmitterTest emitter1;
  @Mock
  private MetricsEmitterTest emitter2;

  private MetricsManager manager;

  @BeforeEach
  public void build() {
    Map<String, String> expectedProperties = Collections.singletonMap(
        "impl", "com.netease.arctic.server.manager.TestMetricsManager$MetricsEmitterTest"
    );
    manager = new MetricsManager("config/path") {
      protected Map<String, String> loadProperties(String pluginName) {
        return expectedProperties;
      }
    };
    manager.install("emitter1");
    manager.install("emitter2");
    emitter1 = (MetricsEmitterTest) manager.get("emitter1");
    emitter2 = (MetricsEmitterTest) manager.get("emitter2");
    emitter1.setAccept(true);
  }

  @AfterEach
  public void tearDown() {
    manager.close();
  }

  @Test
  public void testEmit() {
    MetricsContent<?> metrics = new MetricsContent<String>() {

      @Override
      public String name() {
        return null;
      }

      @Override
      public MetricType type() {
        return null;
      }

      @Override
      public String data() {
        return null;
      }
    };

    manager.emit(metrics);
    assertTrue(emitter1.isEmitted());
    assertFalse(emitter2.isEmitted());
  }

  public static class MetricsEmitterTest implements MetricsEmitter {

    private boolean emitted = false;
    private boolean accept = false;

    @Override
    public String name() {
      return "emitter1";
    }

    @Override
    public void open(Map<String, String> properties) {
    }

    @Override
    public void emit(MetricsContent<?> metrics) {
      emitted = true;
    }

    public void setAccept(boolean accept) {
      this.accept = accept;
    }

    public boolean isEmitted() {
      return emitted;
    }

    @Override
    public boolean accept(MetricsContent<?> metrics) {
      return accept;
    }

    @Override
    public void close() {
    }
  }
}
