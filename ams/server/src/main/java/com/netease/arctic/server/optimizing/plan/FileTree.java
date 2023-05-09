package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.PrimaryKeyedFile;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class FileTree {
  private final DataTreeNode node;
  private final Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles = Maps.newHashMap();
  private final Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles = Maps.newHashMap();

  private FileTree left;
  private FileTree right;

  public FileTree(DataTreeNode node) {
    this.node = node;
  }

  public FileTree getLeft() {
    return left;
  }

  public FileTree getRight() {
    return right;
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
   * @param canSplit  - if this tree can split
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

  public void collectFragmentFiles(Map<IcebergDataFile, List<IcebergContentFile<?>>> collector) {
    collector.putAll(fragmentFiles);
    if (left != null) {
      left.collectFragmentFiles(collector);
    }
    if (right != null) {
      right.collectFragmentFiles(collector);
    }
  }

  public void collectSegmentFiles(Map<IcebergDataFile, List<IcebergContentFile<?>>> collector) {
    collector.putAll(segmentFiles);
    if (left != null) {
      left.collectSegmentFiles(collector);
    }
    if (right != null) {
      right.collectSegmentFiles(collector);
    }
  }

  public long accumulate(Function<FileTree, Long> function) {
    Long apply = function.apply(this);
    if (left != null) {
      apply += left.accumulate(function);
    }
    if (right != null) {
      apply += right.accumulate(function);
    }
    return apply;
  }

  public Map<IcebergDataFile, List<IcebergContentFile<?>>> getFragmentFiles() {
    return fragmentFiles;
  }

  public Map<IcebergDataFile, List<IcebergContentFile<?>>> getSegmentFiles() {
    return segmentFiles;
  }

  public void addSegmentFile(IcebergDataFile file, List<IcebergContentFile<?>> deleteFiles) {
    PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) file.internalFile();
    FileTree node = putNodeIfAbsent(primaryKeyedFile.node());
    node.segmentFiles.put(file, deleteFiles);
  }

  public void addFragmentFile(IcebergDataFile file, List<IcebergContentFile<?>> deleteFiles) {
    PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) file.internalFile();
    FileTree node = putNodeIfAbsent(primaryKeyedFile.node());
    node.fragmentFiles.put(file, deleteFiles);
  }

  public DataTreeNode getNode() {
    return node;
  }

  public boolean isRootEmpty() {
    return segmentFiles.isEmpty() && fragmentFiles.isEmpty();
  }

  public boolean isLeaf() {
    return left == null && right == null;
  }

  /**
   * Complete this binary tree to make every subTree of this Tree As a Full Binary Tree(FBT), if any data exists in this
   * subTree.
   * <p>
   * A Full Binary Tree(FBT) is a binary tree in which all the nodes have either 0 or 2 offspring. In other terms, it is
   * a binary tree in which all nodes, except the leaf nodes, have two offspring.
   * <p>
   * To Complete the tree is to avoid ancestor node's data can't be covered when split subTree.
   */
  public void completeTree() {
    completeTree(false);
  }

  private void completeTree(boolean ancestorFileExist) {
    if (left == null && right == null) {
      return;
    }
    // if any ancestor of this node or this node itself contains any file, this node must be balance
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
    return !segmentFiles.isEmpty() || !fragmentFiles.isEmpty();
  }

  public boolean anyMatch(Predicate<FileTree> predicate) {
    if (predicate.test(this)) {
      return true;
    }
    if (left != null && left.anyMatch(predicate)) {
      return true;
    }
    return right != null && right.anyMatch(predicate);
  }
}
