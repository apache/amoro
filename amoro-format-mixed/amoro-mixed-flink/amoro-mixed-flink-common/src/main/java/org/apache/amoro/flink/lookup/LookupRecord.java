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

public class LookupRecord {
  private final byte[] keyBytes;
  private final byte[] valueBytes;

  private final OpType opType;

  private LookupRecord(OpType opType, byte[] keyBytes, byte[] valueBytes) {
    this.keyBytes = keyBytes;
    this.valueBytes = valueBytes;
    this.opType = opType;
  }

  public static LookupRecord of(OpType opType, byte[] keyBytes, byte[] valueBytes) {
    return new LookupRecord(opType, keyBytes, valueBytes);
  }

  public byte[] keyBytes() {
    return keyBytes;
  }

  public byte[] valueBytes() {
    return valueBytes;
  }

  public OpType opType() {
    return opType;
  }

  enum OpType {
    PUT_BYTES,
    DELETE_BYTES
  }
}
