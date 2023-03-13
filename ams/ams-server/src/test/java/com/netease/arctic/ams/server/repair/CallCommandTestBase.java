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

package com.netease.arctic.ams.server.repair;

import com.google.common.collect.Iterables;
import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.TableTestHelpers;
import com.netease.arctic.ams.api.client.OptimizeManagerClient;
import com.netease.arctic.ams.server.repair.command.CallFactory;
import com.netease.arctic.ams.server.repair.command.DefaultCallFactory;
import com.netease.arctic.catalog.CatalogManager;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.TableDataTestBase;
import com.netease.arctic.op.ArcticHadoopTableOperations;
import com.netease.arctic.table.ChangeTable;
import java.util.Arrays;
import java.util.List;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.io.CloseableIterable;

public class CallCommandTestBase extends TableDataTestBase {

  private static final Integer maxFindSnapshotNum = 100;
  private static final Integer maxRollbackSnapNum = 100;

  public static CallFactory callFactory = new DefaultCallFactory(
      new RepairConfig(TEST_AMS.getServerUrl(), TEST_CATALOG_NAME, maxFindSnapshotNum, maxRollbackSnapNum),
      new CatalogManager(TEST_AMS.getServerUrl()),
      new OptimizeManagerClient(TEST_AMS.getServerUrl()),
      new PooledAmsClient(TEST_AMS.getServerUrl())
  );

  protected String removeFile() {
    CloseableIterable<FileScanTask> fileScanTasks = getArcticTable().asKeyedTable().changeTable().newScan().planFiles();
    FileScanTask[] fileScanTasksArray = Iterables.toArray(fileScanTasks, FileScanTask.class);
    String removeFile = Arrays.stream(fileScanTasksArray).filter(s -> s.file().path().toString().contains("ED"))
        .findAny().get().file().path().toString();
    getArcticTable().io().deleteFile(removeFile);
    return removeFile;
  }

  protected String removeManifest() {
    String removeManifest =
        getArcticTable().asKeyedTable().changeTable().currentSnapshot().allManifests().get(0).path();
    getArcticTable().io().deleteFile(removeManifest);
    return removeManifest;
  }

  protected String removeManifestList() {
    String removeManifestList = getArcticTable().asKeyedTable().changeTable().currentSnapshot().manifestListLocation();
    getArcticTable().io().deleteFile(removeManifestList);
    return removeManifestList;
  }

  protected int removeMetadata() {
    ChangeTable changeTable = getArcticTable().asKeyedTable().changeTable();
    ArcticHadoopTableOperations changeTableOperations =
        getCatalog().getChangeTableOperations(TableTestHelpers.TEST_TABLE_ID);
    int version = changeTableOperations.findVersion();
    List<Path> metadataCandidateFiles =
        changeTableOperations.getMetadataCandidateFiles(version);

    ArcticFileIO arcticFileIO = changeTable.io();
    for (Path path: metadataCandidateFiles) {
      if (arcticFileIO.exists(path.toString())) {
        arcticFileIO.deleteFile(path.toString());
      }
    }
    return version;
  }
}
