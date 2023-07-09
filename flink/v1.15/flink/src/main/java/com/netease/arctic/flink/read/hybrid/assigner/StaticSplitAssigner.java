package com.netease.arctic.flink.read.hybrid.assigner;

import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import com.netease.arctic.flink.read.hybrid.split.ArcticSplitState;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.util.FlinkRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class StaticSplitAssigner implements SplitAssigner {

  private static final Logger LOG = LoggerFactory.getLogger(StaticSplitAssigner.class);

  private static final long POLL_TIMEOUT = 200;
  private final SplitEnumeratorContext<ArcticSplit> enumeratorContext;

  private int totalParallelism;
  private int totalSplitNum;

  private final PriorityBlockingQueue<ArcticSplit> splitQueue;

  private CompletableFuture<Void> availableFuture;

  public StaticSplitAssigner(
      SplitEnumeratorContext<ArcticSplit> enumeratorContext) {
    this.enumeratorContext = enumeratorContext;
    this.totalParallelism = enumeratorContext.currentParallelism();
    this.splitQueue = new PriorityBlockingQueue<>();
  }

  public StaticSplitAssigner(
      SplitEnumeratorContext<ArcticSplit> enumeratorContext,
      Collection<ArcticSplitState> splitStates) {
    this.enumeratorContext = enumeratorContext;
    this.splitQueue = new PriorityBlockingQueue<>();
    splitStates.forEach(state -> onDiscoveredSplits(Collections.singleton(state.toSourceSplit())));
  }

  @Override
  public Split getNext() {
    return getNextSplit().map(Split::of).orElseGet(isEmpty() ? Split::unavailable : Split::subtaskUnavailable);
  }

  @Override
  public Split getNext(int subtaskId) {
    return getNext();
  }

  private Optional<ArcticSplit> getNextSplit() {
    int currentParallelism = enumeratorContext.currentParallelism();
    if (totalParallelism != currentParallelism) {
      throw new FlinkRuntimeException(
          String.format(
              "Source parallelism has been changed, before parallelism is %s, now is %s",
              totalParallelism, currentParallelism));
    }

    ArcticSplit arcticSplit = null;
    try {
      arcticSplit = splitQueue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOG.warn("interruptedException", e);
    }
    if (arcticSplit == null) {
      LOG.debug("couldn't retrieve arctic source split in the queue.");
      return Optional.empty();
    } else {
      LOG.info("get next arctic split taskIndex {}, totalSplitNum {}, arcticSplit {}.",
          arcticSplit.taskIndex(), totalSplitNum, arcticSplit);
      return Optional.of(arcticSplit);
    }
  }

  @Override
  public void onDiscoveredSplits(Collection<ArcticSplit> splits) {
    splits.forEach(this::putArcticIntoQueue);
    // only complete pending future if new splits are discovered
    completeAvailableFuturesIfNeeded();
  }

  @Override
  public void onUnassignedSplits(Collection<ArcticSplit> splits) {
    onDiscoveredSplits(splits);
  }

  void putArcticIntoQueue(final ArcticSplit split) {
    splitQueue.put(split);
  }

  @Override
  public Collection<ArcticSplitState> state() {
    List<ArcticSplitState> arcticSplitStates = new ArrayList<>();
    arcticSplitStates.addAll(splitQueue.stream().map(ArcticSplitState::new)
        .collect(Collectors.toList()));
    return arcticSplitStates;
  }

  @Override
  public synchronized CompletableFuture<Void> isAvailable() {
    if (availableFuture == null) {
      availableFuture = new CompletableFuture<>();
    }
    return availableFuture;
  }

  public boolean isEmpty() {
    return splitQueue.isEmpty();
  }

  @Override
  public void close() throws IOException {
    splitQueue.clear();
  }

  private synchronized void completeAvailableFuturesIfNeeded() {
    if (availableFuture != null && !isEmpty()) {
      availableFuture.complete(null);
    }
    availableFuture = null;
  }
}
