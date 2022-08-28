/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.hive.utils;

import org.apache.hadoop.fs.FileStatus;

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
