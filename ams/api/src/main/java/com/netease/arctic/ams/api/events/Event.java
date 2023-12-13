/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.api.events;

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

/**
 * Standard event interface in Amoro domain this is a parallel concept with specific metrics data of
 * formats like MetricReport in Apache Iceberg Implementations could include all kinds of process
 * metrics and resource metrics.
 */
public class Event<T> {

  private final EventType<T> type;

  private final long eventTime;

  private final T content;

  private final boolean onLoad;

  Event(EventType<T> type, long eventTime, T content, boolean onLoad) {
    this.type = type;
    this.eventTime = eventTime;
    this.content = content;
    this.onLoad = onLoad;
  }

  /**
   * Create an event which just happened.
   * @param eventType event type
   * @param content event content
   * @return new event
   * @param <T> event content type.
   */
  public static <T> Event<T> newEvent(EventType<T> eventType, T content) {
    return new Event<>(eventType, System.currentTimeMillis(), content, false);
  }

  /**
   * Create an event on ams is initializing.
   * @param eventType event type.
   * @param content event content
   * @param eventTime when event is happened.
   * @return event object
   * @param <T> event content type.
   */
  public static <T> Event<T> onLoadEvent(EventType<T> eventType, T content, long eventTime) {
    Preconditions.checkState(eventType.isTriggeredOnLoad(),
        "The event type %s should not be triggered when ams is loaded", eventType.getName());
    return new Event<>(eventType, eventTime, content, true);
  }

  /**
   * Get the name of the metrics type
   *
   * @return metrics type name
   */
  public EventType<T> type() {
    return type;
  }

  /**
   * The timestamp of event when it is happened.
   *
   * @return event created time.
   */
  public long eventTime() {
    return eventTime;
  }

  /** @return True if this event is triggered on ams loaded. */
  public boolean isOnLoad() {
    return onLoad;
  }

  /**
   * Get the content of the event
   *
   * @return event content data
   */
  public T content() {
    return content;
  }
}
