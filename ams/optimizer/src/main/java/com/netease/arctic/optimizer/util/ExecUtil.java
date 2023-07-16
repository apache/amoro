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

package com.netease.arctic.optimizer.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExecUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ExecUtil.class);

  public static int exec(String[] command, List<String> output) {
    int retCode = 1;
    Runtime runtime = Runtime.getRuntime();
    try {
      Process process = runtime.exec(command);
      OutputBufferThread stdOut = new OutputBufferThread(process.getInputStream());
      OutputBufferThread stdErr = new OutputBufferThread(process.getErrorStream());
      stdOut.start();
      stdErr.start();
      retCode = process.waitFor();
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

  private static class OutputBufferThread extends Thread {
    private List<String> output;

    private InputStream is;

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
