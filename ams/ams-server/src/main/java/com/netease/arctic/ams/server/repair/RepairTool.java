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

package com.netease.arctic.ams.server.repair;

import com.netease.arctic.op.ForcedDeleteFiles;
import com.netease.arctic.op.ForcedDeleteManifests;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.ManageSnapshots;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.Table;

import java.util.List;

public class RepairTool {

  public static void syncMetadataForFileLose(Table table, List<ContentFile> loseFile) {
    ForcedDeleteFiles forcedDeleteFiles = ForcedDeleteFiles.of(table);
    for (ContentFile contentFile: loseFile) {
      if (contentFile instanceof DataFile) {
        forcedDeleteFiles.delete((DataFile) contentFile);
      } else if (contentFile instanceof DeleteFile) {
        forcedDeleteFiles.delete((DeleteFile) contentFile);
      }
    }
    forcedDeleteFiles.commit();
  }

  public static void syncMetadataForManifestLose(Table table, List<ManifestFile> loseManifestFile) {
    ForcedDeleteManifests forcedDeleteManifests = ForcedDeleteManifests.of(table);
    for (ManifestFile manifestFile: loseManifestFile) {
      forcedDeleteManifests.deleteManifest(manifestFile);
    }
    forcedDeleteManifests.commit();
  }

  public static void rollback(Table table, long snapshot) {
    ManageSnapshots manageSnapshots = table.manageSnapshots();
    manageSnapshots.rollbackTo(snapshot).commit();
  }
}
