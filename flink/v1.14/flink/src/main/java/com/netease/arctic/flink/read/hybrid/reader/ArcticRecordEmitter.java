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

package com.netease.arctic.flink.read.hybrid.reader;

import com.netease.arctic.flink.read.hybrid.split.ArcticSplitState;
import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.connector.base.source.reader.RecordEmitter;

/**
 * Emitter that emit {@link T} to the next flink operator and update the record offset of {@link T} into split state.
 */
public class ArcticRecordEmitter<T> implements RecordEmitter<ArcticRecordWithOffset<T>, T, ArcticSplitState> {

  @Override
  public void emitRecord(
      ArcticRecordWithOffset<T> element,
      SourceOutput<T> sourceOutput,
      ArcticSplitState split) throws Exception {
    sourceOutput.collect(element.record());
    split.updateOffset(new Object[]{element.insertFileOffset(), element.insertRecordOffset(),
        element.deleteFileOffset(), element.deleteRecordOffset()});
  }
}
