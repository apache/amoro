package com.netease.arctic.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtil {

  public static byte[] gzip(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream zipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
      zipOutputStream.write(bytes);
      zipOutputStream.finish();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return byteArrayOutputStream.toByteArray();
  }

  public static byte[] unGzip(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)){
      while ((len = gzipInputStream.read(buffer)) > 0) {
        byteArrayOutputStream.write(buffer, 0, len);
      }
    }catch (IOException e){
      throw new RuntimeException(e);
    }
    return byteArrayOutputStream.toByteArray();
  }
}
