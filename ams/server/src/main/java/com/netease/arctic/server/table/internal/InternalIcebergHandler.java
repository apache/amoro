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

package com.netease.arctic.server.table.internal;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.utils.InternalTableUtil;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Table handler for iceberg format */
public class InternalIcebergHandler implements InternalTableHandler<TableOperations> {

  private static final Logger LOG = LoggerFactory.getLogger(InternalIcebergHandler.class);

  private boolean closed = false;
  protected final ArcticFileIO io;
  private final TableMetadata tableMetadata;

  public InternalIcebergHandler(CatalogMeta catalogMeta, TableMetadata metadata) {
    this.tableMetadata = metadata;
    this.io = InternalTableUtil.newIcebergFileIo(catalogMeta);
  }

  @Override
  public TableMetadata tableMetadata() {
    return this.tableMetadata;
  }

  @Override
  public TableOperations newTableOperator() {
    checkClosed();
    return new IcebergInternalTableOperations(
        tableMetadata.getTableIdentifier(), tableMetadata, io);
  }

  @Override
  public void dropTable(boolean purge) {
    checkClosed();
    if (purge) {
      purgeIceberg(newTableOperator());
    }
    if (purge && io.supportPrefixOperations()) {
      io.asPrefixFileIO().deletePrefix(tableMetadata.getTableLocation());
    }
  }

  protected void purgeIceberg(TableOperations ops) {
    org.apache.iceberg.TableMetadata current = null;
    try {
      current = ops.current();
    } catch (Exception e) {
      LOG.warn(
          "failed to load iceberg table metadata, metadata file maybe lost: " + e.getMessage());
    }

    if (current != null) {
      org.apache.iceberg.CatalogUtil.dropTableData(io, current);
    }
  }

  @Override
  public void close() {
    io.close();
    closed = true;
  }

  protected void checkClosed() {
    Preconditions.checkState(
        !closed,
        this.getClass().getSimpleName()
            + " for table ["
            + tableMetadata.getTableIdentifier()
            + "] is "
            + "closed");
  }
}
