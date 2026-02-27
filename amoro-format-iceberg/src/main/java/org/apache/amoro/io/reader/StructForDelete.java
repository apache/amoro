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

package org.apache.amoro.io.reader;

import org.apache.iceberg.Accessor;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.StructProjection;

import java.util.Set;

public class StructForDelete<T extends StructLike> {

  private T structLike;

  private final StructProjection pkProjection;

  private final Accessor<StructLike> posAccessor;
  private final Accessor<StructLike> filePathAccessor;
  private final Accessor<StructLike> dataTransactionIdAccessor;

  public StructForDelete(Schema schema, Set<Integer> deleteIds) {
    this.pkProjection = StructProjection.create(schema, TypeUtil.select(schema, deleteIds));
    this.dataTransactionIdAccessor =
        schema.accessorForField(org.apache.amoro.table.MetadataColumns.TRANSACTION_ID_FILED_ID);
    this.posAccessor = schema.accessorForField(MetadataColumns.ROW_POSITION.fieldId());
    this.filePathAccessor = schema.accessorForField(MetadataColumns.FILE_PATH.fieldId());
  }

  public StructForDelete<T> wrap(T structLike) {
    this.structLike = structLike;
    return this;
  }

  public StructLike getPk() {
    return pkProjection.copyFor(structLike);
  }

  public Long getLsn() {
    return (Long) dataTransactionIdAccessor.get(structLike);
  }

  public Long getPosition() {
    return (Long) posAccessor.get(structLike);
  }

  public String filePath() {
    return (String) filePathAccessor.get(structLike);
  }

  public T recover() {
    return structLike;
  }
}
