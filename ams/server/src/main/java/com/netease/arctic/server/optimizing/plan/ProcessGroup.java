package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.IcebergDataFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class ProcessGroup {
  private final String id;
  private final List<TaskDescriptor> taskDescriptors;
  private final Map<String, Long> fromSequence;
  private final Map<String, Long> toSequence;
  public ProcessGroup(
      String id,
      List<TaskDescriptor> taskDescriptors,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence,
      Comparator<TaskDescriptor> taskComparator) {
    this.id = id;
    this.taskDescriptors = taskDescriptors.stream()
        .sorted(taskComparator)
        .collect(Collectors.toList());
    this.fromSequence = fromSequence;
    this.toSequence = toSequence;
  }

  public long dataFileBytes() {
    return taskDescriptors.stream()
        .mapToLong(task -> Arrays.stream(task.getInput().dataFiles()).mapToLong(IcebergDataFile::fileSizeInBytes).sum())
        .sum();
  }

  public long dataFileCount() {
    return taskDescriptors.stream()
        .mapToLong(task -> task.getInput().dataFiles().length)
        .sum();
  }

  public long getDeleteFileBytes() {
    return taskDescriptors.stream()
        .mapToLong(task -> task.getInput().deleteFiles().length)
        .sum();
  }

  public String id() {
    return id;
  }

  public Long earliestSequence() {
    return Collections.min(fromSequence.values());
  }

  public Long latestSequence() {
    return Collections.max(toSequence.values());
  }

  public Map<String, Long> getFromSequence() {
    return fromSequence;
  }

  public Map<String, Long> getToSequence() {
    return toSequence;
  }

  public List<TaskDescriptor> getTaskDescriptors() {
    return taskDescriptors;
  }

  public static Comparator<ProcessGroup> processComparator(String order) {
    return processComparator(ProcessOrder.fromName(order));
  }

  public static Comparator<ProcessGroup> processComparator(ProcessOrder order) {
    switch (order) {
      case BYTES_ASC:
        return Comparator.comparing(ProcessGroup::dataFileBytes);
      case BYTES_DESC:
        return Comparator.comparing(ProcessGroup::dataFileBytes, Comparator.reverseOrder());
      case FILES_ASC:
        return Comparator.comparing(ProcessGroup::dataFileCount);
      case FILES_DESC:
        return Comparator.comparing(ProcessGroup::dataFileCount, Comparator.reverseOrder());
      case SEQUENCE_ASC:
        return Comparator.comparing(ProcessGroup::earliestSequence);
      case SEQUENCE_DESC:
        return Comparator.comparing(ProcessGroup::latestSequence, Comparator.reverseOrder());
      default:
        return (fileGroupOne, fileGroupTwo) -> 0;
    }
  }

  public static Comparator<TaskDescriptor> taskComparator(String order) {
    return taskComparator(ProcessOrder.fromName(order));
  }

  public static Comparator<TaskDescriptor> taskComparator(ProcessOrder order) {
    switch (order) {
      case BYTES_ASC:
        return Comparator.comparing(
            task -> Arrays.stream(task.getInput().dataFiles()).mapToLong(IcebergDataFile::fileSizeInBytes).sum()
        );
      case BYTES_DESC:
        return Comparator.comparing(
          task -> Arrays.stream(task.getInput().dataFiles()).mapToLong(IcebergDataFile::fileSizeInBytes).sum(),
          Comparator.reverseOrder()
        );
      case FILES_ASC:
        return Comparator.comparing(task -> task.getInput().dataFiles().length);
      case FILES_DESC:
        return Comparator.comparing(task -> task.getInput().dataFiles().length, Comparator.reverseOrder());
      default:
        return (fileGroupOne, fileGroupTwo) -> 0;
    }
  }

  public enum ProcessOrder {
    BYTES_ASC("bytes-asc"),
    BYTES_DESC("bytes-desc"),
    FILES_ASC("files-asc"),
    FILES_DESC("files-desc"),
    SEQUENCE_ASC("sequence-asc"),
    SEQUENCE_DESC("sequence-desc"),
    NONE("none");

    private final String orderName;

    ProcessOrder(String orderName) {
      this.orderName = orderName;
    }

    public String orderName() {
      return orderName;
    }

    public static ProcessOrder fromName(String orderName) {
      Preconditions.checkArgument(orderName != null, "Invalid process order name: null");
      try {
        return ProcessOrder.valueOf(orderName.replaceFirst("-", "_").toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid process job order name: %s", orderName), e);
      }
    }
  }
}
