package com.netease.arctic.spark.sql.execution

import com.netease.arctic.hive.table.{SupportHive, UnkeyedHiveTable}
import com.netease.arctic.spark.ArcticSparkCatalog
import com.netease.arctic.spark.table.{ArcticIcebergSparkTable, ArcticSparkTable, SupportDropPartitions}
import com.netease.arctic.table.{ArcticTable, BaseUnkeyedTable, UnkeyedTable}
import org.apache.commons.collections.CollectionUtils
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.{PartitionSpec, UnresolvedPartitionSpec}
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.{SupportsAtomicPartitionManagement, Table, TableCatalog}
import org.apache.spark.sql.execution.datasources.v2.V2CommandExec

import java.util.stream.Collectors
import scala.collection.JavaConverters.{asJavaIterableConverter, seqAsJavaListConverter}

case class AlterArcticTableDropPartitionExec(table: Table,
                                             parts: Seq[PartitionSpec],
                                             retainData: Boolean) extends V2CommandExec {
  override protected def run(): Seq[InternalRow] = {
    table match {
      case table: SupportDropPartitions =>
        val specs = parts.map {
          case part: UnresolvedPartitionSpec =>
            part.spec.map(s => s._1 + "=" + s._2).asJava
        }
        val ident = specs.mkString.replace(",", "/")
          .replace("[", "")
          .replace("]", "")
          .replace(" ", "")
        table.dropPartitions(ident)
      case _ =>
        throw new UnsupportedOperationException(
          s"table ${table.name()} can not drop multiple partitions.")
    }
    Nil
  }

  override def output: Seq[Attribute] = Nil
}
