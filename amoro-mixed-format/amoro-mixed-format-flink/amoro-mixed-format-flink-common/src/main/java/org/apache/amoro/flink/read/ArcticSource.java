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
import org.apache.amoro.flink.read.hybrid.enumerator.ArcticSourceEnumState;
import org.apache.amoro.flink.read.hybrid.enumerator.ArcticSourceEnumStateSerializer;
import org.apache.amoro.flink.read.hybrid.enumerator.ArcticSourceEnumerator;
import org.apache.amoro.flink.read.hybrid.enumerator.StaticArcticSourceEnumerator;
import org.apache.amoro.flink.read.hybrid.reader.ArcticSourceReader;
import org.apache.amoro.flink.read.hybrid.reader.ReaderFunction;
import org.apache.amoro.flink.read.hybrid.split.ArcticSplit;
import org.apache.amoro.flink.read.hybrid.split.ArcticSplitSerializer;
import org.apache.amoro.flink.read.source.ArcticScanContext;
import org.apache.amoro.flink.table.ArcticTableLoader;
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
 * Arctic Source based of FLIP-27.
 *
 * <p>If ArcticSource is used as a build table in lookup join, it will be implemented by temporal
 * join. Two source should use processing time as watermark. ArcticSource will generate watermark
 * after first splits planned by ArcticSourceEnumerator having been finished.
 */
public class ArcticSource<T>
    implements Source<T, ArcticSplit, ArcticSourceEnumState>, ResultTypeQueryable<T> {
  private static final long serialVersionUID = 1L;
  private final ArcticScanContext scanContext;
  private final ReaderFunction<T> readerFunction;
  private final TypeInformation<T> typeInformation;
  private final ArcticTableLoader loader;
  private final String tableName;
  /**
   * generate arctic watermark. This is only for lookup join arctic table, and arctic table is used
   * as build table, i.e. right table.
   */
  private final boolean dimTable;

  public ArcticSource(
      ArcticTableLoader loader,
      ArcticScanContext scanContext,
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
  public SourceReader<T, ArcticSplit> createReader(SourceReaderContext readerContext) {
    return new ArcticSourceReader<>(
        readerFunction, readerContext.getConfiguration(), readerContext, dimTable);
  }

  @Override
  public SplitEnumerator<ArcticSplit, ArcticSourceEnumState> createEnumerator(
      SplitEnumeratorContext<ArcticSplit> enumContext) {
    return createEnumerator(enumContext, null);
  }

  private SplitEnumerator<ArcticSplit, ArcticSourceEnumState> createEnumerator(
      SplitEnumeratorContext<ArcticSplit> enumContext, ArcticSourceEnumState enumState) {
    SplitAssigner splitAssigner;
    if (scanContext.isStreaming()) {
      splitAssigner = new ShuffleSplitAssigner(enumContext, tableName, enumState);
      return new ArcticSourceEnumerator(
          enumContext, splitAssigner, loader, scanContext, enumState, dimTable);
    } else {
      splitAssigner = new StaticSplitAssigner(enumState);
      return new StaticArcticSourceEnumerator(
          enumContext, splitAssigner, loader, scanContext, null);
    }
  }

  @Override
  public SplitEnumerator<ArcticSplit, ArcticSourceEnumState> restoreEnumerator(
      SplitEnumeratorContext<ArcticSplit> enumContext, ArcticSourceEnumState checkpoint) {
    return createEnumerator(enumContext, checkpoint);
  }

  @Override
  public SimpleVersionedSerializer<ArcticSplit> getSplitSerializer() {
    return new ArcticSplitSerializer();
  }

  @Override
  public SimpleVersionedSerializer<ArcticSourceEnumState> getEnumeratorCheckpointSerializer() {
    return new ArcticSourceEnumStateSerializer();
  }

  @Override
  public TypeInformation<T> getProducedType() {
    return typeInformation;
  }
}
