/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.iceberg;

import com.clearspring.analytics.util.Lists;
import org.apache.iceberg.events.CreateSnapshotEvent;
import org.apache.iceberg.exceptions.RuntimeIOException;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModifyChangeTableSequence
    extends SnapshotProducer<ModifyTableSequence>
    implements ModifyTableSequence {
  private final String tableName;
  private final TableOperations ops;
  private final SnapshotSummary.Builder summaryBuilder = SnapshotSummary.builder();

  private long sequence;
  private String targetBranch;

  public ModifyChangeTableSequence(String tableName, TableOperations ops) {
    super(ops);
    this.tableName = tableName;
    this.ops = ops;
  }

  @Override
  protected ModifyTableSequence self() {
    return this;
  }

  @Override
  public ModifyTableSequence set(String property, String value) {
    summaryBuilder.set(property, value);
    return self();
  }

  @Override
  protected String operation() {
    return DataOperations.REPLACE;
  }

  @Override
  protected Map<String, String> summary() {
    summaryBuilder.setPartitionSummaryLimit(ops.current().propertyAsInt(
        TableProperties.WRITE_PARTITION_SUMMARY_LIMIT, TableProperties.WRITE_PARTITION_SUMMARY_LIMIT_DEFAULT));
    return summaryBuilder.build();
  }

  private Map<String, String> summary(TableMetadata previous) {
    Map<String, String> summary = summary();
    if (summary == null) {
      return ImmutableMap.of();
    }

    Map<String, String> previousSummary;
    if (previous.currentSnapshot() != null) {
      if (previous.currentSnapshot().summary() != null) {
        previousSummary = previous.currentSnapshot().summary();
      } else {
        // previous snapshot had no summary, use an empty summary
        previousSummary = ImmutableMap.of();
      }
    } else {
      // if there was no previous snapshot, default the summary to start totals at 0
      ImmutableMap.Builder<String, String> summaryBuilder = ImmutableMap.builder();
      summaryBuilder
          .put(SnapshotSummary.TOTAL_RECORDS_PROP, "0")
          .put(SnapshotSummary.TOTAL_FILE_SIZE_PROP, "0")
          .put(SnapshotSummary.TOTAL_DATA_FILES_PROP, "0")
          .put(SnapshotSummary.TOTAL_DELETE_FILES_PROP, "0")
          .put(SnapshotSummary.TOTAL_POS_DELETES_PROP, "0")
          .put(SnapshotSummary.TOTAL_EQ_DELETES_PROP, "0");
      previousSummary = summaryBuilder.build();
    }

    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    // copy all summary properties from the implementation
    builder.putAll(previousSummary);
    return builder.build();
  }

  @Override
  public Object updateEvent() {
    long snapshotId = snapshotId();
    Snapshot snapshot = current().snapshot(snapshotId);
    long sequenceNumber = snapshot.sequenceNumber();
    return new CreateSnapshotEvent(
        tableName,
        operation(),
        snapshotId,
        sequenceNumber,
        snapshot.summary());
  }

  @Override
  protected void cleanUncommitted(Set<ManifestFile> committed) {
    // nothing to do.
  }

  @Override
  public ModifyTableSequence sequence(long sequence) {
    this.sequence = sequence;
    return self();
  }

  @Override
  protected void targetBranch(String branch) {
    super.targetBranch(branch);
    this.targetBranch = branch;
  }

  @Override
  protected List<ManifestFile> apply(TableMetadata metadataToUpdate, Snapshot snapshot) {
    return Lists.newArrayList();
  }

  @Override
  public Snapshot apply() {
    TableMetadata base = refresh();
    Snapshot parentSnapshot = base.currentSnapshot();
    if (targetBranch != null) {
      SnapshotRef branch = base.ref(targetBranch);
      if (branch != null) {
        parentSnapshot = base.snapshot(branch.snapshotId());
      } else if (base.currentSnapshot() != null) {
        parentSnapshot = base.currentSnapshot();
      }
    }

    long sequenceNumber = nextSequenceNumber(base);
    Long parentSnapshotId = parentSnapshot == null ? null : parentSnapshot.snapshotId();

    validate(base, parentSnapshot);
    List<ManifestFile> manifests = apply(base, parentSnapshot);

    OutputFile manifestList = manifestListPath();

    try (ManifestListWriter writer =
        ManifestLists.write(
            ops.current().formatVersion(),
            manifestList,
            snapshotId(),
            parentSnapshotId,
            sequenceNumber)) {
      // JUST CREATE A MANIFEST_LIST FILE
    } catch (IOException e) {
      throw new RuntimeIOException(e, "Failed to write manifest list file");
    }

    return new BaseSnapshot(
        sequenceNumber,
        snapshotId(),
        parentSnapshotId,
        System.currentTimeMillis(),
        operation(),
        summary(base),
        base.currentSchemaId(),
        manifestList.location());
  }

  protected long nextSequenceNumber(TableMetadata base) {
    return Math.max(base.nextSequenceNumber(), this.sequence);
  }
}
