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

package com.netease.arctic.ams.server.utils;

import com.netease.arctic.table.KeyedTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeFilesUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ChangeFilesUtil.class);

  public static void tryClearChangeFiles(KeyedTable keyedTable, List<DataFile> changeFiles) {
    try {
      if (keyedTable.primaryKeySpec().primaryKeyExisted()) {
        int step = 3000;
        for (int startIndex = 0; startIndex < changeFiles.size(); startIndex += step) {
          int end = startIndex + step;
          List<DataFile> tableFiles = subList(changeFiles, startIndex, end);
          if (tableFiles.isEmpty()) {
            break;
          }
          LOG.info("{} delete {} change files", keyedTable.id(), tableFiles.size());
          deleteChangeFiles(keyedTable, tableFiles);
          LOG.info("{} change committed, delete {} files, complete {}/{}", keyedTable.id(),
              tableFiles.size(), Math.min(end, changeFiles.size()), changeFiles.size());
        }
      }
    } catch (Throwable t) {
      LOG.error(keyedTable.id() + " failed to delete change files, ignore", t);
    }
  }

  private static <T> List<T> subList(List<T> list, int from, int end) {
    List<T> subList = new ArrayList<>();
    for (int i = from; i < end; i++) {
      if (i >= list.size()) {
        break;
      }
      subList.add(list.get(i));
    }
    return subList;
  }

  private static void deleteChangeFiles(KeyedTable keyedTable, List<DataFile> changeFiles) {
    if (CollectionUtils.isEmpty(changeFiles)) {
      return;
    }
    DeleteFiles changeDelete = keyedTable.changeTable().newDelete();
    changeFiles.forEach(changeDelete::deleteFile);
    changeDelete.commit();
  }

  /**
   * Select all the files whose sequence <= maxSequence as Selected-Files, seek the maxSequence to find as many
   * Selected-Files as possible, and also
   * - the cnt of these Selected-Files must <= maxFileCntLimit
   * - the max TransactionId of the Selected-Files must > the min TransactionId of all the left files
   *
   * @param snapshotFileGroups snapshotFileGroups
   * @param maxFileCntLimit    maxFileCntLimit
   * @return the max sequence of selected file, return Long.MAX_VALUE if all files should be selected,
   * Long.MIN_VALUE means no files should be selected
   */
  public static long getMaxSequenceLimit(List<SnapshotFileGroup> snapshotFileGroups, long maxFileCntLimit) {
    if (maxFileCntLimit <= 0 || snapshotFileGroups == null || snapshotFileGroups.isEmpty()) {
      return Long.MIN_VALUE;
    }
    // 1.sort sequence
    Collections.sort(snapshotFileGroups);
    // 2.find the max index where all file cnt <= maxFileCntLimit
    int index = -1;
    int fileCnt = 0;
    for (int i = 0; i < snapshotFileGroups.size(); i++) {
      fileCnt += snapshotFileGroups.get(i).getFileCnt();
      if (fileCnt <= maxFileCntLimit) {
        index = i;
      } else {
        break;
      }
    }
    // all files cnt <= maxFileCntLimit, return all files
    if (fileCnt <= maxFileCntLimit) {
      return Long.MAX_VALUE;
    }
    // if only check the first file groups, then the file cnt > maxFileCntLimit, no files should be selected
    if (index == -1) {
      return Long.MIN_VALUE;
    }

    // 3.wrap file group with the max TransactionId before and min TransactionId after
    List<SnapshotFileGroupWrapper> snapshotFileGroupWrappers = wrapMinMaxTransactionId(snapshotFileGroups);
    // 4.find the valid snapshotFileGroup
    while (true) {
      SnapshotFileGroupWrapper current = snapshotFileGroupWrappers.get(index);
      // check transaction id inorder: max transaction id before(inclusive) < min transaction id after
      if (Math.max(current.getFileGroup().getTransactionId(), current.getMaxTransactionIdBefore()) <
          current.getMinTransactionIdAfter()) {
        return current.getFileGroup().getSequence();
      }
      index--;
      if (index == -1) {
        return Long.MIN_VALUE;
      }
    }
  }

  private static class SnapshotFileGroupWrapper {
    private final SnapshotFileGroup fileGroup;
    // in the ordered file group list, the max transaction before this file group, Long.MIN_VALUE for the first
    private long maxTransactionIdBefore;
    // in the ordered file group list, the min transaction after this file group, Long.MAX_VALUE for the last
    private long minTransactionIdAfter;

    public SnapshotFileGroupWrapper(SnapshotFileGroup fileGroup) {
      this.fileGroup = fileGroup;
    }

    public SnapshotFileGroup getFileGroup() {
      return fileGroup;
    }

    public long getMaxTransactionIdBefore() {
      return maxTransactionIdBefore;
    }

    public void setMaxTransactionIdBefore(long maxTransactionIdBefore) {
      this.maxTransactionIdBefore = maxTransactionIdBefore;
    }

    public long getMinTransactionIdAfter() {
      return minTransactionIdAfter;
    }

    public void setMinTransactionIdAfter(long minTransactionIdAfter) {
      this.minTransactionIdAfter = minTransactionIdAfter;
    }
  }

  private static List<SnapshotFileGroupWrapper> wrapMinMaxTransactionId(List<SnapshotFileGroup> snapshotFileGroups) {
    List<SnapshotFileGroupWrapper> wrappedList = new ArrayList<>();
    for (SnapshotFileGroup snapshotFileGroup : snapshotFileGroups) {
      wrappedList.add(new SnapshotFileGroupWrapper(snapshotFileGroup));
    }
    long maxValue = Long.MIN_VALUE;
    for (int i = 0; i < wrappedList.size(); i++) {
      SnapshotFileGroupWrapper wrapper = wrappedList.get(i);
      wrapper.setMaxTransactionIdBefore(maxValue);
      if (wrapper.getFileGroup().getTransactionId() > maxValue) {
        maxValue = wrapper.getFileGroup().getTransactionId();
      }
    }
    long minValue = Long.MAX_VALUE;
    for (int i = wrappedList.size() - 1; i >= 0; i--) {
      SnapshotFileGroupWrapper wrapper = wrappedList.get(i);
      wrapper.setMinTransactionIdAfter(minValue);
      if (wrapper.getFileGroup().getTransactionId() < minValue) {
        minValue = wrapper.getFileGroup().getTransactionId();
      }
    }
    return wrappedList;
  }

  /**
   * Files grouped by snapshot, but only with the file cnt.
   */
  public static class SnapshotFileGroup implements Comparable<SnapshotFileGroup> {
    private final long sequence;
    private final long transactionId;
    private int fileCnt = 0;

    public SnapshotFileGroup(long sequence, long transactionId) {
      this.sequence = sequence;
      this.transactionId = transactionId;
    }

    public SnapshotFileGroup(long sequence, long transactionId, int fileCnt) {
      this.sequence = sequence;
      this.transactionId = transactionId;
      this.fileCnt = fileCnt;
    }

    public void addFile() {
      fileCnt++;
    }

    public long getTransactionId() {
      return transactionId;
    }

    public int getFileCnt() {
      return fileCnt;
    }

    public long getSequence() {
      return sequence;
    }

    @Override
    public int compareTo(SnapshotFileGroup o) {
      return Long.compare(this.sequence, o.sequence);
    }
  }
}
