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

package com.netease.arctic.utils;

import org.apache.iceberg.ContentFile;
import org.apache.iceberg.util.ByteBuffers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class SerializationUtil {

  public static ByteBuffer toByteBuffer(Object obj) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        oos.writeObject(obj);
        oos.flush();
        return ByteBuffer.wrap(bos.toByteArray());
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("serialization error of " + obj, e);
    }
  }

  public static ByteBuffer byteArrayToByteBuffer(byte[] bytes) {
    return ByteBuffer.wrap(bytes);
  }

  public static byte[] byteBufferToByteArray(ByteBuffer buffer) {
    return ByteBuffers.toByteArray(buffer);
  }

  public static Object toObject(ByteBuffer buffer) {
    byte[] bytes = ByteBuffers.toByteArray(buffer);
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
      try (ObjectInputStream ois = new ObjectInputStream(bis)) {
        return ois.readObject();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException("deserialization error ", e);
    }
  }

  public static Object toObject(byte[] bytes) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
      try (ObjectInputStream ois = new ObjectInputStream(bis)) {
        return ois.readObject();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException("deserialization error ", e);
    }
  }

  public static ContentFile<?> toInternalTableFile(ByteBuffer buffer) {
    return (ContentFile<?>) toObject(buffer);
  }

  public static ContentFile<?> toInternalTableFile(byte[] bytes) {
    return (ContentFile<?>) toObject(bytes);
  }
}
