package com.netease.arctic.optimizer.operator.executor;

import com.netease.arctic.ams.api.JobId;
import com.netease.arctic.ams.api.JobType;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.io.reader.GenericIcebergDataReader;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.scan.IcebergFileScanTask;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpecParser;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SchemaParser;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.expressions.ResidualEvaluator;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT;
import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT_DEFAULT;
import static org.apache.iceberg.expressions.Expressions.alwaysTrue;

public class NativeExecutor extends BaseExecutor<DataFile> {
  private static final Logger LOG = LoggerFactory.getLogger(NativeExecutor.class);

  private final NodeTask task;
  private final ArcticTable table;
  private final long startTime;
  private final OptimizerConfig config;
  private final Map<Integer, List<Integer>> eqDeleteFilesIndex;
  private final Map<Integer, List<Integer>> posDeleteFilesIndex;

  public NativeExecutor(NodeTask nodeTask,
                        ArcticTable table,
                        long startTime,
                        OptimizerConfig config) {
    this.task = nodeTask;
    this.table = table;
    this.startTime = startTime;
    this.config = config;
    this.eqDeleteFilesIndex = MapUtils.isEmpty(nodeTask.getEqDeleteFilesIndex()) ?
        new HashMap<>() : nodeTask.getEqDeleteFilesIndex();
    this.posDeleteFilesIndex = MapUtils.isEmpty(nodeTask.getPosDeleteFilesIndex()) ?
        new HashMap<>() : nodeTask.getPosDeleteFilesIndex();
  }

  @Override
  public OptimizeTaskResult<DataFile> execute() throws Exception {
    Iterable<DataFile> targetFiles;
    LOG.info("start process native optimize task: {}", task);

    List<DataFile> dataFiles = task.baseFiles();
    List<DeleteFile> posDeleteFiles = task.posDeleteFiles();
    List<DeleteFile> eqDeleteFiles = task.eqDeleteFiles();

    targetFiles = table.io().doAs(() -> {
      CloseableIterator<Record> recordIterator =
          openTask(dataFiles, eqDeleteFiles, posDeleteFiles, table.schema(), eqDeleteFilesIndex, posDeleteFilesIndex);
      return optimizeTable(recordIterator);
    });

    long totalFileSize = 0;
    List<ByteBuffer> baseFileBytesList = new ArrayList<>();
    for (DataFile baseFile : targetFiles) {
      totalFileSize += baseFile.fileSizeInBytes();
      baseFileBytesList.add(SerializationUtil.toByteBuffer(baseFile));
    }

    OptimizeTaskStat optimizeTaskStat = new OptimizeTaskStat();
    BeanUtils.copyProperties(optimizeTaskStat, task);
    JobId jobId = new JobId();
    jobId.setId(config.getOptimizerId());
    jobId.setType(JobType.Optimize);
    optimizeTaskStat.setJobId(jobId);
    optimizeTaskStat.setStatus(OptimizeStatus.Prepared);
    optimizeTaskStat.setAttemptId(task.getAttemptId() + "");
    optimizeTaskStat.setCostTime(System.currentTimeMillis() - startTime);
    optimizeTaskStat.setNewFileSize(totalFileSize);
    optimizeTaskStat.setReportTime(System.currentTimeMillis());
    optimizeTaskStat.setFiles(baseFileBytesList);
    optimizeTaskStat.setTableIdentifier(task.getTableIdentifier().buildTableIdentifier());
    optimizeTaskStat.setTaskId(task.getTaskId());

    OptimizeTaskResult<DataFile> result = new OptimizeTaskResult<>();
    result.setTargetFiles(targetFiles);
    result.setOptimizeTaskStat(optimizeTaskStat);

    return result;
  }

  @Override
  public void close() {

  }

  private Iterable<DataFile> optimizeTable(CloseableIterator<Record> recordIterator) throws Exception {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(table.schema(), table.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table.asUnkeyedTable(), table.spec().specId(), task.getAttemptId())
        .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(task.getPartition());

    String formatAsString = table.properties().getOrDefault(DEFAULT_FILE_FORMAT, DEFAULT_FILE_FORMAT_DEFAULT);
    long targetSizeByBytes = PropertyUtil.propertyAsLong(table.properties(),
        TableProperties.WRITE_TARGET_FILE_SIZE_BYTES, TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    List<DataFile> result = new ArrayList<>();
    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.valueOf(formatAsString.toUpperCase()), task.getPartition());

    long insertCount = 0;
    while (recordIterator.hasNext()) {
      if (writer.length() >= targetSizeByBytes) {
        writer.close();
        result.add(writer.toDataFile());
        EncryptedOutputFile newOutputFile = outputFileFactory.newOutputFile(task.getPartition());
        writer = appenderFactory
            .newDataWriter(newOutputFile, FileFormat.valueOf(formatAsString.toUpperCase()), task.getPartition());
      }
      Record baseRecord = recordIterator.next();

      writer.add(baseRecord);
      insertCount++;
      if (insertCount == 1 || insertCount == 100000) {
        LOG.info("task {} insert records number {} and data sampling {}",
            task.getTaskId(), insertCount, baseRecord);
      }
    }

    LOG.info("task {} insert records number {}", task.getTaskId(), insertCount);

    writer.close();
    result.add(writer.toDataFile());

    return result;
  }

  private CloseableIterator<Record> openTask(List<DataFile> dataFiles,
                                             List<DeleteFile> eqDeleteList,
                                             List<DeleteFile> posDeleteList,
                                             Schema requiredSchema,
                                             Map<Integer, List<Integer>> eqDeleteFilesIndex,
                                             Map<Integer, List<Integer>> posDeleteFilesIndex) {
    if (CollectionUtils.isEmpty(dataFiles)) {
      return CloseableIterator.empty();
    }
    
    List<IcebergFileScanTask> fileScanTasks = new ArrayList<>();
    for (int i = 0; i < dataFiles.size(); i++) {
      DataFile dataFile = dataFiles.get(i);
      List<DeleteFile> deleteFiles = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(eqDeleteFilesIndex.get(i))) {
        List<Integer> eqIndexes = eqDeleteFilesIndex.get(i);
        for (Integer eqIndex : eqIndexes) {
          deleteFiles.add(eqDeleteList.get(eqIndex));
        }
      }
      if (CollectionUtils.isNotEmpty(posDeleteFilesIndex.get(i))) {
        List<Integer> posIndexes = posDeleteFilesIndex.get(i);
        for (Integer posIndex : posIndexes) {
          deleteFiles.add(posDeleteList.get(posIndex));
        }
      }
      IcebergFileScanTask icebergFileScanTask = new IcebergFileScanTask(dataFile,
          deleteFiles.toArray(new DeleteFile[0]),
          SchemaParser.toJson(requiredSchema), PartitionSpecParser.toJson(table.spec()),
          ResidualEvaluator.of(table.spec(), alwaysTrue(), false));
      fileScanTasks.add(icebergFileScanTask);
    }

    GenericIcebergDataReader icebergDataReader =
        new GenericIcebergDataReader(table.io(), table.schema(), requiredSchema,
            table.properties().get(TableProperties.DEFAULT_NAME_MAPPING),
            false, IdentityPartitionConverters::convertConstant, false);
    CloseableIterable<Record> dataIterable = CloseableIterable.concat(CloseableIterable.transform(
        CloseableIterable.withNoopClose(fileScanTasks),
        icebergDataReader::readData));

    return dataIterable.iterator();
  }
}
