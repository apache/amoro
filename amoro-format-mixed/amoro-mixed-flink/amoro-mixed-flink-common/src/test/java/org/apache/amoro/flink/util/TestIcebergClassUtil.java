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

package org.apache.amoro.flink.util;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.flink.sink.FlinkWriteResult;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class TestIcebergClassUtil {

  @Test
  public void testIcebergFilesCommitterRunsThroughAuthenticatedFileIO() {
    CountingAuthenticatedFileIO fileIO = new CountingAuthenticatedFileIO();
    OneInputStreamOperator<FlinkWriteResult, Void> committer =
        IcebergClassUtil.newIcebergFilesCommitter(
            null, false, null, PartitionSpec.unpartitioned(), fileIO);

    committer.toString();

    Assert.assertEquals(1, fileIO.doAsCalls.get());
  }

  private static class CountingAuthenticatedFileIO implements AuthenticatedFileIO {
    private final AtomicInteger doAsCalls = new AtomicInteger();

    @Override
    public <T> T doAs(Callable<T> callable) {
      doAsCalls.incrementAndGet();
      try {
        return callable.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public InputFile newInputFile(String path) {
      throw new UnsupportedOperationException();
    }

    @Override
    public OutputFile newOutputFile(String path) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteFile(String path) {
      throw new UnsupportedOperationException();
    }
  }
}
