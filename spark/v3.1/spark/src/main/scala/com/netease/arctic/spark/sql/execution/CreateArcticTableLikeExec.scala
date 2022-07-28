package com.netease.arctic.spark.sql.execution

import com.netease.arctic.spark.{ArcticSparkCatalog, ArcticSparkTable}
import com.netease.arctic.table.KeyedTable
import org.apache.iceberg.spark.Spark3Util
import org.apache.iceberg.spark.Spark3Util.CatalogAndIdentifier
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.catalog.CatalogStorageFormat
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.catalyst.{InternalRow, TableIdentifier}
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2.V2CommandExec

import scala.collection.JavaConverters
import scala.collection.JavaConverters.seqAsJavaList


case class CreateArcticTableLikeExec(sparkSession: SparkSession,
                                     targetTable: TableIdentifier,
                                     sourceTable: TableIdentifier,
                                     fileFormat: CatalogStorageFormat,
                                     provider: Option[String],
                                     properties: Map[String, String] = Map.empty,
                                     ifNotExists: Boolean) extends V2CommandExec  {
  protected def run(): Seq[InternalRow] = {
    val sourceIdentifier = buildArcticIdentifier(sparkSession, sourceTable)
    val targetIdentifier = buildArcticIdentifier(sparkSession, targetTable)
    sourceIdentifier.catalog() match {
      case arcticCatalog: ArcticSparkCatalog =>
        val sourceTable = arcticCatalog.loadTable(sourceIdentifier.identifier())
        var targetProperties = properties
        targetProperties += ("provider" -> "arctic")
        sourceTable match {
          case keyedTable: ArcticSparkTable =>
            keyedTable.table() match {
              case table: KeyedTable =>
                targetProperties += ("primary.keys" -> String.join(",", table.primaryKeySpec().fieldNames()))
            }
          case _ =>
        }
        arcticCatalog.createTable(targetIdentifier.identifier(),
          sourceTable.schema(), sourceTable.partitioning(), JavaConverters.mapAsJavaMap(targetProperties))
      case _ =>
    }
    Seq.empty[InternalRow]
  }

  private def buildArcticIdentifier(sparkSession: SparkSession, originIdentifier: TableIdentifier): CatalogAndIdentifier = {
    var identifier: Seq[String] = Seq.empty[String]
    identifier:+= originIdentifier.database.get
    identifier:+= originIdentifier.table
    Spark3Util.catalogAndIdentifier(sparkSession, seqAsJavaList(identifier))
  }

  override def output: Seq[Attribute] = Nil

  override def children: Seq[SparkPlan] = Nil

}
