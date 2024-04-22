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

import org.apache.flink.table.data.ArrayData;
import org.apache.flink.table.data.DecimalData;
import org.apache.flink.table.data.MapData;
import org.apache.flink.table.data.RawValueData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.types.RowKind;

public class KeyRowData implements RowData {
  private final int[] keyIndexMapping;
  private final RowData rowData;

  public KeyRowData(int[] keyIndexMapping, RowData rowData) {
    this.keyIndexMapping = keyIndexMapping;
    this.rowData = rowData;
  }

  @Override
  public int getArity() {
    return keyIndexMapping.length;
  }

  @Override
  public RowKind getRowKind() {
    return rowData.getRowKind();
  }

  @Override
  public void setRowKind(RowKind kind) {
    rowData.setRowKind(kind);
  }

  @Override
  public boolean isNullAt(int pos) {
    return rowData.isNullAt(keyIndexMapping[pos]);
  }

  @Override
  public boolean getBoolean(int pos) {
    return rowData.getBoolean(keyIndexMapping[pos]);
  }

  @Override
  public byte getByte(int pos) {
    return rowData.getByte(keyIndexMapping[pos]);
  }

  @Override
  public short getShort(int pos) {
    return rowData.getShort(keyIndexMapping[pos]);
  }

  @Override
  public int getInt(int pos) {
    return rowData.getInt(keyIndexMapping[pos]);
  }

  @Override
  public long getLong(int pos) {
    return rowData.getLong(keyIndexMapping[pos]);
  }

  @Override
  public float getFloat(int pos) {
    return rowData.getFloat(keyIndexMapping[pos]);
  }

  @Override
  public double getDouble(int pos) {
    return rowData.getDouble(keyIndexMapping[pos]);
  }

  @Override
  public StringData getString(int pos) {
    return rowData.getString(keyIndexMapping[pos]);
  }

  @Override
  public DecimalData getDecimal(int pos, int precision, int scale) {
    return rowData.getDecimal(keyIndexMapping[pos], precision, scale);
  }

  @Override
  public TimestampData getTimestamp(int pos, int precision) {
    return rowData.getTimestamp(keyIndexMapping[pos], precision);
  }

  @Override
  public <T> RawValueData<T> getRawValue(int pos) {
    return rowData.getRawValue(keyIndexMapping[pos]);
  }

  @Override
  public byte[] getBinary(int pos) {
    return rowData.getBinary(keyIndexMapping[pos]);
  }

  @Override
  public ArrayData getArray(int pos) {
    return rowData.getArray(keyIndexMapping[pos]);
  }

  @Override
  public MapData getMap(int pos) {
    return rowData.getMap(keyIndexMapping[pos]);
  }

  @Override
  public RowData getRow(int pos, int numFields) {
    return rowData.getRow(keyIndexMapping[pos], numFields);
  }
}
