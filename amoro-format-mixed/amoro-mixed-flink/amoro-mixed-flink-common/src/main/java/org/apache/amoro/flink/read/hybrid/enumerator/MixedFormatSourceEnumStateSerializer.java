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

package org.apache.amoro.flink.read.hybrid.enumerator;

import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplitSerializer;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplitState;
import org.apache.amoro.flink.read.hybrid.split.TemporalJoinSplits;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.util.InstantiationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

/**
 * Serializer that serializes and deserializes mixed-format enumerator {@link
 * MixedFormatSourceEnumState}.
 */
public class MixedFormatSourceEnumStateSerializer
    implements SimpleVersionedSerializer<MixedFormatSourceEnumState> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MixedFormatSourceEnumStateSerializer.class);
  private static final int VERSION = 1;
  private final MixedFormatSplitSerializer splitSerializer = MixedFormatSplitSerializer.INSTANCE;
  private final MixedFormatEnumeratorOffsetSerializer offsetSerializer =
      MixedFormatEnumeratorOffsetSerializer.INSTANCE;

  private static final ThreadLocal<DataOutputSerializer> SERIALIZER_CACHE =
      ThreadLocal.withInitial(() -> new DataOutputSerializer(1024));

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public byte[] serialize(MixedFormatSourceEnumState mixedFormatSourceEnumState)
      throws IOException {
    return serializeV1(mixedFormatSourceEnumState);
  }

  private byte[] serializeV1(MixedFormatSourceEnumState enumState) throws IOException {
    DataOutputSerializer out = SERIALIZER_CACHE.get();

    out.writeBoolean(enumState.lastEnumeratedOffset() != null);
    if (enumState.lastEnumeratedOffset() != null) {
      out.writeInt(offsetSerializer.getVersion());
      byte[] positionBytes = offsetSerializer.serialize(enumState.lastEnumeratedOffset());
      out.writeInt(positionBytes.length);
      out.write(positionBytes);
    }

    out.writeInt(splitSerializer.getVersion());
    out.writeInt(enumState.pendingSplits().size());
    for (MixedFormatSplitState splitState : enumState.pendingSplits()) {
      byte[] splitBytes = splitSerializer.serialize(splitState.toSourceSplit());
      out.writeInt(splitBytes.length);
      out.write(splitBytes);
    }

    out.writeBoolean(enumState.shuffleSplitRelation() != null);
    if (enumState.shuffleSplitRelation() != null) {
      long[] shuffleSplitRelation = enumState.shuffleSplitRelation();
      out.writeInt(Objects.requireNonNull(shuffleSplitRelation).length);
      for (long l : shuffleSplitRelation) {
        out.writeLong(l);
      }
    }

    out.writeBoolean(enumState.temporalJoinSplits() != null);
    if (enumState.temporalJoinSplits() != null) {
      byte[] temporalJoinSplits = InstantiationUtil.serializeObject(enumState.temporalJoinSplits());
      out.writeInt(temporalJoinSplits.length);
      out.write(temporalJoinSplits);
    }

    byte[] result = out.getCopyOfBuffer();
    out.clear();
    return result;
  }

  @Override
  public MixedFormatSourceEnumState deserialize(int version, byte[] serialized) throws IOException {
    switch (version) {
      case 1:
        return deserializeV1(serialized);
      default:
        throw new IOException("Unknown version: " + version);
    }
  }

  private MixedFormatSourceEnumState deserializeV1(byte[] serialized) throws IOException {
    DataInputDeserializer in = new DataInputDeserializer(serialized);

    MixedFormatEnumeratorOffset enumeratorOffset = null;
    if (in.readBoolean()) {
      int version = in.readInt();
      byte[] positionBytes = new byte[in.readInt()];
      in.read(positionBytes);
      enumeratorOffset = offsetSerializer.deserialize(version, positionBytes);
    }

    int splitSerializerVersion = in.readInt();
    int splitCount = in.readInt();
    Collection<MixedFormatSplitState> pendingSplits = Lists.newArrayListWithCapacity(splitCount);
    for (int i = 0; i < splitCount; ++i) {
      byte[] splitBytes = new byte[in.readInt()];
      in.read(splitBytes);
      MixedFormatSplit split = splitSerializer.deserialize(splitSerializerVersion, splitBytes);
      pendingSplits.add(new MixedFormatSplitState(split));
    }

    long[] shuffleSplitRelation = null;
    if (in.readBoolean()) {
      int length = in.readInt();
      shuffleSplitRelation = new long[length];
      for (int i = 0; i < length; i++) {
        shuffleSplitRelation[i] = in.readLong();
      }
    }

    TemporalJoinSplits temporalJoinSplits = null;
    if (in.readBoolean()) {
      byte[] bytes = new byte[in.readInt()];
      in.read(bytes);
      try {
        temporalJoinSplits =
            InstantiationUtil.deserializeObject(bytes, TemporalJoinSplits.class.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("deserialize FirstSplit error", e);
      }
    }

    return new MixedFormatSourceEnumState(
        pendingSplits, enumeratorOffset, shuffleSplitRelation, temporalJoinSplits);
  }
}
