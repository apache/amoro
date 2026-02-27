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

package org.apache.amoro.trino;

import static java.util.Objects.requireNonNull;

import io.airlift.json.JsonCodec;
import io.trino.filesystem.TrinoFileSystemFactory;
import io.trino.plugin.iceberg.CommitTaskData;
import io.trino.plugin.iceberg.TableStatisticsWriter;
import io.trino.plugin.iceberg.catalog.TrinoCatalogFactory;
import io.trino.spi.type.TypeManager;
import org.apache.amoro.trino.keyed.KeyedConnectorMetadata;
import org.apache.amoro.trino.unkeyed.IcebergMetadata;

import javax.inject.Inject;

/** A factory to generate {@link MixedFormatConnectorMetadata} */
public class MixedFormatMetadataFactory {
  private final TypeManager typeManager;
  private final JsonCodec<CommitTaskData> commitTaskCodec;
  private final TrinoFileSystemFactory fileSystemFactory;
  private final TableStatisticsWriter tableStatisticsWriter;
  private final MixedFormatCatalogFactory mixedFormatCatalogFactory;
  private final TrinoCatalogFactory trinoCatalogFactory;

  @Inject
  public MixedFormatMetadataFactory(
      TypeManager typeManager,
      JsonCodec<CommitTaskData> commitTaskCodec,
      TrinoFileSystemFactory fileSystemFactory,
      TableStatisticsWriter tableStatisticsWriter,
      MixedFormatCatalogFactory mixedFormatCatalogFactory,
      TrinoCatalogFactory trinoCatalogFactory) {
    this.typeManager = requireNonNull(typeManager, "typeManager is null");
    this.commitTaskCodec = requireNonNull(commitTaskCodec, "commitTaskCodec is null");
    this.fileSystemFactory = requireNonNull(fileSystemFactory, "fileSystemFactory is null");
    this.tableStatisticsWriter =
        requireNonNull(tableStatisticsWriter, "tableStatisticsWriter is null");
    this.mixedFormatCatalogFactory = mixedFormatCatalogFactory;
    this.trinoCatalogFactory = trinoCatalogFactory;
  }

  public MixedFormatConnectorMetadata create() {
    IcebergMetadata icebergMetadata =
        new IcebergMetadata(
            typeManager,
            commitTaskCodec,
            trinoCatalogFactory.create(null),
            fileSystemFactory,
            tableStatisticsWriter);
    KeyedConnectorMetadata keyedConnectorMetadata =
        new KeyedConnectorMetadata(mixedFormatCatalogFactory.getMixedFormatCatalog(), typeManager);
    return new MixedFormatConnectorMetadata(
        keyedConnectorMetadata, icebergMetadata, mixedFormatCatalogFactory.getMixedFormatCatalog());
  }
}
