/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iceberg.flink.sink;

import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class DeltaManifests {

  private static final CharSequence[] EMPTY_REF_DATA_FILES = new CharSequence[0];
  public static final List<Long> EMPTY_TRANSACTION = Collections.emptyList();

  private final ManifestFile dataManifest;
  private final ManifestFile deleteManifest;
  private final CharSequence[] referencedDataFiles;
  private final List<Long> transactionIds;

  DeltaManifests(ManifestFile dataManifest, ManifestFile deleteManifest) {
    this(dataManifest, deleteManifest, EMPTY_REF_DATA_FILES);
  }

  DeltaManifests(ManifestFile dataManifest, ManifestFile deleteManifest, CharSequence[] referencedDataFiles) {
    this(dataManifest, deleteManifest, referencedDataFiles, EMPTY_TRANSACTION);
  }

  DeltaManifests(ManifestFile dataManifest, ManifestFile deleteManifest, CharSequence[] referencedDataFiles
      , List<Long> transactionIds) {
    Preconditions.checkNotNull(referencedDataFiles, "Referenced data files shouldn't be null.");

    this.dataManifest = dataManifest;
    this.deleteManifest = deleteManifest;
    this.referencedDataFiles = referencedDataFiles;
    this.transactionIds = transactionIds;
  }

  ManifestFile dataManifest() {
    return dataManifest;
  }

  ManifestFile deleteManifest() {
    return deleteManifest;
  }

  CharSequence[] referencedDataFiles() {
    return referencedDataFiles;
  }

  public List<Long> getTransactionIds() {
    return transactionIds;
  }

  List<ManifestFile> manifests() {
    List<ManifestFile> manifests = Lists.newArrayListWithCapacity(2);
    if (dataManifest != null) {
      manifests.add(dataManifest);
    }

    if (deleteManifest != null) {
      manifests.add(deleteManifest);
    }

    return manifests;
  }
}
