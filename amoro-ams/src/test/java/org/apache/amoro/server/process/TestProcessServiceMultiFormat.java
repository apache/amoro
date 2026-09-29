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

import org.apache.amoro.Action;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.process.ProcessService.ExecuteEngineManager;
import org.apache.amoro.server.table.TableService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for {@link ProcessService} multi-format ActionCoordinator isolation. Ensures that
 * coordinators sharing the same action name but targeting different formats are not silently
 * overwritten (regression test for AMORO-4281).
 */
public class TestProcessServiceMultiFormat {

  private static final Action TEST_ACTION = Action.register("test-expire-snapshots");

  /** A coordinator that only supports a specific format. */
  static class FormatSpecificCoordinator implements ActionCoordinator {
    private final TableFormat format;
    private final String name;

    FormatSpecificCoordinator(TableFormat format) {
      this.format = format;
      this.name = "coordinator-" + format.name();
    }

    @Override
    public boolean formatSupported(TableFormat format) {
      return this.format.equals(format);
    }

    @Override
    public int parallelism() {
      return 1;
    }

    @Override
    public Action action() {
      return TEST_ACTION;
    }

    @Override
    public long getNextExecutingTime(TableRuntime tableRuntime) {
      return 1000L;
    }

    @Override
    public boolean enabled(TableRuntime tableRuntime) {
      return formatSupported(tableRuntime.getFormat());
    }

    @Override
    public long getExecutorDelay() {
      return 1000L;
    }

    @Override
    public Optional<TableProcess> trigger(TableRuntime tableRuntime) {
      return Optional.empty();
    }

    @Override
    public TableProcess recoverTableProcess(
        TableRuntime tableRuntime, TableProcessStore processStore) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void open(Map<String, String> properties) {}

    @Override
    public void close() {}

    @Override
    public String name() {
      return name;
    }

    TableFormat supportedFormat() {
      return format;
    }
  }

  /**
   * Two coordinators with the same action name but different formats should both be registered
   * without one overwriting the other. This is the core regression test for AMORO-4281.
   */
  @Test
  public void testMultipleCoordinatorsWithSameActionDifferentFormats() {
    FormatSpecificCoordinator icebergCoordinator =
        new FormatSpecificCoordinator(TableFormat.ICEBERG);
    FormatSpecificCoordinator paimonCoordinator = new FormatSpecificCoordinator(TableFormat.PAIMON);

    TableService tableService = Mockito.mock(TableService.class);
    ProcessService processService =
        new ProcessService(tableService, Collections.emptyList(), new ExecuteEngineManager());

    processService.installActionCoordinator(icebergCoordinator);
    processService.installActionCoordinator(paimonCoordinator);

    Map<String, List<ActionCoordinatorScheduler>> coordinators =
        processService.getActionCoordinators();
    Assertions.assertEquals(1, coordinators.size(), "Should have one action entry");
    List<ActionCoordinatorScheduler> schedulers = coordinators.get(TEST_ACTION.getName());
    Assertions.assertNotNull(schedulers, "Schedulers list should not be null");
    Assertions.assertEquals(2, schedulers.size(), "Both coordinators should be registered");

    // Verify both formats are represented
    boolean hasIceberg =
        schedulers.stream().anyMatch(s -> s.getCoordinator().formatSupported(TableFormat.ICEBERG));
    boolean hasPaimon =
        schedulers.stream().anyMatch(s -> s.getCoordinator().formatSupported(TableFormat.PAIMON));
    Assertions.assertTrue(hasIceberg, "ICEBERG coordinator should be present");
    Assertions.assertTrue(hasPaimon, "PAIMON coordinator should be present");
  }

  /**
   * Installing coordinators via installActionCoordinator should also not overwrite existing ones
   * with the same action name.
   */
  @Test
  public void testInstallActionCoordinatorDoesNotOverwrite() {
    FormatSpecificCoordinator icebergCoordinator =
        new FormatSpecificCoordinator(TableFormat.ICEBERG);
    FormatSpecificCoordinator paimonCoordinator = new FormatSpecificCoordinator(TableFormat.PAIMON);

    TableService tableService = Mockito.mock(TableService.class);
    ProcessService processService =
        new ProcessService(tableService, Collections.emptyList(), new ExecuteEngineManager());

    processService.installActionCoordinator(icebergCoordinator);
    processService.installActionCoordinator(paimonCoordinator);

    Map<String, List<ActionCoordinatorScheduler>> coordinators =
        processService.getActionCoordinators();
    List<ActionCoordinatorScheduler> schedulers = coordinators.get(TEST_ACTION.getName());
    Assertions.assertNotNull(schedulers);
    Assertions.assertEquals(2, schedulers.size(), "Both coordinators should be registered");
  }

  /**
   * The getActionCoordinators return type should be Map<String, List<ActionCoordinatorScheduler>>.
   */
  @Test
  public void testGetActionCoordinatorsReturnType() {
    FormatSpecificCoordinator coordinator = new FormatSpecificCoordinator(TableFormat.ICEBERG);
    TableService tableService = Mockito.mock(TableService.class);
    ProcessService processService =
        new ProcessService(tableService, Collections.emptyList(), new ExecuteEngineManager());

    processService.installActionCoordinator(coordinator);

    Map<String, List<ActionCoordinatorScheduler>> coordinators =
        processService.getActionCoordinators();
    Assertions.assertNotNull(coordinators);
    Assertions.assertTrue(coordinators.containsKey(TEST_ACTION.getName()));
  }
}
