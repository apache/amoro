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

package org.apache.spark.sql.catalyst.expressions

import org.apache.iceberg.spark.SparkSchemaUtil
import org.apache.iceberg.transforms.Transforms
import org.apache.iceberg.types.{Type, Types}
import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.catalyst.SQLConfHelper
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.connector.catalog.CatalogV2Implicits
import org.apache.spark.sql.connector.expressions.{BucketTransform, DaysTransform, FieldReference, HoursTransform, IdentityTransform, MonthsTransform, NamedReference, Transform, YearsTransform, Expression => V2Expression}
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

import java.nio.ByteBuffer

object ArcticExpressionUtils extends SQLConfHelper{
  val resolver = conf.resolver
  def toCatalyst(expr: V2Expression, query: LogicalPlan): Expression =  {
    def resolve(parts: Seq[String]): NamedExpression = {
      query.resolve(parts, resolver) match {
        case Some(attr) =>
          attr
        case None =>
          val ref = parts.map(CatalogV2Implicits.quoteIfNeeded).mkString(".")
          throw new AnalysisException(s"Cannot resolve '$ref' using ${query.output}")
      }
    }

    expr match {
      case it: IdentityTransform =>
        resolve(it.ref.fieldNames)
      case ArcticBucketTransform(n, r) =>
        IcebergBucketTransform(n, resolveRef[NamedExpression](r, query))
      case yt: YearsTransform =>
        IcebergYearTransform(resolve(yt.ref.fieldNames))
      case mt: MonthsTransform =>
        IcebergMonthTransform(resolve(mt.ref.fieldNames))
      case dt: DaysTransform =>
        IcebergDayTransform(resolve(dt.ref.fieldNames))
      case ht: HoursTransform =>
        IcebergHourTransform(resolve(ht.ref.fieldNames))
      case ref: FieldReference =>
        resolve(ref.fieldNames)
      case _ =>
        throw new RuntimeException(s"$expr is not currently supported")
    }
  }

  import org.apache.spark.sql.connector.catalog.CatalogV2Implicits.MultipartIdentifierHelper
  def resolveRef[T <: NamedExpression](ref: NamedReference, plan: LogicalPlan): T = {
    plan.resolve(ref.fieldNames.toSeq, conf.resolver) match {
      case Some(namedExpr) =>
        namedExpr.asInstanceOf[T]
      case None =>
        val name = ref.fieldNames.toSeq.quoted
        val outputString = plan.output.map(_.name).mkString(",")
        throw new AnalysisException(s"Cannot resolve '$ref' using ${name}")
    }
  }

  private object ArcticBucketTransform {
    def unapply(transform: Transform): Option[(Int, FieldReference)] = transform match {
      case bt: BucketTransform => bt.columns match {
        case Seq(nf: NamedReference) =>
          Some(bt.numBuckets.value(), FieldReference(nf.fieldNames()))
        case _ =>
          None
      }
      case _ => None
    }
  }

}



abstract class ArcticTransformExpression
  extends UnaryExpression with CodegenFallback with NullIntolerant {

  @transient lazy val icebergInputType: Type = SparkSchemaUtil.convert(child.dataType)

  protected def withNewChildInternal(newChild: Expression): Expression
}


case class ArcticBucketTransform(numBuckets: Int, child: Expression)  extends ArcticTransformExpression {

  @transient lazy val bucketFunc: Any => Int = child.dataType match {
    case _: DecimalType =>
      val t = Transforms.bucket[Any](icebergInputType, numBuckets)
      d: Any => t(d.asInstanceOf[Decimal].toJavaBigDecimal).toInt
    case _: StringType =>
      // the spec requires that the hash of a string is equal to the hash of its UTF-8 encoded bytes
      // TODO: pass bytes without the copy out of the InternalRow
      val t = Transforms.bucket[ByteBuffer](Types.BinaryType.get(), numBuckets)
      s: Any => t(ByteBuffer.wrap(s.asInstanceOf[UTF8String].getBytes)).toInt
    case _: IntegerType =>
      s: Any => s.asInstanceOf[Int] % numBuckets
    case _ =>
      val t = Transforms.bucket[Any](icebergInputType, numBuckets)
      a: Any => t(a).toInt
  }

  override protected def nullSafeEval(value: Any): Any = {
    val a = bucketFunc(value)
    a
  }

  override def dataType: DataType = IntegerType

  override protected def withNewChildInternal(newChild: Expression): Expression = {
    copy(child = newChild)
  }
}
