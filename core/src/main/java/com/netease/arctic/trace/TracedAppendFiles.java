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

import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.Snapshot;

import java.util.function.Consumer;

/**
 * Wrap {@link AppendFiles} with {@link TableTracer}.
 */
public class TracedAppendFiles implements AppendFiles {

  private final AppendFiles appendFiles;
  private final TableTracer tracer;

  public TracedAppendFiles(AppendFiles appendFiles, TableTracer tracer) {
    this.appendFiles = appendFiles;
    this.tracer = tracer;
  }

  @Override
  public AppendFiles appendFile(DataFile file) {
    appendFiles.appendFile(file);
    tracer.addDataFile(file);
    return this;
  }

  @Override
  public AppendFiles appendManifest(ManifestFile file) {
    //TODO read added files from manifest file
    appendFiles.appendManifest(file);
    return this;
  }

  @Override
  public AppendFiles set(String property, String value) {
    appendFiles.set(property, value);
    return this;
  }

  @Override
  public AppendFiles deleteWith(Consumer<String> deleteFunc) {
    appendFiles.deleteWith(deleteFunc);
    return this;
  }

  @Override
  public AppendFiles stageOnly() {
    appendFiles.stageOnly();
    return this;
  }

  @Override
  public Snapshot apply() {
    return appendFiles.apply();
  }

  @Override
  public void commit() {
    appendFiles.commit();
    tracer.commit();
  }

  @Override
  public Object updateEvent() {
    return appendFiles.updateEvent();
  }
}
