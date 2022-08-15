package com.netease.arctic.io.writer;

import org.apache.iceberg.encryption.EncryptedOutputFile;

public interface OutputFileFactory {

  EncryptedOutputFile newOutputFile(TaskWriterKey key);

}
