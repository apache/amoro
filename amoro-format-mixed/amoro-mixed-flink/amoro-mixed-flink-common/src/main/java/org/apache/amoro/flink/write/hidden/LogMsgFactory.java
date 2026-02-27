/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.flink.write.hidden;

import org.apache.amoro.flink.shuffle.ShuffleHelper;
import org.apache.amoro.log.LogData;
import org.apache.amoro.log.LogDataJsonSerialization;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;
import java.util.Properties;

/**
 * A factory creates log queue producers or consumers, e.g. kafka or pulsar distributed event
 * streaming platform.
 */
public interface LogMsgFactory<T> extends Serializable {

  Producer<T> createProducer(
      Properties producerConfig,
      String topic,
      LogDataJsonSerialization<T> logDataJsonSerialization,
      ShuffleHelper helper);

  Consumer<T> createConsumer();

  interface Producer<T> {
    void open() throws Exception;

    void send(LogData<T> logData) throws Exception;

    void sendToAllPartitions(LogData<T> logData) throws Exception;

    void flush();

    void close() throws Exception;
  }

  interface Consumer<T> {

    default void open(Configuration parameters) throws Exception {}

    default void close() throws Exception {}
  }
}
