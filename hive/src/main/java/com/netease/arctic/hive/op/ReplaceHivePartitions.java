package com.netease.arctic.hive.op;

import com.google.common.collect.Lists;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Snapshot;

import java.util.List;
import java.util.function.Consumer;

public class ReplaceHivePartitions implements ReplacePartitions {

  final ReplacePartitions delegate;
  final HMSClient hmsClient;

  final HMSClient transactionalHMSClient;

  final UnkeyedHiveTable table;

  final List<DataFile> addFiles = Lists.newArrayList();

  public ReplaceHivePartitions(
      ReplacePartitions delegate,
      UnkeyedHiveTable table,
      HMSClient client,
      HMSClient transactionalClient) {
    this.delegate = delegate;
    this.hmsClient = client;
    this.transactionalHMSClient = transactionalClient;
    this.table = table;
  }

  @Override
  public ReplacePartitions addFile(DataFile file) {
    delegate.addFile(file);
    this.addFiles.add(file);
    return this;
  }

  @Override
  public ReplacePartitions validateAppendOnly() {
    delegate.validateAppendOnly();
    return this;
  }

  @Override
  public ReplacePartitions set(String property, String value) {
    delegate.set(property, value);
    return this;
  }

  @Override
  public ReplacePartitions deleteWith(Consumer<String> deleteFunc) {
    delegate.deleteWith(deleteFunc);
    return this;
  }

  @Override
  public ReplacePartitions stageOnly() {
    delegate.stageOnly();
    return this;
  }

  @Override
  public Snapshot apply() {
    Snapshot snapshot = delegate.apply();
    if (!addFiles.isEmpty()) {
      applyPartitionRewrite();
    }
    return snapshot;
  }

  @Override
  public void commit() {
    delegate.commit();
  }

  @Override
  public Object updateEvent() {
    return delegate.updateEvent();
  }

  private void applyPartitionRewrite(){
    // StructLikeMap<String> partitionPath = StructLikeMap.create(table.spec().partitionType());
  }
}
