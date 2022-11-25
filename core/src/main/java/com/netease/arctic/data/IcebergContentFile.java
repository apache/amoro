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

package com.netease.arctic.data;

import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.io.Serializable;

public class IcebergContentFile implements Serializable {
  private static final long serialVersionUID = 1L;

  private ContentFile<?> contentFile;
  private Long sequenceNumber;

  public static IcebergContentFile of(ContentFile<?> contentFile, long sequenceNumber) {
    return new IcebergContentFile(contentFile, sequenceNumber);
  }

  public IcebergContentFile() {

  }

  public IcebergContentFile(ContentFile<?> contentFile, Long sequenceNumber) {
    this.contentFile = contentFile;
    this.sequenceNumber = sequenceNumber;
  }

  public ContentFile<?> getContentFile() {
    return contentFile;
  }

  public void setContentFile(ContentFile<?> contentFile) {
    this.contentFile = contentFile;
  }

  public Long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public FileContent content() {
    return contentFile.content();
  }

  public boolean isDataFile() {
    return content() == FileContent.DATA;
  }

  public boolean isDeleteFile() {
    return !isDataFile();
  }

  public DataFile asDataFile() {
    Preconditions.checkArgument(isDataFile(), "Not a data file");
    return (DataFile) contentFile;
  }

  public DeleteFile asDeleteFile() {
    Preconditions.checkArgument(isDeleteFile(), "Not a delete file");
    return (DeleteFile) contentFile;
  }

  @Override
  public String toString() {
    return "IcebergContentFile{" +
        "contentFile=" + contentFile +
        ", sequenceNumber=" + sequenceNumber +
        '}';
  }
}
