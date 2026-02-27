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

package org.apache.amoro.utils;

import org.apache.amoro.serialization.JavaSerializer;
import org.apache.amoro.serialization.KryoSerializer;
import org.apache.amoro.serialization.ResourceSerde;
import org.apache.amoro.serialization.SimpleSerializer;

import java.nio.ByteBuffer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SerializationUtil {

  private static final KryoSerializer kryoSerialize = new KryoSerializer();
  private static final SimpleSerializer simpleSerialize = new SimpleSerializer();

  public static ByteBuffer simpleSerialize(Object obj) {
    return ByteBuffer.wrap(simpleSerialize.serializeResource(obj));
  }

  public static <R> R simpleDeserialize(byte[] bytes) {
    return (R) simpleSerialize.deserializeResource(bytes);
  }

  public static byte[] kryoSerialize(final Object obj) {
    return kryoSerialize.serializeResource(obj);
  }

  public static <R> ResourceSerde<R> createJavaSimpleSerializer() {
    return JavaSerializer.INSTANT;
  }

  @SuppressWarnings("unchecked")
  public static <R> R kryoDeserialize(final byte[] objectData) {
    if (objectData == null) {
      throw new NullPointerException("The byte[] must not be null");
    }
    return (R) kryoSerialize.deserializeResource(objectData);
  }
}
