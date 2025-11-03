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

package org.apache.amoro.spark

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.catalyst.util.{ArrayData, MapData}
import org.apache.spark.sql.types.{DataType, Decimal, StructType}
import org.apache.spark.unsafe.types.{CalendarInterval, UTF8String}

import org.apache.amoro.data.ChangeAction
import org.apache.amoro.spark.sql.utils.ProjectingInternalRow

/** cast internal row to upsert internal row */
class SparkInternalRowCastWrapper(
    private val row: InternalRow,
    private val changeAction: ChangeAction,
    private var schema: StructType) extends GenericInternalRow {
  if (row.isInstanceOf[ProjectingInternalRow]) {
    schema = row.asInstanceOf[ProjectingInternalRow].schema
  }
  private lazy val dataTypeList = schema.fields.map(_.dataType)

  def getSchema: StructType = schema

  override protected def genericGet(ordinal: Int): Any = {
    row.get(ordinal, schema.apply(ordinal).dataType)
  }

  override def toSeq(fieldTypes: Seq[DataType]): Seq[Any] = {
    super.toSeq(fieldTypes)
  }

  override def numFields: Int = {
    schema.size / 2
  }

  override def setNullAt(i: Int): Unit = {
    super.setNullAt(i)
  }

  override def update(i: Int, value: Any): Unit = {
    super.update(i, value)
  }

  override def isNullAt(ordinal: Int): Boolean = {
    row.get(ordinal, dataTypeList(ordinal)) == null
  }

  override def get(pos: Int, dt: DataType): AnyRef = {
    row.get(pos, dt)
  }

  override def getBoolean(ordinal: Int): Boolean = {
    super.getBoolean(ordinal)
  }

  override def getByte(ordinal: Int): Byte = {
    super.getByte(ordinal)
  }

  override def getShort(ordinal: Int): Short = {
    super.getShort(ordinal)
  }

  override def getInt(ordinal: Int): Int = {
    super.getInt(ordinal)
  }

  override def getLong(ordinal: Int): Long = {
    super.getLong(ordinal)
  }

  override def getFloat(ordinal: Int): Float = {
    super.getFloat(ordinal)
  }

  override def getDouble(ordinal: Int): Double = {
    super.getDouble(ordinal)
  }

  override def getDecimal(ordinal: Int, precision: Int, scale: Int): Decimal = {
    super.getDecimal(ordinal, precision, scale)
  }

  override def getUTF8String(ordinal: Int): UTF8String = {
    super.getUTF8String(ordinal)
  }

  override def getBinary(ordinal: Int): Array[Byte] = {
    super.getBinary(ordinal)
  }

  override def getArray(ordinal: Int): ArrayData = {
    super.getArray(ordinal)
  }

  override def getInterval(ordinal: Int): CalendarInterval = {
    super.getInterval(ordinal)
  }

  override def getMap(ordinal: Int): MapData = {
    super.getMap(ordinal)
  }

  override def getStruct(ordinal: Int, numFields: Int): InternalRow = {
    super.getStruct(ordinal, numFields)
  }

  def getRow: InternalRow = this.row

  def getChangeAction: ChangeAction = changeAction

  override def toString: String = super.toString

  override def copy: GenericInternalRow = super.copy

  override def equals(o: Any): Boolean = super.equals(o)

  override def hashCode: Int = super.hashCode

  def setFileOffset(fileOffset: Long): InternalRow = {
    new GenericInternalRow((row.toSeq(schema) ++ Seq(fileOffset)).toArray)
  }
}
