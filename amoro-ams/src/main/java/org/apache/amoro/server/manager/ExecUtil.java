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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExecUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ExecUtil.class);

  public static int exec(String[] command, List<String> output) {
    Runtime runtime = Runtime.getRuntime();
    try {
      Process process = runtime.exec(command);
      OutputBufferThread stdOut = new OutputBufferThread(process.getInputStream());
      OutputBufferThread stdErr = new OutputBufferThread(process.getErrorStream());
      stdOut.start();
      stdErr.start();
      int retCode = process.waitFor();
      if (retCode != 0) {
        String error = stdErr.getOutput().stream().collect(Collectors.joining("\n"));
        LOG.error("exec {} failed, reason is {}", command, error);
      }
      stdOut.join();
      stdErr.join();
      output.addAll(stdOut.getOutput());
      return retCode;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static ProcessBuilder shellCommand(List<String> exports, String action, String... args) {
    String script = exports.isEmpty() ? action : String.join(" && ", exports) + " && " + action;
    List<String> command = new ArrayList<>(List.of("/bin/sh", "-c", script, "amoro"));
    Collections.addAll(command, args);
    return new ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT);
  }

  // Callers redirect output so the command cannot block on an unread Java pipe.
  static void exec(ProcessBuilder command, long deadlineNanos, String operation)
      throws IOException, InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    if (deadlineNanos - System.nanoTime() <= 0) {
      throw new IllegalStateException(operation + " timed out");
    }
    Process process = command.start();
    try {
      if (!process.waitFor(deadlineNanos - System.nanoTime(), TimeUnit.NANOSECONDS)
          || deadlineNanos - System.nanoTime() <= 0) {
        throw new IllegalStateException(operation + " timed out");
      }
      if (process.exitValue() != 0) {
        throw new IllegalStateException(
            operation + " failed with exit code " + process.exitValue());
      }
    } finally {
      if (process.isAlive()) {
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.toHandle().destroyForcibly();
      }
    }
  }

  private static class OutputBufferThread extends Thread {
    private final List<String> output;

    private final InputStream is;

    public OutputBufferThread(InputStream is) {
      this.setDaemon(true);
      output = new ArrayList<>();
      this.is = is;
    }

    @Override
    public void run() {
      try {
        List<String> temp = IOUtils.readLines(is).stream().collect(Collectors.toList());
        output.addAll(temp);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public List<String> getOutput() {
      return output;
    }
  }
}
