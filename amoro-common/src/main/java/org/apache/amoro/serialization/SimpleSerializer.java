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

package org.apache.amoro.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class SimpleSerializer<R> implements ResourceSerde<R> {

  @Override
  public byte[] serializeResource(R resource) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        oos.writeObject(resource);
        oos.flush();
        return bos.toByteArray();
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("serialization error of " + resource, e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public R deserializeResource(byte[] input) {
    if (input == null) {
      return null;
    }
    try (ByteArrayInputStream bis = new ByteArrayInputStream(input)) {
      try (ObjectInputStream ois =
          new ContextClassLoaderObjectInputStream(
              bis, Thread.currentThread().getContextClassLoader())) {
        return (R) ois.readObject();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException("deserialization error ", e);
    }
  }

  private static class ContextClassLoaderObjectInputStream extends ObjectInputStream {

    private final ClassLoader classLoader;

    private ContextClassLoaderObjectInputStream(ByteArrayInputStream input, ClassLoader classLoader)
        throws IOException {
      super(input);
      this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
        throws IOException, ClassNotFoundException {
      if (classLoader != null) {
        try {
          return Class.forName(desc.getName(), false, classLoader);
        } catch (ClassNotFoundException ignored) {
          // Fall through to ObjectInputStream's default loader resolution.
        }
      }
      return super.resolveClass(desc);
    }
  }
}
