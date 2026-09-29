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

package org.apache.amoro.server.table.internal;

import org.apache.amoro.server.table.TableMetadata;

import java.io.Closeable;

/** Interface to create an internal table. */
public interface InternalTableCreator extends Closeable {

  /** Build Iceberg metadata without writing files or registering the table. */
  org.apache.iceberg.TableMetadata stage();

  /** Write Iceberg metadata and prepare the {@link TableMetadata} for AMS. */
  TableMetadata create();

  /** Persist Iceberg metadata and prepare the table metadata for AMS. */
  TableMetadata create(org.apache.iceberg.TableMetadata icebergMetadata);

  /** Remove files written during {@link #create()}. */
  void rollback();

  /** Release resources such as {@link org.apache.iceberg.io.FileIO}. */
  @Override
  default void close() {}
}
