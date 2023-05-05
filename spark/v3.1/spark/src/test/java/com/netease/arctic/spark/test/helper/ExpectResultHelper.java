package com.netease.arctic.spark.test.helper;

import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
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
        .reduce(Lists.newArrayList(), (l,r) -> {
          l.addAll(r);
          return l;
        });
    expects.addAll(source);
    return expects;
  }
}
