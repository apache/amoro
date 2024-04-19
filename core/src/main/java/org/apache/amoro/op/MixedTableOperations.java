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

package org.apache.amoro.op;

import org.apache.amoro.io.MixedFileIO;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;

/** A wrapper class around {@link TableOperations}. Add authentication for all methods. */
public class MixedTableOperations implements TableOperations {

  private final TableOperations ops;
  private final MixedFileIO mixedFileIO;

  public MixedTableOperations(TableOperations ops, MixedFileIO mixedFileIO) {
    this.ops = ops;
    this.mixedFileIO = mixedFileIO;
  }

  @Override
  public TableMetadata current() {
    return mixedFileIO.doAs(ops::current);
  }

  @Override
  public TableMetadata refresh() {
    return mixedFileIO.doAs(ops::refresh);
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    mixedFileIO.doAs(
        () -> {
          ops.commit(base, metadata);
          return null;
        });
  }

  @Override
  public FileIO io() {
    return mixedFileIO;
  }

  @Override
  public String metadataFileLocation(String fileName) {
    return ops.metadataFileLocation(fileName);
  }

  @Override
  public LocationProvider locationProvider() {
    return ops.locationProvider();
  }

  @Override
  public TableOperations temp(TableMetadata uncommittedMetadata) {
    TableOperations temp = ops.temp(uncommittedMetadata);
    return new TableOperations() {
      @Override
      public TableMetadata current() {
        return mixedFileIO.doAs(temp::current);
      }

      @Override
      public TableMetadata refresh() {
        return mixedFileIO.doAs(temp::refresh);
      }

      @Override
      public void commit(TableMetadata base, TableMetadata metadata) {
        mixedFileIO.doAs(
            () -> {
              temp.commit(base, metadata);
              return null;
            });
      }

      @Override
      public FileIO io() {
        return mixedFileIO;
      }

      @Override
      public String metadataFileLocation(String fileName) {
        return mixedFileIO.doAs(() -> temp.metadataFileLocation(fileName));
      }

      @Override
      public LocationProvider locationProvider() {
        return mixedFileIO.doAs(temp::locationProvider);
      }
    };
  }
}
