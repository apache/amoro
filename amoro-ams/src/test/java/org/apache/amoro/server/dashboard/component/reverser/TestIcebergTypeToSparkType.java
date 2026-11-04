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

package org.apache.amoro.server.dashboard.component.reverser;

import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestIcebergTypeToSparkType {

  private static String sparkString(Type type) {
    return TypeUtil.visit(type, new IcebergTypeToSparkType()).catalogString();
  }

  @Test
  public void testTimestampWithoutZone() {
    Assertions.assertEquals("timestamp_ntz", sparkString(Types.TimestampType.withoutZone()));
  }

  @Test
  public void testTimestampWithZone() {
    Assertions.assertEquals("timestamp", sparkString(Types.TimestampType.withZone()));
  }

  @Test
  public void testNestedStructKeepsFieldDataTypes() {
    Types.StructType struct =
        Types.StructType.of(
            Types.NestedField.optional(1, "ts_tz", Types.TimestampType.withZone()),
            Types.NestedField.optional(2, "ts_ntz", Types.TimestampType.withoutZone()));

    Assertions.assertEquals("struct<ts_tz:timestamp,ts_ntz:timestamp_ntz>", sparkString(struct));
  }
}
