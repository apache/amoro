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

package org.apache.amoro.server.manager;

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads and validates an application id from the standard YARN client messages. */
final class YarnApplicationIdReader {
  static final long DEFAULT_READ_TIMEOUT_MILLIS = 600_000L;

  private static final Pattern SUBMITTED_APPLICATION_PATTERN =
      Pattern.compile("^Submitted application (application_[0-9]+_[0-9]+)$");
  private static final Pattern SPARK_APPLICATION_REPORT_PATTERN =
      Pattern.compile("^Application report for (application_[0-9]+_[0-9]+) \\(state: [A-Z_]+\\)$");
  private static final Pattern FLINK_WEB_INTERFACE_PATTERN =
      Pattern.compile("^Found Web Interface \\S+ of application '(application_[0-9]+_[0-9]+)'\\.$");

  private static final String HADOOP_YARN_CLIENT_LOGGER =
      "org.apache.hadoop.yarn.client.api.impl.YarnClientImpl";
  private static final String SPARK_YARN_CLIENT_LOGGER = "org.apache.spark.deploy.yarn.Client";
  private static final String FLINK_YARN_CLUSTER_DESCRIPTOR_LOGGER =
      "org.apache.flink.yarn.YarnClusterDescriptor";

  private YarnApplicationIdReader() {}

  static String parse(String line, Engine engine) {
    if (line == null) {
      return null;
    }

    String message = line.trim();
    String applicationId = extractId(SUBMITTED_APPLICATION_PATTERN, message);
    if (applicationId == null) {
      applicationId =
          extractId(
              engine == Engine.SPARK
                  ? SPARK_APPLICATION_REPORT_PATTERN
                  : FLINK_WEB_INTERFACE_PATTERN,
              message);
    }
    if (applicationId != null) {
      return applicationId;
    }

    int colonSeparator = line.indexOf(": ");
    int dashSeparator = line.indexOf(" - ");
    if (colonSeparator < 0 && dashSeparator < 0) {
      return null;
    }

    int separator;
    int separatorLength;
    if (colonSeparator < 0 || (dashSeparator >= 0 && dashSeparator < colonSeparator)) {
      separator = dashSeparator;
      separatorLength = 3;
    } else {
      separator = colonSeparator;
      separatorLength = 2;
    }

    String logger = normalizeLogger(line.substring(0, separator));
    message = line.substring(separator + separatorLength).trim();
    if (matchesLogger(logger, "YarnClientImpl", HADOOP_YARN_CLIENT_LOGGER)) {
      return extractId(SUBMITTED_APPLICATION_PATTERN, message);
    }
    if (engine == Engine.SPARK && matchesLogger(logger, "Client", SPARK_YARN_CLIENT_LOGGER)) {
      return extractId(SPARK_APPLICATION_REPORT_PATTERN, message);
    }
    if (engine == Engine.FLINK
        && matchesLogger(logger, "YarnClusterDescriptor", FLINK_YARN_CLUSTER_DESCRIPTOR_LOGGER)) {
      return extractId(FLINK_WEB_INTERFACE_PATTERN, message);
    }
    return null;
  }

  static String read(Process process, Engine engine, Consumer<String> outputConsumer) {
    return read(process, engine, DEFAULT_READ_TIMEOUT_MILLIS, outputConsumer);
  }

  /**
   * Drains the process output while waiting for the first reliable application id.
   *
   * <p>The output thread is deliberately a daemon. A caller that reaches the deadline returns
   * unknown without killing a still-running client or starting a replacement submission. The same
   * thread continues draining output after an id has been found so a live client cannot block on a
   * full pipe.
   */
  @VisibleForTesting
  static String read(
      Process process, Engine engine, long timeoutMillis, Consumer<String> outputConsumer) {
    CompletableFuture<String> applicationId = new CompletableFuture<>();
    Thread outputThread =
        new Thread(
            () -> {
              try (BufferedReader reader =
                  new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                  if (!applicationId.isDone()) {
                    outputConsumer.accept(line);
                    String candidate = parse(line, engine);
                    if (candidate != null) {
                      applicationId.complete(candidate);
                    }
                  }
                }
                applicationId.complete(null);
              } catch (IOException | RuntimeException e) {
                applicationId.completeExceptionally(e);
              }
            },
            "amoro-yarn-application-id-reader");
    outputThread.setDaemon(true);
    outputThread.start();

    try {
      return applicationId.get(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      applicationId.complete(null);
      return null;
    } catch (TimeoutException e) {
      applicationId.complete(null);
      return null;
    } catch (ExecutionException e) {
      return null;
    }
  }

  private static boolean matchesLogger(String logger, String simpleName, String qualifiedName) {
    return logger.equals(simpleName)
        || logger.endsWith(" " + simpleName)
        || logger.equals(qualifiedName)
        || logger.endsWith(" " + qualifiedName);
  }

  private static String normalizeLogger(String logger) {
    logger = logger.trim();
    int ndcStart = logger.lastIndexOf(" [");
    return ndcStart >= 0 && logger.endsWith("]") ? logger.substring(0, ndcStart).trim() : logger;
  }

  private static String extractId(Pattern pattern, String message) {
    Matcher matcher = pattern.matcher(message);
    return matcher.matches() ? matcher.group(1) : null;
  }

  enum Engine {
    SPARK,
    FLINK
  }
}
