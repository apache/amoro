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

package org.apache.amoro.flink.lookup;

import org.apache.commons.collections.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Utility class for serializing and deserializing a set of ByteArrayWrapper objects. */
public class ByteArraySetSerializer {

  /**
   * Deserializes a byte array into a set of ByteArrayWrapper objects.
   *
   * @param byteArray the byte array to deserialize
   * @return the deserialized set of ByteArrayWrapper objects
   */
  public static Set<ByteArrayWrapper> deserialize(byte[] byteArray) {
    if (byteArray == null) {
      return Collections.emptySet();
    }

    Set<ByteArrayWrapper> set = new HashSet<>();

    ByteBuffer buffer = ByteBuffer.wrap(byteArray);
    int setSize = buffer.getInt(); // Read the size of the set

    for (int i = 0; i < setSize; i++) {
      int elementSize = buffer.getInt(); // Read the size of the element
      byte[] element = new byte[elementSize];
      buffer.get(element); // Read the element bytes
      ByteArrayWrapper baw = new ByteArrayWrapper(element, elementSize);
      set.add(baw);
    }

    return set;
  }

  /**
   * Serializes a set of ByteArrayWrapper objects into a byte array.
   *
   * @param set the set of ByteArrayWrapper objects to serialize
   * @return the serialized byte array
   */
  public static byte[] serialize(Set<ByteArrayWrapper> set) {
    if (CollectionUtils.isEmpty(set)) {
      return null;
    }

    // Calculate the total size of the resulting byte array
    // The first 4 bytes represent the size of the set
    int totalSize = 4;
    for (ByteArrayWrapper record : set) {
      // Each element consists of 4 bytes representing the size of the element
      totalSize += 4;
      totalSize += record.size;
    }

    // Create a new byte array with the total size
    ByteBuffer buffer = ByteBuffer.allocate(totalSize);
    buffer.putInt(set.size()); // Write the size of the set

    for (ByteArrayWrapper record : set) {
      buffer.putInt(record.size); // Write the size of the element
      buffer.put(record.bytes); // Write the element bytes
    }

    return buffer.array();
  }
}
