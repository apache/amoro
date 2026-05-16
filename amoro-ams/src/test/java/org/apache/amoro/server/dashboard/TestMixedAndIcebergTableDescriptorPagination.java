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

package org.apache.amoro.server.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit tests for the pure pagination helper in {@link MixedAndIcebergTableDescriptor}. These
 * exercise the cursor / offset fallback semantics introduced to fix the JSTP-2356 Iceberg
 * regression where the descriptor previously returned the full filtered list.
 */
class TestMixedAndIcebergTableDescriptorPagination {

  private static AmoroSnapshotsOfTable snapshot(long id) {
    AmoroSnapshotsOfTable s = new AmoroSnapshotsOfTable();
    s.setSnapshotId(String.valueOf(id));
    // Use descending commitTime so the list is already in the "newest first" order that
    // getSnapshots produces after its sort step.
    s.setCommitTime(1_000_000_000L - id);
    return s;
  }

  private static List<AmoroSnapshotsOfTable> buildList(int size) {
    List<AmoroSnapshotsOfTable> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(snapshot(i));
    }
    return list;
  }

  private static List<String> idsOf(List<AmoroSnapshotsOfTable> list) {
    return list.stream().map(AmoroSnapshotsOfTable::getSnapshotId).collect(Collectors.toList());
  }

  @Test
  void paginate_offsetAndLimit() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 3, 0, null);

    assertEquals(10L, page.getRight(), "total should reflect full filtered list");
    assertEquals(3, page.getLeft().size(), "slice should honour limit");
    assertEquals(Arrays.asList("0", "1", "2"), idsOf(page.getLeft()));
  }

  @Test
  void paginate_offsetBeyondList() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 5, 100, null);

    assertEquals(10L, page.getRight());
    assertTrue(page.getLeft().isEmpty(), "offset beyond list yields empty slice");
  }

  @Test
  void paginate_cursorBased() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 3, 0, "5");

    assertEquals(10L, page.getRight());
    assertEquals(Arrays.asList("6", "7", "8"), idsOf(page.getLeft()));
  }

  @Test
  void paginate_cursorNotFound_fallsBackToOffset() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 3, 2, "999");

    assertEquals(10L, page.getRight());
    assertEquals(
        Arrays.asList("2", "3", "4"),
        idsOf(page.getLeft()),
        "missing cursor must fall back to the supplied offset");
  }

  @Test
  void paginate_emptyList() {
    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(Collections.emptyList(), 5, 0, null);

    assertEquals(0L, page.getRight());
    assertTrue(page.getLeft().isEmpty());
  }

  @Test
  void paginate_nullLastSnapshot() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 5, 0, null);

    assertEquals(10L, page.getRight());
    assertEquals(Arrays.asList("0", "1", "2", "3", "4"), idsOf(page.getLeft()));
  }

  @Test
  void paginate_cursorAtEnd() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 3, 0, "9");

    assertEquals(10L, page.getRight(), "total remains the filtered size");
    assertTrue(page.getLeft().isEmpty(), "cursor on the last element yields an empty next page");
  }

  @Test
  void paginate_emptyCursorStringTreatedAsAbsent() {
    List<AmoroSnapshotsOfTable> all = buildList(10);

    Pair<List<AmoroSnapshotsOfTable>, Long> page =
        MixedAndIcebergTableDescriptor.paginate(all, 3, 4, "");

    assertEquals(10L, page.getRight());
    assertEquals(Arrays.asList("4", "5", "6"), idsOf(page.getLeft()));
  }
}
