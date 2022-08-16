package com.netease.arctic.hive.write;

import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.io.writer.TaskWriterKey;
import org.apache.iceberg.encryption.EncryptedOutputFile;

public class AdaptHiveOutputFileFactory implements OutputFileFactory {

  @Override
  public EncryptedOutputFile newOutputFile(TaskWriterKey key) {
    return null;
  }
}
