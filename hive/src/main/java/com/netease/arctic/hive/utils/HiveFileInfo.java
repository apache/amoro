package com.netease.arctic.hive.utils;

import org.apache.hadoop.fs.FileStatus;

/**
 * Created by zyx on 2021/7/11.
 */
public class HiveFileInfo {
  private String path;
  private long length;

  public static HiveFileInfo of(FileStatus fileStatus) {
    return new HiveFileInfo(fileStatus.getPath().toString(), fileStatus.getLen());
  }

  public HiveFileInfo(String path, long length) {
    this.path = path;
    this.length = length;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  @Override
  public String toString() {
    return "HiveFile[length=" + length + "] " + path + " ";
  }
}
