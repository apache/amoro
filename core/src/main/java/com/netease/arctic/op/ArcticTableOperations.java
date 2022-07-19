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

package com.netease.arctic.op;

import com.netease.arctic.io.ArcticFileIO;
import org.apache.iceberg.LocationProviders;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;

public class ArcticTableOperations implements TableOperations {

  private final TableOperations ops;
  private final ArcticFileIO io;

  public ArcticTableOperations(TableOperations ops, ArcticFileIO io) {
    this.ops = ops;
    this.io = io;
  }

  @Override
  public TableMetadata current() {
    return io.doAs(ops::current);
  }

  @Override
  public TableMetadata refresh() {
    return io.doAs(ops::refresh);
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    io.doAs(() -> {
      ops.commit(base, metadata);
      return null;
    });
  }

  @Override
  public FileIO io() {
    return io;
  }

  @Override
  public EncryptionManager encryption() {
    return ops.encryption();
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
    return new TableOperations() {
      @Override
      public TableMetadata current() {
        return uncommittedMetadata;
      }

      @Override
      public TableMetadata refresh() {
        throw new UnsupportedOperationException("Cannot call refresh on temporary table operations");
      }

      @Override
      public void commit(TableMetadata base, TableMetadata metadata) {
        throw new UnsupportedOperationException("Cannot call commit on temporary table operations");
      }

      @Override
      public String metadataFileLocation(String fileName) {
        return ArcticTableOperations.this.metadataFileLocation(fileName);
      }

      @Override
      public LocationProvider locationProvider() {
        return LocationProviders.locationsFor(uncommittedMetadata.location(), uncommittedMetadata.properties());
      }

      @Override
      public FileIO io() {
        return ArcticTableOperations.this.io();
      }

      @Override
      public EncryptionManager encryption() {
        return ArcticTableOperations.this.encryption();
      }

      @Override
      public long newSnapshotId() {
        return ArcticTableOperations.this.newSnapshotId();
      }
    };
  }

  @Override
  public long newSnapshotId() {
    return ops.newSnapshotId();
  }
}
