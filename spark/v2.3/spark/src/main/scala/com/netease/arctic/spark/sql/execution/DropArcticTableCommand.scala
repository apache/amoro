package com.netease.arctic.spark.sql.execution

import com.netease.arctic.spark.source.ArcticSource
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.execution.command.RunnableCommand

case class DropArcticTableCommand(arctic: ArcticSource, tableIdentifier: TableIdentifier, ignoreIfExists: Boolean, purge: Boolean)
  extends RunnableCommand {
  override def run(sparkSession: SparkSession): Seq[Row] = {
    arctic.dropTable(tableIdentifier, purge)
    Seq.empty[Row]
  }
}
