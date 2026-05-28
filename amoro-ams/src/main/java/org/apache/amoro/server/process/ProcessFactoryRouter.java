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

package org.apache.amoro.server.process;

import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Format → {@link ProcessFactory} lookup built from the SPI-discovered factory list.
 *
 * <p>Responsibility is intentionally narrow: find a factory for a format. Lifecycle, trigger,
 * recover, and other {@link ProcessFactory} concerns stay on the delegate instances; the router
 * never implements {@code ProcessFactory} itself.
 */
public final class ProcessFactoryRouter {

  private final List<ProcessFactory> delegates;
  private final Map<TableFormat, ProcessFactory> byFormat;

  public ProcessFactoryRouter(List<ProcessFactory> factories) {
    this.delegates = factories;
    this.byFormat = Collections.unmodifiableMap(buildRoutingTable(this.delegates));
  }

  public ProcessFactory forFormat(TableFormat format) {
    ProcessFactory factory = byFormat.get(format);
    if (factory == null) {
      throw new UnsupportedOperationException(
          "No ProcessFactory registered for table format " + format);
    }
    return factory;
  }

  public Set<TableFormat> supportedFormats() {
    return byFormat.keySet();
  }

  public List<ProcessFactory> delegates() {
    return delegates;
  }

  private static Map<TableFormat, ProcessFactory> buildRoutingTable(
      List<ProcessFactory> factories) {
    Map<TableFormat, ProcessFactory> table = new LinkedHashMap<>();
    for (ProcessFactory factory : factories) {
      for (TableFormat format : factory.supportedFormats()) {
        ProcessFactory existing = table.get(format);
        if (existing != null && existing != factory) {
          throw new IllegalArgumentException(
              String.format(
                  "ProcessFactory conflict for format %s: '%s' and '%s' both claim it",
                  format, existing.name(), factory.name()));
        }
        table.put(format, factory);
      }
    }
    return table;
  }
}
