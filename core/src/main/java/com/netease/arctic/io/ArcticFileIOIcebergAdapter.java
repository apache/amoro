package com.netease.arctic.io;

import org.apache.hadoop.fs.FileStatus;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.io.SupportsPrefixOperations;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ArcticFileIOIcebergAdapter implements ArcticFileIO {

  private final FileIO io;

  public ArcticFileIOIcebergAdapter(FileIO io) {
    this.io = io;
  }

  @Override
  public <T> T doAs(Callable<T> callable) {
    if (io instanceof ArcticFileIO) {
      return ((ArcticFileIO) io).doAs(callable);
    }
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean exists(String path) {
    if (io instanceof ArcticFileIO) {
      return ((ArcticFileIO) io).exists(path);
    }
    return ArcticFileIO.super.exists(path);
  }

  @Override
  public List<FileStatus> list(String location) {
    return null;
  }

  @Override
  public boolean supportPrefixOperations() {
    return io instanceof SupportsPrefixOperations;
  }

  @Override
  public SupportsPrefixOperations asPrefixFileIO() {
    Preconditions.checkArgument(supportPrefixOperations());
    return (SupportsPrefixOperations) io;
  }

  @Override
  public boolean supportFileSystemOperations() {
    return io instanceof ArcticFileIO && ((ArcticFileIO) io).supportFileSystemOperations();
  }

  @Override
  public SupportsFileSystemOperations asFileSystemIO() {
    Preconditions.checkArgument(this.supportFileSystemOperations());
    return ((ArcticFileIO) io).asFileSystemIO();
  }

  @Override
  public boolean supportsFileRecycle() {
    return io instanceof ArcticFileIO && ((ArcticFileIO) io).supportsFileRecycle();
  }

  @Override
  public SupportFileRecycleOperations asFileRecycleIO() {
    Preconditions.checkArgument(this.supportsFileRecycle());
    return ((ArcticFileIO) io).asFileRecycleIO();
  }

  @Override
  public InputFile newInputFile(String path) {
    return io.newInputFile(path);
  }

  @Override
  public InputFile newInputFile(String path, long length) {
    return io.newInputFile(path, length);
  }

  @Override
  public OutputFile newOutputFile(String path) {
    return io.newOutputFile(path);
  }

  @Override
  public void deleteFile(String path) {
    io.deleteFile(path);
  }

  @Override
  public void deleteFile(InputFile file) {
    io.deleteFile(file);
  }

  @Override
  public void deleteFile(OutputFile file) {
    io.deleteFile(file);
  }

  @Override
  public Map<String, String> properties() {
    return io.properties();
  }

  @Override
  public void initialize(Map<String, String> properties) {
    io.initialize(properties);
  }

  @Override
  public void close() {
    io.close();
  }
}
