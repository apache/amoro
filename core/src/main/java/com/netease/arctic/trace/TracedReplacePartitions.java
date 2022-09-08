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

package com.netease.arctic.trace;

import java.util.function.Consumer;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Snapshot;

public class TracedReplacePartitions implements ReplacePartitions {

  private final ReplacePartitions replacePartitions;
  private final TableTracer tracer;

  public TracedReplacePartitions(ReplacePartitions replacePartitions, TableTracer tracer) {
    this.replacePartitions = replacePartitions;
    this.tracer = tracer;
  }

  @Override
  public ReplacePartitions addFile(DataFile file) {
    replacePartitions.addFile(file);
    tracer.addDataFile(file);
    return this;
  }

  @Override
  public ReplacePartitions validateAppendOnly() {
    replacePartitions.validateAppendOnly();
    return this;
  }

  @Override
  public ReplacePartitions set(String property, String value) {
    replacePartitions.set(property, value);
    tracer.setSnapshotSummary(property, value);
    return this;
  }

  @Override
  public ReplacePartitions deleteWith(Consumer<String> deleteFunc) {
    replacePartitions.deleteWith(deleteFunc);
    return this;
  }

  @Override
  public ReplacePartitions stageOnly() {
    replacePartitions.stageOnly();
    return this;
  }

  @Override
  public Snapshot apply() {
    return replacePartitions.apply();
  }

  @Override
  public void commit() {
    replacePartitions.commit();
    tracer.commit();
  }
}
