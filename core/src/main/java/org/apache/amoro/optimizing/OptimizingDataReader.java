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

package org.apache.amoro.optimizing;

import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;

import java.io.Closeable;

/** An interface to read the data and delete data. */
public interface OptimizingDataReader extends Closeable {

  /**
   * Reading data of optimizing task, If a task contains a delete operation, then the read result is
   * MOR.
   */
  CloseableIterable<Record> readData();

  /**
   * Reading data that needs to be deleted during MOR. If there is no delete file, then the result
   * is empty.
   */
  CloseableIterable<Record> readDeletedData();

  /** Close the reader */
  void close();
}
