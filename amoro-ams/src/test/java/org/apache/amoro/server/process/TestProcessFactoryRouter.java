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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestProcessFactoryRouter {

  /**
   * Disposable format used to represent "a format no factory claims". Registered once at class
   * load; TableFormat's global registry dedupes so re-running tests is safe.
   */
  private static final TableFormat UNREGISTERED_FORMAT =
      TableFormat.register("TEST_ROUTER_UNREGISTERED");

  private ProcessFactory mockFactory(String name, Set<TableFormat> formats) {
    ProcessFactory factory = mock(ProcessFactory.class);
    when(factory.name()).thenReturn(name);
    when(factory.supportedFormats()).thenReturn(formats);
    return factory;
  }

  @Test
  public void forFormat_hit() {
    ProcessFactory iceberg =
        mockFactory("iceberg", Set.of(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG));
    ProcessFactory paimon = mockFactory("paimon", Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(iceberg, paimon));

    assertSame(iceberg, router.forFormat(TableFormat.ICEBERG));
    assertSame(iceberg, router.forFormat(TableFormat.MIXED_ICEBERG));
    assertSame(paimon, router.forFormat(TableFormat.PAIMON));
  }

  @Test
  public void forFormat_miss() {
    ProcessFactory iceberg = mockFactory("iceberg", Set.of(TableFormat.ICEBERG));
    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(iceberg));

    UnsupportedOperationException ex =
        assertThrows(
            UnsupportedOperationException.class, () -> router.forFormat(UNREGISTERED_FORMAT));
    assertTrue(ex.getMessage().contains(UNREGISTERED_FORMAT.name()));
  }

  @Test
  public void conflict_detection() {
    ProcessFactory factoryA =
        mockFactory("a", new HashSet<>(Set.of(TableFormat.ICEBERG, TableFormat.PAIMON)));
    ProcessFactory factoryB = mockFactory("b", Set.of(TableFormat.PAIMON));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProcessFactoryRouter(List.of(factoryA, factoryB)));
    assertTrue(ex.getMessage().contains("PAIMON"));
    assertTrue(ex.getMessage().contains("'a'"));
    assertTrue(ex.getMessage().contains("'b'"));
  }

  @Test
  public void empty_factories() {
    ProcessFactoryRouter router = new ProcessFactoryRouter(Collections.emptyList());

    assertTrue(router.supportedFormats().isEmpty());
    assertTrue(router.delegates().isEmpty());
    assertThrows(UnsupportedOperationException.class, () -> router.forFormat(TableFormat.ICEBERG));
  }

  @Test
  public void supportedFormats_union() {
    ProcessFactory iceberg =
        mockFactory(
            "iceberg",
            Set.of(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE));
    ProcessFactory paimon = mockFactory("paimon", Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(iceberg, paimon));

    Set<TableFormat> formats = router.supportedFormats();
    assertEquals(4, formats.size());
    assertTrue(formats.contains(TableFormat.ICEBERG));
    assertTrue(formats.contains(TableFormat.MIXED_ICEBERG));
    assertTrue(formats.contains(TableFormat.MIXED_HIVE));
    assertTrue(formats.contains(TableFormat.PAIMON));

    assertThrows(UnsupportedOperationException.class, () -> formats.add(UNREGISTERED_FORMAT));
  }

  @Test
  public void delegates_exposure() {
    ProcessFactory a = mockFactory("a", Set.of(TableFormat.ICEBERG));
    ProcessFactory b = mockFactory("b", Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(a, b));

    List<ProcessFactory> delegates = router.delegates();
    assertEquals(2, delegates.size());
    assertSame(a, delegates.get(0));
    assertSame(b, delegates.get(1));
    assertThrows(UnsupportedOperationException.class, () -> delegates.add(a));
  }
}
