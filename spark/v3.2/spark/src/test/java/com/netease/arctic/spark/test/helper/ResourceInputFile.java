package com.netease.arctic.spark.test.helper;

import org.apache.iceberg.io.ByteBufferInputStream;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.SeekableInputStream;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ResourceInputFile implements InputFile {

  public static ResourceInputFile newFile(ClassLoader loader, String resource) {
    List<ByteBuffer> bf = Lists.newArrayList();
    int size = 0;
    URL url = loader.getResource(resource);
    Preconditions.checkNotNull(url);
    try (InputStream in = loader.getResourceAsStream(resource)) {
      Preconditions.checkNotNull(in);

      byte[] buff = new byte[1024];
      int nRead = 0;
      while ((nRead = in.read(buff)) > 0) {
        size += nRead;
        byte[] readerBytes = Arrays.copyOfRange(buff, 0, nRead);
        bf.add(ByteBuffer.wrap(readerBytes));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new ResourceInputFile(bf, size, url.getFile());
  }

  private final List<ByteBuffer> bf;
  private final long length;
  private final String location;

  protected ResourceInputFile(List<ByteBuffer> bf, int length, String location){
    this.length = length;
    this.bf = bf;
    this.location = location;
  }

  @Override
  public long getLength() {
    return this.length;
  }

  @Override
  public SeekableInputStream newStream() {
    return ByteBufferInputStream.wrap(bf);
  }

  @Override
  public String location() {
    return location;
  }

  @Override
  public boolean exists() {
    return true;
  }
}
