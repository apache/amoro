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
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.expressions.Expression;

import java.util.function.Consumer;

/**
 * Wrap {@link OverwriteFiles} with {@link TableTracer}.
 */
public class TracedOverwriteFiles implements OverwriteFiles {

  private final OverwriteFiles overwriteFiles;
  private final TableTracer tracer;

  public TracedOverwriteFiles(OverwriteFiles overwriteFiles, TableTracer tracer) {
    this.overwriteFiles = overwriteFiles;
    this.tracer = tracer;
  }

  @Override
  public OverwriteFiles overwriteByRowFilter(Expression expr) {
    overwriteFiles.overwriteByRowFilter(expr);
    return this;
  }

  @Override
  public OverwriteFiles addFile(DataFile file) {
    overwriteFiles.addFile(file);
    tracer.addDataFile(file);
    return this;
  }

  @Override
  public OverwriteFiles deleteFile(DataFile file) {
    overwriteFiles.deleteFile(file);
    tracer.deleteDataFile(file);
    return this;
  }

  @Override
  public OverwriteFiles validateAddedFilesMatchOverwriteFilter() {
    overwriteFiles.validateAddedFilesMatchOverwriteFilter();
    return this;
  }

  @Override
  public OverwriteFiles validateFromSnapshot(long snapshotId) {
    overwriteFiles.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  public OverwriteFiles caseSensitive(boolean caseSensitive) {
    overwriteFiles.caseSensitive(caseSensitive);
    return this;
  }

  @Override
  public OverwriteFiles validateNoConflictingAppends(Expression conflictDetectionFilter) {
    overwriteFiles.validateNoConflictingAppends(conflictDetectionFilter);
    return this;
  }

  @Override
  public OverwriteFiles conflictDetectionFilter(Expression conflictDetectionFilter) {
    overwriteFiles.conflictDetectionFilter(conflictDetectionFilter);
    return this;
  }

  @Override
  public OverwriteFiles validateNoConflictingData() {
    overwriteFiles.validateNoConflictingData();
    return this;
  }

  @Override
  public OverwriteFiles validateNoConflictingDeletes() {
    overwriteFiles.validateNoConflictingDeletes();
    return this;
  }

  @Override
  public OverwriteFiles set(String property, String value) {
    overwriteFiles.set(property, value);
    tracer.setSnapshotSummary(property, value);
    return this;
  }

  @Override
  public OverwriteFiles deleteWith(Consumer<String> deleteFunc) {
    overwriteFiles.deleteWith(deleteFunc);
    return this;
  }

  @Override
  public OverwriteFiles stageOnly() {
    overwriteFiles.stageOnly();
    return this;
  }

  @Override
  public Snapshot apply() {
    return overwriteFiles.apply();
  }

  @Override
  public void commit() {
    overwriteFiles.commit();
    tracer.commit();
  }

  @Override
  public Object updateEvent() {
    return overwriteFiles.updateEvent();
  }
}
