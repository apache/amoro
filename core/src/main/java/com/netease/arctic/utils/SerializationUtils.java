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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.netease.arctic.data.IcebergContentFile;
import org.apache.avro.util.Utf8;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.util.ByteBuffers;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class SerializationUtils {

  private static final ThreadLocal<KryoSerializerInstance> SERIALIZER =
          ThreadLocal.withInitial(KryoSerializerInstance::new);

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

  public static IcebergContentFile toIcebergContentFile(ByteBuffer buffer) {
    return (IcebergContentFile) toObject(buffer);
  }

  public static byte[] serialize(final Object obj) throws IOException {
    return SERIALIZER.get().serialize(obj);
  }

  public static <T> T deserialize(final byte[] objectData) {
    if (objectData == null) {
      throw new IllegalArgumentException("The byte[] must not be null");
    }
    return (T) SERIALIZER.get().deserialize(objectData);
  }

  private static class KryoSerializerInstance implements Serializable {
    public static final int KRYO_SERIALIZER_INITIAL_BUFFER_SIZE = 1048576;
    private final Kryo kryo;
    private final ByteArrayOutputStream baos;

    KryoSerializerInstance() {
      KryoInstantiator kryoInstantiator = new KryoInstantiator();
      kryo = kryoInstantiator.newKryo();
      baos = new ByteArrayOutputStream(KRYO_SERIALIZER_INITIAL_BUFFER_SIZE);
      kryo.setRegistrationRequired(false);
    }

    byte[] serialize(Object obj) {
      kryo.reset();
      baos.reset();
      Output output = new Output(baos);
      this.kryo.writeClassAndObject(output, obj);
      output.close();
      return baos.toByteArray();
    }

    Object deserialize(byte[] objectData) {
      return this.kryo.readClassAndObject(new Input(objectData));
    }
  }

  private static class KryoInstantiator implements Serializable {

    public Kryo newKryo() {
      Kryo kryo = new Kryo();

      // This instance of Kryo should not require prior registration of classes
      kryo.setRegistrationRequired(false);
      Kryo.DefaultInstantiatorStrategy instantiatorStrategy = new Kryo.DefaultInstantiatorStrategy();
      instantiatorStrategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
      kryo.setInstantiatorStrategy(instantiatorStrategy);
      // Handle cases where we may have an odd classloader setup like with libjars
      // for hadoop
      kryo.setClassLoader(Thread.currentThread().getContextClassLoader());

      // Register serializers
      kryo.register(Utf8.class, new AvroUtf8Serializer());

      return kryo;
    }

  }

  private static class AvroUtf8Serializer extends Serializer<Utf8> {

    @SuppressWarnings("unchecked")
    @Override
    public void write(Kryo kryo, Output output, Utf8 utf8String) {
      Serializer<byte[]> bytesSerializer = kryo.getDefaultSerializer(byte[].class);
      bytesSerializer.write(kryo, output, utf8String.getBytes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Utf8 read(Kryo kryo, Input input, Class<Utf8> type) {
      Serializer<byte[]> bytesSerializer = kryo.getDefaultSerializer(byte[].class);
      byte[] bytes = bytesSerializer.read(kryo, input, byte[].class);
      return new Utf8(bytes);
    }
  }
}
