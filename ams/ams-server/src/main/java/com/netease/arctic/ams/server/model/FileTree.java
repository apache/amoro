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

package com.netease.arctic.ams.server.model;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileTree {
  private final DataTreeNode node;
  private List<DataFile> deleteFiles = new ArrayList<>();
  private List<DataFile> insertFiles = new ArrayList<>();
  private List<DataFile> baseFiles = new ArrayList<>();
  private List<DeleteFile> posDeleteFiles = new ArrayList<>();
  private List<DeleteFile> eqDeleteFiles = new ArrayList<>();

  // subTree contains any base file
  private Boolean findAnyBaseFilesInTree = null;

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

  public FileTree putNodeIfAbsent(@Nonnull DataTreeNode newNode) {
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
  public void splitSubTree(List<FileTree> collector, Predicate<FileTree> canSplit) {
    if (canSplit.test(this)) {
      if (left != null) {
        left.splitSubTree(collector, canSplit);
      }
      if (right != null) {
        right.splitSubTree(collector, canSplit);
      }
    } else {
      collector.add(this);
    }
  }

  public void collectInsertFiles(List<DataFile> collector) {
    collector.addAll(insertFiles);
    if (left != null) {
      left.collectInsertFiles(collector);
    }
    if (right != null) {
      right.collectInsertFiles(collector);
    }
  }

  public void collectParentInsertFilesOf(DataTreeNode son, List<DataFile> collector) {
    if (son.mask() <= node.mask()) {
      return;
    }
    if (!son.isSonOf(this.node)) {
      return;
    }
    collector.addAll(insertFiles);
    if (left != null) {
      left.collectParentInsertFilesOf(son, collector);
    }
    if (right != null) {
      right.collectParentInsertFilesOf(son, collector);
    }
  }

  public void collectParentDeleteFilesOf(DataTreeNode son, List<DataFile> collector) {
    if (son.mask() <= node.mask()) {
      return;
    }
    if (!son.isSonOf(this.node)) {
      return;
    }
    collector.addAll(deleteFiles);
    if (left != null) {
      left.collectParentDeleteFilesOf(son, collector);
    }
    if (right != null) {
      right.collectParentDeleteFilesOf(son, collector);
    }
  }

  public void collectDeleteFiles(List<DataFile> collector) {
    collector.addAll(deleteFiles);
    if (left != null) {
      left.collectDeleteFiles(collector);
    }
    if (right != null) {
      right.collectDeleteFiles(collector);
    }
  }

  public void collectBaseFiles(List<DataFile> collector) {
    collectBaseFiles(collector, false, Collections.emptyList());
  }

  public void collectBaseFiles(List<DataFile> collector, boolean isMajor, List<DataFile> needOptimizeFiles) {
    if (isMajor) {
      baseFiles = baseFiles.stream()
          .filter(needOptimizeFiles::contains).collect(Collectors.toList());
    }
    collector.addAll(baseFiles);
    if (left != null) {
      left.collectBaseFiles(collector, isMajor, needOptimizeFiles);
    }
    if (right != null) {
      right.collectBaseFiles(collector, isMajor, needOptimizeFiles);
    }
  }

  public void collectPosDeleteFiles(List<DeleteFile> collector) {
    collector.addAll(posDeleteFiles);
    if (left != null) {
      left.collectPosDeleteFiles(collector);
    }
    if (right != null) {
      right.collectPosDeleteFiles(collector);
    }
  }

  public void collectEqDeleteFiles(List<DeleteFile> collector) {
    collector.addAll(eqDeleteFiles);
    if (left != null) {
      left.collectEqDeleteFiles(collector);
    }
    if (right != null) {
      right.collectEqDeleteFiles(collector);
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

  public List<DataFile> getDeleteFiles() {
    return deleteFiles;
  }

  public List<DataFile> getInsertFiles() {
    return insertFiles;
  }

  public List<DataFile> getBaseFiles() {
    return baseFiles;
  }

  public List<DeleteFile> getPosDeleteFiles() {
    return posDeleteFiles;
  }

  public List<DeleteFile> getEqDeleteFiles() {
    return eqDeleteFiles;
  }

  public void addFile(ContentFile<?> file, DataFileType fileType) {
    switch (fileType) {
      case BASE_FILE:
        baseFiles.add((DataFile) file);
        break;
      case EQ_DELETE_FILE:
        if (file.content() == FileContent.DATA) {
          deleteFiles.add((DataFile) file);
        } else {
          eqDeleteFiles.add((DeleteFile) file);
        }
        break;
      case INSERT_FILE:
        insertFiles.add((DataFile) file);
        break;
      case POS_DELETE_FILE:
        posDeleteFiles.add((DeleteFile) file);
        break;
      default:
    }
  }

  public DataTreeNode getNode() {
    return node;
  }

  public void initFiles() {
    this.baseFiles = Collections.emptyList();
    this.insertFiles = Collections.emptyList();
    this.deleteFiles = Collections.emptyList();
    this.posDeleteFiles = Collections.emptyList();
  }

  /**
   * if parent node exist files, all child node must be balance, in other word, the left child node and
   * the right child node are existing or non-existing at same time. Avoid can't cover parent node data
   * when split child tree.
   * <p>
   * fill the empty node when can't find left/right child node
   *
   * @param parentFileExist -
   */
  public void completeTree(boolean parentFileExist) {
    if (left == null && right == null) {
      return;
    }
    boolean thisNodeMustBalance = false;
    if (parentFileExist) {
      thisNodeMustBalance = true;
    } else if (fileExist()) {
      thisNodeMustBalance = true;
    }
    if (thisNodeMustBalance) {
      if (left == null) {
        left = new FileTree(node.left());
      }
      if (right == null) {
        right = new FileTree(node.right());
      }
    }
    if (left != null) {
      left.completeTree(parentFileExist || fileExist());
    }
    if (right != null) {
      right.completeTree(parentFileExist || fileExist());
    }
  }

  private boolean fileExist() {
    return !baseFiles.isEmpty() || !insertFiles.isEmpty() || !deleteFiles.isEmpty();
  }

  public boolean findAnyBaseFilesInTree() {
    if (this.findAnyBaseFilesInTree != null) {
      return this.findAnyBaseFilesInTree;
    }
    boolean leftContainsBaseFiles = false;
    boolean rightContainsBaseFiles = false;
    if (left != null) {
      leftContainsBaseFiles = left.findAnyBaseFilesInTree();
    }
    if (right != null) {
      rightContainsBaseFiles = right.findAnyBaseFilesInTree();
    }
    this.findAnyBaseFilesInTree = leftContainsBaseFiles || rightContainsBaseFiles || !baseFiles.isEmpty();
    return this.findAnyBaseFilesInTree;
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
