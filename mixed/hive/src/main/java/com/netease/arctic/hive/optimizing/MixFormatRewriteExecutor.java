package com.netease.arctic.hive.optimizing;

import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.io.writer.ArcticTreeNodePosDeleteWriter;
import com.netease.arctic.optimizing.AbstractRewriteFilesExecutor;
import com.netease.arctic.optimizing.OptimizingDataReader;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.WriteOperationKind;
import com.netease.arctic.utils.map.StructLikeCollections;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.io.DeleteWriteResult;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileWriter;
import org.apache.iceberg.io.TaskWriter;

import java.util.List;

/** OptimizingExecutor form mixed format */
public class MixFormatRewriteExecutor extends AbstractRewriteFilesExecutor {

  private final String outputDir;

  public MixFormatRewriteExecutor(
      RewriteFilesInput input,
      ArcticTable table,
      StructLikeCollections structLikeCollections,
      String outputDir) {
    super(input, table, structLikeCollections);
    this.outputDir = outputDir;
  }

  @Override
  protected OptimizingDataReader dataReader() {
    return new MixFormatOptimizingDataReader(table, structLikeCollections, input);
  }

  @Override
  protected FileWriter<PositionDelete<Record>, DeleteWriteResult> posWriter() {
    FileAppenderFactory<Record> appenderFactory = fullMetricAppenderFactory(table.spec());
    return new ArcticTreeNodePosDeleteWriter<>(
        appenderFactory,
        deleteFileFormat(),
        partition(),
        io,
        encryptionManager(),
        getTransactionId(input.rePosDeletedDataFilesForMixed()),
        baseLocation(),
        table.spec());
  }

  @Override
  protected TaskWriter<Record> dataWriter() {
    return AdaptHiveGenericTaskWriterBuilder.builderFor(table)
        .withTransactionId(
            table.isKeyedTable() ? getTransactionId(input.rewrittenDataFilesForMixed()) : null)
        .withTaskId(0)
        .withCustomHiveSubdirectory(outputDir)
        .withTargetFileSize(targetSize())
        .buildWriter(
            StringUtils.isBlank(outputDir)
                ? WriteOperationKind.MAJOR_OPTIMIZE
                : WriteOperationKind.FULL_OPTIMIZE);
  }

  public long getTransactionId(List<PrimaryKeyedFile> dataFiles) {
    return dataFiles.stream().mapToLong(PrimaryKeyedFile::transactionId).max().orElse(0L);
  }

  public String baseLocation() {
    if (table.isKeyedTable()) {
      return table.asKeyedTable().baseTable().location();
    } else {
      return table.asUnkeyedTable().location();
    }
  }
}
