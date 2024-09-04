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

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;

import java.io.IOException;

/**
 * Serializer that serializes and deserializes mixed-format enumerator {@link
 * MixedFormatEnumeratorOffset}.
 */
class MixedFormatEnumeratorOffsetSerializer
    implements SimpleVersionedSerializer<MixedFormatEnumeratorOffset> {
  public static final MixedFormatEnumeratorOffsetSerializer INSTANCE =
      new MixedFormatEnumeratorOffsetSerializer();

  private static final int VERSION = 1;

  private static final ThreadLocal<DataOutputSerializer> SERIALIZER_CACHE =
      ThreadLocal.withInitial(() -> new DataOutputSerializer(128));

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public byte[] serialize(MixedFormatEnumeratorOffset position) throws IOException {
    return serializeV1(position);
  }

  @Override
  public MixedFormatEnumeratorOffset deserialize(int version, byte[] serialized)
      throws IOException {
    switch (version) {
      case 1:
        return deserializeV1(serialized);
      default:
        throw new IOException("Unknown version: " + version);
    }
  }

  private byte[] serializeV1(MixedFormatEnumeratorOffset position) throws IOException {
    DataOutputSerializer out = SERIALIZER_CACHE.get();
    out.writeBoolean(position.changeSnapshotId() != null);
    if (position.changeSnapshotId() != null) {
      out.writeLong(position.changeSnapshotId());
    }
    out.writeBoolean(position.snapshotTimestampMs() != null);
    if (position.snapshotTimestampMs() != null) {
      out.writeLong(position.snapshotTimestampMs());
    }
    byte[] result = out.getCopyOfBuffer();
    out.clear();
    return result;
  }

  private MixedFormatEnumeratorOffset deserializeV1(byte[] serialized) throws IOException {
    DataInputDeserializer in = new DataInputDeserializer(serialized);
    Long snapshotId = null;
    if (in.readBoolean()) {
      snapshotId = in.readLong();
    }

    Long snapshotTimestampMs = null;
    if (in.readBoolean()) {
      snapshotTimestampMs = in.readLong();
    }

    return MixedFormatEnumeratorOffset.of(snapshotId, snapshotTimestampMs);
  }
}
