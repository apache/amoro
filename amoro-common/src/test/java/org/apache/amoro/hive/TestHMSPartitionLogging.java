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

package org.apache.amoro.hive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.PartitionDropOptions;
import org.apache.hadoop.hive.metastore.api.EnvironmentContext;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class TestHMSPartitionLogging {
  private HiveMetaStoreClient delegate;
  private HMSClientImpl client;
  private Logger logger;
  private Level previousLevel;
  private final List<String> messages = new ArrayList<>();
  private AbstractAppender appender;

  @BeforeEach
  void setup() {
    delegate = mock(HiveMetaStoreClient.class);
    client = new HMSClientImpl(delegate);
    logger = (Logger) LogManager.getLogger(HMSClientImpl.class);
    previousLevel = logger.getLevel();
    appender =
        new AbstractAppender("partition-operations-test", null, null, true, Property.EMPTY_ARRAY) {
          @Override
          public void append(LogEvent event) {
            assertEquals(Level.INFO, event.getLevel());
            messages.add(event.getMessage().getFormattedMessage());
          }
        };
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(Level.INFO);
  }

  @AfterEach
  void cleanup() {
    logger.removeAppender(appender);
    appender.stop();
    logger.setLevel(previousLevel);
  }

  @Test
  void testAddPreservesResultAndLogsIdentityAndLocation() throws Exception {
    Partition requested = partition("2026-10-16", "/warehouse/events/day=2026-10-16");
    Partition result = requested.deepCopy();
    when(delegate.add_partition(requested)).thenReturn(result);

    assertSame(result, client.addPartition(requested));

    verify(delegate).add_partition(requested);
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("Added Hive partition database.events"));
    assertTrue(messages.get(0).contains("values=[2026-10-16]"));
    assertTrue(messages.get(0).contains("location=/warehouse/events/day=2026-10-16"));
  }

  @Test
  void testBulkAddReportsActualCountAndRequestedLocations() throws Exception {
    List<Partition> partitions = Arrays.asList(partition("a", "/a"), partition("b", "/b"));
    when(delegate.add_partitions(partitions)).thenReturn(2);

    assertEquals(2, client.addPartitions(partitions));

    verify(delegate).add_partitions(partitions);
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("Added 2 Hive partitions"));
    assertTrue(messages.get(0).contains("values=[a] location=/a"));
    assertTrue(messages.get(0).contains("values=[b] location=/b"));
  }

  @Test
  void testDropOnlyReportsAnActualDeletion() throws Exception {
    List<String> values = Collections.singletonList("a");
    PartitionDropOptions options = PartitionDropOptions.instance();
    when(delegate.dropPartition("database", "events", values, options)).thenReturn(true, false);

    assertTrue(client.dropPartition("database", "events", values, options));
    assertFalse(client.dropPartition("database", "events", values, options));

    verify(delegate, times(2)).dropPartition("database", "events", values, options);
    assertEquals(
        Collections.singletonList("Dropped Hive partition database.events values=[a]"), messages);
  }

  @Test
  void testAlterLogsNewPartitionLocation() {
    Partition partition = partition("a", "/new-location");

    client.alterPartition("database", "events", partition, new EnvironmentContext());

    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("Altered Hive partition"));
    assertTrue(messages.get(0).contains("location=/new-location"));
    assertEquals(
        "alter_partition",
        mockingDetails(delegate).getInvocations().iterator().next().getMethod().getName());
  }

  @Test
  void testBulkAlterLogsNewLocations() {
    List<Partition> partitions = Arrays.asList(partition("a", "/new-a"), partition("b", "/new-b"));

    client.alterPartitions("database", "events", partitions, new EnvironmentContext());

    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("Altered Hive partitions for database.events"));
    assertTrue(messages.get(0).contains("location=/new-a"));
    assertTrue(messages.get(0).contains("location=/new-b"));
    assertEquals(
        "alter_partitions",
        mockingDetails(delegate).getInvocations().iterator().next().getMethod().getName());
  }

  @Test
  void testFailedAddRetainsExceptionAndDoesNotReportSuccess() throws Exception {
    Partition partition = partition("a", "/a");
    MetaException failure = new MetaException("metastore unavailable");
    when(delegate.add_partition(partition)).thenThrow(failure);

    assertSame(failure, assertThrows(MetaException.class, () -> client.addPartition(partition)));
    assertTrue(messages.isEmpty());
  }

  @Test
  void testNoOpBulkAddDoesNotReportSuccess() throws Exception {
    when(delegate.add_partitions(Collections.emptyList())).thenReturn(0);

    assertEquals(0, client.addPartitions(Collections.emptyList()));
    assertTrue(messages.isEmpty());
  }

  private Partition partition(String value, String location) {
    Partition partition = new Partition();
    partition.setDbName("database");
    partition.setTableName("events");
    partition.setValues(Collections.singletonList(value));
    StorageDescriptor descriptor = new StorageDescriptor();
    descriptor.setLocation(location);
    partition.setSd(descriptor);
    return partition;
  }
}
