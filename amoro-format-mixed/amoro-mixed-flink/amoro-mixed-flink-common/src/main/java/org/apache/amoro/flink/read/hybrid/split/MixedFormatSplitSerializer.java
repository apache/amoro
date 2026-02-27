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

package org.apache.amoro.flink.read.hybrid.split;

import org.apache.amoro.log.Bytes;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.flink.util.InstantiationUtil;

import java.io.IOException;

/** Serializer that serializes and deserializes {@link MixedFormatSplit}. */
public class MixedFormatSplitSerializer implements SimpleVersionedSerializer<MixedFormatSplit> {
  public static final MixedFormatSplitSerializer INSTANCE = new MixedFormatSplitSerializer();
  private static final int VERSION = 1;

  private static final byte SNAPSHOT_SPLIT_FLAG = 1;
  private static final byte CHANGELOG_SPLIT_FLAG = 2;
  private static final byte MOR_SPLIT_FLAG = 3;

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public byte[] serialize(MixedFormatSplit split) throws IOException {
    if (split == null) {
      return new byte[0];
    }
    if (split.isMergeOnReadSplit()) {
      MergeOnReadSplit mergeOnReadSplit = (MergeOnReadSplit) split;
      byte[] content = InstantiationUtil.serializeObject(mergeOnReadSplit);
      return Bytes.mergeByte(new byte[] {MOR_SPLIT_FLAG}, content);
    } else if (split.isSnapshotSplit()) {
      SnapshotSplit snapshotSplit = (SnapshotSplit) split;
      byte[] content = InstantiationUtil.serializeObject(snapshotSplit);
      return Bytes.mergeByte(new byte[] {SNAPSHOT_SPLIT_FLAG}, content);
    } else if (split.isChangelogSplit()) {
      ChangelogSplit changelogSplit = (ChangelogSplit) split;
      byte[] content = InstantiationUtil.serializeObject(changelogSplit);
      return Bytes.mergeByte(new byte[] {CHANGELOG_SPLIT_FLAG}, content);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "This mixed-format split is not supported, class %s.",
              split.getClass().getSimpleName()));
    }
  }

  @Override
  public MixedFormatSplit deserialize(int version, byte[] serialized) throws IOException {
    if (serialized.length == 0) {
      return null;
    }
    try {
      byte flag = serialized[0];
      if (version == VERSION) {
        byte[] content = Bytes.subByte(serialized, 1, serialized.length - 1);
        if (flag == MOR_SPLIT_FLAG) {
          return InstantiationUtil.<MergeOnReadSplit>deserializeObject(
              content, MergeOnReadSplit.class.getClassLoader());
        } else if (flag == SNAPSHOT_SPLIT_FLAG) {
          return InstantiationUtil.<SnapshotSplit>deserializeObject(
              content, SnapshotSplit.class.getClassLoader());
        } else if (flag == CHANGELOG_SPLIT_FLAG) {
          return InstantiationUtil.<ChangelogSplit>deserializeObject(
              content, ChangelogSplit.class.getClassLoader());
        } else {
          throw new IllegalArgumentException(
              String.format(
                  "this flag split %s is unsupported. available: %s, %s, and %s.",
                  flag, SNAPSHOT_SPLIT_FLAG, CHANGELOG_SPLIT_FLAG, MOR_SPLIT_FLAG));
        }
      }
    } catch (ClassNotFoundException e) {
      throw new FlinkRuntimeException("deserialize split failed", e);
    }
    throw new FlinkRuntimeException(
        String.format("this version %s is not supported during deserialize split.", version));
  }
}
