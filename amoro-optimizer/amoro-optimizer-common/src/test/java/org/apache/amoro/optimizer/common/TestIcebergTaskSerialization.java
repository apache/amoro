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

package org.apache.amoro.optimizer.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.junit.jupiter.api.Test;

class TestIcebergTaskSerialization {

  @Test
  void testDataFileRoundTrip() {
    // Iceberg initializes its Avro schema when creating the file metadata used in optimizer tasks.
    DataFile file =
        DataFiles.builder(PartitionSpec.unpartitioned())
            .withPath("task-input.parquet")
            .withFileSizeInBytes(123L)
            .withRecordCount(3L)
            .build();

    DataFile restored =
        SerializationUtil.simpleDeserialize(SerializationUtil.simpleSerialize(file).array());

    assertEquals(file.path().toString(), restored.path().toString());
    assertEquals(file.fileSizeInBytes(), restored.fileSizeInBytes());
    assertEquals(file.recordCount(), restored.recordCount());
  }
}
