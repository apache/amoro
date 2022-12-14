package com.netease.arctic.spark.sql.execution

import com.netease.arctic.hive.table.{SupportHive, UnkeyedHiveTable}
import com.netease.arctic.spark.ArcticSparkCatalog
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.table.ArcticTable
import org.apache.commons.collections.CollectionUtils
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.{PartitionSpec, UnresolvedPartitionSpec}
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.{Table, TableCatalog}
import org.apache.spark.sql.execution.datasources.v2.V2CommandExec

import java.util.stream.Collectors
import scala.collection.JavaConverters.seqAsJavaListConverter

case class AlterArcticTableDropPartitionExec(table: Table,
                                             parts: Seq[PartitionSpec],
                                             retainData: Boolean) extends V2CommandExec {
  override protected def run(): Seq[InternalRow] = {
    table match {
      case arctic: ArcticSparkTable =>
        val specs = parts.map {
          case part: UnresolvedPartitionSpec =>
            part
        }
        val ident = specs.map(s => s.spec.values).map(s => s.head).asJava
        arctic.dropPartition(ident)
    }
    Nil
  }

  override def output: Seq[Attribute] = Nil
}
