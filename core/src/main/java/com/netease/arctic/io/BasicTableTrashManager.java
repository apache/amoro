/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.io;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Basic implementation of TableTrashManager.
 */
public class BasicTableTrashManager implements TableTrashManager {
  private static final Logger LOG = LoggerFactory.getLogger(BasicTableTrashManager.class);
  public static String DEFAULT_TRASH_DIR = ".trash";
  private final TableIdentifier tableIdentifier;
  private final ArcticFileIO arcticFileIO;
  private final String trashDir;

  public static BasicTableTrashManager of(ArcticTable table) {
    return new BasicTableTrashManager(table.id(), table.io(), "");
  }

  private BasicTableTrashManager(TableIdentifier tableIdentifier, ArcticFileIO arcticFileIO, String trashDir) {
    this.tableIdentifier = tableIdentifier;
    this.arcticFileIO = arcticFileIO;
    this.trashDir = trashDir;
  }

  @Override
  public TableIdentifier tableId() {
    return tableIdentifier;
  }

  @Override
  public boolean moveFileToTrash(String path) {
    // TODO
    return true;
  }

  @Override
  public boolean fileExistInTrash(String path) {
    // TODO
    return false;
  }

  @Override
  public boolean restoreFileFromTrash(String path) {
    // TODO
    return false;
  }

  @Override
  public void cleanFiles(LocalDate expirationDate) {
    // TODO
  }
}
