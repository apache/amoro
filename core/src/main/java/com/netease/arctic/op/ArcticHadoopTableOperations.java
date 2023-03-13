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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.common.DynMethods;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.CommitStateUnknownException;
import org.apache.iceberg.hadoop.HadoopTableOperations;
import org.apache.iceberg.util.LockManagers;

import java.util.ArrayList;
import java.util.List;

public class ArcticHadoopTableOperations extends HadoopTableOperations {

  private final ArcticFileIO arcticFileIO;

  private DynMethods.BoundMethod findVersion = DynMethods.builder("findVersion")
      .hiddenImpl(HadoopTableOperations.class, null)
      .build(this);

  private DynMethods.BoundMethod metadataFilePath = DynMethods.builder("metadataFilePath")
      .hiddenImpl(HadoopTableOperations.class, Integer.TYPE, TableMetadataParser.Codec.class)
      .build(this);

  private DynMethods.BoundMethod oldMetadataFilePath = DynMethods.builder("oldMetadataFilePath")
      .hiddenImpl(HadoopTableOperations.class, Integer.TYPE, TableMetadataParser.Codec.class)
      .build(this);

  private DynMethods.BoundMethod versionHintFile = DynMethods.builder("versionHintFile")
      .hiddenImpl(HadoopTableOperations.class, null)
      .build(this);

  private final Configuration conf;

  public ArcticHadoopTableOperations(Path location, ArcticFileIO fileIO, Configuration conf) {
    super(location, fileIO, conf, LockManagers.defaultLockManager());
    this.conf = conf;
    this.arcticFileIO = fileIO;
  }

  @Override
  public TableMetadata refresh() {
    return arcticFileIO.doAs(super::refresh);
  }

  public int findVersion() {
    try {
      return arcticFileIO.doAs(() -> findVersion.invokeChecked());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Path metadataFilePath(int metadataVersion, TableMetadataParser.Codec codec) {
    try {
      return metadataFilePath.invokeChecked(metadataVersion, codec);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Path oldMetadataFilePath(int metadataVersion, TableMetadataParser.Codec codec) {
    try {
      return oldMetadataFilePath.invokeChecked(metadataVersion, codec);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Path> getMetadataCandidateFiles(int metadataVersion) {
    List<Path> paths = new ArrayList<>();
    for (TableMetadataParser.Codec codec : TableMetadataParser.Codec.values()) {
      Path metadataFile = metadataFilePath(metadataVersion, codec);
      paths.add(metadataFile);

      if (codec.equals(TableMetadataParser.Codec.GZIP)) {
        // we have to be backward-compatible with .metadata.json.gz files
        metadataFile = oldMetadataFilePath(metadataVersion, codec);
        paths.add(metadataFile);
      }
    }
    return paths;
  }

  public void removeVersionHit() {
    Path path = null;
    try {
      path = versionHintFile.invokeChecked();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    io().deleteFile(path.toString());
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    arcticFileIO.doAs(() -> {
      try {
        super.commit(base, metadata);

        // HadoopTableOperations#commit will throw CommitFailedException even though rename metadata file successfully
        // in hdfs, it may be not safe. So transform all RuntimeException to CommitStateUnknownException to avoid
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
