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

package com.netease.arctic.spark;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.spark.sql.utils.ProjectingInternalRow;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** cast internal row to upsert internal row */
public class SparkInternalRowCastWrapper extends GenericInternalRow {
  private final InternalRow row;
  private final StructType schema;
  private ChangeAction changeAction = ChangeAction.INSERT;
  private List<DataType> dataTypeList;

  public SparkInternalRowCastWrapper(
      InternalRow row, ChangeAction changeAction, StructType schema) {
    this.row = row;
    this.changeAction = changeAction;
    if (row instanceof ProjectingInternalRow) {
      this.schema = ((ProjectingInternalRow) row).schema();
    } else {
      this.schema = schema;
    }
  }

  public StructType getSchema() {
    return this.schema;
  }

  @Override
  public Object genericGet(int ordinal) {
    return row.get(ordinal, schema.apply(ordinal).dataType());
  }

  @Override
  public int numFields() {
    return schema.size() / 2;
  }

  @Override
  public boolean isNullAt(int ordinal) {
    dataTypeList =
        Arrays.stream(schema.fields()).map(StructField::dataType).collect(Collectors.toList());
    return row.get(ordinal, dataTypeList.get(ordinal)) == null;
  }

  @Override
  public Object get(int pos, DataType dt) {
    return row.get(pos, dt);
  }

  public InternalRow getRow() {
    return this.row;
  }

  public ChangeAction getChangeAction() {
    return changeAction;
  }

  public InternalRow setFileOffset(Long fileOffset) {
    List<DataType> dataTypeList =
        Arrays.stream(schema.fields()).map(StructField::dataType).collect(Collectors.toList());
    List<Object> objectSeq = new ArrayList<>(dataTypeList.size() + 1);
    row.toSeq(schema).toStream().foreach(objectSeq::add);
    objectSeq.add(fileOffset);
    return new GenericInternalRow(objectSeq.toArray());
  }
}
