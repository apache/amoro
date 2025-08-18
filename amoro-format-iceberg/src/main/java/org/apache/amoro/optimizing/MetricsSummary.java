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

package org.apache.amoro.optimizing;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.amoro.table.descriptor.FilesStatistics;
import org.apache.amoro.table.descriptor.FilesStatisticsBuilder;
import org.apache.amoro.utils.CommonUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileContent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsSummary {
  public static final String INPUT_DATA_FILES = "input-data-files(rewrite)";
  public static final String INPUT_DATA_SIZE = "input-data-size(rewrite)";
  public static final String INPUT_DATA_RECORDS = "input-data-records(rewrite)";
  public static final String INPUT_READ_ONLY_DATA_FILES = "input-data-files(read-only)";
  public static final String INPUT_READ_ONLY_DATA_SIZE = "input-data-size(read-only)";
  public static final String INPUT_READ_ONLY_DATA_RECORDS = "input-data-records(read-only)";
  public static final String INPUT_EQ_DELETE_FILES = "input-equality-delete-files";
  public static final String INPUT_EQ_DELETE_SIZE = "input-equality-delete-size";
  public static final String INPUT_EQ_DELETE_RECORDS = "input-equality-delete-records";
  public static final String INPUT_POS_DELETE_FILES = "input-position-delete-files";
  public static final String INPUT_POS_DELETE_SIZE = "input-position-delete-size";
  public static final String INPUT_POS_DELETE_RECORDS = "input-position-delete-records";
  public static final String OUTPUT_DATA_FILES = "output-data-files";
  public static final String OUTPUT_DATA_SIZE = "output-data-size";
  public static final String OUTPUT_DATA_RECORDS = "output-data-records";
  public static final String OUTPUT_DELETE_FILES = "output-delete-files";
  public static final String OUTPUT_DELETE_SIZE = "output-delete-size";
  public static final String OUTPUT_DELETE_RECORDS = "output-delete-records";
  private long rewriteDataSize = 0;
  private int rewriteDataFileCnt = 0;
  private long rewriteDataRecordCnt = 0;
  private long rewritePosDataSize = 0;
  private int rewritePosDataFileCnt = 0;

  private long rewritePosDataRecordCnt = 0;
  private long equalityDeleteSize = 0;
  private int eqDeleteFileCnt = 0;
  private long eqDeleteRecordCnt = 0;
  private long positionDeleteSize = 0;

  private int posDeleteFileCnt = 0;
  private long posDeleteRecordCnt = 0;
  private long newDataSize = 0;
  private int newDataFileCnt = 0;
  private long newDataRecordCnt = 0;
  /** Only position delete files will be generated */
  private long newDeleteSize = 0;

  private int newDeleteFileCnt = 0;
  private long newDeleteRecordCnt = 0;

  public static MetricsSummary fromMap(Map<String, String> summary) {
    MetricsSummary metricsSummary = new MetricsSummary();
    metricsSummary.rewriteDataSize = Long.parseLong(summary.getOrDefault(INPUT_DATA_SIZE, "0"));
    metricsSummary.rewriteDataFileCnt =
        Integer.parseInt(summary.getOrDefault(INPUT_DATA_FILES, "0"));
    metricsSummary.rewriteDataRecordCnt =
        Long.parseLong(summary.getOrDefault(INPUT_DATA_RECORDS, "0"));
    metricsSummary.rewritePosDataSize =
        Long.parseLong(summary.getOrDefault(INPUT_POS_DELETE_SIZE, "0"));
    metricsSummary.rewritePosDataFileCnt =
        Integer.parseInt(summary.getOrDefault(INPUT_POS_DELETE_FILES, "0"));
    metricsSummary.rewritePosDataRecordCnt =
        Long.parseLong(summary.getOrDefault(INPUT_POS_DELETE_RECORDS, "0"));
    metricsSummary.equalityDeleteSize =
        Long.parseLong(summary.getOrDefault(INPUT_EQ_DELETE_SIZE, "0"));
    metricsSummary.eqDeleteFileCnt =
        Integer.parseInt(summary.getOrDefault(INPUT_EQ_DELETE_FILES, "0"));
    metricsSummary.eqDeleteRecordCnt =
        Long.parseLong(summary.getOrDefault(INPUT_EQ_DELETE_RECORDS, "0"));
    metricsSummary.positionDeleteSize =
        Long.parseLong(summary.getOrDefault(INPUT_POS_DELETE_SIZE, "0"));
    metricsSummary.posDeleteFileCnt =
        Integer.parseInt(summary.getOrDefault(INPUT_POS_DELETE_FILES, "0"));
    metricsSummary.posDeleteRecordCnt =
        Long.parseLong(summary.getOrDefault(INPUT_POS_DELETE_RECORDS, "0"));
    metricsSummary.newDataSize = Long.parseLong(summary.getOrDefault(OUTPUT_DATA_SIZE, "0"));
    metricsSummary.newDataFileCnt = Integer.parseInt(summary.getOrDefault(OUTPUT_DATA_FILES, "0"));
    metricsSummary.newDataRecordCnt =
        Long.parseLong(summary.getOrDefault(OUTPUT_DATA_RECORDS, "0"));
    metricsSummary.newDeleteSize = Long.parseLong(summary.getOrDefault(OUTPUT_DELETE_SIZE, "0"));
    metricsSummary.newDeleteFileCnt =
        Integer.parseInt(summary.getOrDefault(OUTPUT_DELETE_FILES, "0"));
    metricsSummary.newDeleteRecordCnt =
        Long.parseLong(summary.getOrDefault(OUTPUT_DELETE_RECORDS, "0"));
    return metricsSummary;
  }

  public MetricsSummary() {}

  protected MetricsSummary(RewriteFilesInput input) {
    rewriteDataFileCnt = input.rewrittenDataFiles().length;
    rewritePosDataFileCnt = input.rePosDeletedDataFiles().length;
    for (DataFile rewriteFile : input.rewrittenDataFiles()) {
      rewriteDataSize += rewriteFile.fileSizeInBytes();
      rewriteDataRecordCnt += rewriteFile.recordCount();
    }
    for (DataFile rewritePosDataFile : input.rePosDeletedDataFiles()) {
      rewritePosDataSize += rewritePosDataFile.fileSizeInBytes();
      rewritePosDataRecordCnt += rewritePosDataFile.recordCount();
    }
    for (ContentFile<?> delete : input.deleteFiles()) {
      if (delete.content() == FileContent.POSITION_DELETES) {
        positionDeleteSize += delete.fileSizeInBytes();
        posDeleteRecordCnt += delete.recordCount();
        posDeleteFileCnt++;
      } else {
        equalityDeleteSize += delete.fileSizeInBytes();
        eqDeleteRecordCnt += delete.recordCount();
        eqDeleteFileCnt++;
      }
    }
  }

  public MetricsSummary(Collection<MetricsSummary> taskSummaries) {
    taskSummaries.forEach(
        metrics -> {
          newDataFileCnt += metrics.getNewDataFileCnt();
          newDataSize += metrics.getNewDataSize();
          newDataRecordCnt += metrics.getNewDataRecordCnt();
          newDeleteSize += metrics.getNewDeleteSize();
          newDeleteFileCnt += metrics.getNewDeleteFileCnt();
          newDeleteRecordCnt += metrics.getNewDeleteRecordCnt();
          rewriteDataFileCnt += metrics.getRewriteDataFileCnt();
          rewritePosDataFileCnt += metrics.getRewritePosDataFileCnt();
          rewriteDataSize += metrics.getRewriteDataSize();
          rewritePosDataSize += metrics.getRewritePosDataSize();
          posDeleteFileCnt += metrics.getPosDeleteFileCnt();
          positionDeleteSize += metrics.getPositionDeleteSize();
          eqDeleteFileCnt += metrics.getEqDeleteFileCnt();
          equalityDeleteSize += metrics.getEqualityDeleteSize();
          rewriteDataRecordCnt += metrics.getRewriteDataRecordCnt();
          rewritePosDataRecordCnt += metrics.getRewritePosDataRecordCnt();
          eqDeleteRecordCnt += metrics.getEqDeleteRecordCnt();
          posDeleteRecordCnt += metrics.getPosDeleteRecordCnt();
        });
  }

  public Map<String, String> summaryAsMap(boolean humanReadable) {
    Map<String, String> summary = new LinkedHashMap<>();
    put(summary, INPUT_DATA_FILES, rewriteDataFileCnt);
    put(summary, INPUT_DATA_SIZE, rewriteDataSize, humanReadable);
    put(summary, INPUT_DATA_RECORDS, rewriteDataRecordCnt);
    putIfPositive(summary, INPUT_READ_ONLY_DATA_FILES, rewritePosDataFileCnt);
    putIfPositive(summary, INPUT_READ_ONLY_DATA_SIZE, rewritePosDataSize, humanReadable);
    putIfPositive(summary, INPUT_READ_ONLY_DATA_RECORDS, rewritePosDataRecordCnt);
    putIfPositive(summary, INPUT_EQ_DELETE_FILES, eqDeleteFileCnt);
    putIfPositive(summary, INPUT_EQ_DELETE_SIZE, equalityDeleteSize, humanReadable);
    putIfPositive(summary, INPUT_EQ_DELETE_RECORDS, eqDeleteRecordCnt);
    putIfPositive(summary, INPUT_POS_DELETE_FILES, posDeleteFileCnt);
    putIfPositive(summary, INPUT_POS_DELETE_SIZE, positionDeleteSize, humanReadable);
    putIfPositive(summary, INPUT_POS_DELETE_RECORDS, posDeleteRecordCnt);
    put(summary, OUTPUT_DATA_FILES, newDataFileCnt);
    put(summary, OUTPUT_DATA_SIZE, newDataSize, humanReadable);
    put(summary, OUTPUT_DATA_RECORDS, newDataRecordCnt);
    putIfPositive(summary, OUTPUT_DELETE_FILES, newDeleteFileCnt);
    putIfPositive(summary, OUTPUT_DELETE_SIZE, newDeleteSize, humanReadable);
    putIfPositive(summary, OUTPUT_DELETE_RECORDS, newDeleteRecordCnt);
    return summary;
  }

  private void putIfPositive(Map<String, String> summary, String key, long value) {
    putIfPositive(summary, key, value, false);
  }

  private void putIfPositive(
      Map<String, String> summary, String key, long value, boolean humanReadable) {
    if (value > 0) {
      put(summary, key, value, humanReadable);
    }
  }

  private void put(Map<String, String> summary, String key, long value) {
    put(summary, key, value, false);
  }

  private void put(Map<String, String> summary, String key, long value, boolean humanReadable) {
    summary.put(key, humanReadable ? CommonUtil.byteToXB(value) : String.valueOf(value));
  }

  public FilesStatistics getInputFilesStatistics() {
    FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
    inputBuilder.addFiles(equalityDeleteSize, eqDeleteFileCnt);
    inputBuilder.addFiles(positionDeleteSize, posDeleteFileCnt);
    inputBuilder.addFiles(rewriteDataSize, rewriteDataFileCnt);
    inputBuilder.addFiles(rewritePosDataSize, rewritePosDataFileCnt);
    return inputBuilder.build();
  }

  public FilesStatistics getOutputFilesStatistics() {
    FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
    outputBuilder.addFiles(newDataSize, newDataFileCnt);
    outputBuilder.addFiles(newDeleteSize, newDeleteFileCnt);
    return outputBuilder.build();
  }

  public long getNewDataSize() {
    return newDataSize;
  }

  public void setNewDataSize(long newDataSize) {
    this.newDataSize = newDataSize;
  }

  public int getNewDataFileCnt() {
    return newDataFileCnt;
  }

  public void setNewDataFileCnt(int newDataFileCnt) {
    this.newDataFileCnt = newDataFileCnt;
  }

  public long getNewDataRecordCnt() {
    return newDataRecordCnt;
  }

  protected void setNewDataRecordCnt(long newDataRecordCnt) {
    this.newDataRecordCnt = newDataRecordCnt;
  }

  public void setNewDeleteSize(long newDeleteSize) {
    this.newDeleteSize = newDeleteSize;
  }

  public void setNewDeleteFileCnt(int newDeleteFileCnt) {
    this.newDeleteFileCnt = newDeleteFileCnt;
  }

  public long getNewDeleteSize() {
    return newDeleteSize;
  }

  public int getNewDeleteFileCnt() {
    return newDeleteFileCnt;
  }

  public long getNewDeleteRecordCnt() {
    return newDeleteRecordCnt;
  }

  protected void setNewDeleteRecordCnt(long newDeleteRecordCnt) {
    this.newDeleteRecordCnt = newDeleteRecordCnt;
  }

  public long getRewriteDataSize() {
    return rewriteDataSize;
  }

  public long getRewritePosDataSize() {
    return rewritePosDataSize;
  }

  public long getEqualityDeleteSize() {
    return equalityDeleteSize;
  }

  public long getPositionDeleteSize() {
    return positionDeleteSize;
  }

  public int getRewriteDataFileCnt() {
    return rewriteDataFileCnt;
  }

  public int getRewritePosDataFileCnt() {
    return rewritePosDataFileCnt;
  }

  public int getEqDeleteFileCnt() {
    return eqDeleteFileCnt;
  }

  public int getPosDeleteFileCnt() {
    return posDeleteFileCnt;
  }

  public long getRewriteDataRecordCnt() {
    return rewriteDataRecordCnt;
  }

  public long getRewritePosDataRecordCnt() {
    return rewritePosDataRecordCnt;
  }

  public long getEqDeleteRecordCnt() {
    return eqDeleteRecordCnt;
  }

  public long getPosDeleteRecordCnt() {
    return posDeleteRecordCnt;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("rewriteDataSize", rewriteDataSize)
        .add("rewriteDataFileCnt", rewriteDataFileCnt)
        .add("rewriteDataRecordCnt", rewriteDataRecordCnt)
        .add("rewritePosDataSize", rewritePosDataSize)
        .add("rewritePosDataFileCnt", rewritePosDataFileCnt)
        .add("rewritePosDataRecordCnt", rewritePosDataRecordCnt)
        .add("equalityDeleteSize", equalityDeleteSize)
        .add("eqDeleteFileCnt", eqDeleteFileCnt)
        .add("eqDeleteRecordCnt", eqDeleteRecordCnt)
        .add("positionDeleteSize", positionDeleteSize)
        .add("posDeleteFileCnt", posDeleteFileCnt)
        .add("posDeleteRecordCnt", posDeleteRecordCnt)
        .add("newDataSize", newDataSize)
        .add("newDataFileCnt", newDataFileCnt)
        .add("newDataRecordCnt", newDataRecordCnt)
        .add("newDeleteSize", newDeleteSize)
        .add("newDeleteFileCnt", newDeleteFileCnt)
        .add("newDeleteRecordCnt", newDeleteRecordCnt)
        .toString();
  }
}
