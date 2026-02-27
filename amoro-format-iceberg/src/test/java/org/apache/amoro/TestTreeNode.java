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

package org.apache.amoro;

import org.apache.amoro.data.DataTreeNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestTreeNode {

  @Test(expected = IllegalArgumentException.class)
  public void testParentNodeIllegal() {
    DataTreeNode.of(0, 0).parent();
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "1, 0", "1, 0", "3, 3", "255, 0", "255, 126", "255, 245", "255, 255"})
  public void testParentNode(int level, int index) {
    assertParentNode(DataTreeNode.of(level, index));
  }

  @ParameterizedTest
  @CsvSource({"0, 0, 1", "1, 0, 2", "1, 1, 3", "3, 0, 4", "3, 3, 7", "7, 5, 13", "7, 3, 11"})
  public void testNodeId(int mask, int index, int expectedId) {
    Assert.assertEquals(expectedId, DataTreeNode.of(mask, index).getId());
    Assert.assertEquals(DataTreeNode.of(mask, index), DataTreeNode.ofId(expectedId));
  }

  private void assertParentNode(DataTreeNode node) {
    Assert.assertEquals(node, node.left().parent());
    Assert.assertEquals(node, node.right().parent());
  }
}
