package com.netease.arctic.ams.api.events;

import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * This class defined all the event types created by ams service or tables managed by ams.
 *
 * @param <T> Event content type.
 */
public final class EventType<T> {

  private static final Set<EventType<?>> allTypes = Sets.newConcurrentHashSet();

  /** Event triggered when iceberg rest catalog api /metric/report is called. */
  public static final EventType<IcebergReportEventContent> IcebergReport =
      newType("iceberg.report", IcebergReportEventContent.class).build();

  /**
   * Event triggered when new table runtime is added, no matter internal catalog or external
   * catalog.
   */
  public static final EventType<TableRuntimeEventContent> TableRuntimeAdded =
      newType("table-runtime.added", TableRuntimeEventContent.class).triggeredOnLoad().build();

  /**
   * Event triggered when new table runtime is removed, no matter internal catalog or external
   * catalog.
   */
  public static final EventType<TableRuntimeEventContent> TableRuntimeRemoved =
      newType("table-runtime.removed", TableRuntimeEventContent.class).build();

  /** Event triggered when table optimizing state changed. */
  public static final EventType<TableOptimizingStateChangedEventContent>
      TableOptimizingStateChanged =
          newType("table-optimizing.state-changed", TableOptimizingStateChangedEventContent.class)
              .triggeredOnLoad()
              .build();

  /** Event triggered when table optimizing task is acked. */
  public static final EventType<TableOptimizingTaskEventContent> TableOptimizingTaskAcked =
      newType("table-optimizing.task.acked", TableOptimizingTaskEventContent.class)
          .triggeredOnLoad()
          .build();

  /** Event triggered when table optimizing task is completed. */
  public static final EventType<TableOptimizingTaskEventContent> TableOptimizingTaskCompleted =
      newType("table-optimizing.task.completed", TableOptimizingTaskEventContent.class).build();

  private final String name;

  private final Class<T> contentType;

  private final boolean triggeredOnLoad;

  private static <T> EventType.Builder<T> newType(String name, Class<T> contentType) {
    return new EventType.Builder<>(name, contentType);
  }

  /** @return A set includes all event types. */
  public static Set<EventType<?>> allTypes() {
    return Collections.unmodifiableSet(allTypes);
  }

  private EventType(String name, Class<T> contentType, boolean triggeredOnLoad) {
    this.name = name;
    this.contentType = contentType;
    this.triggeredOnLoad = triggeredOnLoad;
  }

  /** Event type name. */
  public String getName() {
    return name;
  }

  /** Class type of event content. */
  public Class<T> getContentType() {
    return contentType;
  }

  /** @return True if the event will be triggered on ams loaded. */
  public boolean isTriggeredOnLoad() {
    return triggeredOnLoad;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", this.name)
        .add("contentType", this.contentType.getName())
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventType<?> that = (EventType<?>) o;
    return Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }

  static class Builder<T> {
    private final String name;

    private final Class<T> contentType;
    private boolean triggeredOnLoad = false;

    public Builder(String name, Class<T> contentType) {
      this.name = name;
      this.contentType = contentType;
    }

    Builder<T> triggeredOnLoad() {
      this.triggeredOnLoad = true;
      return this;
    }

    public EventType<T> build() {
      EventType<T> eventType = new EventType<>(name, contentType, triggeredOnLoad);
      boolean added = allTypes.add(eventType);
      if (!added) {
        throw new IllegalStateException("Event type: " + name + " already created");
      }
      return eventType;
    }
  }
}
