package com.netease.arctic.flink.read.hybrid.assigner;

import com.netease.arctic.flink.read.FlinkSplitPlanner;
import com.netease.arctic.flink.read.hybrid.reader.TestRowDataReaderFunction;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import org.apache.flink.api.connector.source.ReaderInfo;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.connector.source.SplitsAssignment;
import org.apache.flink.metrics.groups.SplitEnumeratorMetricGroup;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TestStaticSplitAssigner extends TestRowDataReaderFunction {
  private static final Logger LOG = LoggerFactory.getLogger(TestStaticSplitAssigner.class);

  @Test
  public void testSingleParallelism() {
    StaticSplitAssigner staticSplitAssigner = instanceStaticSplitAssigner(1);

    List<ArcticSplit> splitList = FlinkSplitPlanner.mergeOnReadPlan(testKeyedTable, Collections.emptyList(),
        new AtomicInteger());
    staticSplitAssigner.onDiscoveredSplits(splitList);
    List<ArcticSplit> actual = new ArrayList<>();

    while (true) {
      Split splitOpt = staticSplitAssigner.getNext(0);
      if (splitOpt.isAvailable()) {
        actual.add(splitOpt.split());
      } else {
        break;
      }
    }

    Assert.assertEquals(splitList.size(), actual.size());
  }


  @Test
  public void testMultiParallelism() {
    StaticSplitAssigner staticSplitAssigner = instanceStaticSplitAssigner(3);

    List<ArcticSplit> splitList = FlinkSplitPlanner.mergeOnReadPlan(testKeyedTable, Collections.emptyList(),
        new AtomicInteger());
    staticSplitAssigner.onDiscoveredSplits(splitList);
    List<ArcticSplit> actual = new ArrayList<>();

    int subtaskId = 2;
    while (subtaskId >= 0) {
      Split splitOpt = staticSplitAssigner.getNext(subtaskId);
      if (splitOpt.isAvailable()) {
        actual.add(splitOpt.split());
      } else {
        LOG.info("subtask id {}, splits {}.\n {}", subtaskId, actual.size(), actual);
        --subtaskId;
      }
    }

    Assert.assertEquals(splitList.size(), actual.size());
  }

  protected StaticSplitAssigner instanceStaticSplitAssigner(int parallelism) {
    SplitEnumeratorContext<ArcticSplit>
        splitEnumeratorContext = new InternalStaticSplitEnumeratorContext(parallelism);
    return new StaticSplitAssigner(splitEnumeratorContext);
  }

  protected static class InternalStaticSplitEnumeratorContext implements SplitEnumeratorContext<ArcticSplit> {
    private final int parallelism;

    public InternalStaticSplitEnumeratorContext(int parallelism) {
      this.parallelism = parallelism;
    }

    @Override
    public SplitEnumeratorMetricGroup metricGroup() {
      return null;
    }

    @Override
    public void sendEventToSourceReader(int subtaskId, SourceEvent event) {

    }

    @Override
    public int currentParallelism() {
      return parallelism;
    }

    @Override
    public Map<Integer, ReaderInfo> registeredReaders() {
      return null;
    }

    @Override
    public void assignSplits(SplitsAssignment<ArcticSplit> newSplitAssignments) {

    }

    @Override
    public void signalNoMoreSplits(int subtask) {

    }

    @Override
    public <T> void callAsync(Callable<T> callable, BiConsumer<T, Throwable> handler) {

    }

    @Override
    public <T> void callAsync(Callable<T> callable, BiConsumer<T, Throwable> handler, long initialDelay, long period) {

    }

    @Override
    public void runInCoordinatorThread(Runnable runnable) {

    }
  }
}
