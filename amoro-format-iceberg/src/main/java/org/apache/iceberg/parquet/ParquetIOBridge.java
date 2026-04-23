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

package org.apache.iceberg.parquet;

import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;

/**
 * Bridge for reusing Iceberg's package-scoped {@link ParquetIO} conversions from parquet row-group
 * merge path.
 *
 * <p>This class lives in the same package as {@link ParquetIO} on purpose to convert Iceberg {@link
 * org.apache.iceberg.io.InputFile}/{@link org.apache.iceberg.io.OutputFile} instances into Parquet
 * file abstractions without re-implementing Iceberg's adapter logic.
 */
public final class ParquetIOBridge {
  private ParquetIOBridge() {}

  public static org.apache.parquet.io.InputFile file(InputFile file) {
    return ParquetIO.file(file);
  }

  public static org.apache.parquet.io.OutputFile file(OutputFile file) {
    return ParquetIO.file(file);
  }
}
