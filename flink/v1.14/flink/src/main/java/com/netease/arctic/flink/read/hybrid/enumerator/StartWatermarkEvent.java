package com.netease.arctic.flink.read.hybrid.enumerator;

import org.apache.flink.api.connector.source.SourceEvent;

/**
 * It
 */
public class StartWatermarkEvent implements SourceEvent {
  private static final long serialVersionUID = 1L;

  public static final StartWatermarkEvent INSTANCE = new StartWatermarkEvent();
}
