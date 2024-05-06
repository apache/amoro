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

package org.apache.amoro.flink.read;

import org.apache.amoro.flink.read.hybrid.assigner.ShuffleSplitAssigner;
import org.apache.amoro.flink.read.hybrid.assigner.SplitAssigner;
import org.apache.amoro.flink.read.hybrid.assigner.StaticSplitAssigner;
import org.apache.amoro.flink.read.hybrid.enumerator.AmoroSourceEnumState;
import org.apache.amoro.flink.read.hybrid.enumerator.AmoroSourceEnumStateSerializer;
import org.apache.amoro.flink.read.hybrid.enumerator.AmoroSourceEnumerator;
import org.apache.amoro.flink.read.hybrid.enumerator.StaticAmoroSourceEnumerator;
import org.apache.amoro.flink.read.hybrid.reader.AmoroSourceReader;
import org.apache.amoro.flink.read.hybrid.reader.ReaderFunction;
import org.apache.amoro.flink.read.hybrid.split.AmoroSplit;
import org.apache.amoro.flink.read.hybrid.split.AmoroSplitSerializer;
import org.apache.amoro.flink.read.source.AmoroScanContext;
import org.apache.amoro.flink.table.AmoroTableLoader;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.core.io.SimpleVersionedSerializer;

/**
 * Amoro Source based of FLIP-27.
 *
 * <p>If AmoroSource is used as a build table in lookup join, it will be implemented by temporal
 * join. Two source should use processing time as watermark. AmoroSource will generate watermark
 * after first splits planned by AmoroSourceEnumerator having been finished.
 */
public class AmoroSource<T>
    implements Source<T, AmoroSplit, AmoroSourceEnumState>, ResultTypeQueryable<T> {
  private static final long serialVersionUID = 1L;
  private final AmoroScanContext scanContext;
  private final ReaderFunction<T> readerFunction;
  private final TypeInformation<T> typeInformation;
  private final AmoroTableLoader loader;
  private final String tableName;
  /**
   * generate amoro watermark. This is only for lookup join amoro table, and amoro table is used as
   * build table, i.e. right table.
   */
  private final boolean dimTable;

  public AmoroSource(
      AmoroTableLoader loader,
      AmoroScanContext scanContext,
      ReaderFunction<T> readerFunction,
      TypeInformation<T> typeInformation,
      String tableName,
      boolean dimTable) {
    this.loader = loader;
    this.scanContext = scanContext;
    this.readerFunction = readerFunction;
    this.typeInformation = typeInformation;
    this.tableName = tableName;
    this.dimTable = dimTable;
  }

  @Override
  public Boundedness getBoundedness() {
    return scanContext.isStreaming() ? Boundedness.CONTINUOUS_UNBOUNDED : Boundedness.BOUNDED;
  }

  @Override
  public SourceReader<T, AmoroSplit> createReader(SourceReaderContext readerContext) {
    return new AmoroSourceReader<>(
        readerFunction, readerContext.getConfiguration(), readerContext, dimTable);
  }

  @Override
  public SplitEnumerator<AmoroSplit, AmoroSourceEnumState> createEnumerator(
      SplitEnumeratorContext<AmoroSplit> enumContext) {
    return createEnumerator(enumContext, null);
  }

  private SplitEnumerator<AmoroSplit, AmoroSourceEnumState> createEnumerator(
      SplitEnumeratorContext<AmoroSplit> enumContext, AmoroSourceEnumState enumState) {
    SplitAssigner splitAssigner;
    if (scanContext.isStreaming()) {
      splitAssigner = new ShuffleSplitAssigner(enumContext, tableName, enumState);
      return new AmoroSourceEnumerator(
          enumContext, splitAssigner, loader, scanContext, enumState, dimTable);
    } else {
      splitAssigner = new StaticSplitAssigner(enumState);
      return new StaticAmoroSourceEnumerator(enumContext, splitAssigner, loader, scanContext, null);
    }
  }

  @Override
  public SplitEnumerator<AmoroSplit, AmoroSourceEnumState> restoreEnumerator(
      SplitEnumeratorContext<AmoroSplit> enumContext, AmoroSourceEnumState checkpoint) {
    return createEnumerator(enumContext, checkpoint);
  }

  @Override
  public SimpleVersionedSerializer<AmoroSplit> getSplitSerializer() {
    return new AmoroSplitSerializer();
  }

  @Override
  public SimpleVersionedSerializer<AmoroSourceEnumState> getEnumeratorCheckpointSerializer() {
    return new AmoroSourceEnumStateSerializer();
  }

  @Override
  public TypeInformation<T> getProducedType() {
    return typeInformation;
  }
}
