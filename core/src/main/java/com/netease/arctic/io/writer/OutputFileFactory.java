package com.netease.arctic.io.writer;

import org.apache.iceberg.encryption.EncryptedOutputFile;

/**
 * Decide which file the data is written to
 */
public interface OutputFileFactory {

  EncryptedOutputFile newOutputFile(TaskWriterKey key);

}
