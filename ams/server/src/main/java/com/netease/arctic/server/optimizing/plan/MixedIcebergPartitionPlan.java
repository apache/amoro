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

package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.optimizing.MixFormatRewriteExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.Pair;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MixedIcebergPartitionPlan extends AbstractPartitionPlan {

  protected final Map<String, String> partitionProperties;

  public MixedIcebergPartitionPlan(
      TableRuntime tableRuntime,
      ArcticTable table,
      Pair<Integer, StructLike> partition,
      long planTime) {
    super(tableRuntime, table, partition, planTime);
    this.partitionProperties = TablePropertyUtil.getPartitionProperties(table, partition.second());
  }

  @Override
  public boolean addFile(DataFile dataFile, List<ContentFile<?>> deletes) {
    if (!super.addFile(dataFile, deletes)) {
      return false;
    }
    if (evaluator().isChangeFile(dataFile)) {
      markSequence(dataFile.dataSequenceNumber());
    }
    for (ContentFile<?> deleteFile : deletes) {
      if (deleteFile.content() == FileContent.DATA) {
        markSequence(deleteFile.dataSequenceNumber());
      }
    }
    return true;
  }

  @Override
  protected MixedIcebergPartitionEvaluator evaluator() {
    return ((MixedIcebergPartitionEvaluator) super.evaluator());
  }

  @Override
  protected OptimizingInputProperties buildTaskProperties() {
    OptimizingInputProperties properties = new OptimizingInputProperties();
    properties.setExecutorFactoryImpl(MixFormatRewriteExecutorFactory.class.getName());
    return properties;
  }

  protected boolean isKeyedTable() {
    return tableObject.isKeyedTable();
  }

  @Override
  protected TaskSplitter buildTaskSplitter() {
    if (isKeyedTable()) {
      return new TreeNodeTaskSplitter();
    } else {
      return new BinPackingTaskSplitter();
    }
  }

  @Override
  protected CommonPartitionEvaluator buildEvaluator() {
    return new MixedIcebergPartitionEvaluator(
        tableRuntime, partition, partitionProperties, planTime, isKeyedTable());
  }

  protected static class MixedIcebergPartitionEvaluator extends CommonPartitionEvaluator {
    protected final boolean keyedTable;
    protected boolean hasChangeFiles = false;
    private final boolean reachBaseRefreshInterval;

    public MixedIcebergPartitionEvaluator(
        TableRuntime tableRuntime,
        Pair<Integer, StructLike> partition,
        Map<String, String> partitionProperties,
        long planTime,
        boolean keyedTable) {
      super(tableRuntime, partition, planTime);
      this.keyedTable = keyedTable;
      String optimizedTime = partitionProperties.get(TableProperties.PARTITION_BASE_OPTIMIZED_TIME);
      long lastBaseOptimizedTime = optimizedTime == null ? 0 : Long.parseLong(optimizedTime);
      this.reachBaseRefreshInterval =
          config.getBaseRefreshInterval() >= 0
              && planTime - lastBaseOptimizedTime > config.getBaseRefreshInterval();
    }

    @Override
    public boolean addFile(DataFile dataFile, List<ContentFile<?>> deletes) {
      if (!super.addFile(dataFile, deletes)) {
        return false;
      }
      if (!hasChangeFiles && isChangeFile(dataFile)) {
        hasChangeFiles = true;
      }
      return true;
    }

    protected boolean isChangeFile(DataFile dataFile) {
      if (!keyedTable) {
        return false;
      }
      PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile;
      return file.type() == DataFileType.INSERT_FILE || file.type() == DataFileType.EQ_DELETE_FILE;
    }

    @Override
    protected boolean isFragmentFile(DataFile dataFile) {
      PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile;
      if (file.type() == DataFileType.BASE_FILE) {
        return dataFile.fileSizeInBytes() <= fragmentSize;
      } else if (file.type() == DataFileType.INSERT_FILE) {
        // for keyed table, we treat all insert files as fragment files
        return true;
      } else {
        throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
      }
    }

    @Override
    public boolean isMinorNecessary() {
      if (keyedTable) {
        int smallFileCount = fragmentFileCount + equalityDeleteFileCount;
        int baseSplitCount = getBaseSplitCount();
        if (smallFileCount >= Math.max(baseSplitCount, config.getMinorLeastFileCount())) {
          return true;
        } else if ((smallFileCount > baseSplitCount || hasChangeFiles) && reachMinorInterval()) {
          return true;
        } else {
          return hasChangeFiles && reachBaseRefreshInterval();
        }
      } else {
        return super.isMinorNecessary();
      }
    }

    @Override
    public boolean segmentFileShouldRewritePos(DataFile dataFile, List<ContentFile<?>> deletes) {
      if (deletes.stream()
          .anyMatch(
              delete ->
                  delete.content() == FileContent.EQUALITY_DELETES
                      || delete.content() == FileContent.DATA)) {
        // change equality delete file's content is DATA
        return true;
      } else {
        return deletes.stream()
                .filter(delete -> delete.content() == FileContent.POSITION_DELETES)
                .count()
            >= 2;
      }
    }

    protected boolean reachBaseRefreshInterval() {
      return reachBaseRefreshInterval;
    }

    protected int getBaseSplitCount() {
      if (keyedTable) {
        return config.getBaseHashBucket();
      } else {
        return 1;
      }
    }

    @Override
    public boolean isFullNecessary() {
      if (!reachFullInterval()) {
        return false;
      }
      return anyDeleteExist() || fragmentFileCount > getBaseSplitCount() || hasChangeFiles;
    }

    @Override
    public PartitionEvaluator.Weight getWeight() {
      return new Weight(getCost(), hasChangeFiles && reachBaseRefreshInterval());
    }

    protected static class Weight implements PartitionEvaluator.Weight {
      private final long cost;
      private final boolean reachDelay;

      public Weight(long cost, boolean reachDelay) {
        this.cost = cost;
        this.reachDelay = reachDelay;
      }

      @Override
      public int compareTo(PartitionEvaluator.Weight o) {
        Weight that = (Weight) o;
        int compare = Boolean.compare(this.reachDelay, that.reachDelay);
        if (compare != 0) {
          return compare;
        }
        return Long.compare(this.cost, that.cost);
      }
    }
  }

  /** split task with {@link DataTreeNode} */
  private class TreeNodeTaskSplitter implements TaskSplitter {
    @Override
    public List<SplitTask> splitTasks(int targetTaskCount) {
      List<SplitTask> result = Lists.newArrayList();
      FileTree rootTree = FileTree.newTreeRoot();
      rewritePosDataFiles.forEach(rootTree::addRewritePosDataFile);
      rewriteDataFiles.forEach(rootTree::addRewriteDataFile);
      rootTree.completeTree();
      List<FileTree> subTrees = Lists.newArrayList();
      rootTree.splitFileTree(subTrees, new SplitIfNoFileExists());
      for (FileTree subTree : subTrees) {
        Map<DataFile, List<ContentFile<?>>> rewriteDataFiles = Maps.newHashMap();
        Map<DataFile, List<ContentFile<?>>> rewritePosDataFiles = Maps.newHashMap();
        Set<ContentFile<?>> deleteFiles = Sets.newHashSet();
        subTree.collectRewriteDataFiles(rewriteDataFiles);
        subTree.collectRewritePosDataFiles(rewritePosDataFiles);
        rewriteDataFiles.forEach((f, deletes) -> deleteFiles.addAll(deletes));
        rewritePosDataFiles.forEach((f, deletes) -> deleteFiles.addAll(deletes));
        result.add(
            new SplitTask(rewriteDataFiles.keySet(), rewritePosDataFiles.keySet(), deleteFiles));
      }
      return result;
    }
  }

  private static class FileTree {

    private final DataTreeNode node;
    private final Map<DataFile, List<ContentFile<?>>> rewriteDataFiles = Maps.newHashMap();
    private final Map<DataFile, List<ContentFile<?>>> rewritePosDataFiles = Maps.newHashMap();

    private FileTree left;
    private FileTree right;

    public FileTree(DataTreeNode node) {
      this.node = node;
    }

    public static FileTree newTreeRoot() {
      return new FileTree(DataTreeNode.of(0, 0));
    }

    private FileTree putNodeIfAbsent(@Nonnull DataTreeNode newNode) {
      if (newNode.equals(node)) {
        return this;
      }
      if (newNode.isSonOf(node.left())) {
        if (left == null) {
          left = new FileTree(node.left());
        }
        return left.putNodeIfAbsent(newNode);
      } else if (newNode.isSonOf(node.right())) {
        if (right == null) {
          right = new FileTree(node.right());
        }
        return right.putNodeIfAbsent(newNode);
      } else {
        throw new IllegalArgumentException(newNode + " is not son of " + node);
      }
    }

    /**
     * split file tree with split condition.
     *
     * @param collector - collect result
     * @param canSplit - if this tree can split
     */
    public void splitFileTree(List<FileTree> collector, Predicate<FileTree> canSplit) {
      if (canSplit.test(this)) {
        if (left != null) {
          left.splitFileTree(collector, canSplit);
        }
        if (right != null) {
          right.splitFileTree(collector, canSplit);
        }
      } else {
        collector.add(this);
      }
    }

    public void collectRewriteDataFiles(Map<DataFile, List<ContentFile<?>>> collector) {
      collector.putAll(rewriteDataFiles);
      if (left != null) {
        left.collectRewriteDataFiles(collector);
      }
      if (right != null) {
        right.collectRewriteDataFiles(collector);
      }
    }

    public void collectRewritePosDataFiles(Map<DataFile, List<ContentFile<?>>> collector) {
      collector.putAll(rewritePosDataFiles);
      if (left != null) {
        left.collectRewritePosDataFiles(collector);
      }
      if (right != null) {
        right.collectRewritePosDataFiles(collector);
      }
    }

    public void addRewritePosDataFile(DataFile file, List<ContentFile<?>> deleteFiles) {
      PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) file;
      FileTree node = putNodeIfAbsent(primaryKeyedFile.node());
      node.rewritePosDataFiles.put(file, deleteFiles);
    }

    public void addRewriteDataFile(DataFile file, List<ContentFile<?>> deleteFiles) {
      PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) file;
      FileTree node = putNodeIfAbsent(primaryKeyedFile.node());
      node.rewriteDataFiles.put(file, deleteFiles);
    }

    public boolean isRootEmpty() {
      return rewritePosDataFiles.isEmpty() && rewriteDataFiles.isEmpty();
    }

    public boolean isLeaf() {
      return left == null && right == null;
    }

    /**
     * Complete this binary tree to make every subTree of this Tree As a Full Binary Tree(FBT), if
     * any data exists in this subTree.
     *
     * <p>A Full Binary Tree(FBT) is a binary tree in which all the nodes have either 0 or 2
     * offspring. In other terms, it is a binary tree in which all nodes, except the leaf nodes,
     * have two offspring.
     *
     * <p>To Complete the tree is to avoid ancestor node's data can't be covered when split subTree.
     */
    public void completeTree() {
      completeTree(false);
    }

    private void completeTree(boolean ancestorFileExist) {
      if (left == null && right == null) {
        return;
      }
      // if any ancestor of this node or this node itself contains any file, this node must be
      // balance
      boolean thisNodeMustBalance = ancestorFileExist || fileExist();
      if (thisNodeMustBalance) {
        // fill and empty node if left or right node not exist
        if (left == null) {
          left = new FileTree(node.left());
        }
        if (right == null) {
          right = new FileTree(node.right());
        }
      }
      if (left != null) {
        left.completeTree(ancestorFileExist || fileExist());
      }
      if (right != null) {
        right.completeTree(ancestorFileExist || fileExist());
      }
    }

    private boolean fileExist() {
      return !rewritePosDataFiles.isEmpty() || !rewriteDataFiles.isEmpty();
    }
  }

  private static class SplitIfNoFileExists implements Predicate<FileTree> {

    public SplitIfNoFileExists() {}

    /**
     * file tree can split if: - root node isn't leaf node - and no file exists in the root node
     *
     * @param fileTree - file tree to split
     * @return true if this fileTree need split
     */
    @Override
    public boolean test(FileTree fileTree) {
      return !fileTree.isLeaf() && fileTree.isRootEmpty();
    }
  }
}
