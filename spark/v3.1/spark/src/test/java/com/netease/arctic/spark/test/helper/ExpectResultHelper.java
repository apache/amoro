package com.netease.arctic.spark.test.helper;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyData;
import com.netease.arctic.io.writer.TaskWriterKey;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ExpectResultHelper {

  public static List<Record> upsertResult(
      List<Record> target, List<Record> source, Function<Record, Object> keyExtractor) {
    Map<Object, Record> expects = Maps.newHashMap();
    target.forEach(r -> {
      Object key = keyExtractor.apply(r);
      expects.put(key, r);
    });

    source.forEach(r -> {
      Object key = keyExtractor.apply(r);
      expects.put(key, r);
    });
    return Lists.newArrayList(expects.values());
  }

  public static List<Record> upsertDeletes(
      List<Record> target, List<Record> source, Function<Record, Object> keyExtractor
  ) {
    Map<Object, Record> expects = Maps.newHashMap();
    Map<Object, Record> deletes = Maps.newHashMap();
    target.forEach(r -> {
      Object key = keyExtractor.apply(r);
      expects.put(key, r);
    });

    source.forEach(r -> {
      Object key = keyExtractor.apply(r);
      if (expects.containsKey(key)) {
        deletes.put(key, expects.get(key));
      }
    });
    return Lists.newArrayList(deletes.values());
  }

  public static List<Record> dynamicOverwriteResult(
      List<Record> target, List<Record> source, Function<Record, Object> partitionExtractor) {
    Map<Object, List<Record>> targetGroupByPartition = Maps.newHashMap();
    target.forEach(r -> {
      Object pt = partitionExtractor.apply(r);
      targetGroupByPartition.computeIfAbsent(pt, x -> Lists.newArrayList());
      targetGroupByPartition.get(pt).add(r);
    });

    source.forEach(r -> {
      Object pt = partitionExtractor.apply(r);
      targetGroupByPartition.remove(pt);
    });

    List<Record> expects = targetGroupByPartition.values().stream()
        .reduce(Lists.newArrayList(), (l, r) -> {
          l.addAll(r);
          return l;
        });
    expects.addAll(source);
    return expects;
  }


  public static int expectOptimizeWriteFileCount(List<Record> sources, ArcticTable table, int bucket) {
    PartitionKey partitionKey = new PartitionKey(table.spec(), table.schema());
    PrimaryKeyData primaryKey = null;
    if (table.isKeyedTable()) {
      primaryKey = new PrimaryKeyData(table.asKeyedTable().primaryKeySpec(), table.schema());
    }
    int mask = bucket - 1;
    Set<TaskWriterKey> writerKeys = Sets.newHashSet();

    for (Record row: sources){
      partitionKey.partition(row);
      DataTreeNode node;
      if (primaryKey != null) {
        primaryKey.primaryKey(row);
        node = primaryKey.treeNode(mask);
      } else {
        node = DataTreeNode.ROOT;
      }
      writerKeys.add(
          new TaskWriterKey(partitionKey.copy(), node, DataFileType.BASE_FILE)
      );
    }
    return writerKeys.size();
  }
}
