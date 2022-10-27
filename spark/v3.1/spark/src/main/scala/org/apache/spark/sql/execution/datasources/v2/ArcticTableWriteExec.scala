package org.apache.spark.sql.execution.datasources.v2

import com.netease.arctic.spark.table.ArcticSparkTable
import org.apache.spark.executor.CommitDeniedException
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.write.{BatchWrite, DataWriterFactory, PhysicalWriteInfoImpl, WriterCommitMessage}
import org.apache.spark.sql.execution.{SparkPlan, UnaryExecNode}
import org.apache.spark.util.{LongAccumulator, Utils}
import org.apache.spark.{SparkEnv, SparkException, TaskContext}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.util.control.NonFatal

trait ArcticTableWriteExec extends V2CommandExec with UnaryExecNode{
  def table: ArcticSparkTable
  def queryInsert: SparkPlan
  def validate: Boolean

  var count: Long = 0L



  override def child: SparkPlan = queryInsert

  override def output: Seq[Attribute] = Nil

  var commitProgress: Option[StreamWriterCommitProgress] = None

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
    val writerFactory = batchWrite.createBatchWriterFactory(
      PhysicalWriteInfoImpl(rdd.getNumPartitions))
    val useCommitCoordinator = batchWrite.useCommitCoordinator
    val messages = new Array[WriterCommitMessage](rdd.partitions.length)
    val totalNumRowsAccumulator = new LongAccumulator()
    var rows: List[InternalRow] = List.empty

    logInfo(s"Start processing data source write support: $batchWrite. " +
      s"The input RDD has ${messages.length} partitions.")

    try {
      sparkContext.runJob(
        rdd,
        (context: TaskContext, iter: Iterator[InternalRow]) => {
          DataWritingArcticSparkTask.run(writerFactory, context, iter, useCommitCoordinator)
        },
        rdd.partitions.indices,
        (index, result: DataWritingSparkTaskResult) => {
          val commitMessage = result.writerCommitMessage
          messages(index) = commitMessage
          rows = rows ++ result.rows
          totalNumRowsAccumulator.add(result.numRows)
          batchWrite.onDataWriterCommit(commitMessage)
        }
      )

      if (validate) {
        val pkIds = table.table().asKeyedTable().primaryKeySpec().fields().
          map(f =>
            (table.table().schema().findField(f.fieldName()).fieldId(), table.table().schema().findField(f.fieldName()).`type`()))
        val count = rows.map(f => {
          var s: List[String] = List.empty
          pkIds.foreach(p => {
            s = s :+ f.getString(p._1 - 1)
            s
          })
          s
        }).distinct.length
        if (rdd.count() != count) {
          throw new UnsupportedOperationException(s"primary key can not be duplicate")
        }
      }

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

}


object DataWritingArcticSparkTask extends Logging {
  def run(
           writerFactory: DataWriterFactory,
           context: TaskContext,
           iter: Iterator[InternalRow],
           useCommitCoordinator: Boolean,
         ): DataWritingSparkTaskResult = {
    val stageId = context.stageId()
    val stageAttempt = context.stageAttemptNumber()
    val partId = context.partitionId()
    val taskId = context.taskAttemptId()
    val attemptId = context.attemptNumber()
    val dataWriter = writerFactory.createWriter(partId, taskId)
    var count = 0L
    var insertRows: List[InternalRow] = List.empty
    var row: InternalRow = null

    // write the data and commit this writer.
    Utils.tryWithSafeFinallyAndFailureCallbacks(block = {

      while (iter.hasNext) {
        // Count is here.
        count += 1
        row = iter.next().copy()
        insertRows = insertRows :+ row
        dataWriter.write(row)
      }
      val msg = if (useCommitCoordinator) {
        val coordinator = SparkEnv.get.outputCommitCoordinator
        val commitAuthorized = coordinator.canCommit(stageId, stageAttempt, partId, attemptId)
        if (commitAuthorized) {
          logInfo(s"Commit authorized for partition $partId (task $taskId, attempt $attemptId, " +
            s"stage $stageId.$stageAttempt)")
          dataWriter.commit()
        } else {
          val message = s"Commit denied for partition $partId (task $taskId, attempt $attemptId, " +
            s"stage $stageId.$stageAttempt)"
          logInfo(message)
          // throwing CommitDeniedException will trigger the catch block for abort
          throw new CommitDeniedException(message, stageId, partId, attemptId)
        }

      } else {
        logInfo(s"Writer for partition ${context.partitionId()} is committing.")
        dataWriter.commit()
      }

      logInfo(s"Committed partition $partId (task $taskId, attempt $attemptId, " +
        s"stage $stageId.$stageAttempt)")

      DataWritingSparkTaskResult(count, msg, insertRows)

    })(catchBlock = {
      // If there is an error, abort this writer
      logError(s"Aborting commit for partition $partId (task $taskId, attempt $attemptId, " +
        s"stage $stageId.$stageAttempt)")
      dataWriter.abort()
      logError(s"Aborted commit for partition $partId (task $taskId, attempt $attemptId, " +
        s"stage $stageId.$stageAttempt)")
    }, finallyBlock = {
      dataWriter.close()
    })

  }
}

private[v2] case class DataWritingSparkTaskResult(
                                                   numRows: Long,
                                                   writerCommitMessage: WriterCommitMessage,
                                                   rows: List[InternalRow])
