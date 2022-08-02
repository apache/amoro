package com.netease.arctic.spark.sql.catalyst.analysis

import com.netease.arctic.spark.source.ArcticSource
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.catalyst.catalog.{CatalogTable, HiveTableRelation}
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.catalyst.plans.logical.{InsertIntoTable, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.LogicalRelation
import org.apache.spark.sql.execution.datasources.v2.{DataSourceV2Relation, WriteToDataSourceV2}
import org.apache.spark.sql.hive.execution.InsertIntoHiveTable
import org.apache.spark.sql.sources.v2.reader.DataSourceReader
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter
import org.apache.spark.sql.types.StructType

case class ArcticResolutionDelegateHiveRule(spark: SparkSession) extends Rule[LogicalPlan] {

  lazy val arctic = new ArcticSource

  override def apply(plan: LogicalPlan): LogicalPlan = plan transformDown {

    // insert into data source table
    case i @ InsertIntoTable(l: LogicalRelation, partition, query, overwrite, ifPartitionNotExists)
      if l.catalogTable.isDefined && arctic.isDelegateTable(l.catalogTable.get) =>
      val writer = createArcticWriter(l.catalogTable.get, query.schema)
      WriteToDataSourceV2(writer, query)

    // insert into hive table
    case i @ InsertIntoTable(table: HiveTableRelation, partition, query, overwrite, ifPartitionNotExists)
      if arctic.isDelegateTable(table.tableMeta) =>
      val writer = createArcticWriter(table.tableMeta, query.schema)
      WriteToDataSourceV2(writer, query)

    // scan datasource table
    case l @ LogicalRelation(_,_,table,_) if table.isDefined && arctic.isDelegateTable(table.get) =>
      val reader = createArcticReader(table.get)
      val output = reader.readSchema()
        .map(f => AttributeReference(f.name, f.dataType, f.nullable, f.metadata)() )
      DataSourceV2Relation(output, reader)

    // scan hive table
    case h @ HiveTableRelation(tableMeta, dataCols, partitionCols)
      if arctic.isDelegateTable(tableMeta) =>
      val reader = createArcticReader(tableMeta)
      val output = reader.readSchema()
        .map(f => AttributeReference(f.name, f.dataType, f.nullable, f.metadata)() )
      DataSourceV2Relation(output, reader)
  }

  def createArcticWriter(table: CatalogTable, schema: StructType) : DataSourceWriter = {
    val arcticTable = arctic.loadTable(table.identifier)
    val writer = arcticTable.createOverwriteWriter(schema)
    writer
  }

  def createArcticReader(table: CatalogTable): DataSourceReader = {
    val arcticTable = arctic.loadTable(table.identifier)
    val reader = arcticTable.createReader(null)
    reader
  }
}