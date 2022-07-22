package com.netease.arctic.spark.sql.execution

import com.netease.arctic.spark.ArcticSparkCatalog
import org.apache.iceberg.spark.Spark3Util
import org.apache.iceberg.spark.Spark3Util.CatalogAndIdentifier
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.catalog.CatalogStorageFormat
import org.apache.spark.sql.execution.command.RunnableCommand
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.JavaConverters
import scala.collection.JavaConverters.seqAsJavaList


case class CreateArcticTableLikeExec(targetTable: TableIdentifier,
                                     sourceTable: TableIdentifier,
                                     fileFormat: CatalogStorageFormat,
                                     provider: Option[String],
                                     properties: Map[String, String] = Map.empty,
                                     ifNotExists: Boolean) extends RunnableCommand {
  override def run(sparkSession: SparkSession): Seq[Row] = {
    val sourceIdentifier = buildArcticIdentifier(sparkSession, sourceTable)
    val targetIdentifier = buildArcticIdentifier(sparkSession, targetTable)
    sourceIdentifier.catalog() match {
      case arcticCatalog: ArcticSparkCatalog =>
        val sourceTable = arcticCatalog.loadTable(sourceIdentifier.identifier())
        var targetProperties = properties
        targetProperties += ("provider" -> "arctic")
        if (sourceTable.properties().containsKey("primary.keys"))
          targetProperties += ("primary.keys" -> sourceTable.properties().get("primary.keys"))
        arcticCatalog.createTable(targetIdentifier.identifier(),
          sourceTable.schema(), sourceTable.partitioning(), JavaConverters.mapAsJavaMap(targetProperties))
    }
    Seq.empty[Row]
  }

  private def buildArcticIdentifier(sparkSession: SparkSession, originIdentifier: TableIdentifier): CatalogAndIdentifier = {
    var identifier: Seq[String] = Seq.empty[String]
    identifier:+= originIdentifier.database.get
    identifier:+= originIdentifier.table
    Spark3Util.catalogAndIdentifier(sparkSession, seqAsJavaList(identifier))
  }
}
