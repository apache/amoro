package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.IcebergDataFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class PlannedTasks {
  private final String partition;
  private final List<TaskDescriptor> taskDescriptors;
  private final Long fromSequence;
  public PlannedTasks(String partition, List<TaskDescriptor> taskDescriptors, Long fromSequence) {
    this.partition = partition;
    this.taskDescriptors = taskDescriptors;
    this.fromSequence = fromSequence;
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

  public String partition() {
    return partition;
  }

  public Long oldestSequence() {
    return fromSequence;
  }

  public List<TaskDescriptor> getTaskDescriptors() {
    return taskDescriptors;
  }

  public static Comparator<PlannedTasks> processComparator(String order) {
    return processComparator(ProcessOrder.fromName(order));
  }

  public static Comparator<PlannedTasks> processComparator(ProcessOrder order) {
    switch (order) {
      case BYTES_ASC:
        return Comparator.comparing(PlannedTasks::dataFileBytes);
      case BYTES_DESC:
        return Comparator.comparing(PlannedTasks::dataFileBytes, Comparator.reverseOrder());
      case FILES_ASC:
        return Comparator.comparing(PlannedTasks::dataFileCount);
      case FILES_DESC:
        return Comparator.comparing(PlannedTasks::dataFileCount, Comparator.reverseOrder());
      case SEQUENCE_ASC:
        return Comparator.comparing(PlannedTasks::oldestSequence);
      case SEQUENCE_DESC:
        return Comparator.comparing(PlannedTasks::oldestSequence, Comparator.reverseOrder());
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
