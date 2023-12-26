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

package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.events.Event;
import com.netease.arctic.ams.api.events.EventListener;

import java.io.IOException;

/** This class is used to trigger various events in the process and notify event emitter plugins. */
public class EventsManager extends AbstractPluginManager<EventListener> {

  public static final String PLUGIN_TYPE = "event-listeners";
  private static volatile EventsManager INSTANCE;

  /** @return Get the singleton object. */
  public static EventsManager getInstance() {
    if (INSTANCE == null) {
      synchronized (EventsManager.class) {
        if (INSTANCE == null) {
          try {
            INSTANCE = new EventsManager();
            INSTANCE.initialize();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return INSTANCE;
  }

  /** Close the manager */
  public static void dispose() {
    synchronized (EventsManager.class) {
      if (INSTANCE != null) {
        INSTANCE.close();
      }
      INSTANCE = null;
    }
  }

  public EventsManager() {
    super(PLUGIN_TYPE);
  }

  @Override
  protected String pluginCategory() {
    return PLUGIN_TYPE;
  }

  public void emit(Event event) {
    forEachAsync(listener -> listener.handleEvent(event));
  }
}
