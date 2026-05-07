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

package org.apache.amoro.flink.shuffle;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.table.data.RowData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TestRoundRobinShuffleRulePolicy extends FlinkTestBase {

  static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(true, true),
        Arguments.of(true, false),
        Arguments.of(false, true),
        Arguments.of(false, false));
  }

  private void setUpForParam(boolean keyedTable, boolean partitionedTable) throws Exception {
    initFlinkTestBase(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(keyedTable, partitionedTable));
  }

  @ParameterizedTest(name = "keyedTable = {0}, partitionedTable = {1}")
  @MethodSource("parameters")
  public void testPrimaryKeyPartitionedTable(boolean keyedTable, boolean partitionedTable)
      throws Exception {
    setUpForParam(keyedTable, partitionedTable);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    ShuffleHelper helper =
        ShuffleHelper.build(getMixedTable(), getMixedTable().schema(), FLINK_ROW_TYPE);
    RoundRobinShuffleRulePolicy policy = new RoundRobinShuffleRulePolicy(helper, 5, 2);
    Map<Integer, Set<DataTreeNode>> subTaskTreeNodes = policy.getSubtaskTreeNodes();
    Assertions.assertEquals(subTaskTreeNodes.size(), 5);
    subTaskTreeNodes
        .values()
        .forEach(
            nodes -> {
              Assertions.assertEquals(nodes.size(), 2);
              Assertions.assertTrue(nodes.contains(DataTreeNode.of(1, 0)));
              Assertions.assertTrue(nodes.contains(DataTreeNode.of(1, 1)));
            });

    KeySelector<RowData, ShuffleKey> keySelector = policy.generateKeySelector();
    Partitioner<ShuffleKey> partitioner = policy.generatePartitioner();
    Assertions.assertEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello2", "2022-10-11T10:10:11.0")), 5));

    Assertions.assertNotEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello2", "2022-10-12T10:10:11.0")), 5));

    Assertions.assertNotEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(2, "hello2", "2022-10-11T10:10:11.0")), 5));
  }

  @ParameterizedTest(name = "keyedTable = {0}, partitionedTable = {1}")
  @MethodSource("parameters")
  public void testPrimaryKeyTableWithoutPartition(boolean keyedTable, boolean partitionedTable)
      throws Exception {
    setUpForParam(keyedTable, partitionedTable);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
    ShuffleHelper helper =
        ShuffleHelper.build(getMixedTable(), getMixedTable().schema(), FLINK_ROW_TYPE);
    RoundRobinShuffleRulePolicy policy = new RoundRobinShuffleRulePolicy(helper, 5, 2);
    Map<Integer, Set<DataTreeNode>> subTaskTreeNodes = policy.getSubtaskTreeNodes();
    Assertions.assertEquals(subTaskTreeNodes.size(), 5);
    Assertions.assertEquals(
        subTaskTreeNodes.get(0), Sets.newHashSet(DataTreeNode.of(7, 0), DataTreeNode.of(7, 5)));
    Assertions.assertEquals(
        subTaskTreeNodes.get(1), Sets.newHashSet(DataTreeNode.of(7, 1), DataTreeNode.of(7, 6)));
    Assertions.assertEquals(
        subTaskTreeNodes.get(2), Sets.newHashSet(DataTreeNode.of(7, 2), DataTreeNode.of(7, 7)));
    Assertions.assertEquals(subTaskTreeNodes.get(3), Sets.newHashSet(DataTreeNode.of(7, 3)));
    Assertions.assertEquals(subTaskTreeNodes.get(4), Sets.newHashSet(DataTreeNode.of(7, 4)));

    KeySelector<RowData, ShuffleKey> keySelector = policy.generateKeySelector();
    Partitioner<ShuffleKey> partitioner = policy.generatePartitioner();
    Assertions.assertEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello2", "2022-10-11T10:10:11.0")), 5));

    Assertions.assertEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello2", "2022-10-12T10:10:11.0")), 5));

    Assertions.assertNotEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(2, "hello2", "2022-10-11T10:10:11.0")), 5));
  }

  @ParameterizedTest(name = "keyedTable = {0}, partitionedTable = {1}")
  @MethodSource("parameters")
  public void testPartitionedTableWithoutPrimaryKey(boolean keyedTable, boolean partitionedTable)
      throws Exception {
    setUpForParam(keyedTable, partitionedTable);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    ShuffleHelper helper =
        ShuffleHelper.build(getMixedTable(), getMixedTable().schema(), FLINK_ROW_TYPE);
    RoundRobinShuffleRulePolicy policy = new RoundRobinShuffleRulePolicy(helper, 5, 2);
    Map<Integer, Set<DataTreeNode>> subTaskTreeNodes = policy.getSubtaskTreeNodes();
    Assertions.assertEquals(subTaskTreeNodes.size(), 5);
    subTaskTreeNodes
        .values()
        .forEach(
            nodes -> {
              Assertions.assertEquals(nodes.size(), 1);
              Assertions.assertTrue(nodes.contains(DataTreeNode.of(0, 0)));
            });

    KeySelector<RowData, ShuffleKey> keySelector = policy.generateKeySelector();
    Partitioner<ShuffleKey> partitioner = policy.generatePartitioner();
    Assertions.assertEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello2", "2022-10-11T10:10:11.0")), 5));

    Assertions.assertEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(2, "hello2", "2022-10-11T10:10:11.0")), 5));

    Assertions.assertNotEquals(
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello", "2022-10-11T10:10:11.0")), 5),
        partitioner.partition(
            keySelector.getKey(createRowData(1, "hello2", "2022-10-12T10:10:11.0")), 5));
  }
}
