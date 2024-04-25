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

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.utils.IcebergInMemoryLockManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.CommitStateUnknownException;
import org.apache.iceberg.hadoop.HadoopTableOperations;

public class MixedHadoopTableOperations extends HadoopTableOperations {

  private final AuthenticatedFileIO authenticatedFileIO;

  public MixedHadoopTableOperations(Path location, AuthenticatedFileIO fileIO, Configuration conf) {
    super(location, fileIO, conf, IcebergInMemoryLockManager.instance());
    this.authenticatedFileIO = fileIO;
  }

  @Override
  public TableMetadata refresh() {
    return authenticatedFileIO.doAs(super::refresh);
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    authenticatedFileIO.doAs(
        () -> {
          try {
            super.commit(base, metadata);

            // HadoopTableOperations#commit will throw CommitFailedException even though rename
            // metadata file successfully
            // in hdfs, it may be not safe. So transform all RuntimeException to
            // CommitStateUnknownException to avoid
            // delete the committed metadata and manifest files.
            //
            // But this change may invalid the retry action for some commit operation.
          } catch (CommitFailedException e) {
            if (e.getCause() != null) {
              throw new CommitStateUnknownException(e);
            } else {
              // Do to wrap the direct CommitFailedException, we should retry committing.
              throw e;
            }
          }
          return null;
        });
  }
}
