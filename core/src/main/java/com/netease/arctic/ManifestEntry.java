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

package com.netease.arctic;

import org.apache.iceberg.ContentFile;

public class ManifestEntry {
  public enum Status {
    EXISTING(0),
    ADDED(1),
    DELETED(2);

    private final int id;

    Status(int id) {
      this.id = id;
    }

    public int id() {
      return id;
    }

    public static Status of(int id) {
      for (Status status : Status.values()) {
        if (status.id() == id) {
          return status;
        }
      }
      throw new IllegalArgumentException("not support status id " + id);

    }
  }
  
  private int contentId;
  private Status status;
  private Long snapshotId;
  private long sequenceNumber;
  private ContentFile<?> file;

  public ManifestEntry(int contentId, Status status, Long snapshotId, long sequenceNumber,
                       ContentFile<?> file) {
    this.contentId = contentId;
    this.status = status;
    this.snapshotId = snapshotId;
    this.sequenceNumber = sequenceNumber;
    this.file = file;
  }

  public int getContentId() {
    return contentId;
  }

  public void setContentId(int contentId) {
    this.contentId = contentId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Long getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(Long snapshotId) {
    this.snapshotId = snapshotId;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public ContentFile<?> getFile() {
    return file;
  }

  public void setFile(ContentFile<?> file) {
    this.file = file;
  }
  
}
