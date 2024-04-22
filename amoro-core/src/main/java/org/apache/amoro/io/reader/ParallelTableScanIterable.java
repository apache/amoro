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

package org.apache.amoro.io.reader;

import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.data.PublicGenericReader;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableGroup;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.util.ParallelIterable;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class ParallelTableScanIterable extends CloseableGroup implements CloseableIterable<Record> {
  private final ParallelIterable<Record> parallelIterable;

  ParallelTableScanIterable(
      TableScan scan, boolean reuseContainers, ExecutorService executorService) {
    PublicGenericReader reader = new PublicGenericReader(scan, reuseContainers);
    // start planning tasks in the background
    CloseableIterable<FileScanTask> tasks = scan.planFiles();
    CloseableIterable<CloseableIterable<Record>> transform =
        CloseableIterable.transform(tasks, reader::open);
    this.parallelIterable = new ParallelIterable<>(transform, executorService);
  }

  @Override
  public CloseableIterator<Record> iterator() {
    return parallelIterable.iterator();
  }

  @Override
  public void close() throws IOException {
    parallelIterable.close();
    super.close(); // close data files
  }
}
