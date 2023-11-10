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

package com.netease.arctic.spark.actions;

import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.spark.FileRewriteCoordinator;
import org.apache.spark.sql.SparkSession;
import org.sparkproject.guava.collect.Lists;

import java.util.List;
import java.util.UUID;

public abstract class BaseRewriteAction implements Action<BaseRewriteAction.RewriteResult> {

  private final SparkSession spark;
  private final ArcticTable table;
  protected Expression filter;

  public BaseRewriteAction(SparkSession spark, ArcticTable table) {
    this.spark = spark;
    this.table = table;
  }

  @Override
  public BaseRewriteAction.RewriteResult execute() {
    String groupId = UUID.randomUUID().toString();
    CloseableIterable<?> task = planFiles();
    List<?> tasks = Lists.newArrayList(task);
    if (tasks.isEmpty()) {
      return RewriteResult.empty();
    }
    ScanTaskSetManager.get().stageTasks(table, groupId, tasks);

    doRewrite(groupId);

    Table icebergTable = table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    FileRewriteCoordinator coordinator = FileRewriteCoordinator.get();
    RewriteCommitter committer = committer();
    committer.addFiles(coordinator.fetchNewDataFiles(icebergTable, groupId));
    committer.commitOrClean();
    return committer.result();
  }

  public BaseRewriteAction filter(Expression expression) {
    this.filter = expression;
    return this;
  }

  abstract void doRewrite(String groupId);

  abstract CloseableIterable<?> planFiles();

  abstract RewriteCommitter committer();

  protected SparkSession getSpark() {
    return spark;
  }

  protected ArcticTable getTable() {
    return table;
  }

  public static class RewriteResult {
    private final int rewriteFileCount;
    private final int rewriteDeleteFileCount;
    private final int rewritePartitionCount;
    private final int rewriteBaseStorePartitionCount;
    private final String rewritePartitions;
    private final int addFileCount;

    private RewriteResult(
        int rewriteFileCount, int rewriteDeleteFileCount, int rewritePartitionCount,
        int rewriteBaseStorePartitionCount, String rewritePartitions, int addFileCount) {
      this.rewriteFileCount = rewriteFileCount;
      this.rewriteDeleteFileCount = rewriteDeleteFileCount;
      this.rewritePartitionCount = rewritePartitionCount;
      this.rewriteBaseStorePartitionCount = rewriteBaseStorePartitionCount;
      this.rewritePartitions = rewritePartitions;
      this.addFileCount = addFileCount;
    }

    public static RewriteResult of(
        int rewriteFileCount,
        int rewriteDeleteFileCount,
        int rewritePartitionCount,
        int rewriteBaseStorePartitionCount,
        String rewritePartitions,
        int addFileCount) {
      return new RewriteResult(
          rewriteFileCount,
          rewriteDeleteFileCount,
          rewritePartitionCount,
          rewriteBaseStorePartitionCount,
          rewritePartitions,
          addFileCount);
    }

    public static RewriteResult empty() {
      return new RewriteResult(
          0,
          0,
          0,
          0,
          "",
          0);
    }

    public int rewriteFileCount() {
      return rewriteFileCount;
    }

    public int rewriteDeleteFileCount() {
      return rewriteDeleteFileCount;
    }

    public int rewritePartitionCount() {
      return rewritePartitionCount;
    }

    public int rewriteBaseStorePartitionCount() {
      return rewriteBaseStorePartitionCount;
    }

    public String rewritePartitions() {
      return rewritePartitions;
    }

    public int addFileCount() {
      return addFileCount;
    }
  }
}
