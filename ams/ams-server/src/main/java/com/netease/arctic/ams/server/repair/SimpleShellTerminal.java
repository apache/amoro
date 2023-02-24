/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.repair;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class SimpleShellTerminal {

  public static final String PROMPT = "repair> ";

  private CommandHandler commandHandler;

  private TerminalOutput terminalOutput;

  public SimpleShellTerminal(CommandHandler commandHandler) {
    this.commandHandler = commandHandler;
    this.terminalOutput = new TerminalOutput() {
      @Override
      public void output(String st) {
        System.out.println(st);
      }
    };
  }

  public void start() throws IOException {
    terminalOutput.output(commandHandler.welcome());

    Terminal terminal = TerminalBuilder.builder()
        .system(true)
        .build();
    LineReaderBuilder lineReaderBuilder = LineReaderBuilder.builder().terminal(terminal);
    if (commandHandler.keyWord() != null) {
      lineReaderBuilder.completer(new StringsCompleter(commandHandler.keyWord()));
    }
    LineReader lineReader = lineReaderBuilder.build();

    while (true) {
      try {
        String line = lineReader.readLine(PROMPT);
        commandHandler.dispatch(line, terminalOutput);
      } catch (UserInterruptException | EndOfFileException e) {
        commandHandler.close();
        terminalOutput.output("quited repair server");
        return;
      } catch (Throwable t) {
        terminalOutput.output("run error cause:\n" + t.getMessage());
      }
    }
  }
}
