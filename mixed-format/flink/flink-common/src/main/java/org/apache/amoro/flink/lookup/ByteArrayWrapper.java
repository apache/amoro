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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Locale;

/** This byte array wrapper utility class. copied from com.ibm.icu.util.ByteArrayWrapper. */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper>, Serializable {
  private static final long serialVersionUID = -6697944376117365645L;
  public byte[] bytes;

  /**
   * Size of the internal byte array used. Different from bytes.length, size will be &lt;=
   * bytes.length. Semantics of size is similar to java.util.Vector.size().
   */
  public int size;

  /**
   * Construct a new ByteArrayWrapper from a byte array and size.
   *
   * @param bytesToAdopt the byte array to adopt
   * @param size the length of valid data in the byte array
   * @throws IndexOutOfBoundsException if bytesToAdopt == null and size != 0, or size &lt; 0, or
   *     size &gt; bytesToAdopt.length.
   */
  public ByteArrayWrapper(byte[] bytesToAdopt, int size) {
    if ((bytesToAdopt == null && size != 0)
        || size < 0
        || (bytesToAdopt != null && size > bytesToAdopt.length)) {
      throw new IndexOutOfBoundsException("illegal size: " + size);
    }
    this.bytes = bytesToAdopt;
    this.size = size;
  }

  /**
   * Construct a new ByteArrayWrapper from the contents of a ByteBuffer.
   *
   * @param source the ByteBuffer from which to get the data.
   */
  public ByteArrayWrapper(ByteBuffer source) {
    size = source.limit();
    bytes = new byte[size];
    source.get(bytes, 0, size);
  }

  /**
   * Ensure that the internal byte array is at least of length capacity. If the byte array is null
   * or its length is less than capacity, a new byte array of length capacity will be allocated. The
   * contents of the array (between 0 and size) remain unchanged.
   *
   * @param capacity minimum length of internal byte array.
   * @return this ByteArrayWrapper
   */
  public ByteArrayWrapper ensureCapacity(int capacity) {
    if (bytes == null || bytes.length < capacity) {
      byte[] newBytes = new byte[capacity];
      if (bytes != null) {
        copyBytes(bytes, 0, newBytes, 0, size);
      }
      bytes = newBytes;
    }
    return this;
  }

  /**
   * Set the internal byte array from offset 0 to (limit - start) with the contents of src from
   * offset start to limit. If the byte array is null or its length is less than capacity, a new
   * byte array of length (limit - start) will be allocated. This resets the size of the internal
   * byte array to (limit - start).
   *
   * @param src source byte array to copy from
   * @param start start offset of src to copy from
   * @param limit end + 1 offset of src to copy from
   * @return this ByteArrayWrapper
   */
  public final ByteArrayWrapper set(byte[] src, int start, int limit) {
    size = 0;
    append(src, start, limit);
    return this;
  }

  /**
   * Appends the internal byte array from offset size with the contents of src from offset start to
   * limit. This increases the size of the internal byte array to (size + limit - start).
   *
   * @param src source byte array to copy from
   * @param start start offset of src to copy from
   * @param limit end + 1 offset of src to copy from
   * @return this ByteArrayWrapper
   */
  public final ByteArrayWrapper append(byte[] src, int start, int limit) {
    int len = limit - start;
    ensureCapacity(size + len);
    copyBytes(src, start, bytes, size, len);
    size += len;
    return this;
  }

  /**
   * Releases the internal byte array to the caller, resets the internal byte array to null and its
   * size to 0.
   *
   * @return internal byte array.
   */
  public final byte[] releaseBytes() {
    byte[] result = bytes;
    bytes = null;
    size = 0;
    return result;
  }

  /** Returns string value for debugging. */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < size; ++i) {
      if (i != 0) {
        result.append(" ");
      }
      result.append(hex(bytes[i] & 0xFF));
    }
    return result.toString();
  }

  private static String hex(long i) {
    if (i == Long.MIN_VALUE) {
      return "-8000000000000000";
    } else {
      boolean negative = i < 0L;
      if (negative) {
        i = -i;
      }

      String result = Long.toString(i, 16).toUpperCase(Locale.ENGLISH);
      if (result.length() < 2) {
        result = "0000000000000000".substring(result.length(), 2) + result;
      }

      return negative ? '-' + result : result;
    }
  }

  /**
   * Return true if the bytes in each wrapper are equal.
   *
   * @param other the object to compare to.
   * @return true if the two objects are equal.
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!(other instanceof ByteArrayWrapper)) {
      return false;
    }

    ByteArrayWrapper that = (ByteArrayWrapper) other;
    if (size != that.size) {
      return false;
    }
    for (int i = 0; i < size; ++i) {
      if (bytes[i] != that.bytes[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return the hashcode.
   *
   * @return the hashcode.
   */
  @Override
  public int hashCode() {
    int result = size;
    for (int i = 0; i < size; ++i) {
      result = 37 * result + bytes[i];
    }
    return result;
  }

  /**
   * Compare this object to another ByteArrayWrapper, which must not be null.
   *
   * @param other the object to compare to.
   * @return a value &lt;0, 0, or &gt;0 as this compares less than, equal to, or greater than other.
   * @throws ClassCastException if the other object is not a ByteArrayWrapper
   */
  @Override
  public int compareTo(ByteArrayWrapper other) {
    if (this == other) {
      return 0;
    }
    int minSize = Math.min(size, other.size);
    for (int i = 0; i < minSize; ++i) {
      if (bytes[i] != other.bytes[i]) {
        return (bytes[i] & 0xFF) - (other.bytes[i] & 0xFF);
      }
    }
    return size - other.size;
  }

  /**
   * Copies the contents of src byte array from offset srcOff to the target of target byte array at
   * the offset targetOff.
   *
   * @param src source byte array to copy from
   * @param srcOff start offset of src to copy from
   * @param target target byte array to copy to
   * @param targetOff start offset of target to copy to
   * @param length size of contents to copy
   */
  private static void copyBytes(byte[] src, int srcOff, byte[] target, int targetOff, int length) {
    if (length < 64) {
      for (int i = srcOff, n = targetOff; --length >= 0; ++i, ++n) {
        target[n] = src[i];
      }
    } else {
      System.arraycopy(src, srcOff, target, targetOff, length);
    }
  }
}
