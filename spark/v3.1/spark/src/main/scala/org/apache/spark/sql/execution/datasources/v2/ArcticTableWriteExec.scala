package org.apache.spark.sql.execution.datasources.v2

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.{Identifier, Table, TableCatalog}
import org.apache.spark.sql.connector.write.{BatchWrite, PhysicalWriteInfoImpl, WriterCommitMessage}
import org.apache.spark.sql.execution.{BinaryExecNode, SparkPlan}
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkException, TaskContext}

import scala.util.control.NonFatal

trait ArcticTableWriteExec extends V2CommandExec with BinaryExecNode{
  def queryInsert: SparkPlan
  def queryValidate: SparkPlan

  var count: Long = 0L

  var commitProgress: Option[StreamWriterCommitProgress] = None

  override def output: Seq[Attribute] = Nil

  protected def validateData(): Seq[InternalRow] = {
    val rdd: RDD[InternalRow] = {
      val tempRdd = queryValidate.execute()
      // SPARK-23271 If we are attempting to write a zero partition rdd, create a dummy single
      // partition rdd to make sure we at least set up one write task to write the metadata.
      if (tempRdd.partitions.length == 0) {
        sparkContext.parallelize(Array.empty[InternalRow], 1)
      } else {
        tempRdd
      }
    }
    count = rdd.count()
    print(rdd.count())
    Nil
  }

  protected def writeInsert(batchWrite: BatchWrite): Seq[InternalRow] = {
    val rdd: RDD[InternalRow] = {
      val tempRdd = queryInsert.execute()
      // SPARK-23271 If we are attempting to write a zero partition rdd, create a dummy single
      // partition rdd to make sure we at least set up one write task to write the metadata.
      if (tempRdd.partitions.length == 0) {
        sparkContext.parallelize(Array.empty[InternalRow], 1)
      } else {
        tempRdd
      }
    }
    if (rdd.count() != count) {
      throw new UnsupportedOperationException(s"primary key can not be duplicate")
    }
    print(rdd.count())
    val writerFactory = batchWrite.createBatchWriterFactory(
      PhysicalWriteInfoImpl(rdd.getNumPartitions))
    val useCommitCoordinator = batchWrite.useCommitCoordinator
    val messages = new Array[WriterCommitMessage](rdd.partitions.length)
    val totalNumRowsAccumulator = new LongAccumulator()

    logInfo(s"Start processing data source write support: $batchWrite. " +
      s"The input RDD has ${messages.length} partitions.")

    try {
      sparkContext.runJob(
        rdd,
        (context: TaskContext, iter: Iterator[InternalRow]) =>
          DataWritingSparkTask.run(writerFactory, context, iter, useCommitCoordinator),
        rdd.partitions.indices,
        (index, result: DataWritingSparkTaskResult) => {
          val commitMessage = result.writerCommitMessage
          messages(index) = commitMessage
          totalNumRowsAccumulator.add(result.numRows)
          batchWrite.onDataWriterCommit(commitMessage)
        }
      )

      logInfo(s"Data source write support $batchWrite is committing.")
      batchWrite.commit(messages)
      logInfo(s"Data source write support $batchWrite committed.")
      commitProgress = Some(StreamWriterCommitProgress(totalNumRowsAccumulator.value))
    } catch {
      case cause: Throwable =>
        logError(s"Data source write support $batchWrite is aborting.")
        try {
          batchWrite.abort(messages)
        } catch {
          case t: Throwable =>
            logError(s"Data source write support $batchWrite failed to abort.")
            cause.addSuppressed(t)
            throw new SparkException("Writing job failed.", cause)
        }
        logError(s"Data source write support $batchWrite aborted.")
        cause match {
          // Only wrap non fatal exceptions.
          case NonFatal(e) => throw new SparkException("Writing job aborted.", e)
          case _ => throw cause
        }
    }

    Nil
  }

  protected def uncacheTable(
                              session: SparkSession,
                              catalog: TableCatalog,
                              table: Table,
                              ident: Identifier): Unit = {
    val plan = DataSourceV2Relation.create(table, Some(catalog), Some(ident))
    session.sharedState.cacheManager.uncacheQuery(session, plan, cascade = true)
  }

}
