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

package com.netease.arctic.hive.utils;

import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.utils.TableFileUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;

/** Util class to help commit datafile in hive location. */
public class HiveCommitUtil {

  /**
   * Under the Hive commit protocol, the writer will write files with the filename ".filename". In
   * the commit phase, it is necessary to rename it to a visible file to ensure its final
   * consistency.
   */
  public static List<DataFile> commitHiveDataFiles(
      List<DataFile> dataFiles, ArcticHadoopFileIO fileIO, PartitionSpec spec) {
    List<DataFile> afterCommittedFiles = Lists.newArrayList();
    for (DataFile file : dataFiles) {
      String filename = TableFileUtil.getFileName(file.path().toString());
      if (!filename.startsWith(".")) {
        afterCommittedFiles.add(file);
        continue;
      }
      String committedFilename = filename.substring(1);
      String committedLocation =
          TableFileUtil.getFileDir(file.path().toString()) + "/" + committedFilename;

      if (!fileIO.exists(committedLocation)) {
        fileIO.rename(file.path().toString(), committedLocation);
      }
      DataFile committedDatafile =
          DataFiles.builder(spec).copy(file).withPath(committedLocation).build();
      afterCommittedFiles.add(committedDatafile);
    }
    return afterCommittedFiles;
  }
}
