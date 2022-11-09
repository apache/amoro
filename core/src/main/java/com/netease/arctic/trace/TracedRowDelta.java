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

package com.netease.arctic.trace;

import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.expressions.Expression;

import java.util.function.Consumer;

/**
 * Wrap {@link RowDelta} with {@link TableTracer}.
 */
public class TracedRowDelta implements RowDelta {

  private final RowDelta rowDelta;
  private final TableTracer tracer;

  public TracedRowDelta(RowDelta rowDelta, TableTracer tracer) {
    this.rowDelta = rowDelta;
    this.tracer = tracer;
  }

  @Override
  public RowDelta addRows(DataFile inserts) {
    rowDelta.addRows(inserts);
    tracer.addDataFile(inserts);
    return this;
  }

  @Override
  public RowDelta addDeletes(DeleteFile deletes) {
    rowDelta.addDeletes(deletes);
    tracer.addDeleteFile(deletes);
    return this;
  }

  @Override
  public RowDelta validateFromSnapshot(long snapshotId) {
    rowDelta.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  public RowDelta caseSensitive(boolean caseSensitive) {
    rowDelta.caseSensitive(caseSensitive);
    return this;
  }

  @Override
  public RowDelta validateDataFilesExist(Iterable<? extends CharSequence> referencedFiles) {
    rowDelta.validateDataFilesExist(referencedFiles);
    return this;
  }

  @Override
  public RowDelta validateDeletedFiles() {
    rowDelta.validateDeletedFiles();
    return this;
  }

  @Override
  public RowDelta validateNoConflictingAppends(Expression conflictDetectionFilter) {
    rowDelta.validateNoConflictingAppends(conflictDetectionFilter);
    return this;
  }

  @Override
  public RowDelta conflictDetectionFilter(Expression conflictDetectionFilter) {
    rowDelta.conflictDetectionFilter(conflictDetectionFilter);
    return this;
  }

  @Override
  public RowDelta validateNoConflictingDataFiles() {
    rowDelta.validateNoConflictingDataFiles();
    return this;
  }

  @Override
  public RowDelta validateNoConflictingDeleteFiles() {
    rowDelta.validateNoConflictingDeleteFiles();
    return this;
  }

  @Override
  public RowDelta set(String property, String value) {
    rowDelta.set(property, value);
    tracer.setSnapshotSummary(property, value);
    return this;
  }

  @Override
  public RowDelta deleteWith(Consumer<String> deleteFunc) {
    rowDelta.deleteWith(deleteFunc);
    return this;
  }

  @Override
  public RowDelta stageOnly() {
    rowDelta.stageOnly();
    return this;
  }

  @Override
  public Snapshot apply() {
    return rowDelta.apply();
  }

  @Override
  public void commit() {
    rowDelta.commit();
    tracer.commit();
  }
}
